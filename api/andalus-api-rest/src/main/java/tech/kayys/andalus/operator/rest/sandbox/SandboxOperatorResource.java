package tech.kayys.andalus.operator.rest.sandbox;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.sandbox.SandboxOperatorService;

/**
 * REST resource exposing sandbox operator controls under /api/operator/v1/sandboxes.
 */
@Path("/api/operator/v1/sandboxes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SandboxOperatorResource {

    private final SandboxOperatorService service;
    private final OperatorRestContextResolver contextResolver;

    @Inject
    public SandboxOperatorResource(
            SandboxOperatorService service,
            OperatorRestContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @GET
    public Response list() {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.list(context));
    }

    @GET
    @Path("/{sandboxId}")
    public Response inspect(@PathParam("sandboxId") String sandboxId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.inspect(context, sandboxId));
    }

    @GET
    @Path("/{sandboxId}/health")
    public Response health(@PathParam("sandboxId") String sandboxId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.health(context, sandboxId));
    }

    @GET
    @Path("/{sandboxId}/metrics")
    public Response metrics(@PathParam("sandboxId") String sandboxId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.metrics(context, sandboxId));
    }

    @GET
    @Path("/{sandboxId}/diagnostics")
    public Response diagnostics(@PathParam("sandboxId") String sandboxId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.diagnostics(context, sandboxId));
    }

    @POST
    @Path("/{sandboxId}/stop")
    public Response stop(@PathParam("sandboxId") String sandboxId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.stop(context, sandboxId));
    }

    @POST
    @Path("/{sandboxId}/destroy")
    public Response destroy(@PathParam("sandboxId") String sandboxId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.destroy(context, sandboxId));
    }
}
