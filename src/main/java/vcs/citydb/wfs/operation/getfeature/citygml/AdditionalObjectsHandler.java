package vcs.citydb.wfs.operation.getfeature.citygml;

import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.project.global.LogLevel;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.InterruptEvent;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXWriter;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.CityGMLOutputFactory;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import org.citygml4j.xml.io.reader.XMLChunk;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.citygml4j.xml.io.writer.CityModelWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.util.CacheCleanerWork;
import vcs.citydb.wfs.util.CacheCleanerWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdditionalObjectsHandler {
    private final SAXWriter saxWriter;
    private final CityGMLVersion version;
    private final CityGMLBuilder cityGMLBuilder;
    private final WorkerPool<CacheCleanerWork> cacheCleanerPool;
    private final EventDispatcher eventDispatcher;
    private final Object eventChannel;

    private final Map<String, Path> tempFiles = new ConcurrentHashMap<>();
    private final Map<String, CityModelWriter> tempWriters = new ConcurrentHashMap<>();
    private volatile boolean shouldRun = true;

    protected AdditionalObjectsHandler(SAXWriter saxWriter, CityGMLVersion version, CityGMLBuilder cityGMLBuilder, Object eventChannel) {
        this.saxWriter = saxWriter;
        this.version = version;
        this.cityGMLBuilder = cityGMLBuilder;
        this.eventChannel = eventChannel;

        cacheCleanerPool = (WorkerPool<CacheCleanerWork>) ObjectRegistry.getInstance().lookup(CacheCleanerWorker.class.getName());
        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
    }

    protected boolean hasAdditionalObjects() {
        return !tempWriters.isEmpty();
    }

    protected void cacheObject(AbstractFeature feature) throws FeatureWriteException {
        try {
            if (shouldRun)
                getOrCreateCacheWriter().writeFeatureMember(feature);
        } catch (CityGMLWriteException | IOException e) {
            shouldRun = false;
            throw new FeatureWriteException("Failed to cache feature with gml:id '" + feature.getId() + "' as additional object.", e);
        }
    }

    protected void writeObjects() throws FeatureWriteException {
        if (!shouldRun)
            return;

        try {
            // close writers to flush buffers
            for (CityModelWriter tempWriter : tempWriters.values())
                tempWriter.close();

            CityGMLInputFactory in = cityGMLBuilder.createCityGMLInputFactory();
            in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);

            Attributes dummyAttributes = new AttributesImpl();
            saxWriter.startElement(Constants.WFS_NAMESPACE_URI, "additionalObjects", null, dummyAttributes);
            saxWriter.startElement(Constants.WFS_NAMESPACE_URI, "SimpleFeatureCollection", null, dummyAttributes);

            // iterate over temp files and send additional objects to response stream
            for (Path tempFile : tempFiles.values()) {
                try (CityGMLReader tempReader = in.createCityGMLReader(tempFile.toFile())) {
                    while (tempReader.hasNext()) {
                        XMLChunk chunk = tempReader.nextChunk();
                        if ("CityModel".equals(chunk.getTypeName().getLocalPart())
                                && Modules.isCityGMLModuleNamespace(chunk.getTypeName().getNamespaceURI()))
                            continue;

                        saxWriter.startElement(Constants.WFS_NAMESPACE_URI, "member", null, dummyAttributes);
                        chunk.send(saxWriter, true);
                        saxWriter.endElement(Constants.WFS_NAMESPACE_URI, "member", null);
                    }
                } catch (SAXException e) {
                    eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to write additional objects.", LogLevel.ERROR, e, eventChannel, this));
                    break;
                }
            }

            saxWriter.endElement(Constants.WFS_NAMESPACE_URI, "SimpleFeatureCollection", null);
            saxWriter.endElement(Constants.WFS_NAMESPACE_URI, "additionalObjects", null);

        } catch (CityGMLWriteException | CityGMLBuilderException | CityGMLReadException | SAXException e) {
            eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to write additional objects.", LogLevel.ERROR, e, eventChannel, this));
        }
    }

    public void cleanCache() {
        for (Map.Entry<String, Path> entry : tempFiles.entrySet()) {
            CityModelWriter tempWriter = tempWriters.get(entry.getKey());
            if (tempWriter != null) {
                try {
                    tempWriter.close();
                } catch (CityGMLWriteException e) {
                    //
                }
            }

            cacheCleanerPool.addWork(new CacheCleanerWork(entry.getValue()));
        }
    }

    private CityModelWriter getOrCreateCacheWriter() throws CityGMLWriteException, IOException {
        String key = Thread.currentThread().getName();
        CityModelWriter tempWriter = tempWriters.get(key);
        if (tempWriter == null) {
            // create unique temp file
            long n = new SecureRandom().nextLong();
            n = n == Long.MIN_VALUE ? 0 : Math.abs(n);
            Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), "3dcitydb-wfs", Long.toString(n) + ".tmp");
            Files.createDirectories(tempFile.getParent());

            // create temp writer
            CityGMLOutputFactory out = cityGMLBuilder.createCityGMLOutputFactory(version);
            tempWriter = out.createCityModelWriter(tempFile.toFile(), "UTF-8");
            tempWriter.setPrefixes(version);
            tempWriter.setPrefixes(CityGMLContext.getInstance().getADEContexts());
            tempWriter.setIndentString("");

            tempFiles.put(key, tempFile);
            tempWriters.put(key, tempWriter);
        }

        return tempWriter;
    }
}
