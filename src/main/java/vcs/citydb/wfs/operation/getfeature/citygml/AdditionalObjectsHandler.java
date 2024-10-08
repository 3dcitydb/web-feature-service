package vcs.citydb.wfs.operation.getfeature.citygml;

import org.citydb.config.project.global.LogLevel;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.global.InterruptEvent;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.internal.xml.TransformerChain;
import org.citygml4j.util.internal.xml.TransformerChainFactory;
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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.util.CacheCleanerWork;
import vcs.citydb.wfs.util.CacheCleanerWorker;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AdditionalObjectsHandler {
    private final SAXWriter saxWriter;
    private final CityGMLVersion version;
    private final CityGMLBuilder cityGMLBuilder;
    private final TransformerChainFactory transformerChainFactory;
    private final WorkerPool<CacheCleanerWork> cacheCleanerPool;
    private final EventDispatcher eventDispatcher;
    private final Object eventChannel;

    private final Map<String, Path> tempFiles = new ConcurrentHashMap<>();
    private final Map<String, CityModelWriter> tempWriters = new ConcurrentHashMap<>();
    private final Path tempDir;

    private volatile boolean shouldRun = true;
    private boolean isInitialized;

    @SuppressWarnings("unchecked")
    protected AdditionalObjectsHandler(SAXWriter saxWriter, CityGMLVersion version, CityGMLBuilder cityGMLBuilder, TransformerChainFactory transformerChainFactory, Object eventChannel) {
        this.saxWriter = saxWriter;
        this.version = version;
        this.cityGMLBuilder = cityGMLBuilder;
        this.transformerChainFactory = transformerChainFactory;
        this.eventChannel = eventChannel;

        tempDir = (ObjectRegistry.getInstance().lookup(WFSConfig.class)).getServer().getTempDir();
        cacheCleanerPool = (WorkerPool<CacheCleanerWork>) ObjectRegistry.getInstance().lookup(CacheCleanerWorker.class.getName());
        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
    }

    protected void startAdditionalObjects() throws SAXException {
        if (!isInitialized) {
            Attributes dummyAttributes = new AttributesImpl();
            saxWriter.startElement(Constants.WFS_NAMESPACE_URI, "additionalObjects", null, dummyAttributes);
            saxWriter.startElement(Constants.WFS_NAMESPACE_URI, "SimpleFeatureCollection", null, dummyAttributes);
            isInitialized = true;
        }
    }

    protected void endAdditionalObjects() throws SAXException {
        if (isInitialized) {
            saxWriter.endElement(Constants.WFS_NAMESPACE_URI, "SimpleFeatureCollection", null);
            saxWriter.endElement(Constants.WFS_NAMESPACE_URI, "additionalObjects", null);
        }
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

    protected void writeObjects() {
        if (!shouldRun)
            return;

        try {
            // close writers to flush buffers
            for (CityModelWriter tempWriter : tempWriters.values())
                tempWriter.close();

            CityGMLInputFactory in = cityGMLBuilder.createCityGMLInputFactory();
            in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);

            startAdditionalObjects();

            Attributes dummyAttributes = new AttributesImpl();
            String propertyName = "member";
            String propertyQName = saxWriter.getPrefix(Constants.WFS_NAMESPACE_URI) + ":" + propertyName;

            // iterate over temp files and send additional objects to response stream
            for (Path tempFile : tempFiles.values()) {
                try (CityGMLReader tempReader = in.createCityGMLReader(tempFile.toFile())) {
                    while (tempReader.hasNext()) {
                        XMLChunk chunk = tempReader.nextChunk();
                        if ("CityModel".equals(chunk.getTypeName().getLocalPart())
                                && Modules.isCityGMLModuleNamespace(chunk.getTypeName().getNamespaceURI()))
                            continue;

                        ContentHandler handler;
                        if (transformerChainFactory == null)
                            handler = saxWriter;
                        else {
                            TransformerChain chain = transformerChainFactory.buildChain();
                            chain.tail().setResult(new SAXResult(saxWriter));
                            handler = chain.head();
                            handler.startDocument();
                        }

                        handler.startElement(Constants.WFS_NAMESPACE_URI, propertyName, propertyQName, dummyAttributes);
                        chunk.send(handler, true);
                        handler.endElement(Constants.WFS_NAMESPACE_URI, propertyName, propertyQName);

                        if (transformerChainFactory != null)
                            handler.endDocument();
                    }
                }
            }

        } catch (CityGMLWriteException | CityGMLBuilderException | CityGMLReadException | SAXException |
                 TransformerConfigurationException e) {
            eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to write additional objects.", LogLevel.ERROR, e, eventChannel));
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

            cacheCleanerPool.addWork(() -> Files.delete(entry.getValue()));
        }
    }

    private CityModelWriter getOrCreateCacheWriter() throws CityGMLWriteException, IOException {
        String key = Thread.currentThread().getName();
        CityModelWriter tempWriter = tempWriters.get(key);
        if (tempWriter == null) {
            // create unique temp file
            Path tempFile = tempDir.resolve("objects-" + UUID.randomUUID().toString() + ".tmp");

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
