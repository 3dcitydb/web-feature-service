package vcs.citydb.wfs.operation.getpropertyvalue;

import net.opengis.wfs._2.MemberPropertyType;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.TruncatedResponse;
import net.opengis.wfs._2.ValueCollectionType;
import org.citydb.config.Config;
import org.citydb.core.operation.exporter.util.Metadata;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.operation.exporter.writer.FeatureWriter;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.writer.SequentialWriter;
import org.citydb.core.writer.XMLWriterWorkerFactory;
import org.citydb.util.concurrent.SingleWorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.marshal.JAXBMarshaller;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.internal.xml.TransformerChain;
import org.citygml4j.util.internal.xml.TransformerChainFactory;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXFragmentWriter;
import org.citygml4j.util.xml.SAXFragmentWriter.WriteMode;
import org.citygml4j.util.xml.SAXWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.util.GeometryStripper;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xpath.*;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PropertyValueWriter implements FeatureWriter, EventHandler {
    private final String valueReference;
    private final SAXWriter saxWriter;
    private final TransformerChainFactory transformerChainFactory;
    private final NamespaceFilter namespaceFilter;
    private final GeometryStripper geometryStripper;
    private final boolean useSequentialWriting;
    private final Object eventChannel;

    private final Map<Long, Integer> propertyCounts;
    private final SingleWorkerPool<SAXEventBuffer> writerPool;
    private final CityGMLBuilder cityGMLBuilder;
    private final JAXBMarshaller jaxbMarshaller;
    private final XPathFactory xpathFactory;
    private final ObjectFactory wfsFactory;
    private final DatatypeFactory datatypeFactory;
    private final EventDispatcher eventDispatcher;

    private int initialOffset;
    private SequentialWriter<SAXEventBuffer> sequentialWriter;

    public PropertyValueWriter(String valueReference,
                               SAXWriter saxWriter,
                               CityGMLVersion version,
                               TransformerChainFactory transformerChainFactory,
                               NamespaceFilter namespaceFilter,
                               GeometryStripper geometryStripper,
                               boolean useSequentialWriting,
                               Object eventChannel,
                               Config config) throws DatatypeConfigurationException {
        this.valueReference = valueReference;
        this.saxWriter = saxWriter;
        this.transformerChainFactory = transformerChainFactory;
        this.namespaceFilter = namespaceFilter;
        this.geometryStripper = geometryStripper;
        this.useSequentialWriting = useSequentialWriting;
        this.eventChannel = eventChannel;

        cityGMLBuilder = ObjectRegistry.getInstance().getCityGMLBuilder();
        jaxbMarshaller = cityGMLBuilder.createJAXBMarshaller(version);
        xpathFactory = XPathFactory.newInstance();
        wfsFactory = new ObjectFactory();
        datatypeFactory = DatatypeFactory.newInstance();
        propertyCounts = new ConcurrentHashMap<>();

        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

        int queueSize = config.getExportConfig().getResources().getThreadPool().getMaxThreads() * 2;

        writerPool = new SingleWorkerPool<>(
                "citygml_writer_pool",
                new XMLWriterWorkerFactory(saxWriter, eventDispatcher),
                queueSize,
                false);

        writerPool.setEventSource(eventChannel);
        writerPool.prestartCoreWorkers();

        if (useSequentialWriting)
            sequentialWriter = new SequentialWriter<>(writerPool);
    }

    @Override
    public void writeHeader() throws FeatureWriteException {
        // nothing to do
    }

    @Override
    public void useIndentation(boolean useIndentation) {
        saxWriter.setIndentString(useIndentation ? " " : "");
    }

    @Override
    public Metadata getMetadata() {
        // we do not support metadata on the root element
        return null;
    }

    public void setPropertyCount(long sequenceId, int count) {
        propertyCounts.put(sequenceId, count);
    }

    private int getPropertyCount(long sequenceId) {
        Integer count = propertyCounts.remove(sequenceId);
        return count != null ? count : Integer.MAX_VALUE;
    }

    public void setInitialPropertyOffset(int initialOffset) {
        this.initialOffset = initialOffset;
    }

    public void startValueCollection(long matchNo, long returnNo, String previous, String next) throws FeatureWriteException {
        writeValueCollection(matchNo, returnNo, previous, next, WriteMode.HEAD);
    }

    public void endValueCollection() throws FeatureWriteException {
        writeValueCollection(0, 0, null, null, WriteMode.TAIL);
    }

    @Override
    public void write(AbstractFeature feature, long sequenceId) throws FeatureWriteException {
        // security feature: strip geometry from features
        if (geometryStripper != null)
            feature.accept(geometryStripper);

        // compile XPath expression
        XPathExpression xpathExpression;
        try {
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(namespaceFilter);
            xpathExpression = xpath.compile(valueReference);
        } catch (XPathExpressionException e) {
            throw new FeatureWriteException("Failed to build XPath expression from value reference", e);
        }

        // marshal feature to a DOM element
        Element element = jaxbMarshaller.marshalDOMElement(feature);
        if (element == null)
            throw new FeatureWriteException("Failed to unmarshall feature with gml:id '" + feature.getId() + "'.");

        try {
            SAXEventBuffer buffer = new SAXEventBuffer();
            NodeList nodeList = (NodeList) xpathExpression.evaluate(element, XPathConstants.NODESET);

            int maxPropertyCount = getPropertyCount(sequenceId);
            int index = sequenceId == 0 ? initialOffset : 0;
            int propertyCount = Math.min(index + maxPropertyCount, nodeList.getLength());

            for (; index < propertyCount; index++) {
                Node node = nodeList.item(index);
                if (!node.hasChildNodes())
                    throw new FeatureWriteException("Failed to get property value of node '" + new QName(node.getNamespaceURI(), node.getLocalName()) + "' for feature with gml:id '" + feature.getId() + "'.");

                Node value = node.getFirstChild();
                MemberPropertyType memberProperty = new MemberPropertyType();
                memberProperty.getContent().add(value.getNodeType() == Node.TEXT_NODE ? value.getTextContent() : value);
                JAXBElement<?> output = wfsFactory.createMember(memberProperty);

                Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

                if (transformerChainFactory == null)
                    marshaller.marshal(output, buffer);
                else {
                    TransformerChain chain = transformerChainFactory.buildChain();
                    chain.tail().setResult(new SAXResult(buffer));
                    chain.head().startDocument();
                    marshaller.marshal(output, chain.head());
                    chain.head().endDocument();
                }

                if (buffer.isEmpty())
                    throw new FeatureWriteException("Failed to write property value for feature with gml:id '" + feature.getId() + "'.");
            }

            if (!useSequentialWriting)
                writerPool.addWork(buffer);
            else {
                try {
                    sequentialWriter.write(buffer, sequenceId);
                } catch (InterruptedException e) {
                    throw new FeatureWriteException("Failed to write property value for feature with gml:id '" + feature.getId() + "'.", e);
                }
            }
        } catch (XPathExpressionException e) {
            throw new FeatureWriteException("Failed to evaluate the XPath expression of the value reference.", e);
        } catch (JAXBException | SAXException | TransformerConfigurationException e) {
            throw new FeatureWriteException("Failed to write property value for feature with gml:id '" + feature.getId() + "'.", e);
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

    public void writeTruncatedResponse(TruncatedResponse truncatedResponse) throws FeatureWriteException {
        try {
            SAXEventBuffer buffer = new SAXEventBuffer();
            Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
            marshaller.marshal(truncatedResponse, buffer);
            writerPool.addWork(buffer);
        } catch (JAXBException e) {
            throw new FeatureWriteException("Failed to marshal truncated response element.", e);
        }
    }

    public void close() throws FeatureWriteException {
        try {
            writerPool.shutdownAndWait();
        } catch (InterruptedException e) {
            throw new FeatureWriteException("Failed to close CityGML response writer.", e);
        } finally {
            if (!writerPool.isTerminated())
                writerPool.shutdownNow();

            eventDispatcher.removeEventHandler(this);
        }
    }

    private void writeValueCollection(long matchNo, long returnNo, String previous, String next, WriteMode writeMode) throws FeatureWriteException {
        try {
            ValueCollectionType valueCollection = new ValueCollectionType();

            if (writeMode == WriteMode.HEAD) {
                valueCollection.setTimeStamp(getTimeStamp());
                valueCollection.setNumberMatched(matchNo != Constants.UNKNOWN_NUMBER_MATCHED ? String.valueOf(matchNo) : "unknown");
                valueCollection.setNumberReturned(BigInteger.valueOf(returnNo));
                valueCollection.setPrevious(previous);
                valueCollection.setNext(next);
            }

            JAXBElement<?> output = wfsFactory.createValueCollection(valueCollection);

            SAXEventBuffer buffer = new SAXEventBuffer();
            SAXFragmentWriter fragmentWriter = new SAXFragmentWriter(new QName(Constants.WFS_NAMESPACE_URI, "ValueCollection"), buffer, writeMode);
            Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();

            if (transformerChainFactory == null)
                marshaller.marshal(output, fragmentWriter);
            else {
                TransformerChain chain = transformerChainFactory.buildChain();
                chain.tail().setResult(new SAXResult(fragmentWriter));
                marshaller.marshal(output, chain.head());
            }

            writerPool.addWork(buffer);
        } catch (JAXBException | TransformerConfigurationException e) {
            throw new FeatureWriteException("Failed to marshal value collection element.", e);
        }
    }

    private XMLGregorianCalendar getTimeStamp() {
        GregorianCalendar date = new GregorianCalendar();
        return datatypeFactory.newXMLGregorianCalendar(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED);
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        if (event.getChannel() == eventChannel && useSequentialWriting)
            sequentialWriter.interrupt();
    }
}
