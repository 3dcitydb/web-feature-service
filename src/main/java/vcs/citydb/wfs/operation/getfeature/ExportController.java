package vcs.citydb.wfs.operation.getfeature;

import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ResultTypeType;
import org.citydb.config.Config;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.common.cache.IdCacheType;
import org.citydb.core.operation.common.xlink.DBXlink;
import org.citydb.core.operation.exporter.cache.GeometryGmlIdCache;
import org.citydb.core.operation.exporter.cache.ObjectGmlIdCache;
import org.citydb.core.operation.exporter.concurrent.DBExportWorkerFactory;
import org.citydb.core.operation.exporter.database.content.DBSplittingResult;
import org.citydb.core.operation.exporter.util.InternalConfig;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.query.Query;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.util.concurrent.SingleWorkerPool;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.EventDispatcher;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.operation.GetFeatureOutputFormat;
import vcs.citydb.wfs.config.operation.OutputFormat;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.operation.getfeature.citygml.CityGMLWriterBuilder;
import vcs.citydb.wfs.operation.getfeature.cityjson.CityJSONWriterBuilder;
import vcs.citydb.wfs.paging.GetFeatureRequest;
import vcs.citydb.wfs.paging.PageRequest;
import vcs.citydb.wfs.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class ExportController {
    private final CityGMLBuilder cityGMLBuilder;
    private final WFSConfig wfsConfig;
    private final Config config;
    private final DatabaseConnectionPool connectionPool;
    private final WorkerPool<CacheCleanerWork> cacheCleanerPool;
    private final EventDispatcher eventDispatcher;

    private final Object eventChannel = new Object();

    @SuppressWarnings("unchecked")
    public ExportController(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig, Config config) {
        this.cityGMLBuilder = cityGMLBuilder;
        this.wfsConfig = wfsConfig;
        this.config = config;

        connectionPool = DatabaseConnectionPool.getInstance();
        cacheCleanerPool = (WorkerPool<CacheCleanerWork>) ObjectRegistry.getInstance().lookup(CacheCleanerWorker.class.getName());
        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
    }

    public void doExport(GetFeatureRequest pageRequest, HttpServletRequest request, HttpServletResponse response) throws WFSException {
        doExport(pageRequest.getWFSRequest(), pageRequest.getQueryExpressions(), pageRequest, request, response);
    }

    public void doExport(GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, HttpServletRequest request, HttpServletResponse response) throws WFSException {
        doExport(wfsRequest, queryExpressions, null, request, response);
    }

    private void doExport(GetFeatureType wfsRequest,
                          List<QueryExpression> queryExpressions,
                          PageRequest pageRequest,
                          HttpServletRequest request,
                          HttpServletResponse response) throws WFSException {
        InternalConfig internalConfig = new InternalConfig();

        // define queue size for worker pools
        int queueSize = config.getExportConfig().getResources().getThreadPool().getMaxThreads() * 2;

        OutputFormat outputFormat = wfsConfig.getOperations().getGetFeature().getOutputFormat(
                wfsRequest.isSetOutputFormat() ?
                        wfsRequest.getOutputFormat() :
                        GetFeatureOutputFormat.GML3_1.value());

        // check if and how to handle global appearances
        try {
            if (wfsConfig.getConstraints().isExportAppearance()
                    && connectionPool.getActiveDatabaseAdapter().getUtil().containsGlobalAppearances()) {
                internalConfig.setGlobalAppearanceMode(
                        GetFeatureOutputFormat.CITY_JSON.value().equals(wfsRequest.getOutputFormat()) ||
                                "true".equals(outputFormat.getOption(CityGMLWriterBuilder.CONVERT_GLOBAL_APPEARANCES)) ?
                                InternalConfig.GlobalAppearanceMode.CONVERT :
                                InternalConfig.GlobalAppearanceMode.EXPORT);
            }
        } catch (SQLException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Database error while checking for global appearances.", e);
        }

        if (wfsConfig.getConstraints().isExportAppearance()) {
            String serviceURL;
            if (wfsConfig.getServer().isSetTextureServiceURL())
                serviceURL = wfsConfig.getServer().getTextureServiceURL();
            else if (wfsConfig.getServer().isSetExternalServiceURL())
                serviceURL = wfsConfig.getServer().getExternalServiceURL();
            else
                serviceURL = ServerUtil.getServiceURL(request);

            internalConfig.setExportTextureURI(serviceURL + Constants.TEXTURE_SERVICE_PATH);
        }

        IdCacheManager idCacheManager = null;
        CacheTableManager cacheTableManager = null;
        SingleWorkerPool<DBXlink> xlinkPool = null;
        WorkerPool<DBSplittingResult> databaseWorkerPool = null;
        FeatureWriter writer = null;

        try {
            // create instance of cache table manager
            try {
                cacheTableManager = new CacheTableManager(config.getGlobalConfig().getCache());
            } catch (SQLException | IOException e) {
                throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to initialize internal cache manager.", e);
            }

            // create instance of gml:id lookup server manager...
            idCacheManager = new IdCacheManager();

            // ...and start servers
            try {
                idCacheManager.initCache(
                        IdCacheType.GEOMETRY,
                        new GeometryGmlIdCache(cacheTableManager,
                                config.getExportConfig().getResources().getIdCache().getGeometry().getPartitions(),
                                config.getDatabaseConfig().getImportBatching().getGmlIdCacheBatchSize()),
                        config.getExportConfig().getResources().getIdCache().getGeometry().getCacheSize(),
                        config.getExportConfig().getResources().getIdCache().getGeometry().getPageFactor(),
                        config.getExportConfig().getResources().getThreadPool().getMaxThreads());

                idCacheManager.initCache(
                        IdCacheType.OBJECT,
                        new ObjectGmlIdCache(cacheTableManager,
                                config.getExportConfig().getResources().getIdCache().getFeature().getPartitions(),
                                config.getDatabaseConfig().getImportBatching().getGmlIdCacheBatchSize()),
                        config.getExportConfig().getResources().getIdCache().getFeature().getCacheSize(),
                        config.getExportConfig().getResources().getIdCache().getFeature().getPageFactor(),
                        config.getExportConfig().getResources().getThreadPool().getMaxThreads());
            } catch (SQLException e) {
                throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to initialize internal gml:id caches.", e);
            }

            // create worker pools
            // TODO: currently XLinks to texture images and library objects are not resolved
            xlinkPool = new SingleWorkerPool<>(
                    "xlink_exporter_pool",
                    NullWorker::new,
                    1,
                    false);

            // the dummy query is required for the 3dcitydb exporter and will be updated with
            // the query context of the individual query expressions
            Query dummy = new Query();
            SchemaMapping schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();

            // create response writer
            try {
                GetFeatureResponseBuilder builder = getFeatureWriterBuilder(outputFormat, wfsRequest, queryExpressions, idCacheManager, internalConfig);

                response.setContentType(builder.getMimeType());
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                writer = builder.buildFeatureWriter(response.getWriter());
            } catch (FeatureWriteException | IOException e) {
                throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to initialize the response writer.", e);
            }

            databaseWorkerPool = new WorkerPool<>(
                    "db_exporter_pool",
                    config.getExportConfig().getResources().getThreadPool().getMinThreads(),
                    config.getExportConfig().getResources().getThreadPool().getMaxThreads(),
                    PoolSizeAdaptationStrategy.AGGRESSIVE,
                    new DBExportWorkerFactory(
                            schemaMapping,
                            cityGMLBuilder,
                            writer,
                            xlinkPool,
                            idCacheManager,
                            cacheTableManager,
                            dummy,
                            null,
                            internalConfig,
                            config,
                            eventDispatcher),
                    queueSize,
                    false);

            // set virtual channel for events triggered by worker
            xlinkPool.setEventSource(eventChannel);
            databaseWorkerPool.setEventSource(eventChannel);

            // start worker pools with a single worker
            xlinkPool.prestartCoreWorker();
            databaseWorkerPool.prestartCoreWorker();

            // ok, preparation done, start database query
            QueryExecuter queryExecuter = new QueryExecuter(writer,
                    databaseWorkerPool,
                    cacheTableManager,
                    eventChannel,
                    connectionPool,
                    cityGMLBuilder,
                    internalConfig,
                    wfsConfig);

            // execute database query
            queryExecuter.executeQuery(wfsRequest, queryExpressions, pageRequest, dummy, request);

            // database query executed. shutdown pools.
            try {
                databaseWorkerPool.shutdownAndWait();
                xlinkPool.shutdownAndWait();
            } catch (InterruptedException e) {
                throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to shutdown worker pools.", e);
            }

        } catch (WFSException e) {
            throw e;
        } catch (Throwable e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "An unexpected " + e.getClass().getName() + " error occurred.", e);
        } finally {
            // flush response writer
            if (writer != null) {
                try {
                    writer.close();
                } catch (FeatureWriteException e) {
                    //
                }
            }

            // clean up...
            if (databaseWorkerPool != null && !databaseWorkerPool.isTerminated())
                databaseWorkerPool.shutdownNow();

            if (xlinkPool != null && !xlinkPool.isTerminated())
                xlinkPool.shutdownNow();

            if (idCacheManager != null) {
                try {
                    idCacheManager.shutdownAll();
                } catch (SQLException e) {
                    //
                }
            }

            if (cacheTableManager != null)
                cacheCleanerPool.addWork(cacheTableManager::close);
        }
    }

    private GetFeatureResponseBuilder getFeatureWriterBuilder(OutputFormat outputFormat, GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, IdCacheManager idCacheManager, InternalConfig internalConfig) throws FeatureWriteException {
        // create geometry stripper
        GeometryStripper geometryStripper = wfsConfig.getConstraints().isStripGeometry() ? new GeometryStripper() : null;

        // create response builder for requested output format
        GetFeatureResponseBuilder builder;
        switch (outputFormat.getName()) {
            case "application/gml+xml; version=3.1":
                builder = new CityGMLWriterBuilder();
                break;
            case "application/json":
                builder = new CityJSONWriterBuilder();
                break;
            default:
                throw new FeatureWriteException("Failed to create a response builder for the output format '" + outputFormat.getName() + "'.");
        }

        if (wfsRequest.getResultType() == ResultTypeType.HITS && !builder.supportsHitsResponse())
            builder = new CityGMLWriterBuilder();

        // initialize builder
        builder.initializeContext(wfsRequest, queryExpressions, outputFormat.getOptions(), geometryStripper, idCacheManager, builder, internalConfig, wfsConfig, config);

        return builder;
    }

}
