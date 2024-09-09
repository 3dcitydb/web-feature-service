package vcs.citydb.wfs.texture;

import org.citydb.config.Config;
import org.citydb.core.registry.ObjectRegistry;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.GenericExceptionMapper;
import vcs.citydb.wfs.filter.CORSResponseFilter;
import vcs.citydb.wfs.util.json.GsonMessageBodyHandler;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

@ApplicationPath(Constants.TEXTURE_SERVICE_PATH)
public class TextureService extends ResourceConfig {
    private final TextureProvider textureProvider;

    public TextureService(@Context ServletContext context) throws ServletException {
        Object e = context.getAttribute(Constants.INIT_ERROR_ATTRNAME);
        if (e instanceof ServletException)
            throw (ServletException) e;

        WFSConfig wfsConfig = ObjectRegistry.getInstance().lookup(WFSConfig.class);
        Config config = ObjectRegistry.getInstance().getConfig();

        register(GsonMessageBodyHandler.class);
        register(TextureResource.class);
        register(DeleteTexture.class);
        register(GenericExceptionMapper.class);

        if (wfsConfig.getServer().isEnableCORS())
            register(CORSResponseFilter.class);

        textureProvider = new TextureProvider(context, wfsConfig, config);
        register(new AbstractBinder() {
            protected void configure() {
                bind(textureProvider).to(TextureProvider.class);
            }
        });
    }

    @PreDestroy
    public void destroy() {
        textureProvider.destroy();
    }

}
