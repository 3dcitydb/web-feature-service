package vcs.citydb.wfs.operation.getfeature;

import org.citydb.core.query.Query;
import vcs.citydb.wfs.operation.AbstractQueryExpression;

public class QueryExpression extends AbstractQueryExpression {
	private String featureIdentifier;
	private long numberReturned;
	private long startIndex;

	public QueryExpression() {
	}

	public QueryExpression(QueryExpression other) {
		super(other);
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

	public long getNumberReturned() {
		return numberReturned;
	}

	public void setNumberReturned(long numberReturned) {
		this.numberReturned = numberReturned;
	}

	long getStartIndex() {
		return startIndex;
	}

	void setStartIndex(long startIndex) {
		this.startIndex = startIndex;
	}

	@Override
	public void copyFrom(Query query) {
		super.copyFrom(query);
		if (query instanceof QueryExpression) {
			QueryExpression other = (QueryExpression) query;
			featureIdentifier = other.featureIdentifier;
			numberReturned = other.numberReturned;
			startIndex = other.startIndex;
		}
	}
}
