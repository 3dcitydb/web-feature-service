package vcs.citydb.wfs.xml;

import org.citygml4j.model.module.gml.GMLCoreModule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import vcs.citydb.wfs.config.Constants;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.*;

public class NamespaceFilter extends XMLFilterImpl implements NamespaceContext {
	private HashMap<String, String> prefixToUri;
	private HashMap<String, Set<String>> uriToPrefix;	
	
	public NamespaceFilter() {
		this(null);
	}
	
	public NamespaceFilter(XMLReader reader) {
		super(reader);
		prefixToUri = new HashMap<>();
		uriToPrefix = new HashMap<>();
		
		bindNamespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
		bindNamespace(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
	}
	
	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if (Constants.GML_3_2_1_NAMESPACE_URI.equals(uri) || Constants.GML_3_3_NAMESPACE_URI.equals(uri))
			uri = GMLCoreModule.v3_1_1.getNamespaceURI();
		
		super.startPrefixMapping(prefix, uri);
		bindNamespace(prefix, uri);
	}
	
	private void bindNamespace(String prefix, String uri) {
		prefixToUri.put(prefix, uri);
		uriToPrefix.computeIfAbsent(uri, k -> new HashSet<>()).add(prefix);
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix == null)
			throw new IllegalArgumentException("namespace prefix may not be null.");

		return prefixToUri.get(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null)
			throw new IllegalArgumentException("namespace URI may not be null.");
		
		if (uriToPrefix.containsKey(namespaceURI))
			return uriToPrefix.get(namespaceURI).iterator().next();
		
		return null;
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		if (namespaceURI == null)
			throw new IllegalArgumentException("namespace URI may not be null.");
		
		if (uriToPrefix.containsKey(namespaceURI))
			return uriToPrefix.get(namespaceURI).iterator();
		
		return Collections.emptyIterator();
	}

	public Iterator<String> getPrefixes() {
		return prefixToUri.keySet().iterator();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (Constants.GML_3_2_1_NAMESPACE_URI.equals(uri) || Constants.GML_3_3_NAMESPACE_URI.equals(uri))
			uri = GMLCoreModule.v3_1_1.getNamespaceURI();

		super.startElement(uri, localName, qName, atts);
	}
	
}
