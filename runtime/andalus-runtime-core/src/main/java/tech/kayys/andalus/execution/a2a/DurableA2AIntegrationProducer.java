package tech.kayys.andalus.execution.a2a;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import tech.kayys.andalus.a2a.api.A2AClient;
import tech.kayys.andalus.a2a.durable.DurableA2ATaskLedger;
import tech.kayys.andalus.harness.scheduling.temporal.DurableWaitController;

/**
 * CDI wiring for the durable A2A integration (6.16).
 *
 * <p>The {@link DurableA2ATaskLedger} is always available. The full
 * {@link DurableA2AIntegration} is assembled lazily and only when the runtime
 * provides both a {@link DurableWaitController} and an {@link A2AClient} bean —
 * request it through {@code Instance<DurableA2AIntegration>} (as
 * {@link DurableA2AStartupRecovery} does) so deployments without A2A remain valid.</p>
 */
@ApplicationScoped
public class DurableA2AIntegrationProducer {

    @Produces
    @ApplicationScoped
    public DurableA2ATaskLedger durableA2ATaskLedger() {
        return new DurableA2ATaskLedger();
    }

    @Produces
    @ApplicationScoped
    public DurableA2AIntegration durableA2AIntegration(
            DurableA2ATaskLedger ledger,
            Instance<DurableWaitController> waitController,
            Instance<A2AClient> a2aClient) {
        if (!waitController.isResolvable() || !a2aClient.isResolvable()) {
            throw new IllegalStateException(
                    "DurableA2AIntegration requires DurableWaitController and A2AClient beans");
        }
        return DurableA2AIntegration.create(ledger, waitController.get(), a2aClient.get());
    }
}
