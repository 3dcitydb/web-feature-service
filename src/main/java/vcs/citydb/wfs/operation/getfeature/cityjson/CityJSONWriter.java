package vcs.citydb.wfs.operation.getfeature.cityjson;

import java.util.List;

import org.citydb.citygml.common.database.uid.UIDCache;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.uid.UIDCacheType;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.concurrent.SingleWorkerPool;
import org.citydb.config.Config;
import org.citydb.registry.ObjectRegistry;
import org.citydb.writer.CityJSONWriterWorkerFactory;
import org.citygml4j.binding.cityjson.feature.AbstractCityObjectType;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONChunkWriter;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONWriteException;
import org.citygml4j.builder.cityjson.marshal.CityJSONMarshaller;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.feature.AbstractFeature;

import net.opengis.wfs._2.TruncatedResponse;
import vcs.citydb.wfs.operation.getfeature.FeatureWriter;
import vcs.citydb.wfs.util.GeometryStripper;

public class CityJSONWriter implements FeatureWriter {
	private final CityJSONChunkWriter writer;
	private final GeometryStripper geometryStripper;
	private final UIDCacheManager uidCacheManager;
	private final Config exporterConfig;

	private final SingleWorkerPool<AbstractCityObjectType> writerPool;
	private final CityJSONMarshaller marshaller;

	private boolean checkForDuplicates;

	public CityJSONWriter(CityJSONChunkWriter writer, GeometryStripper geometryStripper, UIDCacheManager uidCacheManager, Object eventChannel, Config exporterConfig) {
		this.writer = writer;
		this.geometryStripper = geometryStripper;
		this.uidCacheManager = uidCacheManager;
		this.exporterConfig = exporterConfig;

		marshaller = writer.getCityJSONMarshaller();
		int queueSize = exporterConfig.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads() * 2;

		writerPool = new SingleWorkerPool<AbstractCityObjectType>(
				"cityjson_writer_pool", 
				new CityJSONWriterWorkerFactory(writer, ObjectRegistry.getInstance().getEventDispatcher()), 
				queueSize, 
				false);

		writerPool.setEventSource(eventChannel);
		writerPool.prestartCoreWorkers();
	}

	@Override
	public void setWriteSingleFeature(boolean isWriteSingleFeature) {
		// nothing to do here...
	}

	@Override
	public void startFeatureCollection(long matchNo, long returnNo) throws FeatureWriteException {
		// nothing to do here...
	}

	@Override
	public void endFeatureCollection() throws FeatureWriteException {
		// we only have to check for duplicates after the first set of features
		checkForDuplicates = exporterConfig.getInternal().isRegisterGmlIdInCache();
	}

	@Override
	public void write(AbstractFeature feature) throws FeatureWriteException {	
		if (feature instanceof AbstractCityObject) {
			if (checkForDuplicates && isFeatureAlreadyExported(feature))
				return;

			// security feature: strip geometry from features
			if (geometryStripper != null)
				feature.accept(geometryStripper);

			List<AbstractCityObjectType> cityObjects = marshaller.marshal((AbstractCityObject)feature);
			for (AbstractCityObjectType cityObject : cityObjects)
				writerPool.addWork(cityObject);
		}
	}

	@Override
	public void writeAdditionalObjects() throws FeatureWriteException {
		// not supported by this writer
	}

	@Override
	public void writeTruncatedResponse(TruncatedResponse truncatedResponse) throws FeatureWriteException {
		// not supported by this writer
	}

	@Override
	public void close() throws FeatureWriteException {
		try {
			writerPool.shutdownAndWait();
			writer.writeEndDocument();
			writer.flush();
		} catch (InterruptedException | CityJSONWriteException e) {
			throw new FeatureWriteException("Failed to close CityJSON response writer.", e);
		} finally {
			if (!writerPool.isTerminated())
				writerPool.shutdownNow();
		}
	}

	private boolean isFeatureAlreadyExported(AbstractFeature feature) {
		if (!feature.isSetId())
			return false;

		UIDCache server = uidCacheManager.getCache(UIDCacheType.OBJECT);
		return server.get(feature.getId()) != null;
	}

}
