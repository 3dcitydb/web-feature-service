package vcs.citydb.wfs.config.constraints;

import org.citydb.config.project.query.filter.lod.LodFilter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ConstraintsType", propOrder={
		"stripGeometry",
		"lodFilter"
})
public class Constraints {
	@XmlElement(defaultValue="false")
	private Boolean stripGeometry = false;
	private LodFilter lodFilter;

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
