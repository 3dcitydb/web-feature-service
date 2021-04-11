package vcs.citydb.wfs.operation.getfeature;

import org.citydb.query.Query;

public class QueryExpression extends Query {
	private String handle;
	private String featureIdentifier;

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public boolean isGetFeatureById() {
		return featureIdentifier != null;
	}

	public String getFeatureIdentifier() {
		return featureIdentifier;
	}

	public void setFeatureIdentifier(String featureIdentifier) {
		this.featureIdentifier = featureIdentifier;
	}
	
}
