package vcs.citydb.wfs.config.security;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SecurityType", propOrder={
		"maxFeatureCount",
		"stripGeometry"
})
public class Security {
	private Long maxFeatureCount = Long.MAX_VALUE;
	@XmlElement(defaultValue="false")
	private Boolean stripGeometry = false;

	public long getMaxFeatureCount() {
		return maxFeatureCount;
	}

	public void setMaxFeatureCount(long maxFeatureCount) {
		this.maxFeatureCount = maxFeatureCount;
	}

	public boolean isStripGeometry() {		
		return stripGeometry;
	}

	public void setStripGeometry(boolean stripGeometry) {
		this.stripGeometry = stripGeometry;
	}
	
}
