package tech.kayys.andalus.execution.lifecycle;

public record RetryExecution(
    String executionId
) implements ExecutionLifecycleCommand {
}
