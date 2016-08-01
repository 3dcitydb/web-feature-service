/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2016
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vcs.citydb.wfs.operation.getfeature;

import java.util.ArrayList;
import java.util.List;

import org.citydb.modules.common.filter.feature.FeatureClassFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

import vcs.citydb.wfs.exception.WFSException;

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
			query.append(") query order by query_no, id");

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
				
				if (ids.size() == 1)
					builder.append("co.gmlid = ?");
				else {
					builder.append("co.gmlid in (");
					for (int i = 0; i < ids.size(); i++) {
						builder.append("?");
						if (i < ids.size() - 1)
							builder.append(", ");
						else
							builder.append(")");
					}
				}
				
				return builder.toString();
			}
		}

		return null;
	}

}
