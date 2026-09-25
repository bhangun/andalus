package tech.kayys.andalus.execution.a2a;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.a2a.api.A2AClient;
import tech.kayys.andalus.a2a.durable.A2AInvocationIntent;
import tech.kayys.andalus.a2a.durable.A2ARecoveryDecision;
import tech.kayys.andalus.a2a.durable.DurableA2ATask;
import tech.kayys.andalus.a2a.durable.DurableA2ATaskLedger;
import tech.kayys.andalus.a2a.model.A2AMessage;
import tech.kayys.andalus.a2a.model.A2ATask;
import tech.kayys.andalus.a2a.model.A2ATaskStatus;
import tech.kayys.andalus.harness.execution.state.ExecutionId;
import tech.kayys.andalus.harness.scheduling.temporal.DurableWaitController;
import tech.kayys.andalus.harness.scheduling.temporal.WakeCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end durable A2A scenarios (6.16.8.37/8.39): the complete
 * {@code submit → WAITING → (crash|race) → recovery → resume} chain, exercised with a
 * durable-state-validating wait controller so duplicate and stale wakes are provably
 * harmless (6.16.8.27/8.28).
 */
class DurableA2AEndToEndTest {

    private DurableA2ATaskLedger ledger;          // durable
    private StatefulWaitController controller;    // durable execution lifecycle state
    private FakeRemoteA2AClient remote;           // durable (the remote system)
    private DurableA2AIntegration integration;    // transient (rebuilt on "restart")

    @BeforeEach
    void setUp() {
        ledger = new DurableA2ATaskLedger();
        controller = new StatefulWaitController();
        remote = new FakeRemoteA2AClient();
        integration = DurableA2AIntegration.create(ledger, controller, remote);
    }

    /** Simulates a process restart: transient components rebuilt, durable state kept. */
    private void restart() {
        integration = DurableA2AIntegration.create(ledger, controller, remote);
    }

    private DurableA2ATask boundTask(String executionId, String operationId, String taskId) {
        return ledger.createFromIntent(A2AInvocationIntent.keyed(
                executionId, operationId, taskId, "http://remote/agent", "idem-" + operationId), "ckpt-1");
    }

    // ── 8.39: the canonical durable scenario ─────────────────────────────────

    @Test
    void submit_waiting_crash_recovery_resume() {
        ExecutionId exec = ExecutionId.of("exec-e2e-1");
        DurableA2ATask task = boundTask(exec.value(), "op-1", "task-e2e-1");

        controller.start(exec);
        integration.parkForRemoteTask(exec, task.taskId());
        assertEquals(StatefulWaitController.Phase.WAITING, controller.phase(exec));

        // ---- process crash: only durable state survives ----
        restart();
        remote.remoteState.put(task.taskId(), A2ATaskStatus.COMPLETED);

        int rebuilt = integration.recoverAwaitingExecutions();

        assertEquals(1, rebuilt);
        assertEquals(A2ATaskStatus.COMPLETED, ledger.find(task.taskId()).orElseThrow().status());
        assertEquals(StatefulWaitController.Phase.RESUMING, controller.phase(exec),
                "recovery must wake the parked execution");
        assertEquals(1, controller.transitions(exec));
    }

    @Test
    void crashAfterCompletionPersistedBeforeWake_wakeStillDelivered() {
        // 6.16.5.21: ledger COMPLETED, crash before the wake happened.
        ExecutionId exec = ExecutionId.of("exec-e2e-2");
        DurableA2ATask task = boundTask(exec.value(), "op-2", "task-e2e-2");
        controller.start(exec);
        integration.parkForRemoteTask(exec, task.taskId());
        ledger.complete(task.taskId());                // persisted; wake never sent
        assertEquals(StatefulWaitController.Phase.WAITING, controller.phase(exec));

        restart();
        remote.remoteState.put(task.taskId(), A2ATaskStatus.COMPLETED);
        integration.recoverAwaitingExecutions();

        assertEquals(StatefulWaitController.Phase.RESUMING, controller.phase(exec));
        assertEquals(1, controller.transitions(exec));
    }

    @Test
    void reconciliation_ofRemoteRunningTask_leavesExecutionWaiting() {
        ExecutionId exec = ExecutionId.of("exec-e2e-6");
        DurableA2ATask task = boundTask(exec.value(), "op-6", "task-e2e-6");
        controller.start(exec);
        integration.parkForRemoteTask(exec, task.taskId());

        restart();
        remote.remoteState.put(task.taskId(), A2ATaskStatus.RUNNING);

        assertEquals(A2ARecoveryDecision.FOUND_RUNNING,
                integration.reconciliationService().reconcileTask(task.taskId())
                        .toCompletableFuture().join());

        assertEquals(StatefulWaitController.Phase.WAITING, controller.phase(exec),
                "a still-running remote task must keep the execution parked");
        assertEquals(0, controller.transitions(exec));
    }

    // -- duplicate / stale wake safety (8.27 / 8.28) --------------------------

    @Test
    void duplicateWake_causesExactlyOneLifecycleTransition() {
        ExecutionId exec = ExecutionId.of("exec-e2e-3");
        DurableA2ATask task = boundTask(exec.value(), "op-3", "task-e2e-3");
        controller.start(exec);
        integration.parkForRemoteTask(exec, task.taskId());

        integration.completionBridge().completed(task.taskId());
        integration.completionBridge().completed(task.taskId());   // duplicate delivery
        integration.completionBridge().completed(task.taskId());   // and again

        assertEquals(StatefulWaitController.Phase.RESUMING, controller.phase(exec));
        assertEquals(1, controller.transitions(exec),
                "duplicate remote completion must produce one logical lifecycle transition");
    }

    @Test
    void wakeAfterLocalCancellation_isNoop() {
        // 6.16.7.9 / 6.16.8.15: remote completion must not resurrect a canceled execution.
        ExecutionId exec = ExecutionId.of("exec-e2e-4");
        DurableA2ATask task = boundTask(exec.value(), "op-4", "task-e2e-4");
        controller.start(exec);
        integration.parkForRemoteTask(exec, task.taskId());

        controller.cancel(exec);                                    // user cancels locally
        integration.completionBridge().completed(task.taskId());    // remote completion arrives

        assertEquals(StatefulWaitController.Phase.CANCELED, controller.phase(exec));
        assertEquals(0, controller.transitions(exec));
    }

    @Test
    void localCancellation_remoteCancelFailure_doesNotUndoLocalCancellation() {
        ExecutionId exec = ExecutionId.of("exec-e2e-5");
        DurableA2ATask task = boundTask(exec.value(), "op-5", "task-e2e-5");
        controller.start(exec);
        integration.parkForRemoteTask(exec, task.taskId());
        remote.cancelFailures.add(task.taskId());

        controller.cancel(exec);
        int issued = integration.reconciliationService()
                .cancelPendingTasks(exec.value()).toCompletableFuture().join();

        assertEquals(1, issued);
        assertEquals(StatefulWaitController.Phase.CANCELED, controller.phase(exec),
                "remote cancellation failure must never undo local cancellation");
        assertFalse(ledger.find(task.taskId()).orElseThrow().isTerminal(),
                "task remains for later reconciliation (UNKNOWN remote outcome)");
    }

    // -- Test doubles ---------------------------------------------------------

    /** Durable wait-controller state model: park/wake validate durable state. */
    static final class StatefulWaitController implements DurableWaitController {
        enum Phase { NEW, RUNNING, WAITING, RESUMING, CANCELED }

        final Map<String, Phase> phases = new HashMap<>();
        final Map<String, Integer> transitions = new HashMap<>();
        final Map<String, WakeCondition> conditions = new HashMap<>();

        void start(ExecutionId id)  { phases.put(id.value(), Phase.RUNNING); }
        void cancel(ExecutionId id) { phases.put(id.value(), Phase.CANCELED); }
        Phase phase(ExecutionId id) { return phases.getOrDefault(id.value(), Phase.NEW); }
        int transitions(ExecutionId id) { return transitions.getOrDefault(id.value(), 0); }

        @Override
        public void waitFor(ExecutionId executionId, WakeCondition condition) {
            if (phase(executionId) != Phase.RUNNING) {
                throw new IllegalStateException("park requires RUNNING, was " + phase(executionId));
            }
            phases.put(executionId.value(), Phase.WAITING);
            conditions.put(executionId.value(), condition);
        }

        @Override
        public void wake(ExecutionId executionId) {
            // durable-state validated: only a WAITING execution transitions (6.16.5.16)
            if (phase(executionId) != Phase.WAITING) {
                return;
            }
            phases.put(executionId.value(), Phase.RESUMING);
            transitions.merge(executionId.value(), 1, Integer::sum);
        }

        @Override
        public void cancelWake(ExecutionId executionId) {
            conditions.remove(executionId.value());
        }
    }

    static final class FakeRemoteA2AClient implements A2AClient {
        final Map<String, A2ATaskStatus> remoteState = new HashMap<>();
        final List<String> cancelledTasks = new ArrayList<>();
        final List<String> cancelFailures = new ArrayList<>();

        @Override
        public CompletionStage<A2ATask> sendMessage(A2AMessage message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<A2ATask> getTask(String taskId) {
            A2ATaskStatus status = remoteState.get(taskId);
            if (status == null) {
                return CompletableFuture.failedFuture(new IllegalArgumentException("Task not found: " + taskId));
            }
            return CompletableFuture.completedFuture(
                    new A2ATask(taskId, "exec-remote-1", status, List.of(), Map.of()));
        }

        @Override
        public CompletionStage<Void> cancelTask(String taskId) {
            if (cancelFailures.contains(taskId)) {
                return CompletableFuture.failedFuture(new RuntimeException("cancel rejected"));
            }
            cancelledTasks.add(taskId);
            return CompletableFuture.completedFuture(null);
        }
    }
}
