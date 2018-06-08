package vcs.citydb.wfs.util;

import javax.servlet.http.HttpServletRequest;

public class LoggerUtil {

	public static String getLogMessage(HttpServletRequest request, String message) {
		StringBuilder builder = new StringBuilder();
		builder.append('[').append(request.getRemoteAddr()).append("] ").append(message);
		return builder.toString();
	}
	
}
