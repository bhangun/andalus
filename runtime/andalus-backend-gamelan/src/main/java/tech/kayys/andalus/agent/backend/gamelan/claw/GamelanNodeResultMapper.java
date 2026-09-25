package tech.kayys.andalus.agent.backend.gamelan.claw;

import tech.kayys.andalus.harness.coordination.AgentExecutionBinding;
import tech.kayys.andalus.harness.execution.state.ExecutionState;
import tech.kayys.andalus.harness.execution.state.ExecutionStatus;
import tech.kayys.gamelan.engine.error.ErrorInfo;
import tech.kayys.gamelan.engine.node.DefaultNodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Translates an Andalus Claw terminal {@link ExecutionState} into Gamelan's
 * {@link NodeExecutionResult} (6.17.4 — completion bridge).
 *
 * <p><strong>Source-aligned constraint:</strong> Gamelan's
 * {@code NodeExecutionResults.validateResultSemantics} accepts only
 * {@code COMPLETED} or {@code FAILED} node results — {@code COMPLETED} must carry no
 * error and {@code FAILED} must carry one. Claw cancellation is therefore reported as
 * {@code FAILED} with the distinct code
 * {@value #ERROR_CODE_CANCELED}, so Gamelan can still tell a cancellation apart from a
 * genuine failure while remaining inside its own contract.</p>
 *
 * <p>The Claw execution identity travels in the error/output context as integration
 * metadata — it is never used as a Gamelan node identity.</p>
 */
public final class GamelanNodeResultMapper {

    public static final String ERROR_CODE_CANCELED = "ANDALUS_EXECUTION_CANCELED";
    public static final String ERROR_CODE_FAILED = "ANDALUS_EXECUTION_FAILED";
    public static final String ERROR_CODE_TIMEOUT = "EXECUTION_TIMEOUT";

    /** Context key carrying the Claw {@code ExecutionId} for correlation. */
    public static final String META_EXECUTION_ID = "andalus.execution.id";
    /** Context key carrying the Claw terminal status. */
    public static final String META_EXECUTION_STATUS = "andalus.execution.status";

    private GamelanNodeResultMapper() {
    }

    /**
     * Builds the Gamelan node result for a Claw execution that has finished.
     *
     * @param binding   the Gamelan dispatch binding (supplies run/node/attempt identity)
     * @param state     the terminal Claw execution state
     * @param failureReason optional human-readable reason (used for FAILED/CANCELED)
     */
    public static NodeExecutionResult toNodeResult(AgentExecutionBinding binding,
                                                   ExecutionState state,
                                                   String failureReason) {
        Objects.requireNonNull(binding, "binding");
        Objects.requireNonNull(state, "state");

        // 6.17.4.2: only terminal Claw executions ever produce a Gamelan node result.
        if (!state.status().isTerminal()) {
            throw new IllegalStateException(
                    "Claw execution " + state.executionId().value() + " is not terminal ("
                            + state.status() + "); no Gamelan result may be reported");
        }

        Map<String, Object> output = outputOf(state);
        Map<String, Object> context = correlationContext(state);

        return switch (state.status()) {
            case COMPLETED -> new DefaultNodeExecutionResult(
                    WorkflowRunId.of(binding.workflowRunId()),
                    NodeId.of(binding.nodeId()),
                    binding.attempt(),
                    NodeExecutionStatus.COMPLETED,
                    output,
                    null,                                  // COMPLETED must not carry an error
                    null);                                 // no Gamelan execution token (trusted path)

            case TIMEOUT -> failed(binding, output,
                    ERROR_CODE_TIMEOUT,
                    describe(failureReason, "Andalus execution exceeded its deadline"), context);

            case CANCELED -> new CancelledNodeExecutionResult(
                    WorkflowRunId.of(binding.workflowRunId()),
                    NodeId.of(binding.nodeId()),
                    binding.attempt(),
                    output,
                    new ErrorInfo(ERROR_CODE_CANCELED,
                            describe(failureReason, "Andalus execution was canceled"), null, context));

            default -> failed(binding, output,
                    ERROR_CODE_FAILED, describe(failureReason, "Andalus execution failed"), context);
        };
    }

    private static NodeExecutionResult failed(AgentExecutionBinding binding,
                                              Map<String, Object> output,
                                              String code,
                                              String message,
                                              Map<String, Object> context) {
        return new DefaultNodeExecutionResult(
                WorkflowRunId.of(binding.workflowRunId()),
                NodeId.of(binding.nodeId()),
                binding.attempt(),
                NodeExecutionStatus.FAILED,
                output,
                new ErrorInfo(code, message, null, context),
                null);
    }

    /** Output payload exposed to the workflow: Claw execution data plus correlation. */
    public static Map<String, Object> outputOf(ExecutionState state) {
        Map<String, Object> output = new HashMap<>(state.data().state());
        state.data().metadata().forEach(output::putIfAbsent);
        output.put(META_EXECUTION_ID, state.executionId().value());
        output.put(META_EXECUTION_STATUS, state.status().name());
        return Map.copyOf(output);
    }

    public static Map<String, Object> correlationContext(ExecutionState state) {
        return Map.of(
                META_EXECUTION_ID, state.executionId().value(),
                META_EXECUTION_STATUS, state.status().name());
    }

    /** Maps a Claw status onto the Gamelan status vocabulary (informational). */
    public static NodeExecutionStatus toGamelanStatus(ExecutionStatus status) {
        return switch (status) {
            case COMPLETED -> NodeExecutionStatus.COMPLETED;
            case FAILED, TIMEOUT -> NodeExecutionStatus.FAILED;
            case CANCELED -> NodeExecutionStatus.CANCELLED;
            case WAITING -> NodeExecutionStatus.WAITING;
            case RUNNING, RECOVERING -> NodeExecutionStatus.RUNNING;
            default -> NodeExecutionStatus.PENDING;
        };
    }

    public static TenantId tenantOf(AgentExecutionBinding binding) {
        return TenantId.of(binding.tenantId());
    }

    private static String describe(String reason, String fallback) {
        return reason != null && !reason.isBlank() ? reason : fallback;
    }
}
