package tech.kayys.andalus.operator.rest.diagnostics;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.spi.diagnostics.DiagnosticComponent;
import tech.kayys.andalus.spi.diagnostics.DiagnosticComponentType;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.diagnostics.DiagnosticsOperatorService;

/**
 * REST resource exposing diagnostics operator inspection under /api/operator/v1/diagnostics.
 */
@Path("/api/operator/v1/diagnostics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DiagnosticsOperatorResource {

    private final DiagnosticsOperatorService service;
    private final OperatorRestContextResolver contextResolver;

    @Inject
    public DiagnosticsOperatorResource(
            DiagnosticsOperatorService service,
            OperatorRestContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @GET
    public Response inspectPlatform() {
        OperatorContext context = contextResolver.resolve();
        return OperatorResponseMapper.toResponse(service.diagnoseAll(context));
    }

    @GET
    @Path("/{type}")
    public Response inspectType(@PathParam("type") String type) {
        OperatorContext context = contextResolver.resolve();
        DiagnosticComponentType componentType = DiagnosticComponentType.valueOf(type.toUpperCase());
        return OperatorResponseMapper.toResponse(service.diagnoseType(context, componentType));
    }

    @GET
    @Path("/{type}/{id}")
    public Response inspectComponent(
            @PathParam("type") String type,
            @PathParam("id") String id) {
        OperatorContext context = contextResolver.resolve();
        DiagnosticComponentType componentType = DiagnosticComponentType.valueOf(type.toUpperCase());
        DiagnosticComponent component = new DiagnosticComponent(componentType, id);
        return OperatorResponseMapper.toResponse(service.diagnose(context, component));
    }
}
