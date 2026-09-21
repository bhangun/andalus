package tech.kayys.andalus.sandbox.runtime.observability;

import tech.kayys.andalus.spi.sandbox.observability.SandboxFailure;
import tech.kayys.andalus.spi.sandbox.observability.SandboxMetricsSnapshot;
import tech.kayys.andalus.spi.sandbox.observability.SandboxObservability;
import tech.kayys.andalus.spi.sandbox.observability.SandboxObservation;

public final class NoopSandboxObservability implements SandboxObservability {

    public static final NoopSandboxObservability INSTANCE = new NoopSandboxObservability();

    @Override
    public void recordEvent(SandboxObservation observation) {
    }

    @Override
    public void recordMetrics(SandboxMetricsSnapshot metrics) {
    }

    @Override
    public void recordFailure(SandboxFailure failure) {
    }
}
