package vcs.citydb.wfs.filter;

import vcs.citydb.wfs.config.Constants;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(Constants.WFS_SERVICE_PATH)
public class GZIPFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

		if (httpRequest.getHeader("Accept-Encoding") != null && httpRequest.getHeader("Accept-Encoding").contains("gzip")) {
			httpResponse.addHeader("Content-Encoding", "gzip");
			GZIPResponseWrapper wrapper = new GZIPResponseWrapper(httpResponse);
			
			try {
				chain.doFilter(request, wrapper);
			} finally {
				wrapper.finish();
			}
		}
		
		else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// nothing to do
	}

}
