package tech.kayys.andalus.execution.lifecycle;

public sealed interface ExecutionLifecycleCommand
    permits PauseExecution,
            ResumeExecution,
            CancelExecution,
            RetryExecution {

    String executionId();
}
