package tech.kayys.andalus.execution.lifecycle;

public record PauseExecution(
    String executionId
) implements ExecutionLifecycleCommand {
}
