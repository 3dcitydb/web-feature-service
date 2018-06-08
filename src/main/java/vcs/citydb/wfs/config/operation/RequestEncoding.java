package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="RequestEncodingType", propOrder={
		"method",
		"useXMLValidation"
})
public class RequestEncoding {
	private EncodingMethod method = EncodingMethod.KVP_XML;
	private Boolean useXMLValidation = true;
	
	public EncodingMethod getMethod() {
		return method;
	}

	public void setMethod(EncodingMethod method) {
		this.method = method;
	}

	public boolean isUseXMLValidation() {
		return useXMLValidation;
	}

	public void setUseXMLValidation(boolean useXMLValidation) {
		this.useXMLValidation = useXMLValidation;
	}

}
