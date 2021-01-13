package vcs.citydb.wfs.operation.getfeature.cityjson;

import net.opengis.wfs._2.TruncatedResponse;
import org.citydb.citygml.common.database.uid.UIDCache;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.uid.UIDCacheType;
import org.citydb.citygml.exporter.util.InternalConfig;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.concurrent.SingleWorkerPool;
import org.citydb.config.Config;
import org.citydb.registry.ObjectRegistry;
import org.citydb.writer.CityJSONWriterWorkerFactory;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONChunkWriter;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONWriteException;
import org.citygml4j.builder.cityjson.marshal.CityJSONMarshaller;
import org.citygml4j.cityjson.CityJSON;
import org.citygml4j.cityjson.feature.AbstractCityObjectType;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.feature.AbstractFeature;
import vcs.citydb.wfs.operation.getfeature.FeatureWriter;
import vcs.citydb.wfs.util.GeometryStripper;

public class CityJSONWriter implements FeatureWriter {
	private final CityJSONChunkWriter writer;
	private final GeometryStripper geometryStripper;
	private final UIDCacheManager uidCacheManager;
	private final Object eventChannel;
	private final InternalConfig internalConfig;

	private final SingleWorkerPool<AbstractCityObjectType> writerPool;
	private final CityJSONMarshaller marshaller;

	private boolean hasContent;
	private boolean checkForDuplicates;

	public CityJSONWriter(
			CityJSONChunkWriter writer,
			GeometryStripper geometryStripper,
			UIDCacheManager uidCacheManager,
			Object eventChannel,
			InternalConfig internalConfig,
			Config config) {
		this.writer = writer;
		this.geometryStripper = geometryStripper;
		this.uidCacheManager = uidCacheManager;
		this.eventChannel = eventChannel;
		this.internalConfig = internalConfig;

		marshaller = writer.getCityJSONMarshaller();
		int queueSize = config.getExportConfig().getResources().getThreadPool().getMaxThreads() * 2;

		writerPool = new SingleWorkerPool<>(
				"cityjson_writer_pool",
				new CityJSONWriterWorkerFactory(writer, ObjectRegistry.getInstance().getEventDispatcher()),
				queueSize,
				false);

		writerPool.setEventSource(eventChannel);
		writerPool.prestartCoreWorkers();
	}

	@Override
	public void useIndentation(boolean useIndentation) {
		writer.setIndent(useIndentation ? " " : "");
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
		checkForDuplicates = internalConfig.isRegisterGmlIdInCache();
	}

	@Override
	public void startAdditionalObjects() throws FeatureWriteException {
		// nothing to do here...
	}

	@Override
	public void endAdditionalObjects() throws FeatureWriteException {
		// nothing to do here...
	}

	@Override
	public void write(AbstractFeature feature, long sequenceId) throws FeatureWriteException {
		if (feature instanceof AbstractCityObject) {
			if (checkForDuplicates && isFeatureAlreadyExported(feature))
				return;

			// security feature: strip geometry from features
			if (geometryStripper != null)
				feature.accept(geometryStripper);

			CityJSON cityJSON = new CityJSON();
			AbstractCityObjectType dest = marshaller.marshal((AbstractCityObject) feature, cityJSON);

			for (AbstractCityObjectType child : cityJSON.getCityObjects())
				writerPool.addWork(child);

			if (cityJSON.isSetExtensionProperties())
				cityJSON.getExtensionProperties().forEach(writer::addRootExtensionProperty);

			hasContent = true;
			writerPool.addWork(dest);
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
	public void updateSequenceId(long sequenceId) throws FeatureWriteException {
		// nothing to do
	}

	@Override
	public void close() throws FeatureWriteException {
		try {
			writerPool.shutdownAndWait();
			if (hasContent)
				writer.writeEndDocument();
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
