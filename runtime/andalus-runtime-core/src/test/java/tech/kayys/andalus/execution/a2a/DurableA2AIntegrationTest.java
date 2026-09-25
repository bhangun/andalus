package tech.kayys.andalus.execution.a2a;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.a2a.api.A2AClient;
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
 * End-to-end tests of the runtime A2A integration: park in durable WAITING,
 * completion-before-wake, crash-style reconciliation, and cancellation propagation.
 */
class DurableA2AIntegrationTest {

    private DurableA2ATaskLedger ledger;
    private FakeWaitController waitController;
    private FakeA2AClient client;
    private DurableA2AIntegration integration;

    @BeforeEach
    void setUp() {
        ledger = new DurableA2ATaskLedger();
        waitController = new FakeWaitController();
        client = new FakeA2AClient();
        integration = DurableA2AIntegration.create(ledger, waitController, client);
    }

    @Test
    void park_registers_wake_and_enters_durable_waiting_without_blocking() {
        DurableA2ATask task = ledger.createForCaller("exec-local-1", "http://remote/agent", "ckpt-1");
        ExecutionId execId = ExecutionId.of("exec-local-1");

        integration.parkForRemoteTask(execId, task.taskId());

        assertEquals(List.of("exec-local-1"), waitController.parked);
        WakeCondition condition = waitController.conditions.get("exec-local-1");
        assertInstanceOf(WakeCondition.ConditionWake.class, condition);
        assertEquals(DurableA2AIntegration.WAKE_CONDITION_PREFIX + task.taskId(),
                ((WakeCondition.ConditionWake) condition).conditionExpression());
        assertTrue(waitController.woken.isEmpty());
    }

    @Test
    void remote_completion_wakes_parked_execution() {
        DurableA2ATask task = ledger.createForCaller("exec-local-2", "http://remote/agent", null);
        integration.parkForRemoteTask(ExecutionId.of("exec-local-2"), task.taskId());

        integration.completionBridge().completed(task.taskId());

        assertEquals(A2ATaskStatus.COMPLETED, ledger.find(task.taskId()).orElseThrow().status());
        assertEquals(List.of("exec-local-2"), waitController.woken);
    }

    @Test
    void completion_before_park_does_not_strand_the_execution() {
        // Race: remote completes before the execution finishes parking (6.16.4.20).
        DurableA2ATask task = ledger.createForCaller("exec-local-3", "http://remote/agent", null);
        integration.completionBridge().completed(task.taskId());
        waitController.woken.clear();

        integration.parkForRemoteTask(ExecutionId.of("exec-local-3"), task.taskId());

        assertEquals(List.of("exec-local-3"), waitController.woken,
                "terminal task discovered after parking must re-issue the wake");
    }

    @Test
    void recovery_reconciliation_wakes_waiting_execution() {
        // Crash-style scenario: task was RUNNING remotely while local was WAITING.
        DurableA2ATask task = ledger.createForCaller("exec-local-4", "http://remote/agent", null);
        integration.parkForRemoteTask(ExecutionId.of("exec-local-4"), task.taskId());
        client.remoteStatus.put(task.taskId(), A2ATaskStatus.COMPLETED);

        A2ARecoveryDecision decision =
                integration.reconciliationService().reconcileTask(task.taskId()).toCompletableFuture().join();

        assertEquals(A2ARecoveryDecision.FOUND_COMPLETED, decision);
        assertEquals(A2ATaskStatus.COMPLETED, ledger.find(task.taskId()).orElseThrow().status());
        assertEquals(List.of("exec-local-4"), waitController.woken);
    }

    @Test
    void local_cancellation_propagates_to_remote_tasks_non_blocking() {
        DurableA2ATask task = ledger.createForCaller("exec-local-5", "http://remote/agent", null);
        integration.parkForRemoteTask(ExecutionId.of("exec-local-5"), task.taskId());

        int issued = integration.reconciliationService()
                .cancelPendingTasks("exec-local-5").toCompletableFuture().join();

        assertEquals(1, issued);
        assertEquals(List.of(task.taskId()), client.cancelledTasks);
        assertEquals(A2ATaskStatus.CANCELLED, ledger.find(task.taskId()).orElseThrow().status());
    }

    @Test
    void startup_recovery_rebuilds_registrations_and_reconciles() {
        // Simulate a crash: the execution parked, then the process restarted.
        // The durable ledger survived; the transient wake registration did not.
        DurableA2ATask lost = ledger.createForCaller("exec-local-6", "http://remote/agent", null);
        DurableA2ATask settledRemotely = ledger.createForCaller("exec-local-7", "http://remote/agent", null);
        client.remoteStatus.put(settledRemotely.taskId(), A2ATaskStatus.COMPLETED);

        // A fresh integration instance has an empty transient registration map.
        DurableA2AIntegration recovered = DurableA2AIntegration.create(ledger, waitController, client);

        int rebuilt = recovered.recoverAwaitingExecutions();

        assertEquals(2, rebuilt, "every pending task must get its wake registration rebuilt");
        // The remotely-completed task is persisted and its caller is woken.
        assertEquals(A2ATaskStatus.COMPLETED,
                ledger.find(settledRemotely.taskId()).orElseThrow().status());
        assertEquals(List.of("exec-local-7"), waitController.woken);
        // The still-running remote task remains pending and does not wake anyone.
        assertFalse(ledger.find(lost.taskId()).orElseThrow().isTerminal());
    }

    @Test
    void startup_recovery_is_idempotent() {
        DurableA2ATask task = ledger.createForCaller("exec-local-8", "http://remote/agent", null);
        client.remoteStatus.put(task.taskId(), A2ATaskStatus.COMPLETED);

        integration.recoverAwaitingExecutions();
        integration.recoverAwaitingExecutions();

        assertEquals(A2ATaskStatus.COMPLETED, ledger.find(task.taskId()).orElseThrow().status());
        assertEquals(1, ledger.taskCount(), "recovery must never resubmit or duplicate tasks");
    }
}

// ── Fakes ─────────────────────────────────────────────────────────────────────

final class FakeWaitController implements DurableWaitController {
    final List<String> parked = new ArrayList<>();
    final List<String> woken = new ArrayList<>();
    final Map<String, WakeCondition> conditions = new HashMap<>();

    @Override
    public void waitFor(ExecutionId executionId, WakeCondition condition) {
        parked.add(executionId.value());
        conditions.put(executionId.value(), condition);
    }

    @Override
    public void wake(ExecutionId executionId) {
        woken.add(executionId.value());
    }

    @Override
    public void cancelWake(ExecutionId executionId) {
        conditions.remove(executionId.value());
    }
}

final class FakeA2AClient implements A2AClient {
    final Map<String, A2ATaskStatus> remoteStatus = new HashMap<>();
    final List<String> cancelledTasks = new ArrayList<>();

    @Override
    public CompletionStage<A2ATask> sendMessage(A2AMessage message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionStage<A2ATask> getTask(String taskId) {
        A2ATaskStatus status = remoteStatus.get(taskId);
        if (status == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Task not found: " + taskId));
        }
        return CompletableFuture.completedFuture(
                new A2ATask(taskId, "exec-remote-1", status, List.of(), Map.of()));
    }

    @Override
    public CompletionStage<Void> cancelTask(String taskId) {
        cancelledTasks.add(taskId);
        return CompletableFuture.completedFuture(null);
    }
}
