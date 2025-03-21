package vcs.citydb.wfs.util.xml;

import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Stack;

public class IndentingXMLStreamWriter implements XMLStreamWriter {
    private final static Object SEEN_NOTHING = new Object();
    private final static Object SEEN_ELEMENT = new Object();
    private final static Object SEEN_DATA = new Object();

    private final Stack<Object> stateStack = new Stack<>();
    private final XMLStreamWriter writer;
    private final NamespaceSupport namespaces = new NamespaceSupport();

    private Object state = SEEN_NOTHING;
    private String indentStep = "  ";
    private boolean writeFragment;
    private int depth = 0;

    public IndentingXMLStreamWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public void setIndentStep(String s) {
        this.indentStep = s;
    }

    public void setWriteFragment(boolean writeFragment) {
        this.writeFragment = writeFragment;
    }

    private void onStartElement() throws XMLStreamException {
        namespaces.pushContext();
        stateStack.push(SEEN_ELEMENT);
        state = SEEN_NOTHING;
        if (depth > 0) {
            writer.writeCharacters("\n");
        }
        doIndent();
        depth++;
    }

    private void onEndElement() throws XMLStreamException {
        depth--;
        if (state == SEEN_ELEMENT) {
            writer.writeCharacters("\n");
            doIndent();
        }
        state = stateStack.pop();
        namespaces.popContext();
    }

    private void onEmptyElement() throws XMLStreamException {
        state = SEEN_ELEMENT;
        if (depth > 0) {
            writer.writeCharacters("\n");
        }
        doIndent();
    }

    private void doIndent() throws XMLStreamException {
        if (depth > 0) {
            for (int i = 0; i < depth; i++)
                writer.writeCharacters(indentStep);
        }
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        if (!writeFragment)
            forceStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        if (!writeFragment)
            forceStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        if (!writeFragment)
            forceStartDocument(encoding, version);
    }

    public void forceStartDocument() throws XMLStreamException {
        writer.writeStartDocument();
        writer.writeCharacters("\n");
    }

    public void forceStartDocument(String version) throws XMLStreamException {
        writer.writeStartDocument(version);
        writer.writeCharacters("\n");
    }

    public void forceStartDocument(String encoding, String version) throws XMLStreamException {
        writer.writeStartDocument(encoding, version);
        writer.writeCharacters("\n");
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        onStartElement();
        writer.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        onStartElement();
        writer.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        onStartElement();
        writer.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        onEmptyElement();
        writer.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        onEmptyElement();
        writer.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        onEmptyElement();
        writer.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        onEndElement();
        writer.writeEndElement();
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        state = SEEN_DATA;
        writer.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        state = SEEN_DATA;
        writer.writeCharacters(text, start, len);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        state = SEEN_DATA;
        writer.writeCData(data);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        if (!writeFragment)
            forceEndDocument();
    }

    public void forceEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        if (!writeFragment) {
            writer.close();
        }
    }

    @Override
    public void flush() throws XMLStreamException {
        writer.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writer.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        writer.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writer.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if (shouldWriteNamespace(prefix, namespaceURI)) {
            writer.writeNamespace(prefix, namespaceURI);
        }
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        if (shouldWriteNamespace(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI)) {
            writer.writeDefaultNamespace(namespaceURI);
        }
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        writer.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        writer.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        writer.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        writer.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        writer.writeEntityRef(name);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return writer.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        writer.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        writer.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        writer.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return writer.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return writer.getProperty(name);
    }

    private boolean shouldWriteNamespace(String prefix, String namespaceURI) {
        if (!namespaceURI.equals(namespaces.getURI(prefix))) {
            namespaces.declarePrefix(prefix, namespaceURI);
            return true;
        } else {
            return false;
        }
    }
}
