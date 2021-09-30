package vcs.citydb.wfs.util;

import org.apache.commons.io.IOUtils;
import vcs.citydb.wfs.texture.OpenApiResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SwaggerUtil {

    public static String buildSwaggerUIFor(String url) throws IOException {
        URL input = OpenApiResource.class.getResource("/vcs/citydb/wfs/swagger-ui.html");
        if (input != null) {
            try (InputStream stream = input.openStream()) {
                return IOUtils.toString(stream, StandardCharsets.UTF_8)
                        .replace("${url}", url);
            }
        } else {
            throw new IOException("Failed to load swagger-ui.html template file.");
        }
    }

}
