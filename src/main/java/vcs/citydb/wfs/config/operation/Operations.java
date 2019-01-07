package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="OperationsType", propOrder={
		"requestEncoding",
		"exportCityDBMetadata",
		"describeFeatureType",
		"getFeature"
})
public class Operations {
	private RequestEncoding requestEncoding;
	private Boolean exportCityDBMetadata = false;
	@XmlElement(name="DescribeFeatureType")
	private DescribeFeatureTypeOperation describeFeatureType;
	@XmlElement(name="GetFeature")
    private GetFeatureOperation getFeature;

	public Operations() {
		requestEncoding = new RequestEncoding();
		describeFeatureType = new DescribeFeatureTypeOperation();
		getFeature = new GetFeatureOperation();
	}

	public RequestEncoding getRequestEncoding() {
		return requestEncoding;
	}

	public void setRequestEncoding(RequestEncoding requestEncoding) {
		this.requestEncoding = requestEncoding;
	}

	public boolean isExportCityDBMetadata() {
		return exportCityDBMetadata;
	}

	public void setExportCityDBMetadata(boolean exportCityDBMetadata) {
		this.exportCityDBMetadata = exportCityDBMetadata;
	}

	public DescribeFeatureTypeOperation getDescribeFeatureType() {
		return describeFeatureType;
	}

	public void setDescribeFeatureType(DescribeFeatureTypeOperation describeFeatureType) {
		this.describeFeatureType = describeFeatureType;
	}

	public GetFeatureOperation getGetFeature() {
		return getFeature;
	}

	public void setGetFeature(GetFeatureOperation getFeature) {
		this.getFeature = getFeature;
	}

}
