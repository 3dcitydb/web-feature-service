/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2017
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

import net.opengis.wfs._2.GetFeatureType;

import org.citydb.api.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.api.concurrent.SingleWorkerPool;
import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerFactory;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.EventHandler;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheType;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.exporter.concurrent.DBExportWorkerFactory;
import org.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.modules.citygml.exporter.database.uid.GeometryGmlIdCache;
import org.citydb.modules.common.concurrent.IOWriterWorkerFactory;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.operation.GetFeatureOutputFormat;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.util.CacheTableCleanerWorker;
import vcs.citydb.wfs.util.NullWorker;

public class ExportController implements EventHandler {
	private final JAXBBuilder jaxbBuilder;
	private final WFSConfig wfsConfig;
	private final Config exporterConfig;
	private final DatabaseConnectionPool connectionPool;
	private final EventDispatcher eventDispatcher;

	private final Object eventChannel = new Object();
	private WFSException wfsException;

	public ExportController(JAXBBuilder jaxbBuilder, WFSConfig wfsConfig, Config exporterConfig) {
		this.jaxbBuilder = jaxbBuilder;
		this.wfsConfig = wfsConfig;
		this.exporterConfig = exporterConfig;

		connectionPool = DatabaseConnectionPool.getInstance();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
	}

	@SuppressWarnings("unchecked")
	public void doExport(GetFeatureType wfsRequest,
			List<QueryExpression> queryExpressions,
			CityGMLVersion version,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		// define queue size for worker pools
		int queueSize = exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads() * 2;

		// prepare SAXWriter
		exporterConfig.getProject().getExporter().setCityGMLVersion(Util.fromCityGMLVersion(version));

		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setWriteEncoding(true);
		saxWriter.setIndentString(" ");

		try {
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");

			// gzipped or XML output
			if (wfsRequest.isSetOutputFormat() && 
					GetFeatureOutputFormat.GML3_1_GZIP.value().equals(wfsRequest.getOutputFormat()) && 
					wfsConfig.getOperations().getGetFeature().supportsOutputFormat(GetFeatureOutputFormat.GML3_1_GZIP)) {							
				response.addHeader("Content-Encoding", "gzip");
				saxWriter.setOutput(new GZIPOutputStream(response.getOutputStream()), "UTF-8");
			} else
				saxWriter.setOutput(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));

		} catch (IOException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize XML response writer.", e);
		}

		// set WFS prefix and schema location in case we do not have to return the bare feature
		if (queryExpressions.size() > 1 || !queryExpressions.get(0).isGetFeatureById()) {
			saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			saxWriter.setSchemaLocation(Constants.WFS_NAMESPACE_URI, Constants.WFS_SCHEMA_LOCATION);
		}

		// set CityGML prefixes and schema locations
		ModuleContext moduleContext = new ModuleContext(version);
		for (Module module : moduleContext.getModules()) {
			if (!(module instanceof CityGMLModule) || module.getType() == CityGMLModuleType.CORE)
				saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
			else if (module.getType() == CityGMLModuleType.GENERICS) {
				saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
				saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
			}
		}
		
		for (QueryExpression queryExpression : queryExpressions) {
			for (QName featureTypeName : queryExpression.getFeatureTypeNames()) {
				Module module = Modules.getCityGMLModule(featureTypeName.getNamespaceURI());
				if (module != null) {
					saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
					saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
				}
			}
		}
		
		// general cityobjectgroup settings
		exporterConfig.getProject().getExporter().getCityObjectGroup().setExportMemberAsXLinks(true);

		// general appearance settings
		exporterConfig.getProject().getExporter().getAppearances().setExportAppearances(false);
		exporterConfig.getProject().getExporter().getAppearances().setExportTextureFiles(false);
		exporterConfig.getInternal().setExportGlobalAppearances(false);

		UIDCacheManager uidCacheManager = null;
		CacheTableManager cacheTableManager = null;
		SingleWorkerPool<SAXEventBuffer> writerPool = null;
		SingleWorkerPool<DBXlink> xlinkPool = null;
		WorkerPool<DBSplittingResult> databaseWorkerPool = null;

		try {
			// create instance of cache table manager
			try {
				cacheTableManager = new CacheTableManager(
						connectionPool, 
						exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
						exporterConfig);
			} catch (SQLException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize internal cache manager.", e);
			} catch (IOException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize internal cache manager.", e);
			}

			// create instance of gml:id lookup server manager...
			uidCacheManager = new UIDCacheManager();

			// create export filter
			// TODO: replace with new filter layer
			ExportFilter exportFilter = new ExportFilter(exporterConfig);

			// ...and start servers
			try {		
				uidCacheManager.initCache(
						UIDCacheType.GEOMETRY,
						new GeometryGmlIdCache(cacheTableManager, 
								exporterConfig.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getPartitions(),
								exporterConfig.getProject().getDatabase().getUpdateBatching().getGmlIdCacheBatchValue()),
								exporterConfig.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getCacheSize(),
								exporterConfig.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getPageFactor(),
								exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads());

				uidCacheManager.initCache(
						UIDCacheType.FEATURE,
						new GeometryGmlIdCache(cacheTableManager, 
								exporterConfig.getProject().getExporter().getResources().getGmlIdCache().getFeature().getPartitions(), 
								exporterConfig.getProject().getDatabase().getUpdateBatching().getGmlIdCacheBatchValue()),
								exporterConfig.getProject().getExporter().getResources().getGmlIdCache().getFeature().getCacheSize(),
								exporterConfig.getProject().getExporter().getResources().getGmlIdCache().getFeature().getPageFactor(),
								exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads());
			} catch (SQLException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize internal gml:id caches.", e);
			}

			// create worker pools
			writerPool = new SingleWorkerPool<SAXEventBuffer>(
					"citygml_writer_pool",
					new IOWriterWorkerFactory(saxWriter, eventDispatcher),
					queueSize,
					false);

			// TODO: currently XLinks to texture images and library objects are not resolved
			xlinkPool = new SingleWorkerPool<DBXlink>(
					"xlink_exporter_pool",
					new WorkerFactory<DBXlink>() {
						public Worker<DBXlink> createWorker() {
							return new NullWorker<DBXlink>();
						}
					},
					1,
					false);

			FeatureMemberWriterFactory writerFactory = new FeatureMemberWriterFactory(writerPool, uidCacheManager, jaxbBuilder, wfsConfig, exporterConfig);

			databaseWorkerPool = new WorkerPool<DBSplittingResult>(
					"db_exporter_pool",
					exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMinThreads(), 
					exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
					PoolSizeAdaptationStrategy.AGGRESSIVE,
					new DBExportWorkerFactory(
							connectionPool, 
							jaxbBuilder,
							writerFactory, 
							xlinkPool,
							uidCacheManager,
							cacheTableManager,
							exportFilter,
							exporterConfig,
							eventDispatcher), 
							queueSize,
							false);

			// set virtual channel for events triggered by worker
			writerPool.setEventSource(eventChannel);
			xlinkPool.setEventSource(eventChannel);
			databaseWorkerPool.setEventSource(eventChannel);
			
			// start worker pools with a single worker
			writerPool.prestartCoreWorker();
			xlinkPool.prestartCoreWorker();
			databaseWorkerPool.prestartCoreWorker();

			// ok, preparation done, start database query
			QueryExecuter queryExecuter = null;
			try {
				queryExecuter = new QueryExecuter(wfsRequest, 
						queryExpressions,
						writerFactory,
						databaseWorkerPool,
						writerPool,
						connectionPool,
						jaxbBuilder,
						exportFilter,
						wfsConfig,
						exporterConfig);
			} catch (JAXBException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize internal query executer.", e);			
			} catch (DatatypeConfigurationException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize internal query executer.", e);			
			}

			// execute database query
			queryExecuter.executeQuery();

			// database query executed. shutdown pools.
			try {
				databaseWorkerPool.shutdownAndWait();
				xlinkPool.shutdownAndWait();
				writerPool.shutdownAndWait();
			} catch (InterruptedException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to shutdown worker pools.", e);
			}

			// close SAX writer. this also closes the servlet output stream.
			try {
				saxWriter.close();
			} catch (SAXException e) {
				throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to close the SAX writer.", e);
			}

			// abort if a worker pool has thrown an error 
			if (wfsException != null)
				throw wfsException;

		} finally {
			// clean up...
			if (databaseWorkerPool != null && !databaseWorkerPool.isTerminated())
				databaseWorkerPool.shutdownNow();

			if (xlinkPool != null && !xlinkPool.isTerminated())
				xlinkPool.shutdownNow();

			if (writerPool != null && !writerPool.isTerminated())
				writerPool.shutdownNow();

			if (eventDispatcher != null) {
				try {
					eventDispatcher.flushEvents();
				} catch (InterruptedException e) {
					//
				}
			}

			if (uidCacheManager != null) {
				try {
					uidCacheManager.shutdownAll();
				} catch (SQLException e) {
					//
				}
			}

			if (cacheTableManager != null)
				((WorkerPool<CacheTableManager>)ObjectRegistry.getInstance().lookup(CacheTableCleanerWorker.class.getName())).addWork(cacheTableManager);
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel) {
			wfsException = new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, ((InterruptEvent)event).getLogMessage(), ((InterruptEvent)event).getCause());
		}
	}

}
