package vcs.citydb.wfs.operation.filter;

import net.opengis.fes._2.AbstractIdType;
import net.opengis.fes._2.FilterType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.query.Query;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.comparison.ComparisonFactory;
import org.citydb.query.filter.selection.operator.comparison.NullOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperationFactory;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.model.module.citygml.CoreModule;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

import javax.xml.bind.JAXBElement;
import java.util.List;

public class FilterHandler {
	private final ResourceIdFilterBuilder resourceIdFilterBuilder;
	private final SchemaMapping schemaMapping;

	public FilterHandler() {
		resourceIdFilterBuilder = new ResourceIdFilterBuilder();
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
	}

	public SelectionFilter getSelection(JAXBElement<?> selectionClauseElement, String handle) throws WFSException {
		if (!(selectionClauseElement.getValue() instanceof FilterType))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The element '" + selectionClauseElement.getName() + "' is not supported as selection clause of queries.", handle);

		FilterType filter = (FilterType)selectionClauseElement.getValue();		
		Predicate predicate;

		if (filter.get_Id() != null)
			predicate = buildPredicate(filter.get_Id(), handle);
		else
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "The filer expression '" + selectionClauseElement.getName() + "' is not supported." , handle);

		return predicate != null ? new SelectionFilter(predicate) : null;
	}

	private Predicate buildPredicate(List<JAXBElement<? extends AbstractIdType>> idOperators, String handle) throws WFSException {
		// the resource id operator is always advertised
		return resourceIdFilterBuilder.buildIdOperator(idOperators, handle);			
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
