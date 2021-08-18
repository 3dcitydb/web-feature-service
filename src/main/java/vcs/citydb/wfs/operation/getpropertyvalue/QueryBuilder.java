package vcs.citydb.wfs.operation.getpropertyvalue;

import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.mapping.SimpleAttribute;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.sql.BuildProperties;
import org.citydb.core.query.builder.sql.SQLQueryBuilder;
import org.citydb.core.query.builder.sql.SQLQueryContext;
import org.citydb.core.query.filter.counter.CounterFilter;
import org.citydb.core.query.filter.selection.operator.comparison.ComparisonOperatorName;
import org.citydb.core.query.filter.sorting.SortProperty;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.sqlbuilder.select.projection.WildCardColumn;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

public class QueryBuilder {	
	private final SQLQueryBuilder builder;

	public QueryBuilder(AbstractDatabaseAdapter databaseAdapter, SchemaMapping schemaMapping) {
		builder = new SQLQueryBuilder(schemaMapping, databaseAdapter, BuildProperties.defaults().suppressDistinct(true));
	}

	public Select buildQuery(QueryExpression queryExpression, long startIndex, long count, long numberReturned, long pageNumber) throws WFSException {
		try {
			boolean addSortById = false;
			if (queryExpression.getNumberMatched() > numberReturned) {
				// build counter filter
				CounterFilter counterFilter = new CounterFilter();
				if (pageNumber > 0 && queryExpression.isSetStartId())
					counterFilter.setStartId(queryExpression.getStartId(), ComparisonOperatorName.GREATER_THAN_OR_EQUAL_TO);
				else if (startIndex > 0)
					counterFilter.setStartIndex(startIndex);

				if (queryExpression.getNumberMatched() > count)
					counterFilter.setCount(count + queryExpression.getPropertyOffset());

				if (counterFilter.isSetCount() || counterFilter.isSetStartId() || counterFilter.isSetStartIndex())
					queryExpression.setCounterFilter(counterFilter);
			} else
				addSortById = requiresSortById(queryExpression);

			// build initial query context for the target property
			builder.getBuildProperties().optimizeJoins(true);
			SQLQueryContext context = builder.buildSchemaPath(queryExpression.getSchemaPath(), false);

			// build query
			builder.getBuildProperties().optimizeJoins(isSimpleProperty(queryExpression.getSchemaPath()));
			Select select = builder.buildQuery(queryExpression, context);

			// add sorting by id property if required
			if (addSortById)
				select.addOrderBy(new OrderByToken(context.getFromTable().getColumn(MappingConstants.ID)));

			return select;
		} catch (QueryBuildException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build the database query.", queryExpression.getHandle(), e);
		}
	}

	public Select buildNumberMatchedQuery(QueryExpression queryExpression) throws WFSException {
		QueryExpression hitsQuery = new QueryExpression(queryExpression);
		hitsQuery.unsetCounterFilter();
		hitsQuery.unsetSorting();

		return buildHitsQuery(hitsQuery);
	}

	public Select buildNumberReturnedQuery(QueryExpression queryExpression, long count, long startIndex, long pageNumber) throws WFSException {
		QueryExpression hitsQuery = new QueryExpression(queryExpression);
		hitsQuery.unsetCounterFilter();
		hitsQuery.unsetSorting();

		CounterFilter counterFilter = new CounterFilter();
		if (pageNumber > 0 && hitsQuery.isSetStartId())
			counterFilter.setStartId(hitsQuery.getStartId(), ComparisonOperatorName.GREATER_THAN_OR_EQUAL_TO);
		else if (startIndex > 0)
			counterFilter.setStartIndex(startIndex);

		if (count != Constants.COUNT_DEFAULT)
			counterFilter.setCount(count + queryExpression.getPropertyOffset());

		if (counterFilter.isSetCount() || counterFilter.isSetStartId() || counterFilter.isSetStartIndex())
			hitsQuery.setCounterFilter(counterFilter);

		return buildHitsQuery(hitsQuery);
	}

	private Select buildHitsQuery(QueryExpression queryExpression) throws WFSException {
		try {
			builder.getBuildProperties().optimizeJoins(true);
			SQLQueryContext context = builder.buildSchemaPath(queryExpression.getSchemaPath(), false);

			builder.getBuildProperties().optimizeJoins(isSimpleProperty(queryExpression.getSchemaPath()));
			Select select = builder.buildQuery(queryExpression, context)
					.unsetOrderBy()
					.removeProjectionIf(t -> !(t instanceof Column) || !((Column) t).getName().equals(MappingConstants.ID));

			return new Select().addProjection(new Function("count", new WildCardColumn(new Table(select), false)));
		} catch (QueryBuildException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build the database query.", queryExpression.getHandle(), e);
		}
	}

	private boolean isSimpleProperty(SchemaPath schemaPath) {
		PathElementType type = schemaPath.getLastNode().getPathElement().getElementType();
		return (type == PathElementType.SIMPLE_ATTRIBUTE || type == PathElementType.GEOMETRY_PROPERTY);
	}

	private boolean requiresSortById(QueryExpression queryExpression) {
		if (queryExpression.isSetSorting() && queryExpression.getSorting().hasSortProperties()) {
			int size = queryExpression.getSorting().getSortProperties().size();
			SortProperty last = queryExpression.getSorting().getSortProperties().get(size - 1);
			if (last.getValueReference().getSchemaPath().getLastNode().getPathElement().getElementType() == PathElementType.SIMPLE_ATTRIBUTE) {
				SimpleAttribute attribute = (SimpleAttribute) last.getValueReference().getSchemaPath().getLastNode().getPathElement();
				return MappingConstants.ID.equalsIgnoreCase(attribute.getColumn())
						&& attribute.hasParentType()
						&& MappingConstants.CITYOBJECT.equalsIgnoreCase(attribute.getParentType().getTable());
			}
		}

		return true;
	}
}
