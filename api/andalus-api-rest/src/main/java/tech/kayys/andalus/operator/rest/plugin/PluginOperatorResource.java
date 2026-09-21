package tech.kayys.andalus.operator.rest.plugin;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.plugin.PluginOperatorService;

/**
 * REST resource exposing plugin operator controls under /api/operator/v1/plugins.
 */
@Path("/api/operator/v1/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginOperatorResource {

    private final PluginOperatorService service;
    private final OperatorRestContextResolver contextResolver;

    @Inject
    public PluginOperatorResource(
            PluginOperatorService service,
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
    @Path("/{pluginId}")
    public Response inspect(@PathParam("pluginId") String pluginId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.inspect(context, pluginId));
    }

    @POST
    @Path("/{pluginId}/enable")
    public Response enable(@PathParam("pluginId") String pluginId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.enable(context, pluginId));
    }

    @POST
    @Path("/{pluginId}/disable")
    public Response disable(@PathParam("pluginId") String pluginId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.disable(context, pluginId));
    }

    @POST
    @Path("/{pluginId}/unload")
    public Response unload(@PathParam("pluginId") String pluginId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.unload(context, pluginId));
    }
}
