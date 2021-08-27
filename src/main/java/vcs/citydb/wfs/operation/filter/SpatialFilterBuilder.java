package vcs.citydb.wfs.operation.filter;

import net.opengis.fes._2.BBOXType;
import net.opengis.fes._2.BinarySpatialOpType;
import net.opengis.fes._2.DistanceBufferType;
import net.opengis.fes._2.MeasureType;
import net.opengis.gml.AbstractGeometryType;
import net.opengis.gml.EnvelopeType;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.database.schema.util.SimpleXPathParser;
import org.citydb.core.database.schema.util.XPathException;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.Predicate;
import org.citydb.core.query.filter.selection.expression.ValueReference;
import org.citydb.core.query.filter.selection.operator.spatial.Distance;
import org.citydb.core.query.filter.selection.operator.spatial.DistanceUnit;
import org.citydb.core.query.filter.selection.operator.spatial.SpatialOperationFactory;
import org.citydb.core.query.geometry.DatabaseSrsParser;
import org.citydb.core.query.geometry.GeometryParseException;
import org.citydb.core.query.geometry.SrsParseException;
import org.citydb.core.query.geometry.gml.SimpleGMLParser;
import org.citydb.core.util.Util;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
import org.citygml4j.model.module.gml.GMLCoreModule;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.filter.SpatialOperatorName;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

public class SpatialFilterBuilder {
	private final SimpleXPathParser xpathParser;
	private final WFSConfig wfsConfig;
	private final SimpleGMLParser gmlParser;

	public SpatialFilterBuilder(JAXBUnmarshaller unmarshaller, SimpleXPathParser xpathParser, DatabaseSrsParser srsNameParser, WFSConfig wfsConfig) {
		this.xpathParser = xpathParser;
		this.wfsConfig = wfsConfig;

		gmlParser = new SimpleGMLParser(unmarshaller, srsNameParser);		
	}

	public Predicate buildSpatialOperator(JAXBElement<?> spatialOpsElement, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		if (!spatialOpsElement.getName().getNamespaceURI().equals(Constants.FES_NAMESPACE_URI))
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only spatial operators associated with the namespace " + Constants.FES_NAMESPACE_URI + " are supported.", handle);

		// check whether the operator is advertised
		if (!wfsConfig.getFilterCapabilities().getSpatialCapabilities().containsSpatialOperator(SpatialOperatorName.fromValue(spatialOpsElement.getName().getLocalPart())))
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "The spatial operator '" + spatialOpsElement.getName() + "' is not advertised.", handle);

		Object operator = spatialOpsElement.getValue();
		Predicate predicate = null;

		if (operator instanceof BBOXType)
			predicate = buildBBOXOperator((BBOXType)operator, spatialOpsElement.getName(), featureType, namespaceFilter, handle);
		else if (operator instanceof BinarySpatialOpType)
			predicate = buildBinaryOperator((BinarySpatialOpType)operator, spatialOpsElement.getName(), featureType, namespaceFilter, handle);
		else if (operator instanceof DistanceBufferType)
			predicate = buildDistanceOperator((DistanceBufferType)operator, spatialOpsElement.getName(), featureType, namespaceFilter, handle);

		return predicate;
	}
	
	private Predicate buildBBOXOperator(BBOXType bboxOp, QName opName, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		JAXBElement<?>[] operands = parseOperands(bboxOp.getExpressionOrAny());
		JAXBElement<?> valueReference_ = operands[0];
		JAXBElement<?> envelope_ = operands[1];
		
		if (envelope_ == null || !(envelope_.getValue() instanceof EnvelopeType))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A '" + new QName(GMLCoreModule.v3_1_1.getNamespaceURI(), "Envelope").toString() + "' is expected as spatial operand of the BBOX operator.", handle);

		// map XPath expression
		ValueReference valueReference = null;
		if (valueReference_ != null)
			valueReference = parseValueReference((String)valueReference_.getValue(), featureType, namespaceFilter, handle);

		// map geometry object
		GeometryObject geometry = null;
		try {
			geometry = gmlParser.parseGeometry(envelope_);
			if (geometry == null)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the '" + envelope_.getName() + "' geometry.", handle);
		} catch (GeometryParseException | SrsParseException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the '" + envelope_.getName() + "' geometry.", handle, e);
		}

		try {
			return SpatialOperationFactory.bbox(valueReference, geometry);
		} catch (FilterException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", handle, e);
		}
	}
	
	private Predicate buildDistanceOperator(DistanceBufferType distanceOp, QName opName, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		JAXBElement<?>[] operands = parseOperands(distanceOp.getExpressionOrAny());
		JAXBElement<?> valueReference_ = operands[0];
		JAXBElement<?> geometry_ = operands[1];
		MeasureType distance_ = distanceOp.getDistance();

		if (geometry_ == null)
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The spatial operator '" + opName + "' lacks a GML geometry operand.", handle);

		if (distance_ == null)
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The spatial operator '" + opName + "' requires a Distance operand.", handle);

		// map XPath expression
		ValueReference valueReference = null;
		if (valueReference_ != null)
			valueReference = parseValueReference((String)valueReference_.getValue(), featureType, namespaceFilter, handle);

		// map geometry object
		GeometryObject geometry = null;
		try {
			geometry = gmlParser.parseGeometry(geometry_);
			if (geometry == null)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the '" + geometry_.getName() + "' geometry.", handle);
		} catch (GeometryParseException | SrsParseException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the '" + geometry_.getName() + "' geometry.", handle, e);
		}

		// handle distance
		DistanceUnit unit = null;
		if (distance_.isSetUom()) {
			unit = DistanceUnit.fromSymbol(distance_.getUom());
			if (unit == null) {
				WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.OPERATION_PROCESSING_FAILED);
				message.addExceptionText("Failed to recognize the unit '" + distance_.getUom() + "' on the Distance operand.");
				message.addExceptionText("Supportes units are " + Util.collection2string(Arrays.asList(DistanceUnit.values()), ", ") + ".");
				message.setLocator(handle);
				
				throw new WFSException(message);
			}
		} else 
			unit = DistanceUnit.METER;

		Distance distance = new Distance(distance_.getValue(), unit);

		try {
			switch (SpatialOperatorName.fromValue(opName.getLocalPart())) {
			case DWITHIN:
				return SpatialOperationFactory.dWithin(valueReference, geometry, distance);
			case BEYOND:
				return SpatialOperationFactory.beyond(valueReference, geometry, distance);
			case EQUALS:
			case DISJOINT:
			case TOUCHES:
			case WITHIN:
			case OVERLAPS:
			case INTERSECTS:
			case CONTAINS:
			case BBOX:
				return null;
			}
		} catch (FilterException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", handle, e);
		}

		return null;
	}

	private Predicate buildBinaryOperator(BinarySpatialOpType binarySpatialOp, QName opName, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		JAXBElement<?>[] operands = parseOperands(binarySpatialOp.getExpressionOrAny());
		JAXBElement<?> valueReference_ = operands[0];
		JAXBElement<?> geometry_ = operands[1];

		if (geometry_ == null)
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The spatial operator '" + opName + "' lacks a GML geometry operand.", handle);

		if (valueReference_ == null)
			throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The spatial operator '" + opName + "' requires a ValueReference pointing to the geometry property to be tested.", handle);

		// map XPath expression
		ValueReference valueReference = parseValueReference((String)valueReference_.getValue(), featureType, namespaceFilter, handle);

		// map geometry object
		GeometryObject geometry = null;
		try {
			geometry = gmlParser.parseGeometry(geometry_);
			if (geometry == null)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the '" + geometry_.getName() + "' geometry.", handle);
		} catch (GeometryParseException | SrsParseException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the '" + geometry_.getName() + "' geometry.", handle, e);
		}

		try {
			switch (SpatialOperatorName.fromValue(opName.getLocalPart())) {
			case EQUALS:
				return SpatialOperationFactory.equals(valueReference, geometry);
			case DISJOINT:
				return SpatialOperationFactory.disjoint(valueReference, geometry);
			case TOUCHES:
				return SpatialOperationFactory.touches(valueReference, geometry);
			case WITHIN:
				return SpatialOperationFactory.within(valueReference, geometry);
			case OVERLAPS:
				return SpatialOperationFactory.overlaps(valueReference, geometry);
			case INTERSECTS:
				return SpatialOperationFactory.intersects(valueReference, geometry);
			case CONTAINS:
				return SpatialOperationFactory.contains(valueReference, geometry);
			case BBOX:
			case DWITHIN:
			case BEYOND:
				return null;
			}
		} catch (FilterException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", handle, e);
		}

		return null;
	}
	
	private JAXBElement<?>[] parseOperands(List<Object> operands) {
		JAXBElement<?>[] result = new JAXBElement<?>[2];
		
		for (Object operand : operands) {
			if (!(operand instanceof JAXBElement<?>))
				continue;

			JAXBElement<?> jaxbElement = (JAXBElement<?>)operand;

			if (jaxbElement.getValue() instanceof String && jaxbElement.getName().equals(new QName(Constants.FES_NAMESPACE_URI, "ValueReference")))
				result[0] = jaxbElement;
			else if (jaxbElement.getValue() instanceof AbstractGeometryType)
				result[1] = jaxbElement;
			else if (jaxbElement.getValue() instanceof EnvelopeType)
				result[1] = jaxbElement;
		}
		
		return result;
	}

	private ValueReference parseValueReference(String valueReference_, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		try {
			SchemaPath schemaPath = xpathParser.parse(valueReference_, featureType, namespaceFilter);
			if (schemaPath.getLastNode().getPathElement().getElementType() != PathElementType.GEOMETRY_PROPERTY)
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "'" + valueReference_ + "' is not a geometry property.", KVPConstants.VALUE_REFERENCE);

			return new ValueReference(schemaPath);
		} catch (XPathException e) {
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid XPath expression used in ValueReference.", KVPConstants.VALUE_REFERENCE, e);
		} catch (InvalidSchemaPathException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to map XPath expression used in ValueReference to the CityGML schema.", handle, e);
		}
	}

}
