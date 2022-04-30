package vcs.citydb.wfs.exception;

import org.citydb.util.log.Logger;
import vcs.citydb.wfs.util.LoggerUtil;
import vcs.citydb.wfs.util.json.ExceptionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private final Logger log = Logger.getInstance();

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(Throwable t) {
        int httpStatus;
        ExceptionInfo info = new ExceptionInfo();

        if (t instanceof WFSException) {
            WFSExceptionMessage message = ((WFSException) t).getFirstExceptionMessage();
            httpStatus = message.getExceptionCode().getHttpStatusCode();
            info.setCode(message.getExceptionCode().getValue());
            message.getExceptionTexts().forEach(info::addDescription);
        } else {
            if (t instanceof WebApplicationException) {
                WebApplicationException e = (WebApplicationException) t;
                httpStatus = e.getResponse().getStatusInfo().getStatusCode();
                info.setCode(e.getResponse().getStatusInfo().getReasonPhrase());
            } else {
                httpStatus = WFSExceptionCode.INTERNAL_SERVER_ERROR.getHttpStatusCode();
                info.setCode(WFSExceptionCode.INTERNAL_SERVER_ERROR.getValue());
            }

            do {
                String cause = t.getMessage();
                if (cause != null)
                    info.addDescription(cause);
            } while ((t = t.getCause()) != null);
        }

        info.getDescription().forEach(m -> log.error(LoggerUtil.getLogMessage(request, m)));

        return Response.status(httpStatus)
                .entity(info)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
