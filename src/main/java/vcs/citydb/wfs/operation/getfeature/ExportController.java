package vcs.citydb.wfs.operation.getfeature;

import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ResultTypeType;
import org.citydb.citygml.common.database.cache.CacheTableManager;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.uid.UIDCacheType;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.exporter.concurrent.DBExportWorkerFactory;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.citygml.exporter.database.uid.GeometryGmlIdCache;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.concurrent.SingleWorkerPool;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.EventDispatcher;
import org.citydb.query.Query;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.operation.GetFeatureOutputFormat;
import vcs.citydb.wfs.config.operation.OutputFormat;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.operation.getfeature.citygml.CityGMLWriterBuilder;
import vcs.citydb.wfs.operation.getfeature.cityjson.CityJSONWriterBuilder;
import vcs.citydb.wfs.util.CacheCleanerWork;
import vcs.citydb.wfs.util.CacheCleanerWorker;
import vcs.citydb.wfs.util.GeometryStripper;
import vcs.citydb.wfs.util.NullWorker;

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
	private final EventDispatcher eventDispatcher;

	private final Object eventChannel = new Object();

	public ExportController(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig, Config config) {
		this.cityGMLBuilder = cityGMLBuilder;
		this.wfsConfig = wfsConfig;
		this.config = config;

		connectionPool = DatabaseConnectionPool.getInstance();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
	}

	@SuppressWarnings("unchecked")
	public void doExport(GetFeatureType wfsRequest,
			List<QueryExpression> queryExpressions,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		// define queue size for worker pools
		int queueSize = config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads() * 2;

		// general appearance settings
		config.getInternal().setExportGlobalAppearances(false);

		UIDCacheManager uidCacheManager = null;
		CacheTableManager cacheTableManager = null;
		SingleWorkerPool<DBXlink> xlinkPool = null;
		WorkerPool<DBSplittingResult> databaseWorkerPool = null;
		FeatureWriter writer = null;

		try {
			// create instance of cache table manager
			try {
				cacheTableManager = new CacheTableManager(
						config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
						config);
			} catch (SQLException | IOException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize internal cache manager.", e);
			}

			// create instance of gml:id lookup server manager...
			uidCacheManager = new UIDCacheManager();

			// ...and start servers
			try {		
				uidCacheManager.initCache(
						UIDCacheType.GEOMETRY,
						new GeometryGmlIdCache(cacheTableManager, 
								config.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getPartitions(),
								config.getProject().getDatabase().getUpdateBatching().getGmlIdCacheBatchValue()),
						config.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getCacheSize(),
						config.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getPageFactor(),
						config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads());

				uidCacheManager.initCache(
						UIDCacheType.OBJECT,
						new GeometryGmlIdCache(cacheTableManager, 
								config.getProject().getExporter().getResources().getGmlIdCache().getFeature().getPartitions(),
								config.getProject().getDatabase().getUpdateBatching().getGmlIdCacheBatchValue()),
						config.getProject().getExporter().getResources().getGmlIdCache().getFeature().getCacheSize(),
						config.getProject().getExporter().getResources().getGmlIdCache().getFeature().getPageFactor(),
						config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads());
			} catch (SQLException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize internal gml:id caches.", e);
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
				GetFeatureResponseBuilder builder = getFeatureWriterBuilder(wfsRequest, queryExpressions, uidCacheManager);

				response.setContentType(builder.getMimeType());
				response.setCharacterEncoding(StandardCharsets.UTF_8.name());

				writer = builder.buildFeatureWriter(response.getOutputStream(), StandardCharsets.UTF_8.name());
			} catch (FeatureWriteException | IOException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize the response writer.", e);
			}

			databaseWorkerPool = new WorkerPool<>(
					"db_exporter_pool",
					config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMinThreads(),
					config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
					PoolSizeAdaptationStrategy.AGGRESSIVE,
					new DBExportWorkerFactory(
							null,
							schemaMapping,
							cityGMLBuilder,
							writer,
							xlinkPool,
							uidCacheManager,
							cacheTableManager,
							dummy,
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
			QueryExecuter queryExecuter = new QueryExecuter(wfsRequest,
					writer,
					databaseWorkerPool,
					eventChannel,
					connectionPool,
					cityGMLBuilder,
					wfsConfig,
					config);

			// execute database query
			queryExecuter.executeQuery(queryExpressions, dummy, request);

			// database query executed. shutdown pools.
			try {
				databaseWorkerPool.shutdownAndWait();
				xlinkPool.shutdownAndWait();
			} catch (InterruptedException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to shutdown worker pools.", e);
			}

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

			if (uidCacheManager != null) {
				try {
					uidCacheManager.shutdownAll();
				} catch (SQLException e) {
					//
				}
			}

			if (cacheTableManager != null)
				((WorkerPool<CacheCleanerWork>)ObjectRegistry.getInstance().lookup(CacheCleanerWorker.class.getName())).addWork(new CacheCleanerWork(cacheTableManager));
		}
	}

	private GetFeatureResponseBuilder getFeatureWriterBuilder(GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, UIDCacheManager uidCacheManager) throws FeatureWriteException {
		OutputFormat outputFormat = wfsConfig.getOperations().getGetFeature().getOutputFormat(wfsRequest.isSetOutputFormat() ? 
				wfsRequest.getOutputFormat() : GetFeatureOutputFormat.GML3_1.value());

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
		builder.initializeContext(wfsRequest, queryExpressions, outputFormat.getOptions(), geometryStripper, uidCacheManager, builder, wfsConfig, config);

		return builder;
	}

}
