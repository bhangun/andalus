package tech.kayys.andalus.execution.a2a;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import tech.kayys.andalus.a2a.api.A2AClient;
import tech.kayys.andalus.harness.scheduling.temporal.DurableWaitController;

import java.util.logging.Logger;

/**
 * Startup hook that reconnects durable A2A state after a process restart (6.16.6).
 *
 * <p>On startup it rebuilds the transient wake registrations from the durable task
 * ledger and reconciles every pending A2A task with its remote agent, so that:</p>
 * <ul>
 *   <li>completions that arrived while the process was down still wake the parked
 *       execution (crash after completion persistence, before wake — 6.16.5.21);</li>
 *   <li>locally-cancelled executions with still-active remote tasks get their remote
 *       cancellation re-attempted by reconciliation policy (6.16.7.17);</li>
 *   <li>lost completion notifications are recovered (6.16.4.20).</li>
 * </ul>
 *
 * <p>The hook is inert unless the deployment provides {@link DurableWaitController}
 * and {@link A2AClient} beans — runtimes without durable A2A are unaffected.</p>
 */
@ApplicationScoped
public class DurableA2AStartupRecovery {

    private static final Logger LOG = Logger.getLogger(DurableA2AStartupRecovery.class.getName());

    void onStart(
            @Observes StartupEvent event,
            Instance<DurableWaitController> waitController,
            Instance<A2AClient> a2aClient,
            Instance<DurableA2AIntegration> integration) {
        if (!waitController.isResolvable() || !a2aClient.isResolvable() || !integration.isResolvable()) {
            LOG.fine("Durable A2A startup recovery skipped: no DurableWaitController/A2AClient available");
            return;
        }

        int rebuilt = integration.get().recoverAwaitingExecutions();
        if (rebuilt > 0) {
            LOG.info("Durable A2A startup recovery: rebuilt " + rebuilt
                    + " wake registration(s) and triggered reconciliation");
        }
    }
}
