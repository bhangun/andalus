package tech.kayys.andalus.execution.a2a;

import tech.kayys.andalus.a2a.durable.A2AExecutionWakeup;
import tech.kayys.andalus.harness.execution.state.ExecutionId;
import tech.kayys.andalus.harness.scheduling.temporal.DurableWaitController;

import java.util.Objects;

/**
 * Runtime binding of the A2A wake SPI to the Claw durable WAITING machinery
 * (6.16.4.22).
 *
 * <p>The wake goes through {@link DurableWaitController#wake(ExecutionId)}, which
 * reloads durable state, verifies the execution is still WAITING (not terminal, not
 * expired), and drives WAITING → RESUMING. Restoration/admission owns the subsequent
 * RESUMING → RUNNING transition — this adapter never performs it (6.16.4.12).</p>
 *
 * <p>Stale and duplicate wakes are harmless: a wake against a CANCELED or RUNNING
 * execution is a validated no-op, which is exactly what the cancellation races of
 * 6.16.7 require (remote completion must not resurrect a locally-canceled
 * execution).</p>
 */
public final class ClawA2AExecutionWakeup implements A2AExecutionWakeup {

    private final DurableWaitController waitController;

    public ClawA2AExecutionWakeup(DurableWaitController waitController) {
        this.waitController = Objects.requireNonNull(waitController, "waitController");
    }

    @Override
    public void wake(String callerExecutionId) {
        Objects.requireNonNull(callerExecutionId, "callerExecutionId");
        waitController.wake(ExecutionId.of(callerExecutionId));
    }
}
