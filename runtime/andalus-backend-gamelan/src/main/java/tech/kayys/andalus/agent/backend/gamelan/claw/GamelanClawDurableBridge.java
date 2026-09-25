package tech.kayys.andalus.agent.backend.gamelan.claw;

import tech.kayys.andalus.harness.coordination.AgentExecutionBinding;
import tech.kayys.andalus.harness.coordination.DefaultGamelanClawBridge;
import tech.kayys.andalus.harness.coordination.GamelanDispatchDescriptor;
import tech.kayys.andalus.harness.coordination.GamelanExecutionBindingStore;
import tech.kayys.andalus.harness.execution.state.ExecutionState;
import tech.kayys.gamelan.engine.error.ErrorInfo;
import tech.kayys.gamelan.engine.node.NodeDispatchReservation;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunManager;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Source-aligned Gamelan ↔ Andalus Claw binding/completion bridge (6.17.2/6.17.4).
 *
 * <p>Extends the framework bridge (which owns correlation, binding, staleness and
 * tenant isolation) with the two Gamelan-side responsibilities:</p>
 * <ul>
 *   <li><b>dispatch</b> — {@code NodeDispatchReservation → AgentExecutionBinding}, using
 *       the reservation's own attempt as the durable dispatch identity (6.17.3.31)</li>
 *   <li><b>completion</b> — Claw terminal {@code ExecutionState → NodeExecutionResult →}
 *       {@link WorkflowRunManager#handleNodeResultWithOutcome}, which is Gamelan's
 *       authoritative node-result boundary</li>
 * </ul>
 *
 * <p>Claw never mutates Gamelan state directly: it reports results and Gamelan decides
 * (next nodes, retry, compensation, failure). A failed notification is tolerable because
 * the binding is retained and startup reconciliation re-reports it (6.17.2.24).</p>
 */
public class GamelanClawDurableBridge extends DefaultGamelanClawBridge {

    private static final Logger LOG = Logger.getLogger(GamelanClawDurableBridge.class.getName());

    /** How long a notification may block before it is left to reconciliation. */
    private static final Duration DEFAULT_NOTIFY_TIMEOUT = Duration.ofSeconds(10);

    private final WorkflowRunManager runManager;
    private final Duration notifyTimeout;

    public GamelanClawDurableBridge(GamelanExecutionBindingStore store, WorkflowRunManager runManager) {
        this(store, runManager, DEFAULT_NOTIFY_TIMEOUT);
    }

    public GamelanClawDurableBridge(GamelanExecutionBindingStore store,
                                    WorkflowRunManager runManager,
                                    Duration notifyTimeout) {
        super(store);
        this.runManager = Objects.requireNonNull(runManager, "runManager");
        this.notifyTimeout = Objects.requireNonNull(notifyTimeout, "notifyTimeout");
    }

    // ── Dispatch ──────────────────────────────────────────────────────────────

    /**
     * Binds the reservation Gamelan made for a ready node to a Claw execution.
     *
     * @return the binding, or empty when Gamelan did not actually reserve the attempt
     */
    public Optional<AgentExecutionBinding> bindReservation(NodeDispatchReservation reservation) {
        Objects.requireNonNull(reservation, "reservation");
        if (!reservation.reserved()) {
            LOG.fine(() -> "Skipping un-reserved node dispatch: " + reservation.reason());
            return Optional.empty();
        }
        AgentExecutionBinding binding = bindNodeExecution(
                reservation.tenantId().value(),
                reservation.runId().value(),
                reservation.nodeId().value(),
                reservation.attempt(),
                null);
        return Optional.of(binding);
    }

    /** Descriptor for startup reconciliation, derived from a Gamelan reservation. */
    public GamelanDispatchDescriptor descriptorOf(NodeDispatchReservation reservation) {
        return new GamelanDispatchDescriptor(
                reservation.tenantId().value(),
                reservation.runId().value(),
                reservation.nodeId().value(),
                reservation.attempt(),
                null);
    }

    /** Descriptor for startup reconciliation, derived from a durable binding. */
    public static GamelanDispatchDescriptor descriptorOf(AgentExecutionBinding binding) {
        return new GamelanDispatchDescriptor(
                binding.tenantId(), binding.workflowRunId(), binding.nodeId(), binding.attempt(), binding.reservationId());
    }

    // ── Gamelan notification (framework extension points) ─────────────────────

    /**
     * Reports a terminal Claw execution to Gamelan (6.17.4/6.17.5): staleness is
     * checked, the binding is marked complete (retained), and the mapped
     * {@link NodeExecutionResult} is pushed through
     * {@link WorkflowRunManager#handleNodeResultWithOutcome}. Duplicate or stale
     * deliveries are absorbed here and by Gamelan's {@code acceptanceFor} checks.
     *
     * <p>This is the <em>single</em> reporting code path: the framework
     * {@code onExecution*} entry points delegate to it, so staleness/dedup cannot be
     * checked twice in sequence (which would discard the report).</p>
     *
     * @return completes when the report has been delivered or safely deferred
     */
    public io.smallrye.mutiny.Uni<Void> reportTerminal(AgentExecutionBinding binding,
                                                       ExecutionState state,
                                                       String failureReason) {
        Objects.requireNonNull(binding, "binding");
        Objects.requireNonNull(state, "state");
        if (!isCurrentDispatch(binding)) {
            LOG.fine(() -> "Skipping stale terminal report for exec="
                    + binding.executionId().value() + " (dispatch=" + binding.dispatchId() + ")");
            return io.smallrye.mutiny.Uni.createFrom().voidItem();
        }
        store().complete(binding.executionId());
        push(binding, GamelanNodeResultMapper.toNodeResult(binding, state, failureReason));
        return io.smallrye.mutiny.Uni.createFrom().voidItem();
    }

    @Override
    public void onExecutionCompleted(AgentExecutionBinding binding, ExecutionState finalState) {
        reportTerminal(binding, finalState, null);
    }

    @Override
    public void onExecutionFailed(AgentExecutionBinding binding, String reason) {
        reportTerminal(binding, synthesized(binding,
                tech.kayys.andalus.harness.execution.state.ExecutionStatus.FAILED), reason);
    }

    @Override
    public void onExecutionCancelled(AgentExecutionBinding binding) {
        // Cancellation is a terminal outcome, not a failure: the CANCELED code marks it.
        reportTerminal(binding, synthesized(binding,
                tech.kayys.andalus.harness.execution.state.ExecutionStatus.CANCELED),
                "Andalus execution was canceled");
    }

    /**
     * Reports a node whose Claw execution disappeared (startup {@code MARK_LOST}) and
     * asks Gamelan to release the reserved attempt. Gamelan remains free to re-dispatch.
     */
    public void markNodeLost(AgentExecutionBinding binding, String reason) {
        Objects.requireNonNull(binding, "binding");
        LOG.warning(() -> "Reporting lost node attempt: " + binding.dispatchId() + " (" + reason + ")");
        try {
            runManager.failNodeExecution(
                            WorkflowRunId.of(binding.workflowRunId()),
                            GamelanNodeResultMapper.tenantOf(binding),
                            tech.kayys.gamelan.engine.node.NodeId.of(binding.nodeId()),
                            binding.attempt(),
                            new ErrorInfo("ANDALUS_EXECUTION_LOST", reason, null,
                                    java.util.Map.of("andalus.execution.id", binding.executionId().value())),
                            reason)
                    .await().atMost(notifyTimeout);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Gamelan failNodeExecution failed for " + binding.dispatchId()
                    + "; reconciliation will retry", e);
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void push(AgentExecutionBinding binding, NodeExecutionResult result) {
        try {
            runManager.handleNodeResultWithOutcome(
                            WorkflowRunId.of(binding.workflowRunId()),
                            GamelanNodeResultMapper.tenantOf(binding),
                            result)
                    .await().atMost(notifyTimeout);
            LOG.info(() -> "Gamelan node result reported: " + binding.dispatchId()
                    + " status=" + result.status());
        } catch (RuntimeException e) {
            // Durable binding is retained; startup reconciliation re-reports the result.
            LOG.log(Level.WARNING, "Gamelan node result reporting failed for " + binding.dispatchId()
                    + "; the durable binding allows reconciliation to retry", e);
        }
    }

    /** Minimal terminal state used when only the binding is available. */
    private static ExecutionState synthesized(AgentExecutionBinding binding,
                                              tech.kayys.andalus.harness.execution.state.ExecutionStatus terminal) {
        return new TerminalExecutionState(binding.executionId(), terminal);
    }

    /** Marker terminal state carrying just the Claw identity (used with no live state). */
    private record TerminalExecutionState(
            tech.kayys.andalus.harness.execution.state.ExecutionId executionId,
            tech.kayys.andalus.harness.execution.state.ExecutionStatus terminalStatus)
            implements ExecutionState {
        @Override
        public tech.kayys.andalus.harness.execution.state.ExecutionStatus status() {
            return terminalStatus;
        }

        @Override
        public long version() {
            return 0L;
        }

        @Override
        public tech.kayys.andalus.harness.context.HarnessContext context() {
            return null;
        }

        @Override
        public tech.kayys.andalus.harness.execution.state.ExecutionCursor cursor() {
            return tech.kayys.andalus.harness.execution.state.ExecutionCursor.initial();
        }

        @Override
        public tech.kayys.andalus.harness.execution.state.ExecutionData data() {
            return tech.kayys.andalus.harness.execution.state.ExecutionData.empty();
        }
    }
}
