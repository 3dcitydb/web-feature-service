/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2017
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
package vcs.citydb.wfs.operation.storedquery;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ParameterExpressionType;
import net.opengis.wfs._2.ParameterType;
import net.opengis.wfs._2.QueryExpressionTextType;
import net.opengis.wfs._2.StoredQueryDescriptionType;
import net.opengis.wfs._2.StoredQueryType;
import net.opengis.wfs._2.Title;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.xml.NamespaceFilter;

public class StoredQuery {
	public final static String DEFAULT_LANGUAGE = "urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression";

	private final StoredQueryDescriptionType description;
	private final StoredQueryManager manager;
	private final NamespaceFilter namespaceFilter;

	StoredQuery(StoredQueryDescriptionType description, NamespaceFilter namespaceFilter, StoredQueryManager manager) {
		this.description = description;
		this.namespaceFilter = namespaceFilter;
		this.manager = manager;
	}

	public String getId() {
		return description.getId();
	}

	public List<Title> getTitle() {
		return description.getTitle();
	}

	public NamespaceFilter getNamespaceFilter() {
		return namespaceFilter;
	}

	public List<QName> getReturnFeatureTypeNames() {
		Set<QName> featureTypeNames = new HashSet<QName>();
		for (QueryExpressionTextType queryExpression : description.getQueryExpressionText())
			featureTypeNames.addAll(queryExpression.getReturnFeatureTypes());

		return new ArrayList<QName>(featureTypeNames);		
	}

	public StoredQueryDescriptionType getStoredQueryDescription() {
		return description;
	}

	public void validate(String handle) throws WFSException {
		// TODO: implement
	}

	public List<AbstractQueryExpressionType> compile(StoredQueryType storedQuery, NamespaceFilter namespaceFilter) throws WFSException {
		if (description.getQueryExpressionText().isEmpty())
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The stored query '" + getId() + "' does not specify any query expression texts.", storedQuery.getHandle());

		List<AbstractQueryExpressionType> queries = new ArrayList<AbstractQueryExpressionType>();
		try {
			// build parameter map
			HashMap<String, String> parameterMap = new HashMap<String, String>();
			for (ParameterType parameter : storedQuery.getParameter()) {

				// check whether parameter is declared
				ParameterExpressionType parameterExpression = null;
				for (ParameterExpressionType tmp : description.getParameter()) {
					if (tmp.getName().equals(parameter.getName())) {
						parameterExpression = tmp;
						break;
					}
				}

				if (parameterExpression == null)
					throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The paramter '" + parameter.getName() + "' is not declared for the stored query '" + storedQuery.getId() + "'.", storedQuery.getHandle());

				// convert parameter value to string
				StringBuilder value = new StringBuilder();
				for (Object content : parameter.getContent())
					value.append(toString(content));

				// TODO: we should do a further type mismatch check				
				parameterMap.put(parameter.getName(), value.toString());
			}

			// check for missing parameters
			if (description.getParameter().size() != parameterMap.size()) {
				for (ParameterExpressionType parameter : description.getParameter()) {
					if (!parameterMap.containsKey(parameter.getName()))
						throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The parameter '" + parameter.getName() + "' of the stored query lacks a value.", storedQuery.getHandle());
				}
			}

			// iterate over query expressions and replace tokens with parameter values
			for (QueryExpressionTextType expressionText : description.getQueryExpressionText()) {
				if (expressionText.getContent().isEmpty())
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The stored query '" + storedQuery.getId() + "' does not specify any query elements.", storedQuery.getHandle());

				StringBuilder template = new StringBuilder();

				// we need to embrace the query expression with a GetFeature request since
				// the expression text can contain multiple query elements.
				template.append('<').append(Constants.WFS_NAMESPACE_PREFIX).append(':').append("GetFeature xmlns:").append(Constants.WFS_NAMESPACE_PREFIX).append("=\"").append(Constants.WFS_NAMESPACE_URI).append("\">");

				// get string representation of query expression
				for (Object content : expressionText.getContent())
					template.append(toString(content));

				// replace tokens
				for (ParameterType parameter : storedQuery.getParameter()) {
					String name = parameter.getName();
					String token = "${" + name + "}";

					int index = template.indexOf(token);
					while (index > 0) {
						template.replace(index, index + token.length(), parameterMap.get(name));
						index = template.indexOf(token, index + token.length());
					}
				}

				// close GetFeature 
				template.append("</").append(Constants.WFS_NAMESPACE_PREFIX).append(':').append("GetFeature>");

				// unmarshal to GetFeature request
				Object result = unmarshal(template.toString());
				if (!(result instanceof JAXBElement<?>))
					throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to compile the stored query '" + storedQuery.getId() + "' after token replacement.", storedQuery.getHandle());

				JAXBElement<?> jaxbElement = (JAXBElement<?>)result;
				if (!(jaxbElement.getValue() instanceof GetFeatureType))
					throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "The query expression text of the stored query '" + storedQuery.getId() + "' cannot be compiled to a valid set of query elements.", storedQuery.getHandle());						

				GetFeatureType getFeature = (GetFeatureType)jaxbElement.getValue();
				for (JAXBElement<? extends AbstractQueryExpressionType> queryElement: getFeature.getAbstractQueryExpression())
					queries.add(queryElement.getValue());

				// finally propagate namespace bindings of the stored query
				Iterator<String> iter = this.namespaceFilter.getPrefixes();
				while (iter.hasNext()) {
					String prefix = iter.next();
					namespaceFilter.startPrefixMapping(prefix, this.namespaceFilter.getNamespaceURI(prefix));
				}
			}

		} catch (JAXBException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to compile the stored query '" + storedQuery.getId() + "'.", storedQuery.getHandle(), e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to compile the stored query '" + storedQuery.getId() + "'.", storedQuery.getHandle(), e);
		} catch (TransformerException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to compile the stored query '" + storedQuery.getId() + "'.", storedQuery.getHandle(), e);
		} catch (SAXException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to compile the stored query '" + storedQuery.getId() + "'.", storedQuery.getHandle(), e);
		}

		return queries;
	}

	private String toString(Object object) throws JAXBException, TransformerFactoryConfigurationError, TransformerException {
		if (object instanceof Element)
			return marshal((Element)object);
		else if (object instanceof JAXBElement<?>)
			return marshal((JAXBElement<?>)object);
		else
			return object.toString().trim();			
	}

	private String marshal(Element element) throws TransformerFactoryConfigurationError, TransformerException {
		Transformer transformer = manager.getTransformerFactory().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(element);
		transformer.transform(source, result);

		return result.getWriter().toString();
	}

	private String marshal(JAXBElement<?> element) throws JAXBException {
		StringWriter writer = new StringWriter();
		Marshaller marshaller = manager.getJAXBBuilder().getJAXBContext().createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		marshaller.marshal(element, writer);

		return writer.toString();
	}

	private Object unmarshal(String object) throws JAXBException {
		StringReader reader = new StringReader(object);
		Unmarshaller unmarshaller = manager.getJAXBBuilder().getJAXBContext().createUnmarshaller();

		return unmarshaller.unmarshal(reader);		
	}

}
