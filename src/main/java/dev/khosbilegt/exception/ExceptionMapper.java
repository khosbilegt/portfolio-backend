package dev.khosbilegt.exception;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Provider
public class ExceptionMapper {
    private static final Logger LOG = Logger.getLogger("UnitelCallCenter");

    @ServerExceptionMapper
    public RestResponse<JsonObject> mapException(Throwable throwable) {
        Response.Status status;
        if (throwable instanceof NotFoundException) {
            status = Response.Status.NOT_FOUND;
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        LOG.errorv(throwable, "EXCEPTION_MAPPER_CAUGHT_EXCEPTION: {0}", throwable.getMessage());
        return RestResponse.status(status, new JsonObject()
                .put("status", "FAILED")
                .put("message", throwable.getMessage())
        );
    }
}
