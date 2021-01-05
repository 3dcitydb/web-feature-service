package vcs.citydb.wfs.operation.storedquery;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.wfs._2.Abstract;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.ParameterExpressionType;
import net.opengis.wfs._2.QueryExpressionTextType;
import net.opengis.wfs._2.QueryType;
import net.opengis.wfs._2.StoredQueryDescriptionType;
import net.opengis.wfs._2.StoredQueryType;
import net.opengis.wfs._2.Title;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class StoredQueryManager {
	private final String GET_FEATURE_BY_ID_NAME = "http://www.opengis.net/def/query/OGC-WFS/0/GetFeatureById";
	private final String DEPRECATED_GET_FEATURE_BY_ID_NAME = "urn:ogc:def:query:OGC-WFS::GetFeatureById";

	private final StoredQuery DEFAULT_QUERY;
	private final CityGMLBuilder cityGMLBuilder;
	private final TransformerFactory transformerFactory;
	private final DocumentBuilderFactory documentBuilderFactory;
	private final XMLOutputFactory xmlOutputFactory;
	private final ObjectFactory wfsFactory;
	private final WFSConfig wfsConfig;

	public StoredQueryManager(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) throws ParserConfigurationException, SAXException, NoSuchAlgorithmException, IOException {
		this.cityGMLBuilder = cityGMLBuilder;
		this.wfsConfig = wfsConfig;

		wfsFactory = new ObjectFactory();
		transformerFactory = TransformerFactory.newInstance();
		xmlOutputFactory = XMLOutputFactory.newInstance();
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);

		DEFAULT_QUERY = createDefaultStoredQuery();
	}

	public List<StoredQueryAdapter> listStoredQueries(String handle) throws WFSException {
		List<StoredQueryAdapter> storedQueries = new ArrayList<>();
		storedQueries.add(new StoredQueryAdapter(DEFAULT_QUERY.getId()));

		return storedQueries;
	}

	public StoredQuery getStoredQuery(StoredQueryAdapter adapter, String handle) throws WFSException {
		// urn:ogc:def:query:OGC-WFS::GetFeatureById is deprecated since 2.0.2 but still supported
		if (GET_FEATURE_BY_ID_NAME.equals(adapter.getId()) || DEPRECATED_GET_FEATURE_BY_ID_NAME.equals(adapter.getId()))
			return DEFAULT_QUERY;

		throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "A stored query with identifier '" + adapter.getId() + "' is not offered by this server.", handle);
	}

	public Element parseStoredQuery(StoredQueryAdapter adapter, String handle) throws WFSException {
		// urn:ogc:def:query:OGC-WFS::GetFeatureById is deprecated since 2.0.2 but still supported
		if (GET_FEATURE_BY_ID_NAME.equals(adapter.getId()) || DEPRECATED_GET_FEATURE_BY_ID_NAME.equals(adapter.getId()))
			return DEFAULT_QUERY.toDOMElement(handle);

		throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "A stored query with identifier '" + adapter.getId() + "' is not offered by this server.", handle);
	}

	public void compileQuery(AbstractQueryExpressionType abstractQuery, List<QueryType> queries, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		if (abstractQuery instanceof QueryType)
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Ad hoc queries are not advertised.", handle);

		else if (abstractQuery instanceof StoredQueryType) {
			StoredQueryType query = (StoredQueryType)abstractQuery;

			StoredQuery storedQuery = getStoredQuery(new StoredQueryAdapter(query.getId()), handle);
			if (storedQuery != null) {
				if (storedQuery.getId().equals(DEFAULT_QUERY.getId())) {
					QueryType queryType = (QueryType)storedQuery.compile(query, namespaceFilter).iterator().next();
					queryType.setIsGetFeatureById(true);
					queries.add(queryType);
				} else {
					for (AbstractQueryExpressionType compiled : storedQuery.compile(query, namespaceFilter))
						compileQuery(compiled, queries, namespaceFilter, handle);
				}
			} else
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "No stored query with identifier '" + query.getId() + "' is offered by this server.", handle);
		} 

		else
			throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only ad hoc and stored query expressions are supported.", handle);
	}

	protected Element processStoredQueryElement(Element root, String handle) throws WFSException {
		try {
			NodeList nodeList = root.getElementsByTagNameNS(Constants.WFS_NAMESPACE_URI, "StoredQueryDescription");
			if (nodeList.getLength() == 0 || nodeList.getLength() > 1)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the stored query file. No stored query description provided.", handle);

			Element description = (Element)nodeList.item(0);

			// copy namespace attributes from root element
			NamedNodeMap attributes = root.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Attr attribute = (Attr)attributes.item(i);
				if (attribute.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
					if (attribute.getValue().equals("http://www.w3.org/2001/SMIL20/") 
							|| attribute.getValue().equals("http://www.w3.org/2001/SMIL20/Language"))
						continue;

					description.setAttributeNS(attribute.getNamespaceURI(), attribute.getName(), attribute.getValue());
				}
			}

			// remove empty text nodes
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");  
			NodeList emptyNodeList = (NodeList)xpathExp.evaluate(root, XPathConstants.NODESET);
			for (int i = 0; i < emptyNodeList.getLength(); i++) {
				Node emptyNode = emptyNodeList.item(i);
				emptyNode.getParentNode().removeChild(emptyNode);
			}

			return description;
		} catch (XPathExpressionException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Fatal error whilst processing the stored query.", handle, e);
		}
	}

	protected CityGMLBuilder getCityGMLBuilder() {
		return cityGMLBuilder;
	}

	protected TransformerFactory getTransformerFactory() {
		return transformerFactory;
	}

	protected DocumentBuilderFactory getDocumentBuilderFactory() {
		return documentBuilderFactory;
	}
	
	protected XMLOutputFactory getXMLOutputFactory() {
		return xmlOutputFactory;
	}

	protected ObjectFactory getObjectFactory() {
		return wfsFactory;
	}
	
	protected WFSConfig getWFSConfig() {
		return wfsConfig;
	}

	private StoredQuery createDefaultStoredQuery() throws ParserConfigurationException, SAXException {
		// GetFeatureById query according to the WFS 2.0 spec
		StoredQueryDescriptionType description = new StoredQueryDescriptionType();

		description.setId(!Constants.DEFAULT_WFS_VERSION.equals("2.0.0") ?
				GET_FEATURE_BY_ID_NAME : DEPRECATED_GET_FEATURE_BY_ID_NAME);

		Title queryTitle = new Title();
		queryTitle.setLang("en");
		queryTitle.setValue("Get feature by identifier");
		description.getTitle().add(queryTitle);
		Abstract queryAbstract = new Abstract();
		queryAbstract.setLang("en");
		queryAbstract.setValue("Retrieves a feature by its gml:id.");
		description.getAbstract().add(queryAbstract);

		ParameterExpressionType parameter = new ParameterExpressionType();
		parameter.setName("id");
		parameter.setType(XSDataType.XS_STRING.getName());
		Title parameterTitle = new Title();
		parameterTitle.setLang("en");
		parameterTitle.setValue("Identifier");
		parameter.getTitle().add(parameterTitle);
		Abstract parameterAbstract = new Abstract();
		parameterAbstract.setLang("en");
		parameterAbstract.setValue("The gml:id of the feature to be retrieved.");
		parameter.getAbstract().add(parameterAbstract);
		description.getParameter().add(parameter);

		Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
		Element query = document.createElementNS(Constants.WFS_NAMESPACE_URI, Constants.WFS_NAMESPACE_PREFIX + ":Query");
		query.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);

		NamespaceFilter namespaceFilter = new NamespaceFilter();
		CityGMLVersion version = wfsConfig.getFeatureTypes().getDefaultVersion();
		boolean multipleVersions = wfsConfig.getFeatureTypes().getVersions().size() > 1;
		CityGMLModule module = version.getCityGMLModule(CityGMLModuleType.CORE);		
		String prefix = module.getNamespacePrefix();
		if (multipleVersions)
			prefix += (version == CityGMLVersion.v2_0_0) ? "2" : "1";

		namespaceFilter.startPrefixMapping(prefix, module.getNamespaceURI());
		namespaceFilter.startPrefixMapping("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		query.setAttribute("typeNames", "schema-element(" + prefix + ':' + "_CityObject)");
		query.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + prefix, module.getNamespaceURI());

		Element filter = document.createElementNS(Constants.FES_NAMESPACE_URI, Constants.FES_NAMESPACE_PREFIX + ":Filter");
		filter.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + Constants.FES_NAMESPACE_PREFIX, Constants.FES_NAMESPACE_URI);
		Element resourceId = document.createElementNS(Constants.FES_NAMESPACE_URI, Constants.FES_NAMESPACE_PREFIX + ":ResourceId");
		resourceId.setAttribute("rid", "${id}");
		filter.appendChild(resourceId);
		query.appendChild(filter);

		QueryExpressionTextType queryExpression = new QueryExpressionTextType();
		queryExpression.getContent().add(query);		
		queryExpression.setIsPrivate(false);
		queryExpression.setLanguage("en");
		queryExpression.setReturnFeatureTypes(new ArrayList<>());
		queryExpression.setLanguage(StoredQuery.DEFAULT_LANGUAGE);
		description.getQueryExpressionText().add(queryExpression);

		return new StoredQuery(description, namespaceFilter, this);
	}

}
