package tech.kayys.andalus.execution.lifecycle;

import tech.kayys.andalus.execution.ExecutionStatus;

import java.time.Instant;

public record ExecutionLifecycle(
    String executionId,
    ExecutionStatus status,
    int attempt,
    Instant updatedAt
) {
}
