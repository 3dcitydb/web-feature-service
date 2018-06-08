package vcs.citydb.wfs.operation.filter;

import net.opengis.fes._2.AbstractIdType;
import net.opengis.fes._2.ResourceIdType;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.operator.id.ResourceIdOperator;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.List;

public class ResourceIdFilterBuilder {

	public Predicate buildIdOperator(List<JAXBElement<? extends AbstractIdType>> idOpsElement, String handle) throws WFSException {
		ResourceIdOperator predicate = new ResourceIdOperator();
		
		for (JAXBElement<?> abstractIdElement : idOpsElement) {
			if (!abstractIdElement.getName().getNamespaceURI().equals(Constants.FES_NAMESPACE_URI))
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only comparison operators associated with the namespace " + Constants.FES_NAMESPACE_URI + " are supported.", handle);

			predicate.addResourceId(getResourceId(abstractIdElement, handle));
		}
		
		return predicate;
	}
	
	private String getResourceId(JAXBElement<?> abstractIdElement, String handle) throws WFSException {
		if (!(abstractIdElement.getValue() instanceof ResourceIdType))
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only " + new QName(Constants.FES_NAMESPACE_URI, "ResourceId").toString() + " is supported as ID filter.", handle);

		ResourceIdType resourceId = (ResourceIdType)abstractIdElement.getValue();
		if (!resourceId.isSetRid())
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The mandatory rid attribute is not provided on the " + new QName(Constants.FES_NAMESPACE_URI, "ResourceId").toString() + " element.", handle);

		if (resourceId.isSetVersion() ||
				resourceId.isSetStartDate() ||
				resourceId.isSetEndDate() ||
				resourceId.isSetPreviousRid())
			; // TODO: silently discard according to spec until implemented

		return resourceId.getRid().replaceAll("^#+", "");
	}
}
