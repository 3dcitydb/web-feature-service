package vcs.citydb.wfs.operation.storedquery;

import net.opengis.wfs._2.DescribeStoredQueriesType;
import org.citydb.log.Logger;
import org.citydb.registry.ObjectRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.util.LoggerUtil;
import vcs.citydb.wfs.xml.IndentingXMLStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DescribeStoredQueriesHandler {
	private final Logger log = Logger.getInstance();

	private final BaseRequestHandler baseRequestHandler;
	private final StoredQueryManager storedQueryManager;

	public DescribeStoredQueriesHandler(WFSConfig wfsConfig) throws JAXBException {
		baseRequestHandler = new BaseRequestHandler(wfsConfig);
		storedQueryManager = ObjectRegistry.getInstance().lookup(StoredQueryManager.class);
	}

	public void doOperation(DescribeStoredQueriesType wfsRequest,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		log.info(LoggerUtil.getLogMessage(request, "Accepting DescribeStoredQueries request."));
		final String operationHandle = wfsRequest.getHandle();

		// check base service parameters
		baseRequestHandler.validate(wfsRequest);

		// get stored queries to be described
		List<StoredQueryAdapter> adapters;
		if (wfsRequest.isSetStoredQueryId()) {
			adapters = new ArrayList<>();
			for (String id : new HashSet<>(wfsRequest.getStoredQueryId())) {
				if (!storedQueryManager.containsStoredQuery(id, operationHandle)) {
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A stored query with identifier '" + id + "' is not offered by this server.", operationHandle);
				}

				adapters.add(new StoredQueryAdapter(id));
			}
		} else {
			adapters = storedQueryManager.listStoredQueries(operationHandle);
		}

		try {
			// generate and write response
			response.setContentType("text/xml");
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			
			XMLOutputFactory out = storedQueryManager.getXMLOutputFactory();
			IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(out.createXMLStreamWriter(response.getWriter()));
			writer.setIndentStep("  ");
			writer.setWriteFragment(true);
			
			writer.forceStartDocument(StandardCharsets.UTF_8.name(), "1.0");
			writer.writeStartElement(Constants.WFS_NAMESPACE_PREFIX, "DescribeStoredQueriesResponse", Constants.WFS_NAMESPACE_URI);
			writer.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			writer.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			writer.writeNamespace(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			writer.writeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation", Constants.WFS_NAMESPACE_URI + " " + Constants.WFS_SCHEMA_LOCATION);
			
			Transformer transformer = storedQueryManager.getTransformerFactory().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			
			for (StoredQueryAdapter adapter : adapters) {
				Element storedQuery = storedQueryManager.parseStoredQuery(adapter, operationHandle);
				
				// remove private query expressions
				NodeList nodeList = storedQuery.getElementsByTagNameNS(Constants.WFS_NAMESPACE_URI, "QueryExpressionText");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Element queryExpression = (Element)nodeList.item(i);
					String isPrivate = queryExpression.getAttribute("isPrivate");
					if (isPrivate.toLowerCase().equals("true")) {
						while (queryExpression.getFirstChild() != null)
							queryExpression.removeChild(queryExpression.getFirstChild());
					}
				}
				
				// write stored query
				transformer.transform(new DOMSource(storedQuery), new StAXResult(writer));
			}
			
			writer.writeEndElement();
			writer.forceEndDocument();

			// flush XML writer
			writer.flush();
			
			log.info(LoggerUtil.getLogMessage(request, "DescribeStoredQueries operation successfully finished."));
		} catch (IOException | XMLStreamException | TransformerException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal DOM error occurred whilst marshalling the response document.", e);
		}
	}

}
