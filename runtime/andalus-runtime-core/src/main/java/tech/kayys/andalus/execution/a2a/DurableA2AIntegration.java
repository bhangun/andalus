package tech.kayys.andalus.execution.a2a;

import tech.kayys.andalus.a2a.api.A2AClient;
import tech.kayys.andalus.a2a.durable.A2AClientTaskReconciler;
import tech.kayys.andalus.a2a.durable.A2ACompletionBridge;
import tech.kayys.andalus.a2a.durable.A2AReconciliationService;
import tech.kayys.andalus.a2a.durable.A2AReconciler;
import tech.kayys.andalus.a2a.durable.A2AWakeCoordinator;
import tech.kayys.andalus.a2a.durable.DefaultA2ACompletionBridge;
import tech.kayys.andalus.a2a.durable.DefaultA2AReconciliationService;
import tech.kayys.andalus.a2a.durable.DefaultA2AWakeCoordinator;
import tech.kayys.andalus.a2a.durable.DurableA2ATask;
import tech.kayys.andalus.a2a.durable.DurableA2ATaskLedger;
import tech.kayys.andalus.harness.execution.state.ExecutionId;
import tech.kayys.andalus.harness.scheduling.temporal.DurableWaitController;
import tech.kayys.andalus.harness.scheduling.temporal.WakeCondition;

import java.util.Objects;

/**
 * Runtime assembly of the durable A2A integration for Andalus Claw (6.16).
 *
 * <p>Wires the framework abstractions to the live runtime substrate:</p>
 * <pre>
 * A2A task lifecycle
 *      ↓
 * {@link A2ACompletionBridge}        (persist terminal state + result first)
 *      ↓
 * {@link A2AWakeCoordinator}         (taskId → callerExecutionId, ledger-verified)
 *      ↓
 * {@link ClawA2AExecutionWakeup}     (runtime binding)
 *      ↓
 * {@link DurableWaitController}      (WAITING → RESUMING, durable-state validated)
 * </pre>
 *
 * <p>No worker thread is ever blocked on a remote agent: {@link #parkForRemoteTask}
 * registers the wake and parks the execution in durable WAITING, releasing the
 * worker (6.16.4.11).</p>
 */
public final class DurableA2AIntegration {

    /** Wake-condition expression prefix used when parking for an A2A task. */
    public static final String WAKE_CONDITION_PREFIX = "a2a.task:";

    private final DurableA2ATaskLedger ledger;
    private final DurableWaitController waitController;
    private final A2AWakeCoordinator wakeCoordinator;
    private final A2ACompletionBridge completionBridge;
    private final A2AReconciliationService reconciliationService;

    private DurableA2AIntegration(
            DurableA2ATaskLedger ledger,
            DurableWaitController waitController,
            A2AWakeCoordinator wakeCoordinator,
            A2ACompletionBridge completionBridge,
            A2AReconciliationService reconciliationService) {
        this.ledger = ledger;
        this.waitController = waitController;
        this.wakeCoordinator = wakeCoordinator;
        this.completionBridge = completionBridge;
        this.reconciliationService = reconciliationService;
    }

    /**
     * Assembles the integration with the default reconciler backed by the given
     * {@link A2AClient}.
     */
    public static DurableA2AIntegration create(
            DurableA2ATaskLedger ledger,
            DurableWaitController waitController,
            A2AClient a2aClient) {
        return create(ledger, waitController, a2aClient, new A2AClientTaskReconciler(a2aClient));
    }

    /**
     * Assembles the integration with a custom reconciler (e.g. multi-endpoint or
     * registry-backed remote task lookup).
     */
    public static DurableA2AIntegration create(
            DurableA2ATaskLedger ledger,
            DurableWaitController waitController,
            A2AClient a2aClient,
            A2AReconciler reconciler) {
        Objects.requireNonNull(ledger, "ledger");
        Objects.requireNonNull(waitController, "waitController");
        Objects.requireNonNull(a2aClient, "a2aClient");
        Objects.requireNonNull(reconciler, "reconciler");

        ClawA2AExecutionWakeup wakeup = new ClawA2AExecutionWakeup(waitController);
        A2AWakeCoordinator wakeCoordinator = new DefaultA2AWakeCoordinator(ledger, wakeup);
        A2ACompletionBridge completionBridge = new DefaultA2ACompletionBridge(ledger, wakeCoordinator);
        A2AReconciliationService reconciliation = new DefaultA2AReconciliationService(
                ledger, completionBridge, reconciler, a2aClient);

        return new DurableA2AIntegration(
                ledger, waitController, wakeCoordinator, completionBridge, reconciliation);
    }

    /**
     * Parks the local execution in durable WAITING until the given remote A2A task
     * resolves, without occupying a worker thread (6.16.4.11).
     *
     * <p>Sequence: transient wake registration → durable WAITING entry →
     * race-closing re-check. If the remote task reached a terminal state while the
     * execution was parking, an idempotent wake is re-issued so the execution can
     * never be stranded by a completion that arrived just before WAITING was
     * persisted (6.16.4.20).</p>
     *
     * @param callerExecutionId the local execution to park
     * @param taskId            the durably-bound A2A task to wait on
     */
    public void parkForRemoteTask(ExecutionId callerExecutionId, String taskId) {
        Objects.requireNonNull(callerExecutionId, "callerExecutionId");
        Objects.requireNonNull(taskId, "taskId");

        wakeCoordinator.register(callerExecutionId.value(), taskId);
        waitController.waitFor(callerExecutionId,
                new WakeCondition.ConditionWake(WAKE_CONDITION_PREFIX + taskId));

        ledger.find(taskId)
                .filter(DurableA2ATask::isTerminal)
                .ifPresent(t -> wakeCoordinator.complete(taskId));
    }

    // ── Startup recovery (6.16.4.10 / 6.16.6) ─────────────────────────────────

    /**
     * Rebuilds the transient wake machinery from the durable ledger after a restart
     * and reconciles every still-pending A2A task with its remote agent.
     *
     * <p>Sequence per pending task:</p>
     * <ol>
     *   <li>Re-register {@code taskId → callerExecutionId} (registrations are
     *       transient; the ledger is the durable source — 6.16.4.10)</li>
     *   <li>Reconcile remote state; terminal remote outcomes are persisted and the
     *       parked execution is woken through the completion bridge (6.16.6.8)</li>
     * </ol>
     *
     * <p>Idempotent and safe to run on every startup, including when nothing was
     * interrupted.</p>
     *
     * @return the number of wake registrations rebuilt
     */
    public int recoverAwaitingExecutions() {
        var all = ledger.allTasks();
        var pending = all.stream().filter(t -> !t.isTerminal()).toList();

        // 1. Already-terminal tasks: the terminal state is durable, but the wake may
        //    have been lost to a crash between persistence and wake (6.16.5.21).
        //    Re-issuing is safe — the wake path validates durable execution state, so
        //    an execution that is not WAITING simply ignores it.
        for (DurableA2ATask task : all) {
            if (task.isTerminal()) {
                reissueWake(task);
            }
        }

        // 2. Pending tasks: rebuild the transient registration and reconcile remotely.
        int rebuilt = 0;
        for (DurableA2ATask task : pending) {
            try {
                wakeCoordinator.register(task.callerExecutionId(), task.taskId());
                rebuilt++;
            } catch (IllegalArgumentException e) {
                // Task vanished between scan and registration — another worker may
                // have settled it; safe to skip (6.16.6.19).
            }
        }

        pending.stream()
                .map(DurableA2ATask::callerExecutionId)
                .distinct()
                .forEach(reconciliationService::reconcile);

        return rebuilt;
    }

    /** Re-issues the wake matching a task's terminal status (idempotent by design). */
    private void reissueWake(DurableA2ATask task) {
        switch (task.status()) {
            case COMPLETED -> wakeCoordinator.complete(task.taskId());
            case FAILED    -> wakeCoordinator.fail(task.taskId());
            case CANCELLED -> wakeCoordinator.cancel(task.taskId());
            default -> { /* not terminal — handled by the pending path */ }
        }
    }

    // ── Component access ──────────────────────────────────────────────────────

    public DurableA2ATaskLedger ledger() {
        return ledger;
    }

    public DurableWaitController waitController() {
        return waitController;
    }

    public A2AWakeCoordinator wakeCoordinator() {
        return wakeCoordinator;
    }

    /** Entry point for transport-level terminal signals (6.16.5.12). */
    public A2ACompletionBridge completionBridge() {
        return completionBridge;
    }

    /** Entry point for startup recovery and cancellation propagation (6.16.6/6.16.7). */
    public A2AReconciliationService reconciliationService() {
        return reconciliationService;
    }
}
