package vcs.citydb.wfs.util;

import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class ServerUtil {

    public static String getServiceURL(HttpServletRequest request) throws WFSException {
        try {
            URL requestURL = new URL(request.getRequestURL().toString());
            StringBuilder serverURL = new StringBuilder(requestURL.getProtocol()).append("://").append(requestURL.getHost());
            if (requestURL.getPort() != -1 && requestURL.getPort() != requestURL.getDefaultPort())
                serverURL.append(":").append(requestURL.getPort());

            return serverURL.append(request.getContextPath()).toString();
        } catch (MalformedURLException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to create server URL.", e);
        }
    }

    public static String getParameter(HttpServletRequest request, String name) {
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name))
                return entry.getValue()[0];
        }

        return null;
    }

    public static boolean containsParameter(HttpServletRequest request, String name) {
        return getParameter(request, name) != null;
    }
}
