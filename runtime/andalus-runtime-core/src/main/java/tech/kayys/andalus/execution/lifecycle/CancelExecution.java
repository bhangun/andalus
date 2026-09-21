package tech.kayys.andalus.execution.lifecycle;

public record CancelExecution(
    String executionId
) implements ExecutionLifecycleCommand {
}
