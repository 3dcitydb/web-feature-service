package vcs.citydb.wfs.config.capabilities;

import net.opengis.ows._1.LanguageStringType;
import net.opengis.ows._1.ServiceIdentification;
import net.opengis.ows._1.ServiceProvider;
import vcs.citydb.wfs.config.Constants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="OWSMetadataType", propOrder={
		"serviceIdentification",
		"serviceProvider"
})
public class OWSMetadata {
	@XmlElement(name="ServiceIdentification", namespace=Constants.OWS_NAMESPACE_URI, required=true)
	private ServiceIdentification serviceIdentification;
	@XmlElement(name="ServiceProvider", namespace=Constants.OWS_NAMESPACE_URI, required=true)
	private ServiceProvider serviceProvider;
	
	public OWSMetadata() {
		serviceIdentification = new ServiceIdentification();
		LanguageStringType defaultTitle = new LanguageStringType();
		defaultTitle.setValue(Constants.DEFAULT_OWS_TITLE);
		serviceIdentification.getTitle().add(defaultTitle);		
		serviceProvider = new ServiceProvider();
	}
	
	public ServiceIdentification getServiceIdentification() {
		return serviceIdentification;
	}
	
	public void setServiceIdentification(ServiceIdentification serviceIdentification) {
		this.serviceIdentification = serviceIdentification;
	}
	
	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}
	
	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}
	
}
