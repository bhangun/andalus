package tech.kayys.andalus.operator.rest.configuration;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.configuration.ConfigurationOperatorService;
import tech.kayys.andalus.spi.operator.configuration.ConfigurationUpdateRequest;

/**
 * REST resource exposing configuration operator controls under /api/operator/v1/configurations.
 */
@Path("/api/operator/v1/configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationOperatorResource {

    private final ConfigurationOperatorService service;
    private final OperatorRestContextResolver contextResolver;

    @Inject
    public ConfigurationOperatorResource(
            ConfigurationOperatorService service,
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
    @Path("/active")
    public Response active() {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.active(context));
    }

    @GET
    @Path("/{configurationId}")
    public Response inspect(@PathParam("configurationId") String configurationId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.inspect(context, configurationId));
    }

    @PUT
    @Path("/{configurationId}")
    public Response update(
            @PathParam("configurationId") String configurationId,
            ConfigurationUpdateRequest request) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.update(context, configurationId, request));
    }

    @POST
    @Path("/{configurationId}/activate")
    public Response activate(@PathParam("configurationId") String configurationId) {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.activate(context, configurationId));
    }
}
