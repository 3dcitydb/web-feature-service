package vcs.citydb.wfs.config.constraints;

import vcs.citydb.wfs.config.Constants;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ConstraintsType", propOrder={})
public class Constraints {
	private Boolean supportAdHocQueries = true;
	private Long countDefault = Constants.COUNT_DEFAULT;
	private Boolean computeNumberMatched = true;
	private Boolean useDefaultSorting = false;
	private Boolean currentVersionOnly = true;
	private Boolean exportCityDBMetadata = false;
	private Boolean exportAppearance = false;
	private Boolean useResultPaging = true;
	private Boolean stripGeometry = false;
	private LodFilter lodFilter;

	public Constraints() {
		lodFilter = new LodFilter();
	}

	public boolean isSupportAdHocQueries() {
		return supportAdHocQueries;
	}

	public void setSupportAdHocQueries(boolean supportAdHocQueries) {
		this.supportAdHocQueries = supportAdHocQueries;
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

	public boolean isComputeNumberMatched() {
		return computeNumberMatched;
	}

	public void setComputeNumberMatched(boolean computeNumberMatched) {
		this.computeNumberMatched = computeNumberMatched;
	}

	public boolean isUseDefaultSorting() {
		return useDefaultSorting;
	}

	public void setUseDefaultSorting(boolean useDefaultSorting) {
		this.useDefaultSorting = useDefaultSorting;
	}

	public boolean isCurrentVersionOnly() {
		return currentVersionOnly;
	}

	public void setCurrentVersionOnly(boolean currentVersionOnly) {
		this.currentVersionOnly = currentVersionOnly;
	}

	public boolean isExportCityDBMetadata() {
		return exportCityDBMetadata;
	}

	public void setExportCityDBMetadata(boolean exportCityDBMetadata) {
		this.exportCityDBMetadata = exportCityDBMetadata;
	}

	public boolean isExportAppearance() {
		return exportAppearance;
	}

	public void setExportAppearance(boolean exportAppearance) {
		this.exportAppearance = exportAppearance;
	}

	public boolean isUseResultPaging() {
		return useResultPaging;
	}

	public void setUseResultPaging(boolean useResultPaging) {
		this.useResultPaging = useResultPaging;
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
