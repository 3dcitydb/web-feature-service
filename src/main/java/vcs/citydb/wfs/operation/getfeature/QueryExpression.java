package vcs.citydb.wfs.operation.getfeature;

import org.citydb.query.Query;

public class QueryExpression extends Query {
	private String handle;
	private boolean isGetFeatureById;

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public boolean isGetFeatureById() {
		return isGetFeatureById;
	}

	public void setIsGetFeatureById(boolean isGetFeatureById) {
		this.isGetFeatureById = isGetFeatureById;
	}
	
}
