package vcs.citydb.wfs.operation.getfeature;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.PropertyName;
import net.opengis.wfs._2.QueryType;
import net.opengis.wfs._2.ResolveValueType;
import org.citydb.config.Config;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.AbstractProperty;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.util.SimpleXPathParser;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.config.LodFilterBuilder;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.projection.Projection;
import org.citydb.core.query.filter.type.FeatureTypeFilter;
import org.citydb.core.query.geometry.DatabaseSrsParser;
import org.citydb.core.query.geometry.SrsParseException;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.xml.schema.SchemaHandler;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.operation.filter.FeatureTypeHandler;
import vcs.citydb.wfs.operation.filter.FilterHandler;
import vcs.citydb.wfs.operation.filter.SortingHandler;
import vcs.citydb.wfs.operation.storedquery.StoredQueryManager;
import vcs.citydb.wfs.util.LoggerUtil;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GetFeatureHandler {
	private final Logger log = Logger.getInstance();
	private final WFSConfig wfsConfig;

	private final AbstractDatabaseAdapter databaseAdapter;
	private final ExportController controller;
	private final BaseRequestHandler baseRequestHandler;
	private final FeatureTypeHandler featureTypeHandler;
	private final DatabaseSrsParser srsNameParser;
	private final FilterHandler filterHandler;
	private final SortingHandler sortingHandler;
	private final SchemaMapping schemaMapping;
	private final StoredQueryManager storedQueryManager;

	public GetFeatureHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig, Config config) {
		this.wfsConfig = wfsConfig;

		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		controller = new ExportController(cityGMLBuilder, wfsConfig, config);
		baseRequestHandler = new BaseRequestHandler(wfsConfig);
		featureTypeHandler = new FeatureTypeHandler();
		srsNameParser = new DatabaseSrsParser(databaseAdapter, config);
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		storedQueryManager = ObjectRegistry.getInstance().lookup(StoredQueryManager.class);

		JAXBUnmarshaller unmarshaller = cityGMLBuilder.createJAXBUnmarshaller(ObjectRegistry.getInstance().lookup(SchemaHandler.class));
		SimpleXPathParser xpathParser = new SimpleXPathParser(schemaMapping);
		filterHandler = new FilterHandler(unmarshaller, xpathParser, srsNameParser, wfsConfig);
		sortingHandler = new SortingHandler(xpathParser);
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
			WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE);
			message.addExceptionText("The output format of a GetFeature request must match one of the following formats:");
			message.addExceptionTexts(wfsConfig.getOperations().getGetFeature().getOutputFormatsAsString());
			message.setLocator(KVPConstants.OUTPUT_FORMAT);

			throw new WFSException(message);
		}

		if (wfsRequest.getResolve() != ResolveValueType.NONE && wfsRequest.getResolve() != ResolveValueType.LOCAL)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Resolving of remote resources is not supported.", operationHandle);

		if (wfsRequest.getResolve() == ResolveValueType.LOCAL && !wfsRequest.getResolveDepth().equals("*"))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Only the value '*' is supported as resolve depth.", KVPConstants.RESOLVE_DEPTH);

		// check for queries to be present
		if (wfsRequest.getAbstractQueryExpression().isEmpty())
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "No query provided.", operationHandle);

		// compile queries to be executed from ad-hoc and stored queries
		List<QueryType> queries = new ArrayList<>();
		for (JAXBElement<? extends AbstractQueryExpressionType> queryElem : wfsRequest.getAbstractQueryExpression())
			storedQueryManager.compileQuery(queryElem.getValue(), queries, namespaceFilter, operationHandle);
		
		// lod filter constraint
		if (wfsConfig.getConstraints().getLodFilter().isEnabled()) {
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

			// join queries are not supported
			if (query.getTypeNames().size() > 1)
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Join queries are not supported.", queryHandle);

			// create and populate query expression
			QueryExpression queryExpression = new QueryExpression();
			queryExpression.setLodFilter(lodFilter);
			queryExpression.setFeatureIdentifier(query.getFeatureIdentifier());
			queryExpression.setHandle(queryHandle);
			
			// get SRS for coordinate transformation
			if (query.isSetSrsName()) {
				try {
					queryExpression.setTargetSrs(srsNameParser.getDatabaseSrs(query.getSrsName()));
				} catch (SrsParseException e) {
					throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Failed to parse srsName attribute.", KVPConstants.SRS_NAME, e);
				}
			} else
				queryExpression.setTargetSrs(databaseAdapter.getConnectionMetaData().getReferenceSystem());
			
			// create filter from feature type names
			Set<FeatureType> featureTypes = featureTypeHandler.getFeatureTypes(query.getTypeNames(), namespaceFilter, false, KVPConstants.TYPE_NAMES, queryHandle);
			try {
				queryExpression.setFeatureTypeFilter(new FeatureTypeFilter(featureTypes));
			} catch (FilterException e) {
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build filter expression.", queryHandle, e);
			}

			// check for unique CityGML version over multiple queries
			queryExpression.setTargetVersion(featureTypeHandler.getCityGMLVersion());
			if (version == null)
				version = queryExpression.getTargetVersion();
			else if (version != queryExpression.getTargetVersion())
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Mixing feature types from different CityGML versions is not supported.", queryHandle);

			// get requested projection attributes
			if (query.isSetAbstractProjectionClause())
				queryExpression.setProjection(getProjection(query.getAbstractProjectionClause(), featureTypes, queryHandle));

			// get selection clause of query
			FeatureType featureType = schemaMapping.getCommonSuperType(featureTypes);
			if (query.isSetAbstractSelectionClause())
				queryExpression.setSelection(filterHandler.getSelection(query.getAbstractSelectionClause(), featureType, namespaceFilter, queryHandle));

			// only process non-terminated objects if required
			if (wfsConfig.getConstraints().isCurrentVersionOnly())
				filterHandler.addNotTerminatedFilter(queryExpression, queryHandle);

			// get sorting clause
			if (query.isSetAbstractSortingClause())
				queryExpression.setSorting(sortingHandler.getSorting(query.getAbstractSortingClause(), featureType, namespaceFilter, queryHandle));
			else if (wfsConfig.getConstraints().isUseDefaultSorting())
				sortingHandler.setDefaultSorting(queryExpression, featureType, queryHandle);

			queryExpressions.add(queryExpression);
		}

		if (queryExpressions.size() == 0)
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "No valid query expressions provided.", operationHandle);

		controller.doExport(wfsRequest, queryExpressions, request, response);
		log.info(LoggerUtil.getLogMessage(request, "GetFeature operation successfully finished."));
	}

	private Projection getProjection(Collection<JAXBElement<?>> propertyNameElements, Set<FeatureType> featureTypes, String handle) throws WFSException {
		Projection projection = new Projection();

		for (JAXBElement<?> propertyNameElement : propertyNameElements) {
			if (!(propertyNameElement.getValue() instanceof PropertyName))
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The element " + propertyNameElement.getName() + " is not allowed within the projection clause of queries.", handle);

			PropertyName propertyName = (PropertyName)propertyNameElement.getValue();

			// TODO: add support for local resource resolving
			if (propertyName.getResolve() != ResolveValueType.NONE)
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Resource resolving on property names is not supported.", handle);

			QName name = ((PropertyName)propertyNameElement.getValue()).getValue();
			if (name == null)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse qualified property name '" + propertyNameElement.getName() + "' in projection clause of the query.", handle);

			// assign property to appropriate feature types
			boolean found = false;
			for (FeatureType featureType : featureTypes) {
				AbstractProperty property = featureType.getProperty(name.getLocalPart(), name.getNamespaceURI(), true);
				if (property != null) {
					projection.getProjectionFilter(featureType).addProperty(property);
					found = true;
				}
			}

			if (!found)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The property " + name.toString() + " is not available for the requested feature "
						+ (featureTypes.size() == 1 ? "type" : "types") + ".", handle);
		}

		return projection;
	}

}
