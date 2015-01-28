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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.fes._2.FilterType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.QueryType;
import net.opengis.wfs._2.ResolveValueType;
import net.opengis.wfs._2.StoredQueryType;

import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.project.filter.FeatureClass;
import org.citydb.config.project.filter.FilterMode;
import org.citydb.config.project.filter.GmlId;
import org.citydb.log.Logger;
import org.citydb.modules.common.filter.feature.FeatureClassFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.operation.FeatureTypeHandler;
import vcs.citydb.wfs.operation.FilterHandler;
import vcs.citydb.wfs.operation.storedquery.StoredQuery;
import vcs.citydb.wfs.operation.storedquery.StoredQueryManager;
import vcs.citydb.wfs.util.LoggerUtil;
import vcs.citydb.wfs.xml.NamespaceFilter;

public class GetFeatureHandler {
	private final Logger log = Logger.getInstance();
	private final WFSConfig wfsConfig;
	private final Config exporterConfig;

	private final ExportController controller;
	private final BaseRequestHandler baseRequestHandler;
	private final FeatureTypeHandler featureTypeHandler;
	private final FilterHandler filterHandler;
	private final StoredQueryManager storedQueryManager;

	public GetFeatureHandler(JAXBBuilder jaxbBuilder, WFSConfig wfsConfig, Config exporterConfig) {
		this.wfsConfig = wfsConfig;
		this.exporterConfig = exporterConfig;

		controller = new ExportController(jaxbBuilder, wfsConfig, exporterConfig);
		baseRequestHandler = new BaseRequestHandler();
		featureTypeHandler = new FeatureTypeHandler();
		filterHandler = new FilterHandler();

		storedQueryManager = (StoredQueryManager)ObjectRegistry.getInstance().lookup(StoredQueryManager.class.getName());
	}

	public void doOperation(GetFeatureType wfsRequest,
			NamespaceFilter namespaceFilter,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		log.info(LoggerUtil.getLogMessage(request, "Accepting GetFeature request."));
		final List<QueryExpression> queryExpressions = new ArrayList<QueryExpression>();
		final String operationHandle = wfsRequest.getHandle();
		CityGMLVersion version = null;

		// check base service parameters
		baseRequestHandler.validate(wfsRequest);

		// check output format
		if (wfsRequest.isSetOutputFormat() && !wfsConfig.getOperations().getGetFeature().supportsOutputFormat(wfsRequest.getOutputFormat())) {
			WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.OPTION_NOT_SUPPORTED);
			message.addExceptionText("The output format of a GetFeature request must match one of the following formats:");
			message.addExceptionTexts(wfsConfig.getOperations().getGetFeature().getOutputFormatAsString());
			message.setLocator(operationHandle);
			
			throw new WFSException(message);
		}
		
		// TODO: add support for response paging
		if (wfsRequest.isSetStartIndex())
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Response paging is not supported.", operationHandle);

		// TODO: add support for local resource resolving
		if (wfsRequest.getResolve() != ResolveValueType.NONE)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Resource resolving is not supported.", operationHandle);

		// check for queries to be present
		if (wfsRequest.getAbstractQueryExpression().isEmpty())
			throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "No query provided.", operationHandle);

		// compile queries to be executed from ad hoc and stored queries
		List<QueryTypeWrapper> queries = new ArrayList<QueryTypeWrapper>();
		for (JAXBElement<? extends AbstractQueryExpressionType> queryElem: wfsRequest.getAbstractQueryExpression()) {
			if (!(queryElem.getValue() instanceof StoredQueryType))
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only stored query expressions are supported in a GetFeature request.", operationHandle);
				
			compileQuery(queryElem.getValue(), queries, namespaceFilter, operationHandle);
		}

		// iterate through queries		
		for (QueryTypeWrapper query : queries) {
			// dummy fields to store parsing results
			// TODO: must be replaced with FE layer
			Set<QName> featureTypeNames = null;
			Set<String> resourceIds = null;

			String queryHandle = query.queryType.isSetHandle() ? query.queryType.getHandle() : operationHandle;

			// TODO: aliases - and implicitly joins - are not supported
			if (!query.queryType.getAliases().isEmpty())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Aliases for feature type names are not supported.", queryHandle);

			if (query.queryType.isSetFeatureVersion())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Feature versioning is not supported.", queryHandle);

			// TODO: add support for sorting
			if (query.queryType.isSetAbstractSortingClause())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Sorting is not supported.", queryHandle);

			// TODO: add support for coordinate transformation
			if (query.queryType.isSetSrsName())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Coordinate transformation is not supported.", queryHandle);

			// get feature type names and check for unique CityGML version
			if (query.queryType.getTypeNames().size() > 1)
				throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "Join queries are not supported.", queryHandle);
			
			featureTypeNames = featureTypeHandler.getFeatureTypeNames(query.queryType.getTypeNames(), namespaceFilter, false, queryHandle);
			CityGMLVersion featureVersion = CityGMLVersion.fromCityGMLModule(Modules.getCityGMLModule(featureTypeNames.iterator().next().getNamespaceURI()));
			if (version == null)
				version = featureVersion;
			else if (version != featureVersion)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Mixing feature types from different CityGML versions is not supported.", queryHandle);
			
			// TODO: add support for projection attributes
			if (query.queryType.isSetAbstractProjectionClause())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Property projection is not supported.", queryHandle);

			// validate selection clause of query
			if (query.queryType.isSetAbstractSelectionClause()) {
				FilterType filter = validateSelectionClause(query.queryType.getAbstractSelectionClause(), queryHandle);

				if (filter != null) {
					resourceIds = filterHandler.getResourceIds(filter, queryHandle);
				}
			}

			// map parsing result into query expression
			// TODO: again, the query expression should simply hold
			// objects from the intermediate FE layer
			QueryExpression queryExpression = getQueryExpression(featureTypeNames, resourceIds, query.isGetFeatureById);
			queryExpression.setHandle(queryHandle);
			queryExpressions.add(queryExpression);
		}

		if (queryExpressions.size() == 0)
			throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "No valid query expressions provided.", operationHandle);
				
		controller.doExport(wfsRequest, queryExpressions, version, request, response);
		log.info(LoggerUtil.getLogMessage(request, "GetFeature operation successfully finished."));
	}

	private void compileQuery(AbstractQueryExpressionType abstractQuery, List<QueryTypeWrapper> queries, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		if (abstractQuery instanceof QueryType) {
			queries.add(new QueryTypeWrapper((QueryType)abstractQuery, false));
		} 
		
		else if (abstractQuery instanceof StoredQueryType) {
			StoredQueryType query = (StoredQueryType)abstractQuery;

			StoredQuery storedQuery = storedQueryManager.getStoredQuery(query.getId(), handle);
			if (storedQuery != null) {
				if (storedQuery.getId().equals("http://www.opengis.net/def/query/OGC-WFS/0/GetFeatureById")) {
					queries.add(new QueryTypeWrapper((QueryType)storedQuery.compile(query, namespaceFilter).iterator().next(), true));
				} else {
				for (AbstractQueryExpressionType compiled : storedQuery.compile(query, namespaceFilter))
					compileQuery(compiled, queries, namespaceFilter, handle);
				}
			} else
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "No stored query with identifier '" + query.getId() + "' is offered by this server.", handle);
		} 
		
		else
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only ad hoc and stored query expressions are supported in a GetFeature request.", handle);
	}

	private FilterType validateSelectionClause(JAXBElement<?> selectionClauseElement, String handle) throws WFSException {
		if (!(selectionClauseElement.getValue() instanceof FilterType))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The element " + selectionClauseElement.getName() + " is not supported as selection clause of queries.", handle); 

		FilterType filter = (FilterType)selectionClauseElement.getValue();

		if (filter.getSpatialOps() != null)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Spatial filter expressions are not supported.", handle);

		if (filter.getTemporalOps() != null)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Temporal filter expressions are not supported.", handle);

		if (filter.getLogicOps() != null)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Logical filter expressions are not supported.", handle);

		if (filter.getComparisonOps() != null)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Arithmetic filter expressions are not supported.", handle);

		if (filter.getExtensionOps() != null)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Filter extensions are not supported.", handle);

		if (filter.getFunction() != null)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Filter functions are not supported.", handle);

		return filter;
	}

	private QueryExpression getQueryExpression(Set<QName> featureTypeNames,
			Set<String> resourceIds,
			boolean isGetFeatureById) {
		QueryExpression queryExpression = new QueryExpression();

		// set exporter filter according to the parsing results
		// TODO: In future, a FE layer should be populated instead
		// of directly using the exporter filter

		// populate feature type filter
		if (featureTypeNames != null && !featureTypeNames.isEmpty()) {
			queryExpression.setFeatureTypeNames(featureTypeNames);
			FeatureClass featureClassFilter = new FeatureClass();
			featureClassFilter.setActive(!featureTypeNames.isEmpty());

			for (QName qName : featureTypeNames) {
				String localPart = qName.getLocalPart();
				if ("Building".equals(localPart))
					featureClassFilter.setBuilding(false);
				else if ("Bridge".equals(localPart))
					featureClassFilter.setBridge(false);
				else if ("Tunnel".equals(localPart))
					featureClassFilter.setTunnel(false);
				else if ("TransportationComplex".equals(localPart))
					featureClassFilter.setTransportation(false);	
				else if ("Road".equals(localPart))
					featureClassFilter.setRoad(false);
				else if ("Track".equals(localPart))
					featureClassFilter.setTrack(false);
				else if ("Square".equals(localPart))
					featureClassFilter.setSquare(false);
				else if ("Railway".equals(localPart))
					featureClassFilter.setRailway(false);
				else if ("CityFurniture".equals(localPart))
					featureClassFilter.setCityFurniture(false);
				else if ("LandUse".equals(localPart))
					featureClassFilter.setLandUse(false);
				else if ("WaterBody".equals(localPart))
					featureClassFilter.setWaterBody(false);
				else if ("PlantCover".equals(localPart))
					featureClassFilter.setPlantCover(false);
				else if ("SolitaryVegetationObject".equals(localPart))
					featureClassFilter.setSolitaryVegetationObject(false);
				else if ("ReliefFeature".equals(localPart))
					featureClassFilter.setReliefFeature(false);
				else if ("GenericCityObject".equals(localPart))
					featureClassFilter.setGenericCityObject(false);
				else if ("CityObjectGroup".equals(localPart))
					featureClassFilter.setCityObjectGroup(false);
			}

			// TODO: this is a hack to create a valid exporter filter
			exporterConfig.getProject().getExporter().getFilter().setMode(FilterMode.COMPLEX);
			exporterConfig.getProject().getExporter().getFilter().getComplexFilter().setFeatureClass(featureClassFilter);
			queryExpression.setFeatureTypeFilter(new FeatureClassFilter(exporterConfig, org.citydb.modules.common.filter.FilterMode.EXPORT));
		}

		// populate resource id filter
		if (resourceIds != null && !resourceIds.isEmpty()) {
			GmlId gmlIdFilter = new GmlId();

			for (String resourceId : resourceIds)
				gmlIdFilter.addGmlId(resourceId);

			exporterConfig.getProject().getExporter().getFilter().setMode(FilterMode.SIMPLE);
			exporterConfig.getProject().getExporter().getFilter().getSimpleFilter().setGmlIdFilter(gmlIdFilter);
			queryExpression.setGmlIdFilter(new GmlIdFilter(exporterConfig, org.citydb.modules.common.filter.FilterMode.EXPORT));
		}

		// is this the GetFeatureById query?
		queryExpression.setGetFeatureById(isGetFeatureById);

		return queryExpression;
	}
	private final class QueryTypeWrapper {
		protected final QueryType queryType;
		protected final boolean isGetFeatureById;
		
		protected QueryTypeWrapper(QueryType queryType, boolean isGetFeatureById) {
			this.queryType = queryType;
			this.isGetFeatureById = isGetFeatureById;
		}
	}

}
