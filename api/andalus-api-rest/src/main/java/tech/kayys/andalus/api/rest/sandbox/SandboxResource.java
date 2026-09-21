package tech.kayys.andalus.api.rest.sandbox;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.execution.core.profile.DefaultSandboxProfileRegistry;
import tech.kayys.andalus.execution.sandbox.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Endpoint for managing execution sandboxes and isolation boundaries.
 */
@Path("/api/v1/sandboxes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SandboxResource {

    @Inject
    SandboxManager sandboxManager;

    @Inject
    DefaultSandboxProfileRegistry profileRegistry;

    /**
     * Creates a new execution sandbox using a predefined profile or default spec.
     */
    @POST
    public Response createSandbox(CreateSandboxRequest request) {
        try {
            String profileName = (request != null && request.profile() != null && !request.profile().isBlank())
                    ? request.profile().trim()
                    : "MINIMAL";

            SandboxId id = (request != null && request.sandboxId() != null && !request.sandboxId().isBlank())
                    ? SandboxId.of(request.sandboxId().trim())
                    : SandboxId.generate();

            SandboxSpec spec = profileRegistry.resolve(profileName, SandboxContext.defaultContext());
            SandboxRequest sbxReq = SandboxRequest.of(id, spec);
            SandboxHandle handle = sandboxManager.create(sbxReq);

            return Response.status(Response.Status.CREATED).entity(Map.of(
                    "sandboxId", handle.id().value(),
                    "state", handle.state().name(),
                    "profile", profileName,
                    "workspacePath", handle.workspace().rootDirectory().toAbsolutePath().toString()
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to create sandbox: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Lists all registered sandboxes.
     */
    @GET
    public Response listSandboxes() {
        List<SandboxHandle> handles = sandboxManager.list();
        List<Map<String, Object>> list = handles.stream()
                .map(h -> Map.<String, Object>of(
                        "sandboxId", h.id().value(),
                        "state", h.state().name(),
                        "workspacePath", h.workspace() != null ? h.workspace().rootDirectory().toAbsolutePath().toString() : "none"
                ))
                .toList();

        return Response.ok(Map.of(
                "total", list.size(),
                "sandboxes", list
        )).build();
    }

    /**
     * Retrieves status and details of a specific sandbox.
     */
    @GET
    @Path("/{sandboxId}")
    public Response getSandbox(@PathParam("sandboxId") String sandboxId) {
        if (sandboxId == null || sandboxId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "sandboxId cannot be blank"))
                    .build();
        }

        Optional<SandboxHandle> handleOpt = sandboxManager.find(SandboxId.of(sandboxId));
        if (handleOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sandbox not found: " + sandboxId))
                    .build();
        }

        SandboxHandle handle = handleOpt.get();
        return Response.ok(Map.of(
                "sandboxId", handle.id().value(),
                "state", handle.state().name(),
                "workspacePath", handle.workspace() != null ? handle.workspace().rootDirectory().toAbsolutePath().toString() : "none",
                "workspaceMode", handle.workspace() != null ? handle.workspace().mode().name() : "unknown"
        )).build();
    }

    /**
     * Starts a sandbox.
     */
    @POST
    @Path("/{sandboxId}/start")
    public Response startSandbox(@PathParam("sandboxId") String sandboxId) {
        try {
            SandboxHandle handle = sandboxManager.start(SandboxId.of(sandboxId));
            return Response.ok(Map.of(
                    "sandboxId", handle.id().value(),
                    "state", handle.state().name()
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Pauses a running sandbox.
     */
    @POST
    @Path("/{sandboxId}/pause")
    public Response pauseSandbox(@PathParam("sandboxId") String sandboxId) {
        try {
            sandboxManager.pause(SandboxId.of(sandboxId));
            Optional<SandboxHandle> handle = sandboxManager.find(SandboxId.of(sandboxId));
            return Response.ok(Map.of(
                    "sandboxId", sandboxId,
                    "state", handle.map(h -> h.state().name()).orElse("UNKNOWN")
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Terminates and cleans up a sandbox.
     */
    @DELETE
    @Path("/{sandboxId}")
    public Response destroySandbox(@PathParam("sandboxId") String sandboxId) {
        try {
            sandboxManager.destroy(SandboxId.of(sandboxId));
            return Response.ok(Map.of(
                    "sandboxId", sandboxId,
                    "destroyed", true
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Inspects sandbox health.
     */
    @GET
    @Path("/{sandboxId}/health")
    public Response getHealth(@PathParam("sandboxId") String sandboxId) {
        Optional<SandboxHandle> handleOpt = sandboxManager.find(SandboxId.of(sandboxId));
        if (handleOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sandbox not found: " + sandboxId))
                    .build();
        }

        SandboxHandle handle = handleOpt.get();
        boolean viable = handle.state() != SandboxState.FAILED && handle.state() != SandboxState.DESTROYED;
        return Response.ok(Map.of(
                "sandboxId", sandboxId,
                "healthy", viable,
                "state", handle.state().name()
        )).build();
    }

    public record CreateSandboxRequest(String sandboxId, String profile, Map<String, Object> attributes) {}
}
