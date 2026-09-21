package tech.kayys.andalus.operator.rest.execution;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.spi.execution.ExecutionQuery;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.execution.ExecutionOperatorService;

/**
 * REST resource exposing execution operator controls under /api/operator/v1/executions.
 */
@Path("/api/operator/v1/executions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExecutionOperatorResource {

    private final ExecutionOperatorService service;
    private final OperatorRestContextResolver contextResolver;

    @Inject
    public ExecutionOperatorResource(
            ExecutionOperatorService service,
            OperatorRestContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @GET
    public Response list() {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.list(context, ExecutionQuery.all()));
    }

    @GET
    @Path("/{executionId}")
    public Response inspect(@PathParam("executionId") String executionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.inspect(context, executionId));
    }

    @POST
    @Path("/{executionId}/pause")
    public Response pause(@PathParam("executionId") String executionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.pause(context, executionId));
    }

    @POST
    @Path("/{executionId}/resume")
    public Response resume(@PathParam("executionId") String executionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.resume(context, executionId));
    }

    @POST
    @Path("/{executionId}/cancel")
    public Response cancel(@PathParam("executionId") String executionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.cancel(context, executionId));
    }

    @POST
    @Path("/{executionId}/retry")
    public Response retry(@PathParam("executionId") String executionId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.retry(context, executionId));
    }
}
