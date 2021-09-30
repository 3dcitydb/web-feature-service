package vcs.citydb.wfs.management;

import io.swagger.v3.jaxrs2.integration.JaxrsApplicationScanner;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import org.apache.commons.lang3.StringUtils;
import vcs.citydb.wfs.util.SwaggerUtil;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;

@Path("/api")
public class OpenApiResource extends BaseOpenApiResource {
    @Context
    ServletConfig servletConfig;

    @Context
    Application application;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml", MediaType.TEXT_HTML})
    @Operation(hidden = true)
    public Response getOpenApi(@Context HttpHeaders headers,
                               @Context UriInfo uriInfo,
                               @QueryParam("f") String outputFormat) throws Exception {
        if (StringUtils.isNotBlank(outputFormat) && outputFormat.trim().equalsIgnoreCase("html")) {
            return Response.ok()
                    .entity(SwaggerUtil.buildSwaggerUIFor(uriInfo.getPath()))
                    .type(MediaType.TEXT_HTML)
                    .build();
        } else {
            SwaggerConfiguration openApiConfig = new SwaggerConfiguration()
                    .prettyPrint(true)
                    .scannerClass(JaxrsApplicationScanner.class.getName());

            setOpenApiConfiguration(openApiConfig);

            return super.getOpenApi(headers, servletConfig, application, uriInfo, outputFormat);
        }
    }
}
