package vcs.citydb.wfs.operation.storedquery;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.wfs._2.DescribeStoredQueriesResponseType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ParameterExpressionType;
import net.opengis.wfs._2.ParameterType;
import net.opengis.wfs._2.QueryExpressionTextType;
import net.opengis.wfs._2.StoredQueryDescriptionType;
import net.opengis.wfs._2.StoredQueryType;
import net.opengis.wfs._2.Title;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Set<QName> featureTypeNames = new HashSet<>();
		for (QueryExpressionTextType queryExpression : description.getQueryExpressionText())
			featureTypeNames.addAll(queryExpression.getReturnFeatureTypes());

		return new ArrayList<>(featureTypeNames);
	}

	public StoredQueryDescriptionType getStoredQueryDescription() {
		return description;
	}

	public void validate(String handle) throws WFSException {
		if (!description.isSetId())
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The stored query description lacks the mandatory identifier.", KVPConstants.STOREDQUERY_ID);

		Matcher tokenMatcher = Pattern.compile("\\$\\{(.*?)\\}", Pattern.UNICODE_CHARACTER_CLASS).matcher("");

		try {
			Set<String> parameterNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			for (ParameterExpressionType parameterExpression : description.getParameter()) {
				// check for duplicate parameter names
				String parameterName = parameterExpression.getName();
				if (parameterNames.contains(parameterName))
					throw new WFSException(WFSExceptionCode.DUPLICATE_STORED_QUERY_PARAMETER_VALUE, "The parameter name '" + parameterName + "' is already being used within the same stored query.", parameterName);

				// check for collision with predefined WFS KVP keywords
				if (KVPConstants.PARAMETERS.contains(parameterName.toUpperCase()))
					throw new WFSException(WFSExceptionCode.DUPLICATE_STORED_QUERY_PARAMETER_VALUE, "The parameter name '" + parameterName + "' collides with a predefined WFS keyword. Choose another parameter name.", parameterName);
				
				parameterNames.add(parameterName);
			}

			if (description.getQueryExpressionText().isEmpty())
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The stored query description lacks a mandatory query expressions.", handle);
			
			for (QueryExpressionTextType expressionText : description.getQueryExpressionText()) {
				// check whether the requested feature type is advertised
				for (QName featureType : expressionText.getReturnFeatureTypes()) {
					if (!manager.getWFSConfig().getFeatureTypes().contains(featureType))
						throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The feature type '" + featureType.toString() + "' is not advertised.", handle);
				}

				// check language
				if (!DEFAULT_LANGUAGE.equals(expressionText.getLanguage()))
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Only the language '" + DEFAULT_LANGUAGE + "' is supported for query expressions.", handle);

				// get string representation of query expression
				StringBuilder value = new StringBuilder();
				for (Object content : expressionText.getContent())
					value.append(toString(content));
				
				// check whether declared tokens are used
				for (String parameterName : parameterNames) {
					String token = "${" + parameterName + "}";
					if (value.indexOf(token) < 0)
						throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The parameter '" + parameterName + "' is never being used within the query expression.", handle);
				}
				
				// check whether no undeclared token is used
				tokenMatcher.reset(value);
				while (tokenMatcher.find()) {
					String parameterName = tokenMatcher.group(1);
					if (!parameterNames.contains(parameterName))
						throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The parameter '" + parameterName + "' is being used within the query expression but is not declared.", handle);
				}

			}
		} catch (JAXBException | TransformerFactoryConfigurationError | TransformerException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to validate the stored query '" + description.getId() + "'.", handle, e);
		}

	}

	public List<AbstractQueryExpressionType> compile(StoredQueryType storedQuery, NamespaceFilter namespaceFilter) throws WFSException {
		if (description.getQueryExpressionText().isEmpty())
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The stored query '" + getId() + "' does not specify any query expression texts.", storedQuery.getHandle());

		List<AbstractQueryExpressionType> queries = new ArrayList<>();
		try {
			// build parameter map
			HashMap<String, String> parameterMap = new HashMap<>();
			for (ParameterType parameter : storedQuery.getParameter()) {

				// check whether parameter is declared
				ParameterExpressionType parameterExpression = null;
				for (ParameterExpressionType tmp : description.getParameter()) {
					if (tmp.getName().equalsIgnoreCase(parameter.getName())) {
						parameterExpression = tmp;
						break;
					}
				}

				if (parameterExpression == null)
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The parameter '" + parameter.getName() + "' is not declared for the stored query '" + storedQuery.getId() + "'.", storedQuery.getHandle());

				// convert parameter value to string
				StringBuilder value = new StringBuilder();
				for (Object content : parameter.getContent())
					value.append(toString(content));

				// TODO: we should do a further type mismatch check				
				parameterMap.put(parameterExpression.getName(), value.toString());
			}
			
			// check for missing parameters
			if (description.getParameter().size() != parameterMap.size()) {
				for (ParameterExpressionType parameter : description.getParameter()) {
					if (!parameterMap.containsKey(parameter.getName()))
						throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The parameter '" + parameter.getName() + "' of the stored query lacks a value.", parameter.getName());
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
				for (Entry<String, String> entry : parameterMap.entrySet()) {
					String token = "${" + entry.getKey() + "}";

					int index = template.indexOf(token);
					while (index > 0) {
						template.replace(index, index + token.length(), entry.getValue());
						index = template.indexOf(token, index + token.length());
					}
				}

				// close GetFeature 
				template.append("</").append(Constants.WFS_NAMESPACE_PREFIX).append(':').append("GetFeature>");

				// unmarshal to GetFeature request
				Object result = unmarshal(template.toString());
				if (!(result instanceof JAXBElement<?>))
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to compile the stored query '" + storedQuery.getId() + "' after token replacement.", storedQuery.getHandle());

				JAXBElement<?> jaxbElement = (JAXBElement<?>)result;
				if (!(jaxbElement.getValue() instanceof GetFeatureType))
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The query expression text of the stored query '" + storedQuery.getId() + "' cannot be compiled to a valid set of query elements.", storedQuery.getHandle());

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

		} catch (JAXBException | TransformerFactoryConfigurationError | TransformerException | SAXException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to compile the stored query '" + storedQuery.getId() + "'.", storedQuery.getHandle(), e);
		} 

		return queries;
	}

	protected Element toDOMElement(String handle) throws WFSException {
		try {
			Document document = manager.getDocumentBuilderFactory().newDocumentBuilder().newDocument();

			DescribeStoredQueriesResponseType response = new DescribeStoredQueriesResponseType();
			response.getStoredQueryDescription().add(description);
			JAXBElement<DescribeStoredQueriesResponseType> jaxbElement = manager.getObjectFactory().createDescribeStoredQueriesResponse(response);

			Marshaller marshaller = manager.getCityGMLBuilder().getJAXBContext().createMarshaller();
			marshaller.marshal(jaxbElement, document);

			return manager.processStoredQueryElement(document.getDocumentElement(), handle);
		} catch (ParserConfigurationException | JAXBException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to compile the stored query '" + getId() + "'.", handle, e);
		}
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
		Marshaller marshaller = manager.getCityGMLBuilder().getJAXBContext().createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		marshaller.marshal(element, writer);

		return writer.toString();
	}

	private Object unmarshal(String object) throws JAXBException {
		StringReader reader = new StringReader(object);
		Unmarshaller unmarshaller = manager.getCityGMLBuilder().getJAXBContext().createUnmarshaller();

		return unmarshaller.unmarshal(reader);		
	}

}
