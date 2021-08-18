package vcs.citydb.wfs.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

public class CORSResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		// do nothing if this is not a CORS request
		if (requestContext.getHeaderString("Origin") == null)
			return;

		MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		headers.add("Access-Control-Allow-Origin", "*");

		// add preflight headers
		if ("OPTIONS".equals(requestContext.getMethod()) 
				&& requestContext.getHeaderString("Access-Control-Request-Method") != null) {
			headers.add("Access-Control-Allow-Methods", "GET, DELETE");
			headers.add("Access-Control-Max-Age", "86400");

			String requestCORSHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");
			if (requestCORSHeaders != null)
				headers.add("Access-Control-Allow-Headers", requestCORSHeaders);
		}
	}

}
