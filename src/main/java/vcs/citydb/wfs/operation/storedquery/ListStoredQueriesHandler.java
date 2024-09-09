package vcs.citydb.wfs.operation.storedquery;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import net.opengis.wfs._2.ListStoredQueriesResponseType;
import net.opengis.wfs._2.ListStoredQueriesType;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.StoredQueryListItemType;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleVersion;
import org.citygml4j.util.xml.SAXWriter;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.feature.FeatureType;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.util.LoggerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ListStoredQueriesHandler {
    private final Logger log = Logger.getInstance();
    private final WFSConfig wfsConfig;

    private final BaseRequestHandler baseRequestHandler;
    private final StoredQueryManager storedQueryManager;
    private final Marshaller marshaller;
    private final ObjectFactory wfsFactory;

    public ListStoredQueriesHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) throws JAXBException {
        this.wfsConfig = wfsConfig;

        baseRequestHandler = new BaseRequestHandler(wfsConfig);
        storedQueryManager = ObjectRegistry.getInstance().lookup(StoredQueryManager.class);
        wfsFactory = storedQueryManager.getObjectFactory();
        marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
    }

    public void doOperation(ListStoredQueriesType wfsRequest,
                            HttpServletRequest request,
                            HttpServletResponse response) throws WFSException {

        log.info(LoggerUtil.getLogMessage(request, "Accepting ListStoredQueries request."));
        final String operationHandle = wfsRequest.getHandle();

        // check base service parameters
        baseRequestHandler.validate(wfsRequest);

        // get stored queries offered by server
        List<StoredQueryAdapter> adapters = storedQueryManager.listStoredQueries(operationHandle);

        final SAXWriter saxWriter = new SAXWriter();

        try {
            // generate stored queries list
            ListStoredQueriesResponseType listStoredQueriesResponse = new ListStoredQueriesResponseType();
            for (StoredQueryAdapter adapter : adapters) {
                StoredQuery storedQuery = storedQueryManager.getStoredQuery(adapter, operationHandle);
                StoredQueryListItemType listItem = new StoredQueryListItemType();
                listItem.setId(storedQuery.getId());
                listItem.setTitle(storedQuery.getTitle());

                List<QName> returnTypeNames = storedQuery.getReturnFeatureTypeNames();
                if (returnTypeNames.isEmpty() && wfsRequest.getVersion().equals("2.0.0")) {
                    for (FeatureType featureType : wfsConfig.getFeatureTypes().getAdvertisedFeatureTypes())
                        returnTypeNames.add(featureType.getName());
                }

                listItem.setReturnFeatureType(returnTypeNames);

                // add namespace declarations
                int i = 1;
                for (QName featureType : returnTypeNames) {
                    String prefix;

                    Module module = Modules.getModule(featureType.getNamespaceURI());
                    if (module != null) {
                        prefix = module.getNamespacePrefix();
                        if (module instanceof CityGMLModule) {
                            CityGMLModule cityGMLModule = (CityGMLModule) module;
                            prefix += cityGMLModule.getVersion() == CityGMLModuleVersion.v2_0_0 ? "2" : "1";
                        }
                    } else
                        prefix = storedQuery.getNamespaceFilter().getPrefix(featureType.getNamespaceURI());

                    if (prefix != null) {
                        String uri = saxWriter.getNamespaceURI(prefix);
                        if (uri != null && !uri.equals(featureType.getNamespaceURI()))
                            prefix = "ns" + (i++);
                    } else
                        prefix = "ns" + (i++);

                    saxWriter.setPrefix(prefix, featureType.getNamespaceURI());
                }

                listStoredQueriesResponse.getStoredQuery().add(listItem);
            }

            JAXBElement<ListStoredQueriesResponseType> responseElement = wfsFactory.createListStoredQueriesResponse(listStoredQueriesResponse);

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

            log.info(LoggerUtil.getLogMessage(request, "ListStoredQueriesHandler operation successfully finished."));
        } catch (JAXBException | IOException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal error occurred whilst marshalling the response document.", e);
        }
    }

}
