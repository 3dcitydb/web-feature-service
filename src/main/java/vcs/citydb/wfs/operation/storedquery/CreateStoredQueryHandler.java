package vcs.citydb.wfs.operation.storedquery;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import net.opengis.wfs._2.CreateStoredQueryResponseType;
import net.opengis.wfs._2.CreateStoredQueryType;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.StoredQueryDescriptionType;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.util.xml.SAXWriter;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.util.LoggerUtil;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CreateStoredQueryHandler {
	private final Logger log = Logger.getInstance();

	private final BaseRequestHandler baseRequestHandler;
	private final StoredQueryManager storedQueryManager;
	private final Marshaller marshaller;
	private final ObjectFactory wfsFactory;
	private final WFSConfig wfsConfig;

	public CreateStoredQueryHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) throws JAXBException {
		this.wfsConfig = wfsConfig;

		baseRequestHandler = new BaseRequestHandler(wfsConfig);
		storedQueryManager = ObjectRegistry.getInstance().lookup(StoredQueryManager.class);
		
		wfsFactory = storedQueryManager.getObjectFactory();
		marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
	}

	public void doOperation(CreateStoredQueryType wfsRequest,
			NamespaceFilter namespaceFilter,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		final String operationHandle = wfsRequest.getHandle();

		// check whether the create stored query operation is advertised
		if (!wfsConfig.getOperations().getManagedStoredQueries().isEnabled())
			throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "The CreateStoredQuery operation is not advertised.", operationHandle);

		log.info(LoggerUtil.getLogMessage(request, "Accepting CreateStoredQuery request."));

		// check base service parameters
		baseRequestHandler.validate(wfsRequest);

		// create stored queries
		for (StoredQueryDescriptionType description : wfsRequest.getStoredQueryDefinition())			
			storedQueryManager.createStoredQuery(description, namespaceFilter, operationHandle);
		
		final SAXWriter saxWriter = new SAXWriter();

		try {
			// generate response
			CreateStoredQueryResponseType createStoredQueryResponse = new CreateStoredQueryResponseType();
			createStoredQueryResponse.setStatus("OK");

			JAXBElement<CreateStoredQueryResponseType> responseElement = wfsFactory.createCreateStoredQueryResponse(createStoredQueryResponse);

			// write response
			response.setContentType("text/xml");
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());

			saxWriter.setWriteEncoding(true);
			saxWriter.setIndentString("  ");
			saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			saxWriter.setSchemaLocation(Constants.WFS_NAMESPACE_URI, Constants.WFS_SCHEMA_LOCATION);
			saxWriter.setOutput(response.getWriter());

			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
				public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
					return saxWriter.getPrefix(namespaceUri);
				}
			});
			
			marshaller.marshal(responseElement, saxWriter);

			log.info(LoggerUtil.getLogMessage(request, "CreateStoredQuery operation successfully finished."));
		} catch (JAXBException | IOException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal error occurred whilst marshalling the response document.", e);
		}

	}

}
