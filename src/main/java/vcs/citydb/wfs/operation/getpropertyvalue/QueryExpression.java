package vcs.citydb.wfs.operation.getpropertyvalue;

import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.query.Query;
import vcs.citydb.wfs.operation.AbstractQueryExpression;

public class QueryExpression extends AbstractQueryExpression {
	private String valueReference;
	private SchemaPath schemaPath;
	private long propertyOffset;

	public QueryExpression() {
	}

	public QueryExpression(QueryExpression other) {
		super(other);
	}
	
	public String getValueReference() {
		return valueReference;
	}

	public void setValueReference(String valueReference) {
		this.valueReference = valueReference;
	}

	public SchemaPath getSchemaPath() {
		return schemaPath;
	}

	public void setSchemaPath(SchemaPath schemaPath) {
		this.schemaPath = schemaPath;
	}

	public long getPropertyOffset() {
		return propertyOffset;
	}

	public void setPropertyOffset(long propertyOffset) {
		this.propertyOffset = Math.max(propertyOffset, 0);
	}

	@Override
	public void copyFrom(Query query) {
		super.copyFrom(query);
		if (query instanceof QueryExpression) {
			QueryExpression other = (QueryExpression) query;
			valueReference = other.valueReference;
			schemaPath = other.schemaPath;
			propertyOffset = other.propertyOffset;
		}
	}
}
