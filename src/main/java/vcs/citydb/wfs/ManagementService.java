package vcs.citydb.wfs;

import org.citydb.core.registry.ObjectRegistry;
import org.glassfish.jersey.server.ResourceConfig;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.GenericExtensionMapper;
import vcs.citydb.wfs.filter.CORSResponseFilter;
import vcs.citydb.wfs.rest.json.GsonMessageBodyHandler;
import vcs.citydb.wfs.rest.management.VersionResource;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

@ApplicationPath(Constants.MANAGEMENT_SERVICE_PATH)
public class ManagementService extends ResourceConfig {

	public ManagementService(@Context ServletContext context) throws ServletException {
		Object e = context.getAttribute(Constants.INIT_ERROR_ATTRNAME);
		if (e instanceof ServletException)
			throw (ServletException)e;
		
		WFSConfig wfsConfig = ObjectRegistry.getInstance().lookup(WFSConfig.class);

		if (wfsConfig.getServer().isEnableCORS())
			register(CORSResponseFilter.class);
		
		register(GsonMessageBodyHandler.class);
		register(VersionResource.class);
		register(GenericExtensionMapper.class);
	}

}
