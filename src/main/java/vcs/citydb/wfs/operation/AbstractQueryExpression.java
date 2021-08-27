package vcs.citydb.wfs.operation;

import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.database.schema.mapping.SimpleAttribute;
import org.citydb.core.query.Query;
import org.citydb.core.query.filter.sorting.SortProperty;

public abstract class AbstractQueryExpression extends Query {
    private String handle;
    private long numberMatched;
    private long startId;

    public AbstractQueryExpression() {
        numberMatched = -1;
        startId = -1;
    }

    public AbstractQueryExpression(Query other) {
        super(other);
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public boolean isSetNumberMatched() {
        return numberMatched != -1;
    }

    public long getNumberMatched() {
        return isSetNumberMatched() ? numberMatched : 0;
    }

    public void setNumberMatched(long numberMatched) {
        this.numberMatched = Math.max(numberMatched, -1);
    }

    public boolean isSetStartId() {
        return startId != -1;
    }

    public long getStartId() {
        return isSetStartId() ? startId : 0;
    }

    public void setStartId(long startId) {
        this.startId = Math.max(startId, -1);
    }

    public boolean supportsPagingByStartId() {
        if (!isSetSorting())
            return true;

        if (getSorting().getSortProperties().size() != 1)
            return false;

        SortProperty sortProperty = getSorting().getSortProperties().get(0);
        if (sortProperty.getValueReference().getSchemaPath().getLastNode().getPathElement().getElementType() == PathElementType.SIMPLE_ATTRIBUTE) {
            SimpleAttribute attribute = (SimpleAttribute) sortProperty.getValueReference().getSchemaPath().getLastNode().getPathElement();
            return MappingConstants.ID.equalsIgnoreCase(attribute.getColumn())
                    && attribute.hasParentType()
                    && MappingConstants.CITYOBJECT.equalsIgnoreCase(attribute.getParentType().getTable());
        }

        return false;
    }

    @Override
    public void copyFrom(Query query) {
        super.copyFrom(query);
        if (query instanceof AbstractQueryExpression) {
            AbstractQueryExpression other = (AbstractQueryExpression) query;
            handle = other.handle;
            numberMatched = other.numberMatched;
            startId = other.startId;
        }
    }
}
