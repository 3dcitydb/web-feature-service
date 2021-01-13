package vcs.citydb.wfs.config;

import net.opengis.ows._1.CodeType;
import org.citydb.config.project.common.XSLTransformation;
import vcs.citydb.wfs.config.capabilities.OWSMetadata;
import vcs.citydb.wfs.config.server.Server;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class WFSConfigLoader {
    private final ServletContext context;

    private WFSConfigLoader(ServletContext context) {
        this.context = context;
    }

    public static WFSConfig load(ServletContext context) throws JAXBException {
        return new WFSConfigLoader(context).unmarshal();
    }

    public WFSConfig unmarshal() throws JAXBException {
        JAXBContext configContext = JAXBContext.newInstance(WFSConfig.class);
        Unmarshaller um = configContext.createUnmarshaller();

        Object object = um.unmarshal(context.getResourceAsStream(Constants.CONFIG_PATH + '/' + Constants.CONFIG_FILE));
        if (!(object instanceof WFSConfig))
            throw new JAXBException("Illegal XML root element used in config file.");

        WFSConfig wfsConfig = (WFSConfig) object;
        afterUnmarshal(wfsConfig.getCapabilities().getOwsMetadata());
        afterUnmarshal(wfsConfig.getPostProcessing().getXSLTransformation());
        afterUnmarshal(wfsConfig.getServer());

        return wfsConfig;
    }

    private void afterUnmarshal(OWSMetadata owsMetadata) {
        // set service type
        CodeType serviceType = new CodeType();
        serviceType.setValue(Constants.WFS_SERVICE_STRING);
        owsMetadata.getServiceIdentification().setServiceType(serviceType);

        // check and set service protocol versions
        if (!owsMetadata.getServiceIdentification().isSetServiceTypeVersion()) {
            owsMetadata.getServiceIdentification().setServiceTypeVersion(Constants.SUPPORTED_WFS_VERSIONS);
        } else {
            List<String> versions = owsMetadata.getServiceIdentification().getServiceTypeVersion();
            versions.removeIf(version -> !Constants.SUPPORTED_WFS_VERSIONS.contains(version));

            if (!versions.isEmpty())
                versions.sort(Collections.reverseOrder());
            else
                versions = Constants.SUPPORTED_WFS_VERSIONS;

            Constants.DEFAULT_WFS_VERSION = versions.get(0);
        }
    }

    private void afterUnmarshal(XSLTransformation transformation) {
        if (transformation.isEnabled() && transformation.isSetStylesheets()) {
            transformation.getStylesheets().replaceAll(stylesheet -> !Paths.get(stylesheet).isAbsolute() ?
                    context.getRealPath(Constants.XSLT_STYLESHEETS_PATH + "/" + stylesheet) :
                    stylesheet);
        }
    }

    private void afterUnmarshal(Server server) {
        server.setExternalServiceURL(processServiceURL(server.getExternalServiceURL()));
    }

    private String processServiceURL(String serviceURL) {
        if (serviceURL != null && serviceURL.endsWith("/"))
            serviceURL = serviceURL.substring(0, serviceURL.length() - 1);

        return serviceURL;
    }
}
