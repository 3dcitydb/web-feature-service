package vcs.citydb.wfs.config.feature;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name="ADEFeatureTypeNameType")
public class ADEFeatureTypeName {
	@XmlAttribute(required=true)
	private String namespaceURI;
	@XmlValue
	private String localPart;
	
	public String getNamespaceURI() {
		return namespaceURI;
	}
	
	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}
	
	public String getLocalPart() {
		return localPart;
	}
	
	public void setLocalPart(String localPart) {
		this.localPart = localPart;
	}
	
}
