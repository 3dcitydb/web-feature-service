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
import de.tub.citydb.api.concurrent.PoolSizeAdaptationStrategy;
import de.tub.citydb.api.concurrent.SingleWorkerPool;
import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerFactory;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.modules.citygml.common.database.cache.CacheTableManager;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCacheType;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.exporter.concurrent.DBExportWorkerFactory;
import de.tub.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import de.tub.citydb.modules.citygml.exporter.database.uid.GeometryGmlIdCache;
import de.tub.citydb.modules.common.concurrent.IOWriterWorkerFactory;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.util.Util;

public class ExportController {
	private final JAXBBuilder jaxbBuilder;
	private final WFSConfig wfsConfig;
	private final Config exporterConfig;
	private final DatabaseConnectionPool connectionPool;
	private EventDispatcher eventDispatcher;

	public ExportController(JAXBBuilder cityGMLBuilder, WFSConfig wfsConfig, Config exporterConfig) {
		this.jaxbBuilder = cityGMLBuilder;
		this.wfsConfig = wfsConfig;
		this.exporterConfig = exporterConfig;

		connectionPool = DatabaseConnectionPool.getInstance();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
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

		// set WFS prefix and schema location
		saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
		saxWriter.setSchemaLocation(Constants.WFS_NAMESPACE_URI, Constants.WFS_SCHEMA_LOCATION);

		// set CityGML prefixes and schema locations
		ModuleContext moduleContext = new ModuleContext(version);
		for (Module module : moduleContext.getModules()) {
			if (!(module instanceof CityGMLModule) ||
					module.getType() == CityGMLModuleType.CORE ||
					module.getType() == CityGMLModuleType.GENERICS)
				saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
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
					new IOWriterWorkerFactory(saxWriter),
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

			databaseWorkerPool = new WorkerPool<DBSplittingResult>(
					"db_exporter_pool",
					exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMinThreads(), 
					exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
					PoolSizeAdaptationStrategy.AGGRESSIVE,
					new DBExportWorkerFactory(
							connectionPool, 
							jaxbBuilder,
							new FeatureMemberWriter(writerPool, uidCacheManager, jaxbBuilder, exporterConfig), 
							xlinkPool,
							uidCacheManager,
							cacheTableManager,
							exportFilter,
							exporterConfig,
							eventDispatcher), 
							queueSize,
							false);

			// start worker pools with a single worker
			writerPool.prestartCoreWorker();
			xlinkPool.prestartCoreWorker();
			databaseWorkerPool.prestartCoreWorker();

			// ok, preparation done, start database query
			QueryExecuter queryExecuter = null;
			try {
				queryExecuter = new QueryExecuter(wfsRequest, 
						queryExpressions,
						databaseWorkerPool,
						writerPool,
						connectionPool,
						jaxbBuilder,
						exportFilter,
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

}
