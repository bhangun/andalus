package tech.kayys.andalus.operator.rest.session;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.session.SessionOperatorService;
import tech.kayys.andalus.spi.session.SessionId;
import tech.kayys.andalus.spi.session.SessionQuery;

/**
 * REST resource exposing session operator controls under /api/operator/v1/sessions.
 */
@Path("/api/operator/v1/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionOperatorResource {

    private final SessionOperatorService service;
    private final OperatorRestContextResolver contextResolver;

    @Inject
    public SessionOperatorResource(
            SessionOperatorService service,
            OperatorRestContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @GET
    public Response list() {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.list(context, SessionQuery.all()));
    }

    @GET
    @Path("/{sessionId}")
    public Response inspect(@PathParam("sessionId") String sessionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.inspect(context, SessionId.of(sessionId)));
    }

    @POST
    @Path("/{sessionId}/suspend")
    public Response suspend(@PathParam("sessionId") String sessionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.suspend(context, SessionId.of(sessionId)));
    }

    @POST
    @Path("/{sessionId}/resume")
    public Response resume(@PathParam("sessionId") String sessionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.resume(context, SessionId.of(sessionId)));
    }

    @POST
    @Path("/{sessionId}/close")
    public Response close(@PathParam("sessionId") String sessionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.close(context, SessionId.of(sessionId)));
    }
}
