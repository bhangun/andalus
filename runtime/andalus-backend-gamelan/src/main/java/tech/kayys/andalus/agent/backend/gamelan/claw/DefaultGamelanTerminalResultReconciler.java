package tech.kayys.andalus.agent.backend.gamelan.claw;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.harness.coordination.AgentExecutionBinding;
import tech.kayys.andalus.harness.coordination.GamelanExecutionBindingStore;
import tech.kayys.andalus.harness.execution.state.ExecutionId;
import tech.kayys.andalus.harness.execution.state.ExecutionState;
import tech.kayys.andalus.harness.execution.state.ExecutionStateStore;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Default {@link GamelanTerminalResultReconciler} (6.17.5).
 *
 * <p>Resolves the execution's Gamelan binding from the durable store, loads the Claw
 * execution state, and reports terminal outcomes through the durable bridge. Reporting
 * is idempotent end-to-end: the bridge skips stale bindings, the store retains completed
 * bindings, and Gamelan's {@code acceptanceFor} recognizes ALREADY_PROCESSED / STALE /
 * ALREADY_APPLIED — so crash/restart re-reporting is safe (6.17.4.10/6.17.4.12).</p>
 */
public final class DefaultGamelanTerminalResultReconciler implements GamelanTerminalResultReconciler {

    private static final Logger LOG =
            Logger.getLogger(DefaultGamelanTerminalResultReconciler.class.getName());

    private final GamelanExecutionBindingStore bindings;
    private final ExecutionStateStore executionStates;
    private final GamelanClawDurableBridge bridge;

    public DefaultGamelanTerminalResultReconciler(GamelanExecutionBindingStore bindings,
                                                  ExecutionStateStore executionStates,
                                                  GamelanClawDurableBridge bridge) {
        this.bindings = Objects.requireNonNull(bindings, "bindings");
        this.executionStates = Objects.requireNonNull(executionStates, "executionStates");
        this.bridge = Objects.requireNonNull(bridge, "bridge");
    }

    @Override
    public Uni<Void> reconcile(ExecutionId executionId) {
        Objects.requireNonNull(executionId, "executionId");

        Optional<AgentExecutionBinding> binding = bindings.findByExecutionId(executionId);
        if (binding.isEmpty()) {
            LOG.fine(() -> "No Gamelan binding for exec=" + executionId.value() + " — nothing to reconcile");
            return Uni.createFrom().voidItem();
        }

        Optional<ExecutionState> state = executionStates.load(executionId);
        if (state.isEmpty()) {
            LOG.warning(() -> "Binding " + binding.get().dispatchId()
                    + " references missing Claw execution " + executionId.value()
                    + " — startup adapter handles this as MARK_LOST");
            return Uni.createFrom().voidItem();
        }

        if (!state.get().status().isTerminal()) {
            // RUNNING / WAITING / SUSPENDED / RECOVERING: not a Gamelan result (6.17.4.2).
            LOG.fine(() -> "exec=" + executionId.value() + " is " + state.get().status()
                    + " — no terminal result to report");
            return Uni.createFrom().voidItem();
        }

        return bridge.reportTerminal(binding.get(), state.get(), null);
    }
}
