package vcs.citydb.wfs.config.constraints;

import org.citydb.config.project.query.filter.lod.LodFilter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ConstraintsType", propOrder={
		"countDefault",
		"stripGeometry",
		"lodFilter"
})
public class Constraints {
	private Long countDefault = Long.MAX_VALUE;
	@XmlElement(defaultValue="false")
	private Boolean stripGeometry = false;
	private LodFilter lodFilter;

	public long getCountDefault() {
		return countDefault;
	}

	public void setCountDefault(long maxFeatureCount) {
		this.countDefault = maxFeatureCount;
	}
	
	public boolean isSetCountDefault() {
		return countDefault != Long.MAX_VALUE;
	}

	public boolean isStripGeometry() {		
		return stripGeometry;
	}

	public void setStripGeometry(boolean stripGeometry) {
		this.stripGeometry = stripGeometry;
	}

	public LodFilter getLodFilter() {
		return lodFilter;
	}

	public void setLodFilter(LodFilter lodFilter) {
		this.lodFilter = lodFilter;
	}
	
	public boolean isSetLodFilter() {
		return lodFilter != null;
	}
	
}
