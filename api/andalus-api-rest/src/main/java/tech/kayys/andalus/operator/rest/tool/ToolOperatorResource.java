package tech.kayys.andalus.operator.rest.tool;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.tool.ToolCapabilityOperatorService;

/**
 * REST resource exposing tool operator queries under /api/operator/v1/tools.
 */
@Path("/api/operator/v1/tools")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ToolOperatorResource {

    private final ToolCapabilityOperatorService service;
    private final OperatorRestContextResolver contextResolver;

    @Inject
    public ToolOperatorResource(
            ToolCapabilityOperatorService service,
            OperatorRestContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @GET
    public Response list() {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.listTools(context));
    }

    @GET
    @Path("/{toolId}")
    public Response inspect(@PathParam("toolId") String toolId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.inspectTool(context, toolId));
    }
}
