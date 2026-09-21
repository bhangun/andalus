package tech.kayys.andalus.execution.lifecycle;

public record ResumeExecution(
    String executionId
) implements ExecutionLifecycleCommand {
}
