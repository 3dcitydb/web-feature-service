package vcs.citydb.wfs.util;

import javax.servlet.http.HttpServletRequest;

public class LoggerUtil {

    public static String getLogMessage(HttpServletRequest request, String message) {
        return '[' + request.getRemoteAddr() + "] " + message;
    }

    public static String getLogMessage(HttpServletRequest request, String id, String message) {
        return '[' + request.getRemoteAddr() + ", " + id + "] " + message;
    }
}
