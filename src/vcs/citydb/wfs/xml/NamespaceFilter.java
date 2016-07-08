/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2016
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
package vcs.citydb.wfs.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.citygml4j.model.module.gml.GMLCoreModule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import vcs.citydb.wfs.config.Constants;

public class NamespaceFilter extends XMLFilterImpl implements NamespaceContext {
	private HashMap<String, String> prefixToUri;
	private HashMap<String, Set<String>> uriToPrefix;	
	
	public NamespaceFilter() {
		this(null);
	}
	
	public NamespaceFilter(XMLReader reader) {
		super(reader);
		prefixToUri = new HashMap<String, String>();
		uriToPrefix = new HashMap<String, Set<String>>();
		
		bindNamespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
		bindNamespace(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
	}
	
	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if (uri == Constants.GML_3_2_1_NAMESPACE_URI || uri == Constants.GML_3_3_NAMESPACE_URI)
			uri = GMLCoreModule.v3_1_1.getNamespaceURI();
		
		super.startPrefixMapping(prefix, uri);
		bindNamespace(prefix, uri);
	}
	
	private void bindNamespace(String prefix, String uri) {
		prefixToUri.put(prefix, uri);
		if (uriToPrefix.get(uri) == null)
			uriToPrefix.put(uri, new HashSet<String>());
		
		uriToPrefix.get(uri).add(prefix);
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

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		if (namespaceURI == null)
			throw new IllegalArgumentException("namespace URI may not be null.");
		
		if (uriToPrefix.containsKey(namespaceURI))
			return uriToPrefix.get(namespaceURI).iterator();
		
		return Collections.EMPTY_SET.iterator();
	}

	public Iterator<String> getPrefixes() {
		return prefixToUri.keySet().iterator();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (uri == Constants.GML_3_2_1_NAMESPACE_URI || uri == Constants.GML_3_3_NAMESPACE_URI)
			uri = GMLCoreModule.v3_1_1.getNamespaceURI();

		super.startElement(uri, localName, qName, atts);
	}
	
}
