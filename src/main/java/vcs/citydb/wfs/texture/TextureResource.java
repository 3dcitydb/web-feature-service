package vcs.citydb.wfs.texture;

import org.citydb.util.log.Logger;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.util.LoggerUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/{bucket}/{name}")
public class TextureResource {
    private final Logger log = Logger.getInstance();

    @Inject
    private TextureProvider textureProvider;

    @GET
    @Produces("image/*")
    public Response getTexture(@PathParam("bucket") String bucket, @PathParam("name") String name, @Context HttpServletRequest request) throws WFSException, SQLException {
        try {
            TextureWrapper texture = null;

            if (textureProvider.isUseTextureCache())
                texture = textureProvider.getFromCache(name, bucket);

            if (texture == null) {
                texture = textureProvider.getFromDatabase(name, request);

                if (textureProvider.isUseTextureCache())
                    textureProvider.addTextureToCache(texture, name, bucket);
            }

            return Response.ok()
                    .entity(texture.isSetPath() ? texture.getPath().toFile() : texture.getBytes())
                    .type(texture.getMimeType())
                    .build();
        } catch (WFSException | SQLException e) {
            log.error(LoggerUtil.getLogMessage(request, "Failed to load texture '" + bucket + "/" + name + "'. Check next messages for reasons."));
            throw e;
        }
    }
}