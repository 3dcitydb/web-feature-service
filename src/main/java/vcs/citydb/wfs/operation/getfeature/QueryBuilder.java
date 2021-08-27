package vcs.citydb.wfs.operation.getfeature;

import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.sql.SQLQueryBuilder;
import org.citydb.core.query.filter.counter.CounterFilter;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.sqlbuilder.select.projection.WildCardColumn;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

public class QueryBuilder {
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SQLQueryBuilder builder;

	public QueryBuilder(AbstractDatabaseAdapter databaseAdapter, SchemaMapping schemaMapping) {
		this.databaseAdapter = databaseAdapter;
		builder = new SQLQueryBuilder(schemaMapping, databaseAdapter);
	}

	public Select buildQuery(QueryExpression queryExpression, long count, long pageNumber) throws WFSException {
		CounterFilter counterFilter = new CounterFilter();
		if (pageNumber > 0 && queryExpression.isSetStartId())
			counterFilter.setStartId(queryExpression.getStartId());
		else if (queryExpression.getStartIndex() > 0)
			counterFilter.setStartIndex(queryExpression.getStartIndex());

		if (count != Constants.COUNT_DEFAULT)
			counterFilter.setCount(queryExpression.getNumberReturned());

		if (counterFilter.isSetCount() || counterFilter.isSetStartId() || counterFilter.isSetStartIndex())
			queryExpression.setCounterFilter(counterFilter);

		try {
			return builder.buildQuery(queryExpression);
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
		if (pageNumber > 0 && queryExpression.isSetStartId())
			counterFilter.setStartId(queryExpression.getStartId());
		else if (startIndex > 0)
			counterFilter.setStartIndex(startIndex);

		if (count != Constants.COUNT_DEFAULT)
			counterFilter.setCount(count);

		if (counterFilter.isSetCount() || counterFilter.isSetStartId() || counterFilter.isSetStartIndex())
			hitsQuery.setCounterFilter(counterFilter);

		return buildHitsQuery(hitsQuery);
	}

	private Select buildHitsQuery(QueryExpression queryExpression) throws WFSException {
		try {
			Select select = builder.buildQuery(queryExpression)
					.unsetOrderBy()
					.removeProjectionIf(t -> !(t instanceof Column) || !((Column) t).getName().equals(MappingConstants.ID));

			return new Select().addProjection(new Function("count", new WildCardColumn(new Table(select), false)));
		} catch (QueryBuildException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build the database query.", queryExpression.getHandle(), e);
		}
	}

	public Select buildGlobalAppearanceQuery(CacheTable globalAppTempTable) {
		String schema = databaseAdapter.getConnectionDetails().getSchema();

		Table appearance = new Table("appearance", schema);
		Table appearToSurfaceData = new Table("appear_to_surface_data", schema);
		Table surfaceData = new Table("surface_data", schema);
		Table textureParam = new Table("textureparam", schema);
		Table tempTable = new Table(globalAppTempTable.getTableName());

		return new Select()
				.addProjection(appearance.getColumn(MappingConstants.ID)).setDistinct(true)
				.addJoin(JoinFactory.inner(appearToSurfaceData, "appearance_id", ComparisonName.EQUAL_TO, appearance.getColumn(MappingConstants.ID)))
				.addJoin(JoinFactory.inner(surfaceData, MappingConstants.ID, ComparisonName.EQUAL_TO, appearToSurfaceData.getColumn("surface_data_id")))
				.addJoin(JoinFactory.inner(textureParam, "surface_data_id", ComparisonName.EQUAL_TO, surfaceData.getColumn(MappingConstants.ID)))
				.addJoin(JoinFactory.inner(tempTable, MappingConstants.ID, ComparisonName.EQUAL_TO, textureParam.getColumn("surface_geometry_id")))
				.addSelection(ComparisonFactory.isNull(appearance.getColumn("cityobject_id")));
	}
}
