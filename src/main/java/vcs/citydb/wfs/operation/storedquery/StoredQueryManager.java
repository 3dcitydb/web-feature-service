package vcs.citydb.wfs.operation.storedquery;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.fes._2.FilterType;
import net.opengis.fes._2.ResourceIdType;
import net.opengis.wfs._2.*;
import org.citydb.util.xml.SecureXMLProcessors;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.w3c.dom.Element;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class StoredQueryManager {
	private final String GET_FEATURE_BY_ID_NAME = "http://www.opengis.net/def/query/OGC-WFS/0/GetFeatureById";
	private final String DEPRECATED_GET_FEATURE_BY_ID_NAME = "urn:ogc:def:query:OGC-WFS::GetFeatureById";

	private final StoredQuery DEFAULT_QUERY;
	private final CityGMLBuilder cityGMLBuilder;
	private final SAXParserFactory saxParserFactory;
	private final TransformerFactory transformerFactory;
	private final DocumentBuilderFactory documentBuilderFactory;
	private final XMLOutputFactory xmlOutputFactory;
	private final Path storedQueriesPath;
	private final ObjectFactory wfsFactory;
	private final WFSConfig wfsConfig;
	private final MessageDigest md5;

	public StoredQueryManager(CityGMLBuilder cityGMLBuilder, SAXParserFactory saxParserFactory, String path, WFSConfig wfsConfig) throws ParserConfigurationException, SAXException, NoSuchAlgorithmException, IOException, TransformerConfigurationException {
		this.cityGMLBuilder = cityGMLBuilder;
		this.saxParserFactory = saxParserFactory;
		this.wfsConfig = wfsConfig;

		md5 = MessageDigest.getInstance("MD5");
		wfsFactory = new ObjectFactory();
		transformerFactory = SecureXMLProcessors.newTransformerFactory();
		xmlOutputFactory = XMLOutputFactory.newInstance();
		documentBuilderFactory = SecureXMLProcessors.newDocumentBuilderFactory();
		documentBuilderFactory.setNamespaceAware(true);

		storedQueriesPath = Paths.get(path);
		Files.createDirectories(storedQueriesPath);
		if (!Files.isDirectory(storedQueriesPath) || !Files.isReadable(storedQueriesPath))
			throw new IOException("Path for stored queries is not readable.");

		DEFAULT_QUERY = createDefaultStoredQuery();
	}

	public List<StoredQueryAdapter> listStoredQueries(String handle) throws WFSException {
		List<StoredQueryAdapter> storedQueries = new ArrayList<>();
		storedQueries.add(new StoredQueryAdapter(DEFAULT_QUERY.getId()));

		try {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(storedQueriesPath)) {
				for (Path file : stream) {
					if (Files.isRegularFile(file))
						storedQueries.add(new StoredQueryAdapter(file));
				}
			}
		} catch (IOException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to list stored queries.", handle, e);
		}

		return storedQueries;
	}

	public StoredQuery getStoredQuery(StoredQueryAdapter adapter, String handle) throws WFSException {
		// urn:ogc:def:query:OGC-WFS::GetFeatureById is deprecated since 2.0.2 but still supported
		if (GET_FEATURE_BY_ID_NAME.equals(adapter.getId()) || DEPRECATED_GET_FEATURE_BY_ID_NAME.equals(adapter.getId()))
			return DEFAULT_QUERY;

		Path file = adapter.isSetId() ? storedQueriesPath.resolve(getFileName(adapter.getId())) : adapter.getFile();
		if (!Files.exists(file))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "A stored query with identifier '" + adapter.getId() + "' is not offered by this server.", KVPConstants.STOREDQUERY_ID);

		return unmarshalStoredQuery(file, handle);
	}

	public Element parseStoredQuery(StoredQueryAdapter adapter, String handle) throws WFSException {
		// urn:ogc:def:query:OGC-WFS::GetFeatureById is deprecated since 2.0.2 but still supported
		if (GET_FEATURE_BY_ID_NAME.equals(adapter.getId()) || DEPRECATED_GET_FEATURE_BY_ID_NAME.equals(adapter.getId()))
			return DEFAULT_QUERY.toDOMElement(handle);

		Path file = adapter.isSetId() ? storedQueriesPath.resolve(getFileName(adapter.getId())) : adapter.getFile();
		if (!Files.exists(file))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "A stored query with identifier '" + adapter.getId() + "' is not offered by this server.", KVPConstants.STOREDQUERY_ID);

		return parseStoredQuery(file, handle);
	}

	public StoredQuery createStoredQuery(StoredQueryDescriptionType description, NamespaceFilter namespaceFilter, String handle) throws WFSException {		
		if (!description.isSetId())
			throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The stored query description lacks the mandatory identifier.", KVPConstants.STOREDQUERY_ID);

		// urn:ogc:def:query:OGC-WFS::GetFeatureById is deprecated since 2.0.2 but still supported
		if (GET_FEATURE_BY_ID_NAME.equals(description.getId()) || DEPRECATED_GET_FEATURE_BY_ID_NAME.equals(description.getId()))
			throw new WFSException(WFSExceptionCode.DUPLICATE_STORED_QUERY_ID_VALUE, "The identifier '" + description.getId() + "' is associated to the mandatory GetFeatureById query. Choose another identifier.", description.getId());

		// serialize stored query to file
		Path file = storedQueriesPath.resolve(getFileName(description.getId()));
		if (Files.exists(file))
			throw new WFSException(WFSExceptionCode.DUPLICATE_STORED_QUERY_ID_VALUE, "The identifier '" + description.getId() + "' has already been associated with to a stored query. Drop the stored query first or choose another identifier.", description.getId());

		StoredQuery storedQuery = new StoredQuery(description, namespaceFilter, this);
		storedQuery.validate(handle);

		try {
			DescribeStoredQueriesResponseType response = new DescribeStoredQueriesResponseType();
			response.getStoredQueryDescription().add(description);
			JAXBElement<DescribeStoredQueriesResponseType> jaxbElement = wfsFactory.createDescribeStoredQueriesResponse(response);

			Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(jaxbElement, file.toFile());
		} catch (JAXBException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to persist stored query.", handle, e);
		}

		return storedQuery;
	}
	
	public void dropStoredQuery(String id, String handle) throws WFSException {
		if (GET_FEATURE_BY_ID_NAME.equals(id) || DEPRECATED_GET_FEATURE_BY_ID_NAME.equals(id))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The mandatory stored query '" + id + "' must not be dropped.", handle);
		
		Path file = storedQueriesPath.resolve(getFileName(id));
		if (!Files.exists(file))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A stored query with identifier '" + id + "' is not offered by this server.", handle);

		try {
			Files.delete(file);
		} catch (IOException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to drop the stored query with identifier '" + id + "'.", handle);
		}
	}

	public void compileQuery(AbstractQueryExpressionType abstractQuery, List<QueryType> queries, NamespaceFilter namespaceFilter, String handle) throws WFSException {
		if (abstractQuery instanceof QueryType) {
			if (!wfsConfig.getConstraints().isSupportAdHocQueries())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Ad hoc queries are not advertised.", handle);
			
			queries.add((QueryType)abstractQuery);
		} 

		else if (abstractQuery instanceof StoredQueryType) {
			StoredQueryType query = (StoredQueryType)abstractQuery;

			StoredQuery storedQuery = getStoredQuery(new StoredQueryAdapter(query.getId()), handle);
			if (storedQuery != null) {
				if (storedQuery.getId().equals(DEFAULT_QUERY.getId())) {
					QueryType queryType = (QueryType) storedQuery.compile(query, namespaceFilter).iterator().next();

					if (queryType.isSetAbstractSelectionClause() && queryType.getAbstractSelectionClause().getValue() instanceof FilterType) {
						FilterType filter = (FilterType) queryType.getAbstractSelectionClause().getValue();
						if (filter.isSet_Id() && filter.get_Id().get(0).getValue() instanceof ResourceIdType) {
							ResourceIdType resourceId = (ResourceIdType) filter.get_Id().get(0).getValue();
							queryType.setFeatureIdentifier(resourceId.getRid());
						}
					}

					if (!queryType.isSetFeatureIdentifier())
						throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Lacking identifier for the '" + DEFAULT_QUERY.getId() + "' stored query.", handle);

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

	public boolean containsStoredQuery(String id, String handle) {
		// urn:ogc:def:query:OGC-WFS::GetFeatureById is deprecated since 2.0.2 but still supported
		return GET_FEATURE_BY_ID_NAME.equals(id)
				|| DEPRECATED_GET_FEATURE_BY_ID_NAME.equals(id)
				|| Files.exists(storedQueriesPath.resolve(getFileName(id)));
	}

	private StoredQuery unmarshalStoredQuery(Path file, String handle) throws WFSException {
		if (!Files.isReadable(file))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to read the stored query file.", handle);

		Object object;
		NamespaceFilter namespaceFilter;

		try {
			Unmarshaller unmarshaller = cityGMLBuilder.getJAXBContext().createUnmarshaller();
			UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();			

			// use SAX parser to keep track of namespace declarations
			SAXParser parser = saxParserFactory.newSAXParser();			
			XMLReader reader = parser.getXMLReader();
			namespaceFilter = new NamespaceFilter(reader);
			namespaceFilter.setContentHandler(unmarshallerHandler);

			namespaceFilter.parse(new InputSource(new BufferedReader(new FileReader(file.toFile()))));
			object = unmarshallerHandler.getResult();

		} catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Fatal JAXB error whilst processing the stored query.", handle, e);
		}

		if (!(object instanceof JAXBElement<?>))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the stored query file.", handle);

		JAXBElement<?> jaxbElement = (JAXBElement<?>)object;
		if (!(jaxbElement.getValue() instanceof DescribeStoredQueriesResponseType))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Invalid content of the stored query file.", handle);

		DescribeStoredQueriesResponseType response = (DescribeStoredQueriesResponseType)jaxbElement.getValue();
		if (response.getStoredQueryDescription().size() == 0)
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to parse the stored query file. No stored query description provided.", handle);

		StoredQueryDescriptionType description = response.getStoredQueryDescription().get(0);
		if (!file.getFileName().toString().equals(getFileName(description.getId())))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The stored query identifier '" + description.getId() + "' does not match the storage identifier.", handle);

		return new StoredQuery(description, namespaceFilter, this);
	}

	private Element parseStoredQuery(Path file, String handle) throws WFSException {
		if (!Files.isReadable(file))
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to read the stored query file.", handle);

		try {
			Document document = documentBuilderFactory.newDocumentBuilder().parse(file.toFile());
			return processStoredQueryElement(document.getDocumentElement(), handle);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Fatal error whilst processing the stored query.", handle, e);
		}
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

	private String getFileName(String id) {
		byte[] md5Hash = md5.digest(id.getBytes());

		StringBuilder hexString = new StringBuilder();
		for (byte item : md5Hash)
			hexString.append(Integer.toString((item & 0xff) + 0x100, 16).substring(1));

		return hexString.append(".xml").toString();
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
