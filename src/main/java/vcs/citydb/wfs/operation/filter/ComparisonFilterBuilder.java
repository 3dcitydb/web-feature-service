package vcs.citydb.wfs.operation.filter;

import net.opengis.fes._2.*;
import org.citydb.core.database.schema.mapping.AbstractProperty;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.database.schema.mapping.SimpleAttribute;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.database.schema.util.SimpleXPathParser;
import org.citydb.core.database.schema.util.XPathException;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.Predicate;
import org.citydb.core.query.filter.selection.expression.AbstractLiteral;
import org.citydb.core.query.filter.selection.expression.ValueReference;
import org.citydb.core.query.filter.selection.operator.comparison.BinaryComparisonOperator;
import org.citydb.core.query.filter.selection.operator.comparison.ComparisonFactory;
import org.citydb.core.query.filter.selection.operator.comparison.LikeOperator;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.filter.ComparisonOperatorName;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class ComparisonFilterBuilder {
    private final WFSConfig wfsConfig;
    private final SimpleXPathParser xpathParser;
    private final LiteralBuilder literalHandler;

    public ComparisonFilterBuilder(SimpleXPathParser xpathParser, WFSConfig wfsConfig) {
        this.wfsConfig = wfsConfig;
        this.xpathParser = xpathParser;

        literalHandler = new LiteralBuilder();
    }

    public Predicate buildComparisonOperator(JAXBElement<?> comparisonOpsElement, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
        if (!comparisonOpsElement.getName().getNamespaceURI().equals(Constants.FES_NAMESPACE_URI))
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only comparison operators associated with the namespace " + Constants.FES_NAMESPACE_URI + " are supported.", handle);

        // check whether the operator is advertised
        if (!wfsConfig.getFilterCapabilities().getScalarCapabilities().containsComparisonOperator(ComparisonOperatorName.fromValue(comparisonOpsElement.getName().getLocalPart())))
            throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "The comparison operator '" + comparisonOpsElement.getName() + "' is not advertised.", handle);

        Object operator = comparisonOpsElement.getValue();
        Predicate predicate = null;

        if (operator instanceof BinaryComparisonOpType)
            predicate = buildBinaryOperator((BinaryComparisonOpType) operator, comparisonOpsElement.getName(), featureType, namespaceFilter, handle);
        else if (operator instanceof PropertyIsBetweenType)
            predicate = buildBetweenOperator((PropertyIsBetweenType) operator, comparisonOpsElement.getName(), featureType, namespaceFilter, handle);
        else if (operator instanceof PropertyIsLikeType)
            predicate = buildLikeOperator((PropertyIsLikeType) operator, comparisonOpsElement.getName(), featureType, namespaceFilter, handle);
        else if (operator instanceof PropertyIsNullType)
            predicate = buildNullOperator((PropertyIsNullType) operator, comparisonOpsElement.getName(), featureType, namespaceFilter, handle);
        else if (operator instanceof PropertyIsNilType) {
            // CityGML does not support nillable property elements
            // so we map this to the null operator
            PropertyIsNilType isNil = (PropertyIsNilType) operator;
            PropertyIsNullType isNull = new PropertyIsNullType();

            isNull.setExpression(isNil.getExpression());
            predicate = buildNullOperator(isNull, comparisonOpsElement.getName(), featureType, namespaceFilter, handle);
        }

        return predicate;
    }

    public Predicate buildBinaryOperator(BinaryComparisonOpType binaryComparisonOp, QName opName, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
        if (binaryComparisonOp.getExpression().size() != 2)
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Two operands are expected for the binary comparison operator '" + opName + "'.");

        if (binaryComparisonOp.isSetMatchAction() && binaryComparisonOp.getMatchAction() != MatchActionType.ANY)
            throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Match action '" + binaryComparisonOp.getMatchAction() + "' is not supported.", handle);

        String valueReference_ = null;
        String literal_ = null;

        for (JAXBElement<?> jaxbElement : binaryComparisonOp.getExpression()) {
            if (jaxbElement.getValue() instanceof String && jaxbElement.getName().equals(new QName(Constants.FES_NAMESPACE_URI, "ValueReference")))
                valueReference_ = (String) jaxbElement.getValue();
            else if (jaxbElement.getValue() instanceof LiteralType) {
                LiteralType literalType = (LiteralType) jaxbElement.getValue();
                Object content = literalHandler.buildLiteral(literalType, handle);

                // we currently only support primitive literals
                if (!(content instanceof String))
                    throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only primitive literals are supported.", handle);

                literal_ = (String) content;
            }
        }

        // we currently only support a combination of ValueReference and Literal as operands
        if (valueReference_ == null || literal_ == null)
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only combinations of '" + new QName(Constants.FES_NAMESPACE_URI, "ValueReference") + "' and '"
                    + new QName(Constants.FES_NAMESPACE_URI, "Literal") + "' are supported as operands of the '" + opName + "' operation.", handle);

        // map XPath expression and literal
        ValueReference valueReference = parseValueReference(valueReference_, featureType, namespaceFilter, handle);
        if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "'" + valueReference_ + "' is not supported as ValueReference of the '" + opName + "' operation.", KVPConstants.VALUE_REFERENCE);

        AbstractLiteral<?> literal = parseLiteral(valueReference, literal_, handle);

        BinaryComparisonOperator operator = null;
        try {
            switch (ComparisonOperatorName.fromValue(opName.getLocalPart())) {
                case PROPERTY_IS_EQUAL_TO:
                    operator = ComparisonFactory.equalTo(valueReference, literal);
                    break;
                case PROPERTY_IS_NOT_EQUAL_TO:
                    operator = ComparisonFactory.notEqualTo(valueReference, literal);
                    break;
                case PROPERTY_IS_LESS_THAN:
                    operator = ComparisonFactory.lessThan(valueReference, literal);
                    break;
                case PROPERTY_IS_GREATER_THAN:
                    operator = ComparisonFactory.greaterThan(valueReference, literal);
                    break;
                case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
                    operator = ComparisonFactory.lessThanOrEqualTo(valueReference, literal);
                    break;
                case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
                    operator = ComparisonFactory.greaterThanOrEqualTo(valueReference, literal);
                    break;
                case PROPERTY_IS_BETWEEN:
                case PROPERTY_IS_LIKE:
                case PROPERTY_IS_NULL:
                case PROPERTY_IS_NIL:
                    return null;
            }
        } catch (FilterException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", handle, e);
        }

        if (binaryComparisonOp.isSetMatchCase())
            operator.setMatchCase(binaryComparisonOp.isMatchCase());

        return operator;
    }

    public Predicate buildBetweenOperator(PropertyIsBetweenType betweenOp, QName opName, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
        // we currently only support a combination of ValueReference and Literals
        if (!(betweenOp.getExpression().getValue() instanceof String && betweenOp.getExpression().getName().equals(new QName(Constants.FES_NAMESPACE_URI, "ValueReference"))))
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only '" + new QName(Constants.FES_NAMESPACE_URI, "ValueReference") + "' is supported as operand of the '" + opName + "' operation.", handle);

        if (!(betweenOp.getLowerBoundary().getExpression().getValue() instanceof LiteralType))
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only '" + new QName(Constants.FES_NAMESPACE_URI, "Literal") + "' is supported as lower boundary of the '" + opName + "' operation.", handle);

        if (!(betweenOp.getUpperBoundary().getExpression().getValue() instanceof LiteralType))
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only '" + new QName(Constants.FES_NAMESPACE_URI, "Literal") + "' is supported as upper boundary of the '" + opName + "' operation.", handle);

        Object lowerBoundary_ = literalHandler.buildLiteral((LiteralType) betweenOp.getLowerBoundary().getExpression().getValue(), handle);
        Object upperBoundary_ = literalHandler.buildLiteral((LiteralType) betweenOp.getUpperBoundary().getExpression().getValue(), handle);

        if (!(lowerBoundary_ instanceof String) || !(upperBoundary_ instanceof String))
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only primitive literals are supported.", handle);

        // map XPath expression and literals
        ValueReference valueReference = parseValueReference((String) betweenOp.getExpression().getValue(), featureType, namespaceFilter, handle);
        if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "'" + betweenOp.getExpression().getValue() + "' is not supported as ValueReference of the '" + opName + "' operation.", KVPConstants.VALUE_REFERENCE);

        AbstractLiteral<?> lowerBoundary = parseLiteral(valueReference, (String) lowerBoundary_, handle);
        AbstractLiteral<?> upperBoundary = parseLiteral(valueReference, (String) upperBoundary_, handle);

        try {
            return ComparisonFactory.between(valueReference, lowerBoundary, upperBoundary);
        } catch (FilterException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", handle, e);
        }
    }

    public Predicate buildLikeOperator(PropertyIsLikeType likeOp, QName opName, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
        if (likeOp.getExpression().size() != 2)
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Two operands are expected for the comparison operator '" + opName + "'.");

        String valueReference_ = null;
        String literal_ = null;

        for (JAXBElement<?> jaxbElement : likeOp.getExpression()) {
            if (jaxbElement.getValue() instanceof String && jaxbElement.getName().equals(new QName(Constants.FES_NAMESPACE_URI, "ValueReference")))
                valueReference_ = (String) jaxbElement.getValue();
            else if (jaxbElement.getValue() instanceof LiteralType) {
                LiteralType literalType = (LiteralType) jaxbElement.getValue();
                Object content = literalHandler.buildLiteral(literalType, handle);

                // we currently only support primitive literals
                if (!(content instanceof String))
                    throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only primitive literals are supported.", handle);

                literal_ = (String) content;
            }
        }

        // we currently only support a combination of ValueReference and Literal as operands
        if (valueReference_ == null || literal_ == null)
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only combinations of '" + new QName(Constants.FES_NAMESPACE_URI, "ValueReference") + "' and '"
                    + new QName(Constants.FES_NAMESPACE_URI, "Literal") + "' are supported as operands of the '" + opName + "' operation.", handle);

        // map XPath expression and literal
        ValueReference valueReference = parseValueReference(valueReference_, featureType, namespaceFilter, handle);
        if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "'" + valueReference_ + "' is not supported as ValueReference of the '" + opName + "' operation.", KVPConstants.VALUE_REFERENCE);

        AbstractLiteral<?> literal = parseLiteral(valueReference, literal_, handle);

        if (literal.getLiteralType() != org.citydb.core.query.filter.selection.expression.LiteralType.STRING)
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The '" + opName + "' operation may only be applied to character strings.", handle);

        try {
            LikeOperator likeOperator = new LikeOperator(valueReference, literal);
            likeOperator.setWildCard(likeOp.getWildCard());
            likeOperator.setSingleCharacter(likeOp.getSingleChar());
            likeOperator.setEscapeCharacter(likeOp.getEscapeChar());
            likeOperator.setMatchCase(likeOp.isMatchCase());

            return likeOperator;
        } catch (FilterException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", handle, e);
        }
    }

    public Predicate buildNullOperator(PropertyIsNullType nullOp, QName opName, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
        if (!(nullOp.getExpression().getValue() instanceof String && nullOp.getExpression().getName().equals(new QName(Constants.FES_NAMESPACE_URI, "ValueReference"))))
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only '" + new QName(Constants.FES_NAMESPACE_URI, "ValueReference") + "' is supported as operand of the '" + opName + "' operation.", handle);

        ValueReference valueReference = parseValueReference((String) nullOp.getExpression().getValue(), featureType, namespaceFilter, handle);
        if (!(valueReference.getTarget() instanceof AbstractProperty))
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "'" + nullOp.getExpression().getValue() + "' is not supported as ValueReference of the '" + opName + "' operation.", KVPConstants.VALUE_REFERENCE);

        try {
            return ComparisonFactory.isNull(valueReference);
        } catch (FilterException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", handle, e);
        }
    }

    private ValueReference parseValueReference(String valueReference_, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
        try {
            SchemaPath schemaPath = xpathParser.parse(valueReference_, featureType, namespaceFilter);
            return new ValueReference(schemaPath);
        } catch (XPathException e) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid XPath expression used in ValueReference.", KVPConstants.VALUE_REFERENCE, e);
        } catch (InvalidSchemaPathException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to map XPath expression used in ValueReference to the CityGML schema.", handle, e);
        }
    }

    private AbstractLiteral<?> parseLiteral(ValueReference valueReference, String literal_, String handle) throws WFSException {
        SimpleAttribute attribute = (SimpleAttribute) valueReference.getSchemaPath().getLastNode().getPathElement();
        AbstractLiteral<?> literal = literalHandler.convertToSchemaLiteral(literal_, attribute.getType(), handle);

        if (literal == null) {
            WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.OPERATION_PROCESSING_FAILED);
            message.addExceptionText("Failed to parse the literal value '" + literal_ + "'.");
            message.addExceptionText("Expected a literal value of type '" + attribute.getType().value() + "'.");
            message.setLocator(handle);

            throw new WFSException(message);
        }

        return literal;
    }

}
