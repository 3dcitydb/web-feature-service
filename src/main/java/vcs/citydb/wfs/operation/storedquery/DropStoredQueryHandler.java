package vcs.citydb.wfs.operation.storedquery;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import net.opengis.wfs._2.DropStoredQueryType;
import net.opengis.wfs._2.ExecutionStatusType;
import net.opengis.wfs._2.ObjectFactory;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DropStoredQueryHandler {
    private final Logger log = Logger.getInstance();

    private final BaseRequestHandler baseRequestHandler;
    private final StoredQueryManager storedQueryManager;
    private final Marshaller marshaller;
    private final ObjectFactory wfsFactory;
    private final WFSConfig wfsConfig;

    public DropStoredQueryHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) throws JAXBException {
        this.wfsConfig = wfsConfig;

        baseRequestHandler = new BaseRequestHandler(wfsConfig);
        storedQueryManager = ObjectRegistry.getInstance().lookup(StoredQueryManager.class);

        wfsFactory = storedQueryManager.getObjectFactory();
        marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
    }

    public void doOperation(DropStoredQueryType wfsRequest,
                            HttpServletRequest request,
                            HttpServletResponse response) throws WFSException {

        final String operationHandle = wfsRequest.getHandle();

        // check whether the create stored query operation is advertised
        if (!wfsConfig.getOperations().getManagedStoredQueries().isEnabled())
            throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "The DropStoredQuery operation is not advertised.", operationHandle);

        log.info(LoggerUtil.getLogMessage(request, "Accepting CreateStoredQuery request."));

        // check base service parameters
        baseRequestHandler.validate(wfsRequest);

        // check for stored query id to be present
        if (!wfsRequest.isSetId())
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The operation requires a stored query id.", operationHandle);

        // drop stored query
        storedQueryManager.dropStoredQuery(wfsRequest.getId(), operationHandle);

        final SAXWriter saxWriter = new SAXWriter();

        try {
            // generate response
            ExecutionStatusType status = new ExecutionStatusType();
            status.setStatus("OK");

            JAXBElement<ExecutionStatusType> responseElement = wfsFactory.createDropStoredQueryResponse(status);

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

            log.info(LoggerUtil.getLogMessage(request, "DropStoredQuery operation successfully finished."));
        } catch (JAXBException | IOException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal error occurred whilst marshalling the response document.", e);
        }

    }

}
