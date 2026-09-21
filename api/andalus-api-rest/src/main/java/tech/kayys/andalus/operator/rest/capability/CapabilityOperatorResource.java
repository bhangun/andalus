package tech.kayys.andalus.operator.rest.capability;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.tool.ToolCapabilityOperatorService;

/**
 * REST resource exposing capability operator controls under /api/operator/v1/capabilities.
 */
@Path("/api/operator/v1/capabilities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CapabilityOperatorResource {

    private final ToolCapabilityOperatorService service;
    private final OperatorRestContextResolver contextResolver;

    @Inject
    public CapabilityOperatorResource(
            ToolCapabilityOperatorService service,
            OperatorRestContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @GET
    public Response list() {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.listCapabilities(context));
    }

    @GET
    @Path("/{capabilityId}")
    public Response inspect(@PathParam("capabilityId") String capabilityId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.inspectCapability(context, capabilityId));
    }

    @GET
    @Path("/type/{type}")
    public Response byType(@PathParam("type") String type) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.findCapabilitiesByType(context, type));
    }

    @GET
    @Path("/tag/{tag}")
    public Response byTag(@PathParam("tag") String tag) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.findCapabilitiesByTag(context, tag));
    }
}
