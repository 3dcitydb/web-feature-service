package vcs.citydb.wfs.operation.filter;

import net.opengis.fes._2.*;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.database.schema.util.SimpleXPathParser;
import org.citydb.core.query.Query;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.Predicate;
import org.citydb.core.query.filter.selection.SelectionFilter;
import org.citydb.core.query.filter.selection.expression.ValueReference;
import org.citydb.core.query.filter.selection.operator.comparison.ComparisonFactory;
import org.citydb.core.query.filter.selection.operator.comparison.NullOperator;
import org.citydb.core.query.filter.selection.operator.logical.LogicalOperationFactory;
import org.citydb.core.query.geometry.DatabaseSrsParser;
import org.citydb.core.registry.ObjectRegistry;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
import org.citygml4j.model.module.citygml.CoreModule;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilterHandler {
	private final WFSConfig wfsConfig;
	private final ComparisonFilterBuilder comparisonFilterBuilder;
	private final SpatialFilterBuilder spatialFilterBuilder;
	private final ResourceIdFilterBuilder resourceIdFilterBuilder;
	private final SchemaMapping schemaMapping;

	public FilterHandler(JAXBUnmarshaller unmarshaller, SimpleXPathParser xpathParser, DatabaseSrsParser srsNameParser, WFSConfig wfsConfig) {
		this.wfsConfig = wfsConfig;

		comparisonFilterBuilder = new ComparisonFilterBuilder(xpathParser, wfsConfig);
		spatialFilterBuilder = new SpatialFilterBuilder(unmarshaller, xpathParser, srsNameParser, wfsConfig);
		resourceIdFilterBuilder = new ResourceIdFilterBuilder();
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
	}

	public SelectionFilter getSelection(JAXBElement<?> selectionClauseElement, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		if (!(selectionClauseElement.getValue() instanceof FilterType))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The element '" + selectionClauseElement.getName() + "' is not supported as selection clause of queries.", handle);

		FilterType filter = (FilterType)selectionClauseElement.getValue();		
		Predicate predicate;

		if (filter.getComparisonOps() != null)
			predicate = buildPredicate(filter.getComparisonOps(), featureType, namespaceFilter, handle);
		else if (filter.getSpatialOps() != null)
			predicate = buildPredicate(filter.getSpatialOps(), featureType, namespaceFilter, handle);
		else if (filter.getLogicOps() != null)
			predicate = buildPredicate(filter.getLogicOps(), featureType, namespaceFilter, handle);
		else if (filter.get_Id() != null)
			predicate = buildPredicate(filter.get_Id(), handle);
		else
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "The filer expression '" + selectionClauseElement.getName() + "' is not supported." , handle);

		return predicate != null ? new SelectionFilter(predicate) : null;
	}
	
	public SelectionFilter getSelection(FilterType filter, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		if (filter == null)
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "No valid filter expression provided.", handle);

		Predicate predicate = null;

		if (filter.getComparisonOps() != null)
			predicate = buildPredicate(filter.getComparisonOps(), featureType, namespaceFilter, handle);
		else if (filter.getSpatialOps() != null)
			predicate = buildPredicate(filter.getSpatialOps(), featureType, namespaceFilter, handle);
		else if (filter.getLogicOps() != null)
			predicate = buildPredicate(filter.getLogicOps(), featureType, namespaceFilter, handle);
		else if (filter.get_Id() != null)
			predicate = buildPredicate(filter.get_Id(), handle);

		return predicate != null ? new SelectionFilter(predicate) : null;
	}

	private Predicate buildPredicate(JAXBElement<?> operator, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		Predicate predicate = null;

		if (operator.getValue() instanceof ComparisonOpsType) {
			if (!wfsConfig.getFilterCapabilities().getScalarCapabilities().isSetComparisonOperators())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Comparison filter capabilities are not advertised.", handle);

			predicate = comparisonFilterBuilder.buildComparisonOperator(operator, featureType, namespaceFilter, handle);
		}
		
		else if (operator.getValue() instanceof SpatialOpsType) {
			if (!wfsConfig.getFilterCapabilities().isSetSpatialCapabilities())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Spatial filter capabilities are not advertised.", handle);
			
			predicate = spatialFilterBuilder.buildSpatialOperator(operator, featureType, namespaceFilter, handle);
		}

		else if (operator.getValue() instanceof LogicOpsType) {
			if (!wfsConfig.getFilterCapabilities().getScalarCapabilities().isSetLogicalOperators())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Logical filter capabilities are not advertised.", handle);

			predicate = buildLogicalOperator(operator, featureType, namespaceFilter, handle);
		}

		else if (operator.getValue() instanceof AbstractIdType) {
			// the resource id operator is always advertised
			predicate = resourceIdFilterBuilder.buildIdOperator(operator, handle);
		}

		return predicate;
	}

	private Predicate buildPredicate(List<JAXBElement<? extends AbstractIdType>> idOperators, String handle) throws WFSException {
		// the resource id operator is always advertised
		return resourceIdFilterBuilder.buildIdOperator(idOperators, handle);			
	}

	@SuppressWarnings("unchecked")
	private Predicate buildLogicalOperator(JAXBElement<?> logicOpsElement, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		if (!logicOpsElement.getName().getNamespaceURI().equals(Constants.FES_NAMESPACE_URI))
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only logical operators associated with the namespace " + Constants.FES_NAMESPACE_URI + " are supported.", handle);

		Predicate predicate = null;

		if (logicOpsElement.getValue() instanceof BinaryLogicOpType) {
			BinaryLogicOpType binaryLogicOp = (BinaryLogicOpType)logicOpsElement.getValue();
			int nrOfOperands = binaryLogicOp.getComparisonOpsOrSpatialOpsOrTemporalOps().size();

			if (nrOfOperands == 0)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "No operand provided for the logical comparison operator '" + logicOpsElement.getName() + "'.");

			if (nrOfOperands == 1)
				return buildPredicate(binaryLogicOp.getComparisonOpsOrSpatialOpsOrTemporalOps().get(0), featureType, namespaceFilter, handle);

			// collect all id operators into one array list
			List<JAXBElement<? extends AbstractIdType>> idOpsElements = null;
			Iterator<JAXBElement<?>> iter = binaryLogicOp.getComparisonOpsOrSpatialOpsOrTemporalOps().iterator();
			while (iter.hasNext()) {
				JAXBElement<?> jaxbElement = iter.next(); 
				if (jaxbElement.getValue() instanceof AbstractIdType) {
					if (idOpsElements == null)
						idOpsElements = new ArrayList<>();

					idOpsElements.add((JAXBElement<? extends AbstractIdType>)jaxbElement);
					iter.remove();
				}
			}

			// build non-id operators
			List<Predicate> operands = new ArrayList<>();
			for (JAXBElement<?> jaxbElement : binaryLogicOp.getComparisonOpsOrSpatialOpsOrTemporalOps()) {
				Predicate operand = buildPredicate(jaxbElement, featureType, namespaceFilter, handle);
				if (operand == null)
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the operands of the logical comparison operator '" + logicOpsElement.getName() + "'." , handle);

				operands.add(operand);
			}

			// build id operators
			if (idOpsElements != null) {
				Predicate operand = buildPredicate(idOpsElements, handle);
				if (operand == null)
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the operands of the logical comparison operator '" + logicOpsElement.getName() + "'." , handle);

				operands.add(operand);				
			}

			try {
				if (logicOpsElement.getName().getLocalPart().equals("And"))
					predicate = LogicalOperationFactory.AND(operands);
				else if (logicOpsElement.getName().getLocalPart().equals("Or"))
					predicate = LogicalOperationFactory.OR(operands);
			} catch (FilterException e) {
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", handle, e);
			}
		}

		else if (logicOpsElement.getValue() instanceof UnaryLogicOpType) {
			UnaryLogicOpType unaryLogicOp = (UnaryLogicOpType)logicOpsElement.getValue();
			Predicate notOperand;

			if (unaryLogicOp.getComparisonOps() != null)
				notOperand = buildPredicate(unaryLogicOp.getComparisonOps(), featureType, namespaceFilter, handle);
			else if (unaryLogicOp.getSpatialOps() != null)
				notOperand = buildPredicate(unaryLogicOp.getSpatialOps(), featureType, namespaceFilter, handle);
			else if (unaryLogicOp.getLogicOps() != null)
				notOperand = buildPredicate(unaryLogicOp.getLogicOps(), featureType, namespaceFilter, handle);
			else if (unaryLogicOp.get_Id() != null)
				notOperand = buildPredicate(unaryLogicOp.get_Id(), handle);
			else
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the operand of the logical comparison operator '" + logicOpsElement.getName() + "'." , handle);

			if (notOperand != null)
				predicate = LogicalOperationFactory.NOT(notOperand);
		}

		return predicate;
	}

	public void addNotTerminatedFilter(Query query, String handle) throws WFSException {
		try {
			FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());
			SchemaPath schemaPath = new SchemaPath(superType).appendChild(superType.getProperty("terminationDate", CoreModule.v2_0_0.getNamespaceURI(), true));
			NullOperator isNull = ComparisonFactory.isNull(new ValueReference(schemaPath));

			if (query.isSetSelection()) {
				SelectionFilter selection = query.getSelection();
				selection.setPredicate(LogicalOperationFactory.AND(selection.getPredicate(), isNull));
			} else
				query.setSelection(new SelectionFilter(isNull));
		} catch (InvalidSchemaPathException | FilterException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to add is null test for termination date.", handle, e);
		}
	}
}
