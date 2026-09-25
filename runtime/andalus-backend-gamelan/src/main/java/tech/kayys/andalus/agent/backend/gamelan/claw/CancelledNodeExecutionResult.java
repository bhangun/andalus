package tech.kayys.andalus.agent.backend.gamelan.claw;

import tech.kayys.gamelan.engine.error.ErrorInfo;
import tech.kayys.gamelan.engine.execution.ExecutionContext;
import tech.kayys.gamelan.engine.execution.ExecutionError;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.WaitInfo;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Cancellation node result (6.17.5.4).
 *
 * <p><strong>Source-aligned constraint:</strong> Gamelan's {@code DefaultNodeExecutionResult}
 * validator accepts only {@code COMPLETED}/{@code FAILED} — it is the trusted-executor
 * result shape. Cancellation is a control-plane outcome, so it is expressed through the
 * {@link NodeExecutionResult} interface directly with status {@code CANCELLED}, which
 * Gamelan's acceptance logic ({@code NodeExecutionResults.acceptanceFor}) already
 * understands. This deliberately avoids modifying Gamelan or misreporting cancellation
 * as failure.</p>
 */
public record CancelledNodeExecutionResult(
        WorkflowRunId runId,
        NodeId nodeId,
        int attempt,
        Map<String, Object> output,
        ErrorInfo error
) implements NodeExecutionResult {

    public CancelledNodeExecutionResult {
        Objects.requireNonNull(runId, "runId");
        Objects.requireNonNull(nodeId, "nodeId");
        if (attempt <= 0) {
            throw new IllegalArgumentException("attempt must be positive");
        }
        output = output != null ? Map.copyOf(output) : Map.of();
    }

    @Override
    public NodeExecutionStatus getStatus() {
        return NodeExecutionStatus.CANCELLED;
    }

    @Override
    public String getNodeId() {
        return nodeId.value();
    }

    @Override
    public Instant getExecutedAt() {
        return Instant.now();
    }

    @Override
    public Duration getDuration() {
        return Duration.ZERO;
    }

    @Override
    public ExecutionContext getUpdatedContext() {
        return ExecutionContext.builder().variables(output).build();
    }

    @Override
    public ExecutionError getError() {
        return null;
    }

    @Override
    public WaitInfo getWaitInfo() {
        return null;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Map.of();
    }
}
