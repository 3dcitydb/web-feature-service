package vcs.citydb.wfs.operation.getfeature;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.QueryType;
import net.opengis.wfs._2.ResolveValueType;
import net.opengis.wfs._2.StoredQueryType;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.log.Logger;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.config.LodFilterBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.operation.filter.FeatureTypeHandler;
import vcs.citydb.wfs.operation.filter.FilterHandler;
import vcs.citydb.wfs.operation.storedquery.StoredQueryManager;
import vcs.citydb.wfs.util.LoggerUtil;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GetFeatureHandler {
	private final Logger log = Logger.getInstance();
	private final WFSConfig wfsConfig;

	private final AbstractDatabaseAdapter databaseAdapter;
	private final ExportController controller;
	private final BaseRequestHandler baseRequestHandler;
	private final FeatureTypeHandler featureTypeHandler;
	private final FilterHandler filterHandler;
	private final StoredQueryManager storedQueryManager;

	public GetFeatureHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig, Config config) {
		this.wfsConfig = wfsConfig;

		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		controller = new ExportController(cityGMLBuilder, wfsConfig, config);
		baseRequestHandler = new BaseRequestHandler(wfsConfig);
		featureTypeHandler = new FeatureTypeHandler();
		storedQueryManager = (StoredQueryManager)ObjectRegistry.getInstance().lookup(StoredQueryManager.class.getName());
		filterHandler = new FilterHandler();
	}

	public void doOperation(GetFeatureType wfsRequest,
			NamespaceFilter namespaceFilter,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		log.info(LoggerUtil.getLogMessage(request, "Accepting GetFeature request."));
		final List<QueryExpression> queryExpressions = new ArrayList<>();
		final String operationHandle = wfsRequest.getHandle();
		CityGMLVersion version = null;
		LodFilter lodFilter = null;

		// check base service parameters
		baseRequestHandler.validate(wfsRequest);

		// check output format
		if (wfsRequest.isSetOutputFormat() && !wfsConfig.getOperations().getGetFeature().supportsOutputFormat(wfsRequest.getOutputFormat())) {
			WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.OPTION_NOT_SUPPORTED);
			message.addExceptionText("The output format of a GetFeature request must match one of the following formats:");
			message.addExceptionTexts(wfsConfig.getOperations().getGetFeature().getOutputFormatsAsString());
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

		// compile queries to be executed from ad-hoc and stored queries
		List<QueryType> queries = new ArrayList<>();
		for (JAXBElement<? extends AbstractQueryExpressionType> queryElem : wfsRequest.getAbstractQueryExpression()) {
			if (!(queryElem.getValue() instanceof StoredQueryType))
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only stored query expressions are supported in a GetFeature request.", operationHandle);

			storedQueryManager.compileQuery(queryElem.getValue(), queries, namespaceFilter, operationHandle);
		}

		// lod filter constraint
		if (wfsConfig.getConstraints().isSetLodFilter()) {
			try {
				lodFilter = new LodFilterBuilder().buildLodFilter(wfsConfig.getConstraints().getLodFilter());
			} catch (QueryBuildException e) {
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build the LoD filter.", operationHandle, e);
			}
		}

		// iterate through queries		
		for (QueryType query : queries) {
			String queryHandle = query.isSetHandle() ? query.getHandle() : operationHandle;

			// TODO: aliases are not supported
			if (!query.getAliases().isEmpty())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Aliases for feature type names are not supported.", queryHandle);

			// feature versions are not supported
			if (query.isSetFeatureVersion())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Feature versioning is not supported.", queryHandle);

			// TODO: add support for sorting
			if (query.isSetAbstractSortingClause())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Sorting is not supported.", queryHandle);
			
			// join queries are not supported
			if (query.getTypeNames().size() > 1)
				throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "Join queries are not supported.", queryHandle);

			// create and populate query expression
			QueryExpression queryExpression = new QueryExpression();
			queryExpression.setLodFilter(lodFilter);
			queryExpression.setIsGetFeatureById(query.isIsGetFeatureById());
			queryExpression.setHandle(queryHandle);
			
			// TODO: add support for coordinate transformation
			if (query.isSetSrsName())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Coordinate transformation is not supported.", queryHandle);
			else
				queryExpression.setTargetSrs(databaseAdapter.getConnectionMetaData().getReferenceSystem());
			
			// create filter from feature type names
			Set<FeatureType> featureTypes = featureTypeHandler.getFeatureTypes(query.getTypeNames(), namespaceFilter, false, queryHandle);
			try {
				queryExpression.setFeatureTypeFilter(new FeatureTypeFilter(featureTypes));
			} catch (FilterException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to build filter expression.", queryHandle, e);
			}

			// check for unique CityGML version over multiple queries
			queryExpression.setTargetVersion(featureTypeHandler.getCityGMLVersion());
			if (version == null)
				version = queryExpression.getTargetVersion();
			else if (version != queryExpression.getTargetVersion())
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Mixing feature types from different CityGML versions is not supported.", queryHandle);

			// TODO: add support for projection attributes
			if (query.isSetAbstractProjectionClause())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Property projection is not supported.", queryHandle);

			// get selection clause of query
			if (query.isSetAbstractSelectionClause())
				queryExpression.setSelection(filterHandler.getSelection(query.getAbstractSelectionClause(), queryHandle));

			queryExpressions.add(queryExpression);
		}

		if (queryExpressions.size() == 0)
			throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "No valid query expressions provided.", operationHandle);

		controller.doExport(wfsRequest, queryExpressions, request, response);
		log.info(LoggerUtil.getLogMessage(request, "GetFeature operation successfully finished."));
	}

}
