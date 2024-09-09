package vcs.citydb.wfs.operation.getpropertyvalue;

import net.opengis.wfs._2.GetPropertyValueType;
import net.opengis.wfs._2.QueryType;
import net.opengis.wfs._2.ResolveValueType;
import org.citydb.config.Config;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.AbstractProperty;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.Joinable;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.path.AbstractNode;
import org.citydb.core.database.schema.path.FeatureTypeNode;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.database.schema.util.SimpleXPathParser;
import org.citydb.core.database.schema.util.XPathException;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.config.LodFilterBuilder;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.projection.Projection;
import org.citydb.core.query.filter.selection.Predicate;
import org.citydb.core.query.filter.selection.SelectionFilter;
import org.citydb.core.query.filter.selection.expression.ValueReference;
import org.citydb.core.query.filter.selection.operator.comparison.ComparisonFactory;
import org.citydb.core.query.filter.selection.operator.logical.LogicalOperationFactory;
import org.citydb.core.query.filter.sorting.SortProperty;
import org.citydb.core.query.filter.sorting.Sorting;
import org.citydb.core.query.filter.type.FeatureTypeFilter;
import org.citydb.core.query.geometry.DatabaseSrsParser;
import org.citydb.core.query.geometry.SrsParseException;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
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
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GetPropertyValueHandler {
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
    private final SimpleXPathParser xpathParser;

    public GetPropertyValueHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig, Config config) {
        this.wfsConfig = wfsConfig;

        databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
        controller = new ExportController(cityGMLBuilder, wfsConfig, config);
        baseRequestHandler = new BaseRequestHandler(wfsConfig);
        featureTypeHandler = new FeatureTypeHandler();
        srsNameParser = new DatabaseSrsParser(databaseAdapter, config);
        schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
        xpathParser = new SimpleXPathParser(schemaMapping);
        storedQueryManager = ObjectRegistry.getInstance().lookup(StoredQueryManager.class);

        JAXBUnmarshaller unmarshaller = cityGMLBuilder.createJAXBUnmarshaller(ObjectRegistry.getInstance().lookup(SchemaHandler.class));
        filterHandler = new FilterHandler(unmarshaller, xpathParser, srsNameParser, wfsConfig);
        sortingHandler = new SortingHandler(xpathParser);
    }

    public void doOperation(GetPropertyValueType wfsRequest,
                            NamespaceFilter namespaceFilter,
                            HttpServletRequest request,
                            HttpServletResponse response) throws WFSException {

        final String operationHandle = wfsRequest.getHandle();

        // check whether GetPropertyValue operation is advertised
        if (!wfsConfig.getOperations().getGetPropertyValue().isEnabled())
            throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "The GetPropertyValue operation is not advertised.", operationHandle);

        log.info(LoggerUtil.getLogMessage(request, "Accepting GetPropertyValue request."));

        // check base service parameters
        baseRequestHandler.validate(wfsRequest);

        // check output format
        if (wfsRequest.isSetOutputFormat() && !wfsConfig.getOperations().getGetPropertyValue().supportsOutputFormat(wfsRequest.getOutputFormat())) {
            WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE);
            message.addExceptionText("The output format of a GetPropertyValue request must match one of the following formats:");
            message.addExceptionTexts(wfsConfig.getOperations().getGetPropertyValue().getOutputFormatsAsString());
            message.setLocator(KVPConstants.OUTPUT_FORMAT);

            throw new WFSException(message);
        }

        if (wfsRequest.getResolve() != ResolveValueType.NONE && wfsRequest.getResolve() != ResolveValueType.LOCAL)
            throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Resolving of remote resources is not supported.", operationHandle);

        if (wfsRequest.getResolve() == ResolveValueType.LOCAL && !wfsRequest.getResolveDepth().equals("*"))
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Only the value '*' is supported as resolve depth.", KVPConstants.RESOLVE_DEPTH);

        // check for value reference to be present
        if (!wfsRequest.isSetValueReference())
            throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "No value reference provided.", KVPConstants.VALUE_REFERENCE);

        // check for queries to be present
        if (!wfsRequest.isSetAbstractQueryExpression())
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "No query provided.", operationHandle);

        // get query
        List<QueryType> queries = new ArrayList<>();
        storedQueryManager.compileQuery(wfsRequest.getAbstractQueryExpression().getValue(), queries, namespaceFilter, operationHandle);
        if (queries.size() > 1)
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A GetPropertyValue operation may only contain one query.", operationHandle);

        QueryType query = queries.get(0);
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

        // set CityGML version
        queryExpression.setTargetVersion(featureTypeHandler.getCityGMLVersion());

        // get selection clause of query
        FeatureType featureType = schemaMapping.getCommonSuperType(featureTypes);
        if (query.isSetAbstractSelectionClause())
            queryExpression.setSelection(filterHandler.getSelection(query.getAbstractSelectionClause(), featureType, namespaceFilter, queryHandle));

        // only process non-terminated objects if required
        if (wfsConfig.getConstraints().isCurrentVersionOnly())
            filterHandler.addNotTerminatedFilter(queryExpression, queryHandle);

        // get sorting clause
        if (query.isSetAbstractSortingClause()) {
            Sorting sorting = sortingHandler.getSorting(query.getAbstractSortingClause(), featureType, namespaceFilter, queryHandle);
            for (SortProperty sortProperty : sorting.getSortProperties()) {
                SchemaPath schemaPath = sortProperty.getValueReference().getSchemaPath();
                AbstractNode<?> child = schemaPath.getFirstNode().child();
                do {
                    if (child.getPathElement() instanceof Joinable && ((Joinable) child.getPathElement()).isSetJoin()) {
                        WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE);
                        message.addExceptionText("Sorting cannot be performed based on the provided sort property.");
                        message.addExceptionText("It involves joins that my lead to inconsistent sorting results for the GetPropertyValue operation.");
                        message.setLocator(queryHandle);
                        throw new WFSException(message);
                    }
                } while ((child = child.child()) != null);
            }

            queryExpression.setSorting(sorting);
        } else if (wfsConfig.getConstraints().isUseDefaultSorting())
            sortingHandler.setDefaultSorting(queryExpression, featureType, queryHandle);

        // lod filter constraint
        if (wfsConfig.getConstraints().getLodFilter().isEnabled()) {
            try {
                queryExpression.setLodFilter(new LodFilterBuilder().buildLodFilter(wfsConfig.getConstraints().getLodFilter()));
            } catch (QueryBuildException e) {
                throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build the LoD filter.", operationHandle, e);
            }
        }

        // map the value reference
        try {
            SchemaPath schemaPath = xpathParser.parse(wfsRequest.getValueReference(), schemaMapping.getCommonSuperType(featureTypes), namespaceFilter);

            switch (schemaPath.getLastNode().getPathElement().getElementType()) {
                case FEATURE_TYPE:
                case OBJECT_TYPE:
                case COMPLEX_TYPE:
                    throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The valueReference must point to a feature property.", KVPConstants.VALUE_REFERENCE);
                default:
                    queryExpression.setSchemaPath(schemaPath);
            }

            // set projection clause based on value reference
            queryExpression.setProjection(getProjection(schemaPath));

            // add a not null selection filter for the value reference
            Predicate isNotNull = LogicalOperationFactory.NOT(ComparisonFactory.isNull(new ValueReference(schemaPath)));
            if (!queryExpression.isSetSelection())
                queryExpression.setSelection(new SelectionFilter(isNotNull));
            else {
                SelectionFilter selection = queryExpression.getSelection();
                Predicate predicate = selection.getPredicate();
                selection.setPredicate(LogicalOperationFactory.AND(predicate, isNotNull));
            }

            // remove namespace prefixes from XML attributes
            queryExpression.setValueReference(schemaPath.toXPath(false, true));
        } catch (XPathException e) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid XPath expression used in valueReference.", KVPConstants.VALUE_REFERENCE, e);
        } catch (InvalidSchemaPathException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to map XPath expression used in valueReference to the CityGML schema.", queryHandle, e);
        } catch (FilterException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to add a not null filter for the target property.", queryExpression.getHandle(), e);
        }

        controller.doExport(wfsRequest, queryExpression, namespaceFilter, request, response);
        log.info(LoggerUtil.getLogMessage(request, "GetPropertyValue operation successfully finished."));
    }

    private Projection getProjection(SchemaPath schemaPath) throws WFSException {
        Projection projection = new Projection();
        AbstractNode<?> node = schemaPath.getFirstNode();

        while (node != null) {
            if (node instanceof FeatureTypeNode) {
                FeatureType featureType = ((FeatureTypeNode) node).getPathElement();
                node = node.child();
                if (node != null && node.getPathElement() instanceof AbstractProperty) {
                    AbstractProperty property = (AbstractProperty) node.getPathElement();
                    projection.getProjectionFilter(featureType).addProperty(property);
                }
            } else
                node = node.child();
        }

        return projection;
    }

}
