package tech.kayys.andalus.api.rest.execution;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.execution.attempt.AttemptId;
import tech.kayys.andalus.execution.checkpoint.CheckpointIntegrity;
import tech.kayys.andalus.execution.checkpoint.CheckpointManifest;
import tech.kayys.andalus.execution.checkpoint.CheckpointStore;
import tech.kayys.andalus.execution.checkpoint.ExecutionCheckpoint;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * REST Endpoint for saving, querying, and verifying execution checkpoints.
 */
@Path("/api/v1/checkpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CheckpointResource {

    @Inject
    CheckpointStore checkpointStore;

    @POST
    public Response saveCheckpoint(SaveCheckpointRequest request) {
        if (request == null || request.executionId() == null || request.executionId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "executionId is required"))
                    .build();
        }

        String cpId = (request.checkpointId() != null && !request.checkpointId().isBlank())
                ? request.checkpointId().trim()
                : "cp-" + UUID.randomUUID();

        AttemptId attId = (request.attemptId() != null && !request.attemptId().isBlank())
                ? AttemptId.of(request.attemptId().trim())
                : AttemptId.random();

        byte[] payload = request.payloadBase64() != null
                ? Base64.getDecoder().decode(request.payloadBase64())
                : (request.payloadText() != null ? request.payloadText().getBytes(StandardCharsets.UTF_8) : new byte[0]);

        CheckpointIntegrity integrity = CheckpointIntegrity.sha256(payload);
        CheckpointManifest manifest = new CheckpointManifest(
                cpId,
                request.sequence(),
                request.stateKeys(),
                request.artifactIds(),
                List.of(),
                integrity,
                Instant.now(),
                request.metadata() != null ? request.metadata() : Map.of()
        );

        ExecutionCheckpoint checkpoint = new ExecutionCheckpoint(
                cpId,
                request.executionId().trim(),
                attId,
                request.sequence(),
                payload,
                manifest,
                Instant.now(),
                request.metadata() != null ? request.metadata() : Map.of()
        );

        checkpointStore.save(checkpoint);

        return Response.status(Response.Status.CREATED).entity(Map.of(
                "checkpointId", cpId,
                "executionId", request.executionId(),
                "sequence", request.sequence(),
                "checksum", integrity.checksum(),
                "payloadBytes", payload.length
        )).build();
    }

    @GET
    @Path("/{checkpointId}")
    public Response getCheckpoint(@PathParam("checkpointId") String checkpointId) {
        Optional<ExecutionCheckpoint> cpOpt = checkpointStore.get(checkpointId);
        if (cpOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Checkpoint not found: " + checkpointId))
                    .build();
        }

        ExecutionCheckpoint cp = cpOpt.get();
        return Response.ok(Map.of(
                "checkpointId", cp.checkpointId(),
                "executionId", cp.executionId(),
                "attemptId", cp.attemptId().value(),
                "sequence", cp.sequence(),
                "payloadBytes", cp.payload().length,
                "timestamp", cp.timestamp().toString(),
                "manifest", Map.of(
                        "integrityChecksum", cp.manifest().integrity().checksum(),
                        "stateKeys", cp.manifest().stateKeys(),
                        "artifactIds", cp.manifest().artifactIds()
                )
        )).build();
    }

    @GET
    @Path("/{checkpointId}/verify")
    public Response verifyCheckpoint(@PathParam("checkpointId") String checkpointId) {
        Optional<ExecutionCheckpoint> cpOpt = checkpointStore.get(checkpointId);
        if (cpOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Checkpoint not found: " + checkpointId))
                    .build();
        }

        ExecutionCheckpoint cp = cpOpt.get();
        boolean valid = cp.manifest().integrity().verify(cp.payload());

        return Response.ok(Map.of(
                "checkpointId", checkpointId,
                "valid", valid,
                "checksum", cp.manifest().integrity().checksum(),
                "bytesVerified", cp.payload().length
        )).build();
    }

    @GET
    @Path("/execution/{executionId}")
    public Response listByExecution(@PathParam("executionId") String executionId) {
        List<ExecutionCheckpoint> list = checkpointStore.list(executionId);
        List<Map<String, Object>> summaries = list.stream()
                .map(cp -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("checkpointId", cp.checkpointId());
                    map.put("attemptId", cp.attemptId().value());
                    map.put("sequence", cp.sequence());
                    map.put("payloadBytes", cp.payload().length);
                    map.put("checksum", cp.manifest().integrity().checksum());
                    map.put("timestamp", cp.timestamp().toString());
                    return map;
                })
                .toList();

        return Response.ok(Map.of(
                "executionId", executionId,
                "total", summaries.size(),
                "checkpoints", summaries
        )).build();
    }

    @GET
    @Path("/execution/{executionId}/latest")
    public Response getLatest(@PathParam("executionId") String executionId) {
        Optional<ExecutionCheckpoint> latestOpt = checkpointStore.getLatest(executionId);
        if (latestOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No checkpoints found for execution: " + executionId))
                    .build();
        }

        ExecutionCheckpoint cp = latestOpt.get();
        return Response.ok(Map.of(
                "checkpointId", cp.checkpointId(),
                "executionId", cp.executionId(),
                "attemptId", cp.attemptId().value(),
                "sequence", cp.sequence(),
                "payloadBytes", cp.payload().length,
                "checksum", cp.manifest().integrity().checksum(),
                "timestamp", cp.timestamp().toString()
        )).build();
    }

    public record SaveCheckpointRequest(
            String checkpointId,
            String executionId,
            String attemptId,
            long sequence,
            String payloadBase64,
            String payloadText,
            List<String> stateKeys,
            List<String> artifactIds,
            Map<String, Object> metadata
    ) {}
}
