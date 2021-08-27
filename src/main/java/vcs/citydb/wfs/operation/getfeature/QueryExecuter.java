package vcs.citydb.wfs.operation.getfeature;

import net.opengis.ows._1.ExceptionReport;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ResultTypeType;
import net.opengis.wfs._2.TruncatedResponse;
import org.citydb.config.project.global.CacheMode;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.model.CacheTableModel;
import org.citydb.core.operation.exporter.database.content.DBSplittingResult;
import org.citydb.core.operation.exporter.util.InternalConfig;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.query.Query;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.event.global.InterruptEvent;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.citygml.AppearanceModule;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionReportHandler;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.paging.PageRequest;
import vcs.citydb.wfs.paging.PagingCacheManager;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QueryExecuter implements EventHandler {
    private final FeatureWriter writer;
    private final WorkerPool<DBSplittingResult> databaseWorkerPool;
    private final CacheTableManager cacheTableManager;
    private final Object eventChannel;
    private final DatabaseConnectionPool connectionPool;
    private final CityGMLBuilder cityGMLBuilder;
    private final InternalConfig internalConfig;

    private final long countDefault;
    private final boolean computeNumberMatched;
    private final boolean usePaging;
    private final PagingCacheManager pagingCacheManager;
    private final SchemaMapping schemaMapping;
    private final QueryBuilder queryBuilder;
    private final EventDispatcher eventDispatcher;

    private WFSException wfsException;
    private volatile boolean shouldRun = true;

    public QueryExecuter(FeatureWriter writer,
                         WorkerPool<DBSplittingResult> databaseWorkerPool,
                         CacheTableManager cacheTableManager,
                         Object eventChannel,
                         DatabaseConnectionPool connectionPool,
                         CityGMLBuilder cityGMLBuilder,
                         InternalConfig internalConfig,
                         WFSConfig wfsConfig) {
        this.writer = writer;
        this.databaseWorkerPool = databaseWorkerPool;
        this.cacheTableManager = cacheTableManager;
        this.eventChannel = eventChannel;
        this.connectionPool = connectionPool;
        this.cityGMLBuilder = cityGMLBuilder;
        this.internalConfig = internalConfig;

        countDefault = wfsConfig.getConstraints().getCountDefault();
        computeNumberMatched = wfsConfig.getConstraints().isComputeNumberMatched();
        usePaging = wfsConfig.getConstraints().isUseResultPaging();
        pagingCacheManager = ObjectRegistry.getInstance().lookup(PagingCacheManager.class);
        schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
        queryBuilder = new QueryBuilder(connectionPool.getActiveDatabaseAdapter(), schemaMapping);

        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
    }

    public void executeQuery(GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, PageRequest pageRequest, Query dummy, HttpServletRequest request) throws WFSException {
        boolean purgeConnectionPool = false;
        boolean invalidatePageRequest = false;

        // get standard request parameters
        long count = wfsRequest.isSetCount() && wfsRequest.getCount().longValue() < countDefault ? wfsRequest.getCount().longValue() : countDefault;
        long startIndex = wfsRequest.isSetStartIndex() ? wfsRequest.getStartIndex().longValue() : 0;
        ResultTypeType resultType = wfsRequest.getResultType();

        // create paging request
        if (pageRequest == null) {
            pageRequest = usePaging && count != Constants.COUNT_DEFAULT ?
                    pagingCacheManager.create(wfsRequest, queryExpressions) :
                    PageRequest.dummy();
        }

        try (Connection connection = initConnection()) {
            long matchAll = getNumberMatched(queryExpressions, resultType, connection);
            boolean isMultipleQueryRequest = queryExpressions.size() > 1;
            boolean isWriteSingleFeature = !isMultipleQueryRequest
                    && queryExpressions.get(0).isGetFeatureById()
                    && matchAll <= 1;

            if (resultType == ResultTypeType.RESULTS && matchAll > 0) {
                long returnAll = matchAll != Constants.UNKNOWN_NUMBER_MATCHED ?
                        getNumberReturned(queryExpressions, count, startIndex) :
                        getNumberReturned(queryExpressions, count, startIndex, pageRequest.getPageNumber(), connection);

                writer.setWriteSingleFeature(isWriteSingleFeature);
                if (!isWriteSingleFeature) {
                    writer.startFeatureCollection(matchAll, returnAll,
                            pageRequest.getPageNumber() > 0 ? pageRequest.previous(request) : null,
                            returnAll == count && matchAll - startIndex - returnAll > 0 ? pageRequest.next(request) : null);
                }

                for (int i = 0; shouldRun && i < queryExpressions.size(); i++) {
                    QueryExpression queryExpression = queryExpressions.get(i);
                    if (queryExpression.getNumberReturned() > 0) {
                        long sequenceId = writer.setSequentialWriting(queryExpression.isSetSorting());
                        setExporterContext(queryExpression, dummy, isMultipleQueryRequest);

                        if (isMultipleQueryRequest)
                            writer.startFeatureCollection(queryExpression.getNumberMatched(), queryExpression.getNumberReturned());

                        Select select = queryBuilder.buildQuery(queryExpression, count, pageRequest.getPageNumber());
                        try (PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(select, connection);
                             ResultSet rs = stmt.executeQuery()) {
                            long id = -1;
                            while (shouldRun && rs.next()) {
                                id = rs.getLong(MappingConstants.ID);
                                int objectClassId = rs.getInt(MappingConstants.OBJECTCLASS_ID);

                                AbstractObjectType<?> type = schemaMapping.getAbstractObjectType(objectClassId);
                                if (type == null) {
                                    throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED,
                                            "Failed to map the object class id '" + objectClassId + "' to an object type (ID: " + id + ").");
                                }

                                // put feature on worker queue
                                DBSplittingResult splitter = new DBSplittingResult(id, type, sequenceId++);
                                databaseWorkerPool.addWork(splitter);
                            }

                            if (usePaging && queryExpression.supportsPagingByStartId())
                                queryExpression.setStartId(id);
                        }

                        // join database workers
                        databaseWorkerPool.join();

                        if (isMultipleQueryRequest)
                            writer.endFeatureCollection();
                    } else if (isMultipleQueryRequest) {
                        writer.startFeatureCollection(queryExpression.getNumberMatched(), 0);
                        writer.endFeatureCollection();
                    }
                }

                if (!isWriteSingleFeature) {
                    // query and write global appearances
                    if (internalConfig.isExportGlobalAppearances() && returnAll > 0)
                        processGlobalAppearances();

                    // write additional objects
                    writer.writeAdditionalObjects();
                    writer.endAdditionalObjects();

                    // write truncated response if a worker pool has thrown an error
                    if (wfsException != null) {
                        writer.writeTruncatedResponse(getTruncatedResponse(wfsException, request));
                        invalidatePageRequest = true;
                    }

                    writer.endFeatureCollection();
                }
            } else if (resultType == ResultTypeType.HITS) {
                writer.startFeatureCollection(matchAll, 0, null, matchAll > 0 ? pageRequest.first(request) : null);
                writer.endFeatureCollection();
                invalidatePageRequest = matchAll == 0 && pageRequest.getPageNumber() == 0;
            } else if (isWriteSingleFeature) {
                // the getFeatureById query requires a special not found exception message
                String identifier = queryExpressions.get(0).getFeatureIdentifier();
                throw new WFSException(WFSExceptionCode.NOT_FOUND, "There is no feature with identifier '" + identifier + "'.", identifier);
            } else {
                writer.startFeatureCollection(matchAll, 0, null, null);

                if (isMultipleQueryRequest) {
                    for (int i = 0; i < queryExpressions.size(); i++) {
                        writer.startFeatureCollection(0, 0);
                        writer.endFeatureCollection();
                    }
                }

                writer.endFeatureCollection();
                invalidatePageRequest = matchAll == 0 && pageRequest.getPageNumber() == 0;
            }

            // update paging cache
            if (usePaging && !invalidatePageRequest) {
                try {
                    pagingCacheManager.update(pageRequest);
                } catch (IOException e) {
                    invalidatePageRequest = true;
                    throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to update the paging cache.", e);
                }
            }
        } catch (SQLException e) {
            purgeConnectionPool = true;
            invalidatePageRequest = true;
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal SQL error occurred whilst querying the database.", e);
        } catch (FeatureWriteException | InterruptedException e) {
            invalidatePageRequest = true;
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal error occurred whilst writing the response document.", e);
        } finally {
            // purge connection pool to remove possibly defect connections
            if (purgeConnectionPool)
                connectionPool.purge();

            // invalidate paging cache
            if (usePaging && invalidatePageRequest)
                pagingCacheManager.remove(pageRequest);

            eventDispatcher.removeEventHandler(this);
        }
    }

    private void processGlobalAppearances() throws FeatureWriteException, SQLException, InterruptedException {
        CacheTable globalAppTempTable = cacheTableManager.getCacheTable(CacheTableModel.GLOBAL_APPEARANCE);
        globalAppTempTable.createIndexes();

        Select select = queryBuilder.buildGlobalAppearanceQuery(globalAppTempTable);

        try (PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(select, globalAppTempTable.getConnection());
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                FeatureType appearanceType = schemaMapping.getFeatureType("Appearance", AppearanceModule.v2_0_0.getNamespaceURI());
                writer.startAdditionalObjects();

                do {
                    long appearanceId = rs.getLong(1);

                    // put appearance on worker queue
                    DBSplittingResult splitter = new DBSplittingResult(appearanceId, appearanceType);
                    databaseWorkerPool.addWork(splitter);
                } while (rs.next() && shouldRun);
            }
        }

        // join database workers
        databaseWorkerPool.join();
    }

    private long getNumberMatched(List<QueryExpression> queryExpressions, ResultTypeType resultType, Connection connection) throws WFSException, SQLException {
        long matchAll = 0;
        boolean isUnknown = false;

        for (QueryExpression queryExpression : queryExpressions) {
            if (!queryExpression.isSetNumberMatched()) {
                if (computeNumberMatched || resultType == ResultTypeType.HITS) {
                    Select query = queryBuilder.buildNumberMatchedQuery(queryExpression);
                    try (PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(query, connection);
                         ResultSet rs = stmt.executeQuery()) {
                        queryExpression.setNumberMatched(rs.next() ? rs.getLong(1) : 0);
                    }
                } else
                    queryExpression.setNumberMatched(Constants.UNKNOWN_NUMBER_MATCHED);
            }

            if (queryExpression.getNumberMatched() == Constants.UNKNOWN_NUMBER_MATCHED)
                isUnknown = true;
            else if (!isUnknown)
                matchAll += queryExpression.getNumberMatched();
        }

        return !isUnknown ? matchAll : Constants.UNKNOWN_NUMBER_MATCHED;
    }

    private long getNumberReturned(List<QueryExpression> queryExpressions, long count, long startIndex) {
        long returnAll = 0;
        for (QueryExpression queryExpression : queryExpressions) {
            if (count == 0) {
                queryExpression.setNumberReturned(0);
            } else if (queryExpression.getNumberMatched() <= startIndex) {
                startIndex -= queryExpression.getNumberMatched();
                queryExpression.setNumberReturned(0);
            } else {
                long numberReturned = queryExpression.getNumberMatched() - startIndex;
                if (count != Constants.COUNT_DEFAULT) {
                    numberReturned = Math.min(numberReturned, count);
                    count = Math.max(count - numberReturned, 0);
                }

                queryExpression.setNumberReturned(numberReturned);
                queryExpression.setStartIndex(startIndex);

                startIndex = 0;
                returnAll += numberReturned;
            }
        }

        return returnAll;
    }

    private long getNumberReturned(List<QueryExpression> queryExpressions, long count, long startIndex, long pageNumber, Connection connection) throws WFSException, SQLException {
        long returnAll = 0;
        for (int i = 0; i < queryExpressions.size(); i++) {
            QueryExpression queryExpression = queryExpressions.get(i);

            if (count == 0) {
                queryExpression.setNumberReturned(0);
            } else {
                Select query = queryBuilder.buildNumberReturnedQuery(queryExpression, count, startIndex, pageNumber);
                try (PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(query, connection);
                     ResultSet rs = stmt.executeQuery()) {
                    queryExpression.setNumberReturned(rs.next() ? rs.getLong(1) : 0);
                }

                if (queryExpression.getNumberReturned() == 0 && startIndex > 0 && i < queryExpressions.size() - 1
                        && !queryExpressions.get(i + 1).isSetStartId()
                        && (!queryExpression.isSetNumberMatched()
                        || queryExpression.getNumberMatched() == Constants.UNKNOWN_NUMBER_MATCHED)) {
                    query = queryBuilder.buildNumberMatchedQuery(queryExpression);
                    try (PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(query, connection);
                         ResultSet rs = stmt.executeQuery()) {
                        queryExpression.setNumberMatched(rs.next() ? rs.getLong(1) : 0);
                        startIndex = Math.max(startIndex - queryExpression.getNumberMatched(), 0);
                    }
                } else {
                    if (count != Constants.COUNT_DEFAULT)
                        count = Math.max(count - queryExpression.getNumberReturned(), 0);

                    queryExpression.setStartIndex(startIndex);

                    startIndex = 0;
                    returnAll += queryExpression.getNumberReturned();
                }
            }
        }

        return returnAll;
    }

    private void setExporterContext(QueryExpression queryExpression, Query dummy, boolean isMultipleQueryRequest) {
        // enable xlink references in multiple query responses
        internalConfig.setRegisterGmlIdInCache(isMultipleQueryRequest);

        // set flag for coordinate transformation
        internalConfig.setTransformCoordinates(queryExpression.getTargetSrs().getSrid() != connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());

        // update filter configuration
        dummy.copyFrom(queryExpression);
    }

    private TruncatedResponse getTruncatedResponse(WFSException wfsException, HttpServletRequest request) {
        WFSExceptionReportHandler reportHandler = new WFSExceptionReportHandler(cityGMLBuilder);
        ExceptionReport exceptionReport = reportHandler.getExceptionReport(wfsException, KVPConstants.GET_FEATURE, request, true);

        TruncatedResponse truncatedResponse = new TruncatedResponse();
        truncatedResponse.setExceptionReport(exceptionReport);

        return truncatedResponse;
    }

    private Connection initConnection() throws SQLException {
        Connection connection = connectionPool.getConnection();
        connection.setAutoCommit(false);

        // create temporary table for global appearances if needed
        if (internalConfig.isExportGlobalAppearances())
            cacheTableManager.createCacheTable(CacheTableModel.GLOBAL_APPEARANCE, CacheMode.DATABASE);

        return connection;
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        if (event.getChannel() == eventChannel) {
            wfsException = new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, ((InterruptEvent) event).getLogMessage(), ((InterruptEvent) event).getCause());
            shouldRun = false;
        }
    }
}
