package vcs.citydb.wfs.operation.filter;

import net.opengis.fes._2.FilterType;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.SelectionFilter;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

import javax.xml.bind.JAXBElement;

public class FilterHandler {
	private final ResourceIdFilterBuilder resourceIdFilterBuilder;

	public FilterHandler() {
		resourceIdFilterBuilder = new ResourceIdFilterBuilder();
	}

	public SelectionFilter getSelection(JAXBElement<?> selectionClauseElement, String handle) throws WFSException {
		if (!(selectionClauseElement.getValue() instanceof FilterType))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The element '" + selectionClauseElement.getName() + "' is not supported as selection clause of queries.", handle);

		FilterType filter = (FilterType)selectionClauseElement.getValue();		
		Predicate predicate;

		if (filter.get_Id() != null)
			predicate = resourceIdFilterBuilder.buildIdOperator(filter.get_Id(), handle);
		else
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "The filer expression '" + selectionClauseElement.getName() + "' is not supported." , handle);

		return predicate != null ? new SelectionFilter(predicate) : null;
	}

}
