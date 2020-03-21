package vcs.citydb.wfs.config.capabilities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(name="CapabilitiesType", propOrder={
		"owsMetadata"
})
public class Capabilities {
	@XmlElement(required=true)
	private OWSMetadata owsMetadata;
	
	public Capabilities() {
		owsMetadata = new OWSMetadata();
	}

	public OWSMetadata getOwsMetadata() {
		return owsMetadata;
	}

	public void setOwsMetadata(OWSMetadata owsMetadata) {
		this.owsMetadata = owsMetadata;
	}
	
	public List<String> getSupportedWFSVersions() {
		return owsMetadata.getServiceIdentification().getServiceTypeVersion();
	}

}
