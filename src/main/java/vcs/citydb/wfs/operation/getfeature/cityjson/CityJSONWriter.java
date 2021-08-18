package vcs.citydb.wfs.operation.getfeature.cityjson;

import net.opengis.wfs._2.TruncatedResponse;
import org.citydb.config.Config;
import org.citydb.core.operation.common.cache.IdCache;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.common.cache.IdCacheType;
import org.citydb.core.operation.exporter.util.InternalConfig;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.writer.CityJSONWriterWorkerFactory;
import org.citydb.core.writer.SequentialWriter;
import org.citydb.util.concurrent.SingleWorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONChunkWriter;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONWriteException;
import org.citygml4j.builder.cityjson.marshal.CityJSONMarshaller;
import org.citygml4j.cityjson.CityJSON;
import org.citygml4j.cityjson.feature.AbstractCityObjectType;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.feature.AbstractFeature;
import vcs.citydb.wfs.operation.getfeature.FeatureWriter;
import vcs.citydb.wfs.util.GeometryStripper;

public class CityJSONWriter implements FeatureWriter, EventHandler {
	private final CityJSONChunkWriter writer;
	private final GeometryStripper geometryStripper;
	private final IdCacheManager idCacheManager;
	private final Object eventChannel;
	private final InternalConfig internalConfig;
	private final EventDispatcher eventDispatcher;

	private final SingleWorkerPool<AbstractCityObjectType> writerPool;
	private final CityJSONMarshaller marshaller;

	private boolean isWriteSingleFeature;
	private boolean hasContent;
	private boolean checkForDuplicates;
	private boolean useSequentialWriting;
	private SequentialWriter<AbstractCityObjectType> sequentialWriter;

	private boolean addSequenceId;
	private long idOffset;

	public CityJSONWriter(
			CityJSONChunkWriter writer,
			GeometryStripper geometryStripper,
			IdCacheManager idCacheManager,
			Object eventChannel,
			InternalConfig internalConfig,
			Config config) {
		this.writer = writer;
		this.geometryStripper = geometryStripper;
		this.idCacheManager = idCacheManager;
		this.eventChannel = eventChannel;
		this.internalConfig = internalConfig;

		marshaller = writer.getCityJSONMarshaller();
		int queueSize = config.getExportConfig().getResources().getThreadPool().getMaxThreads() * 2;

		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		writerPool = new SingleWorkerPool<>(
				"cityjson_writer_pool",
				new CityJSONWriterWorkerFactory(writer, eventDispatcher),
				queueSize,
				false);

		writerPool.setEventSource(eventChannel);
		writerPool.prestartCoreWorkers();
	}

	void addSequenceIdWhenSorting(boolean addSequenceId) {
		this.addSequenceId = addSequenceId;
	}

	@Override
	public void useIndentation(boolean useIndentation) {
		writer.setIndent(useIndentation ? " " : "");
	}

	@Override
	public void setWriteSingleFeature(boolean isWriteSingleFeature) {
		this.isWriteSingleFeature = isWriteSingleFeature;
	}

	@Override
	public void startFeatureCollection(long matchNo, long returnNo, String previous, String next) throws FeatureWriteException {
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
	public long setSequentialWriting(boolean useSequentialWriting) {
		long sequenceId = 0;

		if (useSequentialWriting) {
			if (this.useSequentialWriting) {
				sequenceId = sequentialWriter.getCurrentSequenceId();
			} else if (sequentialWriter != null) {
				idOffset = sequentialWriter.getCurrentSequenceId();
				sequenceId = sequentialWriter.reset();
			} else {
				sequentialWriter = new SequentialWriter<>(writerPool);
			}
		}

		this.useSequentialWriting = useSequentialWriting;
		return sequenceId;
	}

	@Override
	public void write(AbstractFeature feature, long sequenceId) throws FeatureWriteException {
		if (feature instanceof AbstractCityObject) {
			if (checkForDuplicates && isFeatureAlreadyExported(feature)) {
				return;
			}

			// security feature: strip geometry from features
			if (geometryStripper != null) {
				feature.accept(geometryStripper);
			}

			CityJSON cityJSON = new CityJSON();

			AbstractCityObjectType cityObject = marshaller.marshal((AbstractCityObject) feature, cityJSON);
			for (AbstractCityObjectType child : cityJSON.getCityObjects()) {
				writerPool.addWork(child);
			}

			if (cityJSON.isSetExtensionProperties()) {
				cityJSON.getExtensionProperties().forEach(writer::addRootExtensionProperty);
			}

			if (cityObject != null) {
				if (!useSequentialWriting) {
					writerPool.addWork(cityObject);
				} else {
					try {
						if (addSequenceId && sequenceId >= 0) {
							cityObject.getAttributes().addExtensionAttribute("sequenceId", sequenceId + idOffset);
						}

						sequentialWriter.write(cityObject, sequenceId);
					} catch (InterruptedException e) {
						throw new FeatureWriteException("Failed to write city object with gml:id '" + feature.getId() + "'.", e);
					}
				}
			}

			hasContent = cityObject != null || cityJSON.hasCityObjects();
		}
	}

	@Override
	public void updateSequenceId(long sequenceId) throws FeatureWriteException {
		if (useSequentialWriting) {
			try {
				sequentialWriter.updateSequenceId(sequenceId);
			} catch (InterruptedException e) {
				throw new FeatureWriteException("Failed to update sequence id.", e);
			}
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
			if (!isWriteSingleFeature || hasContent) {
				writer.writeEndDocument();
			}
		} catch (InterruptedException | CityJSONWriteException e) {
			throw new FeatureWriteException("Failed to close CityJSON response writer.", e);
		} finally {
			if (!writerPool.isTerminated()) {
				writerPool.shutdownNow();
			}

			eventDispatcher.removeEventHandler(this);
		}
	}

	private boolean isFeatureAlreadyExported(AbstractFeature feature) {
		if (!feature.isSetId()) {
			return false;
		}

		IdCache cache = idCacheManager.getCache(IdCacheType.OBJECT);
		return cache.get(feature.getId()) != null;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel && sequentialWriter != null) {
			sequentialWriter.interrupt();
		}
	}
}
