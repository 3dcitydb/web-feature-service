package vcs.citydb.wfs.config.constraints;

import vcs.citydb.wfs.config.Constants;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ConstraintsType", propOrder={})
public class Constraints {
	private Long countDefault = Constants.COUNT_DEFAULT;
	private Boolean exportCityDBMetadata = false;
	private Boolean stripGeometry = false;
	private LodFilter lodFilter;

	public Constraints() {
		lodFilter = new LodFilter();
	}
	
	public long getCountDefault() {
		return countDefault;
	}

	public void setCountDefault(long countDefault) {
		this.countDefault = countDefault >= 0 ? countDefault : Constants.COUNT_DEFAULT;
	}

	public boolean isSetCountDefault() {
		return countDefault != Constants.COUNT_DEFAULT;
	}

	public boolean isExportCityDBMetadata() {
		return exportCityDBMetadata;
	}

	public void setExportCityDBMetadata(boolean exportCityDBMetadata) {
		this.exportCityDBMetadata = exportCityDBMetadata;
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
}
