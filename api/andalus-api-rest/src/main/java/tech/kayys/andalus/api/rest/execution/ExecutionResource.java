package tech.kayys.andalus.api.rest.execution;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.execution.core.lifecycle.DefaultExecutionStateMachine;
import tech.kayys.andalus.execution.lifecycle.ExecutionState;
import tech.kayys.andalus.execution.lifecycle.ExecutionStateMachine;
import tech.kayys.andalus.execution.lifecycle.ExecutionTransition;
import tech.kayys.andalus.execution.lifecycle.TransitionResult;
import tech.kayys.andalus.execution.recovery.ExecutionRecoveryCoordinator;
import tech.kayys.andalus.execution.recovery.RecoveryAssessment;
import tech.kayys.andalus.execution.recovery.RecoveryPlan;
import tech.kayys.andalus.execution.recovery.RecoveryResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST Endpoint for managing execution lifecycles, state machine transitions,
 * and durable recovery workflows.
 */
@Path("/api/v1/executions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExecutionResource {

    @Inject
    ExecutionStateMachine stateMachine;

    @Inject
    ExecutionRecoveryCoordinator recoveryCoordinator;

    /**
     * Gets current state of an execution.
     */
    @GET
    @Path("/{executionId}/state")
    public Response getState(@PathParam("executionId") String executionId) {
        if (executionId == null || executionId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "executionId cannot be blank"))
                    .build();
        }
        ExecutionState state = stateMachine.state(executionId);
        return Response.ok(Map.of(
                "executionId", executionId,
                "state", state.name()
        )).build();
    }

    /**
     * Triggers a validated state transition on the execution state machine.
     */
    @POST
    @Path("/{executionId}/transitions")
    public Response transition(
            @PathParam("executionId") String executionId,
            TransitionRequest request) {
        if (executionId == null || executionId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "executionId cannot be blank"))
                    .build();
        }
        if (request == null || request.to() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Target state 'to' is required"))
                    .build();
        }

        try {
            ExecutionState currentState = stateMachine.state(executionId);
            ExecutionState targetState = ExecutionState.valueOf(request.to().toUpperCase());
            String reason = request.cause() != null ? request.cause() : "API transition request";

            ExecutionTransition transition = ExecutionTransition.of(currentState, targetState, reason);
            TransitionResult result = stateMachine.transition(executionId, transition);

            if (result.success()) {
                return Response.ok(Map.of(
                        "executionId", executionId,
                        "success", true,
                        "state", result.state().name()
                )).build();
            } else {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of(
                                "executionId", executionId,
                                "success", false,
                                "state", result.state().name(),
                                "error", result.errorMessage().orElse("Transition rejected")
                        )).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid state name: " + request.to()))
                    .build();
        }
    }

    /**
     * Retrieves the transition history of an execution.
     */
    @GET
    @Path("/{executionId}/history")
    public Response getHistory(@PathParam("executionId") String executionId) {
        if (executionId == null || executionId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "executionId cannot be blank"))
                    .build();
        }

        if (stateMachine instanceof DefaultExecutionStateMachine defaultMachine) {
            List<ExecutionTransition> history = defaultMachine.history(executionId);
            List<Map<String, Object>> records = history.stream()
                    .map(t -> Map.<String, Object>of(
                            "from", t.from().name(),
                            "to", t.to().name(),
                            "reason", t.reason(),
                            "timestamp", t.timestamp().toString()
                    ))
                    .toList();
            return Response.ok(Map.of(
                    "executionId", executionId,
                    "totalTransitions", records.size(),
                    "history", records
            )).build();
        }

        return Response.ok(Map.of(
                "executionId", executionId,
                "currentState", stateMachine.state(executionId).name()
        )).build();
    }

    /**
     * Assesses recoverability and recommends a recovery mode for an execution.
     */
    @POST
    @Path("/{executionId}/recovery/assess")
    public Response assessRecovery(@PathParam("executionId") String executionId) {
        if (executionId == null || executionId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "executionId cannot be blank"))
                    .build();
        }

        RecoveryAssessment assessment = recoveryCoordinator.assess(executionId);
        return Response.ok(Map.of(
                "executionId", assessment.executionId(),
                "recoverable", assessment.recoverable(),
                "recommendedMode", assessment.recommendedMode().name(),
                "checkpointAvailable", assessment.latestCheckpoint().isPresent(),
                "checkpointId", assessment.latestCheckpoint().map(cp -> cp.checkpointId()).orElse("none"),
                "explanation", assessment.explanation()
        )).build();
    }

    /**
     * Generates a concrete recovery plan for an execution.
     */
    @POST
    @Path("/{executionId}/recovery/plan")
    public Response planRecovery(@PathParam("executionId") String executionId) {
        if (executionId == null || executionId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "executionId cannot be blank"))
                    .build();
        }

        RecoveryAssessment assessment = recoveryCoordinator.assess(executionId);
        RecoveryPlan plan = recoveryCoordinator.plan(executionId, assessment);
        return Response.ok(Map.of(
                "executionId", plan.executionId(),
                "mode", plan.mode().name(),
                "targetCheckpointId", plan.checkpointId().orElse("none"),
                "resourceRequirements", plan.requiredResources(),
                "coordinationRequirements", plan.coordinationRequirements(),
                "plannedAt", plan.plannedAt().toString()
        )).build();
    }

    /**
     * Executes the recovery plan for an execution.
     */
    @POST
    @Path("/{executionId}/recovery/execute")
    public Response executeRecovery(@PathParam("executionId") String executionId) {
        if (executionId == null || executionId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "executionId cannot be blank"))
                    .build();
        }

        RecoveryAssessment assessment = recoveryCoordinator.assess(executionId);
        RecoveryPlan plan = recoveryCoordinator.plan(executionId, assessment);
        RecoveryResult result = recoveryCoordinator.execute(plan);

        if (result.success()) {
            ExecutionState targetState = (result.modeUsed().name().equals("RESUME"))
                    ? ExecutionState.RESUMING
                    : ExecutionState.STARTING;
            ExecutionState curState = stateMachine.state(executionId);
            if (stateMachine.isValidTransition(curState, targetState)) {
                stateMachine.transition(executionId, ExecutionTransition.of(curState, targetState, "Recovery executed"));
            }

            return Response.ok(Map.of(
                    "executionId", result.executionId(),
                    "success", true,
                    "mode", result.modeUsed().name(),
                    "newAttemptId", result.newAttemptId().orElse("unknown")
            )).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "executionId", result.executionId(),
                            "success", false,
                            "mode", result.modeUsed().name(),
                            "error", result.errorMessage().orElse("Recovery execution failed")
                    )).build();
        }
    }

    public record TransitionRequest(String to, String cause) {}
}
