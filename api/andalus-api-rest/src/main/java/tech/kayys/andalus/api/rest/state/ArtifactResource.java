package tech.kayys.andalus.api.rest.state;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.state.artifact.*;
import tech.kayys.andalus.state.lineage.LineageGraph;
import tech.kayys.andalus.state.lineage.LineageResolver;
import tech.kayys.andalus.state.provenance.ProvenanceQueryService;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * REST Endpoint for content-addressed artifact persistence, metadata querying,
 * and provenance lineage DAG resolution.
 */
@Path("/api/v1/artifacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtifactResource {

    @Inject
    ArtifactStore artifactStore;

    @Inject
    ArtifactMetadataStore metadataStore;

    @Inject
    LineageResolver lineageResolver;

    @Inject
    ProvenanceQueryService provenanceService;

    @POST
    public Response createArtifact(CreateArtifactRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Artifact name is required"))
                    .build();
        }

        try {
            ArtifactType type = (request.type() != null && !request.type().isBlank())
                    ? ArtifactType.valueOf(request.type().toUpperCase())
                    : ArtifactType.CUSTOM;

            byte[] content;
            if (request.contentBase64() != null && !request.contentBase64().isBlank()) {
                content = Base64.getDecoder().decode(request.contentBase64());
            } else if (request.contentText() != null) {
                content = request.contentText().getBytes(StandardCharsets.UTF_8);
            } else {
                content = new byte[0];
            }

            String mimeType = (request.mimeType() != null && !request.mimeType().isBlank())
                    ? request.mimeType()
                    : "application/octet-stream";

            ArtifactInput input = new ArtifactInput(
                    request.name().trim(),
                    type,
                    content,
                    mimeType,
                    request.metadata() != null ? request.metadata() : Map.of()
            );

            ArtifactReference reference = artifactStore.put(input);
            metadataStore.save(reference);

            return Response.status(Response.Status.CREATED).entity(Map.of(
                    "artifactId", reference.id().value(),
                    "name", reference.name(),
                    "type", reference.type().name(),
                    "digest", reference.digest().hexValue(),
                    "bytes", reference.size().bytes(),
                    "location", reference.location().uri().toString(),
                    "createdAt", reference.createdAt().toString()
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid parameter: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to store artifact: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{artifactId}")
    public Response getMetadata(@PathParam("artifactId") String artifactId) {
        if (artifactId == null || artifactId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "artifactId cannot be blank"))
                    .build();
        }

        Optional<ArtifactReference> refOpt = metadataStore.get(ArtifactId.of(artifactId.trim()));
        if (refOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Artifact not found: " + artifactId))
                    .build();
        }

        ArtifactReference ref = refOpt.get();
        return Response.ok(Map.of(
                "artifactId", ref.id().value(),
                "name", ref.name(),
                "type", ref.type().name(),
                "digest", ref.digest().hexValue(),
                "bytes", ref.size().bytes(),
                "location", ref.location().uri().toString(),
                "createdAt", ref.createdAt().toString(),
                "metadata", ref.metadata()
        )).build();
    }

    @GET
    @Path("/{artifactId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadContent(@PathParam("artifactId") String artifactId) {
        if (artifactId == null || artifactId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<ArtifactReference> refOpt = metadataStore.get(ArtifactId.of(artifactId.trim()));
        if (refOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<ArtifactContent> contentOpt = artifactStore.get(refOpt.get());
        if (contentOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ArtifactContent content = contentOpt.get();
        String mime = content.mimeType() != null ? content.mimeType() : MediaType.APPLICATION_OCTET_STREAM;

        return Response.ok(content.data())
                .header("Content-Disposition", "attachment; filename=\"" + refOpt.get().name() + "\"")
                .header("Content-Type", mime)
                .build();
    }

    @GET
    @Path("/{artifactId}/lineage")
    public Response getArtifactLineage(@PathParam("artifactId") String artifactId) {
        if (artifactId == null || artifactId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "artifactId cannot be blank"))
                    .build();
        }

        LineageGraph graph = provenanceService.traceArtifact(ArtifactId.of(artifactId.trim()));
        List<Map<String, Object>> nodes = graph.nodes().stream()
                .map(n -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", n.id());
                    map.put("type", n.type());
                    map.put("label", n.label());
                    map.put("attributes", n.attributes());
                    return map;
                })
                .toList();

        List<Map<String, Object>> edges = graph.edges().stream()
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("source", e.sourceId());
                    map.put("target", e.targetId());
                    map.put("relationship", e.relation());
                    map.put("attributes", e.attributes());
                    return map;
                })
                .toList();

        return Response.ok(Map.of(
                "artifactId", artifactId,
                "nodeCount", nodes.size(),
                "edgeCount", edges.size(),
                "nodes", nodes,
                "edges", edges
        )).build();
    }

    @GET
    @Path("/execution/{executionId}/lineage")
    public Response getExecutionLineage(@PathParam("executionId") String executionId) {
        if (executionId == null || executionId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "executionId cannot be blank"))
                    .build();
        }

        LineageGraph graph = provenanceService.traceExecution(executionId.trim());
        List<Map<String, Object>> nodes = graph.nodes().stream()
                .map(n -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", n.id());
                    map.put("type", n.type());
                    map.put("label", n.label());
                    map.put("attributes", n.attributes());
                    return map;
                })
                .toList();

        List<Map<String, Object>> edges = graph.edges().stream()
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("source", e.sourceId());
                    map.put("target", e.targetId());
                    map.put("relationship", e.relation());
                    map.put("attributes", e.attributes());
                    return map;
                })
                .toList();

        return Response.ok(Map.of(
                "executionId", executionId,
                "nodeCount", nodes.size(),
                "edgeCount", edges.size(),
                "nodes", nodes,
                "edges", edges
        )).build();
    }

    public record CreateArtifactRequest(
            String name,
            String type,
            String contentBase64,
            String contentText,
            String mimeType,
            Map<String, Object> metadata
    ) {}
}
