/*
 * This file is part of the 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright (c) 2014
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 * 
 * The 3D City Database Web Feature Service is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package vcs.citydb.wfs.operation.getfeature;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;

import vcs.citydb.wfs.exception.WFSException;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.modules.common.filter.feature.GmlIdFilter;
import de.tub.citydb.util.Util;

public class QueryBuilder {
	// TODO: rewrite query builder using a dynamic sql generator such as squiggle
	private String optimizerHint;

	public String buildQuery(List<QueryExpression> queryExpressions) throws WFSException {
		optimizerHint = "";
		StringBuilder query = new StringBuilder();

		int nrOfQueries = queryExpressions.size();
		boolean isMultipleQueryRequest = nrOfQueries > 1;
		String queryMatchAlias = null;

		if (isMultipleQueryRequest) {
			query.append("select id, objectclass_id, count(1) over () match_all, query_no, match_query from (");
			queryMatchAlias = "match_query";
		} else
			queryMatchAlias = "match_all";

		for (int i = 0; i < queryExpressions.size(); ++i) {
			QueryExpression queryExpression = queryExpressions.get(i);
			StringBuilder select = new StringBuilder();

			String resourceIdSelection = getResourceIdSelection(queryExpression);

			select.append("select ").append(optimizerHint).append(" co.id, co.objectclass_id, count(1) over () as ").append(queryMatchAlias).append(" ");

			if (isMultipleQueryRequest)
				select.append(", " + i + " as query_no ");

			select.append("from cityobject co ")
			.append("where ").append(getFeatureTypeSelection(queryExpression));

			if (resourceIdSelection != null)
				select.append(" and ").append(resourceIdSelection);

			if (nrOfQueries == 1)
				select.append(" order by co.id");
			else if (i < nrOfQueries - 1)
				select.append(" union all ");

			query.append(select);
		}

		if (isMultipleQueryRequest)
			query.append(" order by query_no, id) as query");

		return query.toString();
	}

	public String buildHitsQuery(QueryExpression queryExpression) {
		optimizerHint = "";
		StringBuilder select = new StringBuilder();

		String resourceIdSelection = getResourceIdSelection(queryExpression);

		select.append("select ").append(optimizerHint).append(" count(co.id) ")
		.append("from cityobject co ")
		.append("where ").append(getFeatureTypeSelection(queryExpression));

		if (resourceIdSelection != null)
			select.append(" and ").append(resourceIdSelection);

		return select.toString();
	}

	private String getFeatureTypeSelection(QueryExpression queryExpression) {
		FeatureClassFilter filter = queryExpression.getFeatureTypeFilter();

		if (filter != null) {
			List<Integer> classIds = new ArrayList<Integer>();
			for (CityGMLClass cityGMLClass : filter.getNotFilterState())
				classIds.add(Util.cityObject2classId(cityGMLClass));

			if (!classIds.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				builder.append(" co.objectclass_id in (").append(Util.collection2string(classIds, ", ")).append(") ");
				return builder.toString();
			}
		}

		return null;
	}

	private String getResourceIdSelection(QueryExpression queryExpression) {
		GmlIdFilter filter = queryExpression.getGmlIdFilter();

		if (filter != null) {
			List<String> ids = filter.getFilterState();
			if (ids != null && !ids.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				builder.append("co.gmlid in ('").append(Util.collection2string(ids, "', '")).append("')");
				return builder.toString();
			}
		}

		return null;
	}

}
