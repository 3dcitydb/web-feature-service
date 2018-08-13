package vcs.citydb.wfs.operation.getfeature;

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.SQLQueryBuilder;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.set.SetOperationFactory;
import org.citydb.sqlbuilder.select.projection.ConstantColumn;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.sqlbuilder.select.projection.WildCardColumn;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;

import java.util.List;

public class QueryBuilder {	
	private final SQLQueryBuilder builder;

	public QueryBuilder(AbstractDatabaseAdapter databaseAdapter, SchemaMapping schemaMapping) {
		builder = new SQLQueryBuilder(schemaMapping, databaseAdapter);
	}

	public Select buildQuery(List<QueryExpression> queryExpressions) throws WFSException {
		Select select = null;

		int nrOfQueries = queryExpressions.size();
		String queryMatchAlias = nrOfQueries > 1 ? "match_query" : "match_all";

		for (int i = 0; i < queryExpressions.size(); ++i) {
			QueryExpression queryExpression = queryExpressions.get(i);
			Select tmp = null;
			Table table = null;

			try {
				tmp = builder.buildQuery(queryExpression);
			} catch (QueryBuildException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to build database query.", queryExpression.getHandle(), e);
			}

			// add ordering
			if (!queryExpression.isSetOrderBy()) {
				ProjectionToken token = tmp.getProjection().get(0);
				if (token instanceof Column && ((Column)token).getName().equals(MappingConstants.ID))
					tmp.addOrderBy(new OrderByToken((Column)token));
				else {
					WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.INTERNAL_SERVER_ERROR);
					message.addExceptionText("Failed to build database query.");
					message.addExceptionText("Unexpected SQL projection clause.");
					message.setLocator(queryExpression.getHandle());
					throw new WFSException(message);
				}
			}

			table = new Table(tmp);			
			tmp = new Select();

			tmp.addProjection(table.getColumn(MappingConstants.ID));
			tmp.addProjection(table.getColumn(MappingConstants.OBJECTCLASS_ID));
			tmp.addProjection(new Function("count(1) over", queryMatchAlias));

			if (nrOfQueries > 1)
				tmp.addProjection(new ConstantColumn(new IntegerLiteral(i), "query_no"));

			if (i == 0)
				select = tmp;
			else {
				table = new Table(SetOperationFactory.unionAll(select, tmp));
				select = new Select();

				if (i < nrOfQueries - 1)
					select.addProjection(new WildCardColumn(table));
				else {
					select.addProjection(table.getColumn(MappingConstants.ID));
					select.addProjection(table.getColumn(MappingConstants.OBJECTCLASS_ID));
					select.addProjection(new Function("count(1) over", "match_all"));
					select.addProjection(table.getColumn("match_query"));
					select.addProjection(table.getColumn("query_no"));
				}
			}
		}

		return select;
	}

	public Select buildHitsQuery(QueryExpression queryExpression) throws WFSException {
		Select select = null;

		try {
			select = builder.buildQuery(queryExpression);
		} catch (QueryBuildException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to build database query.", queryExpression.getHandle(), e);
		}

		select.unsetProjection();
		select.addProjection(new Function("count", new WildCardColumn()));

		return select;
	}

}
