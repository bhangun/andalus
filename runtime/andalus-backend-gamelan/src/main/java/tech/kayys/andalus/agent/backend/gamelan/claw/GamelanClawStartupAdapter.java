package tech.kayys.andalus.agent.backend.gamelan.claw;

import tech.kayys.andalus.harness.coordination.AgentExecutionBinding;
import tech.kayys.andalus.harness.coordination.GamelanClawReconciliation;
import tech.kayys.andalus.harness.coordination.GamelanClawStartupReconciler;
import tech.kayys.andalus.harness.coordination.GamelanDispatchDescriptor;
import tech.kayys.andalus.harness.coordination.GamelanExecutionBindingStore;
import tech.kayys.andalus.harness.coordination.GamelanReconciliationAction;
import tech.kayys.andalus.harness.execution.state.ExecutionState;
import tech.kayys.andalus.harness.execution.state.ExecutionStateStore;
import tech.kayys.andalus.harness.execution.state.ExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeExecution;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.workflow.WorkflowRunSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Gamelan-side startup adapter (6.17.3), source-aligned to
 * {@code WorkflowRunSnapshot}/{@code NodeExecution}.
 *
 * <p>Gamelan stays authoritative: this adapter only <em>discovers</em> the node attempts
 * Gamelan still considers active, asks the framework reconciler what that implies, and
 * applies the resulting report through the durable bridge — it never runs workflows,
 * retries nodes, checkpoints, or invents executions (6.17.3.24/6.17.3.29).</p>
 *
 * <p>Startup ordering (6.17.3.9): discovery is bounded per snapshot and never blocks
 * indefinitely; a failure to report is recoverable because the durable binding is
 * retained and the next startup/reconciliation pass re-reports it (6.17.3.26).</p>
 */
public final class GamelanClawStartupAdapter {

    private static final Logger LOG = Logger.getLogger(GamelanClawStartupAdapter.class.getName());

    private final GamelanClawDurableBridge bridge;
    private final GamelanExecutionBindingStore store;
    private final ExecutionStateStore executionStates;
    private final GamelanClawStartupReconciler reconciler;
    private final GamelanTerminalResultReconciler terminalReconciler;

    public GamelanClawStartupAdapter(GamelanClawDurableBridge bridge,
                                     GamelanExecutionBindingStore store,
                                     ExecutionStateStore executionStates) {
        this.bridge = Objects.requireNonNull(bridge, "bridge");
        this.store = Objects.requireNonNull(store, "store");
        this.executionStates = Objects.requireNonNull(executionStates, "executionStates");
        this.reconciler = new GamelanClawStartupReconciler(store, executionStates);
        // 6.17.5.17: completion and cancellation share one terminal-result reconciler.
        this.terminalReconciler = new DefaultGamelanTerminalResultReconciler(store, executionStates, bridge);
    }

    /**
     * Extracts the node attempts Gamelan still considers in-flight from a run snapshot.
     * Only attempts that actually hold a reservation (attempt &gt; 0) are considered.
     * The result is deterministically ordered by dispatch id so repeated startups and
     * tests observe a stable sequence.
     */
    public List<GamelanDispatchDescriptor> activeDispatches(WorkflowRunSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<GamelanDispatchDescriptor> dispatches = new ArrayList<>();
        for (NodeExecution nodeExecution : snapshot.nodeExecutions().values()) {
            if (!isInFlight(nodeExecution)) {
                continue;
            }
            dispatches.add(new GamelanDispatchDescriptor(
                    snapshot.tenantId() != null ? snapshot.tenantId().value() : null,
                    snapshot.id().value(),
                    nodeExecution.getNodeId().value(),
                    nodeExecution.getAttempt(),
                    null));
        }
        dispatches.sort(java.util.Comparator.comparing(GamelanDispatchDescriptor::dispatchId));
        return List.copyOf(dispatches);
    }

    /**
     * Reconciles one run snapshot and reports the resulting actions to Gamelan.
     *
     * @return the outcomes that were acted upon (for observability/tests)
     */
    public List<GamelanClawReconciliation> reconcileAndApply(WorkflowRunSnapshot snapshot) {
        List<GamelanClawReconciliation> decisions = reconciler.reconcile(activeDispatches(snapshot));
        List<GamelanClawReconciliation> applied = new ArrayList<>();
        for (GamelanClawReconciliation decision : decisions) {
            if (apply(decision)) {
                applied.add(decision);
            }
        }
        return List.copyOf(applied);
    }

    private boolean apply(GamelanClawReconciliation decision) {
        AgentExecutionBinding binding = decision.binding().orElse(null);
        if (binding == null) {
            // REBIND_REQUIRED: Claw must bind the dispatch; nothing to tell Gamelan yet.
            LOG.info(() -> "Startup: dispatch " + decision.dispatch().dispatchId()
                    + " has no Claw binding — awaiting rebind");
            return false;
        }

        switch (decision.action()) {
            case NOTIFY_COMPLETED, NOTIFY_FAILED, NOTIFY_CANCELLED -> {
                // Unified terminal reporting (6.17.5.17): the reconciler re-loads the
                // Claw state and maps COMPLETED/FAILED/CANCELED/TIMEOUT itself.
                terminalReconciler.reconcile(binding.executionId())
                        .await().atMost(java.time.Duration.ofSeconds(10));
                return true;
            }
            case MARK_LOST -> {
                bridge.markNodeLost(binding, "Claw execution missing at startup");
                return true;
            }
            case KEEP_WAITING, STALE_DISPATCH -> {
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    /** In-flight node attempts are the ones that may still be waiting on Claw. */
    private static boolean isInFlight(NodeExecution nodeExecution) {
        if (nodeExecution == null || nodeExecution.getStatus() == null || nodeExecution.getAttempt() <= 0) {
            return false;
        }
        NodeExecutionStatus status = nodeExecution.getStatus();
        return status == NodeExecutionStatus.EXECUTING
                || status == NodeExecutionStatus.RUNNING
                || status == NodeExecutionStatus.RETRYING;
    }

    /** Terminal Claw statuses that require a report (used by diagnostics). */
    public Optional<ExecutionState> terminalStateOf(AgentExecutionBinding binding) {
        return executionStates.load(binding.executionId())
                .filter(state -> state.status() == ExecutionStatus.COMPLETED
                        || state.status() == ExecutionStatus.FAILED
                        || state.status() == ExecutionStatus.CANCELED);
    }
}
