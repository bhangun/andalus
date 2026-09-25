package tech.kayys.andalus.agent.backend.gamelan.claw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.harness.context.DefaultHarnessContext;
import tech.kayys.andalus.harness.context.DefaultHarnessIdentity;
import tech.kayys.andalus.harness.context.DefaultHarnessSession;
import tech.kayys.andalus.harness.coordination.AgentExecutionBinding;
import tech.kayys.andalus.harness.coordination.InMemoryGamelanExecutionBindingStore;
import tech.kayys.andalus.harness.execution.state.DefaultExecutionState;
import tech.kayys.andalus.harness.execution.state.ExecutionCursor;
import tech.kayys.andalus.harness.execution.state.ExecutionData;
import tech.kayys.andalus.harness.execution.state.ExecutionId;
import tech.kayys.andalus.harness.execution.state.ExecutionStatus;
import tech.kayys.andalus.harness.execution.state.InMemoryExecutionStateStore;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the unified terminal-result reconciler (6.17.5.17): one reconciler for
 * completion, failure, cancellation, and timeout — non-terminal executions produce
 * no Gamelan result.
 */
class GamelanTerminalResultReconcilerTest {

    private InMemoryGamelanExecutionBindingStore store;
    private InMemoryExecutionStateStore states;
    private GamelanClawDurableBridgeTest.RecordingWorkflowRunManager gamelan;
    private GamelanClawDurableBridge bridge;
    private DefaultGamelanTerminalResultReconciler reconciler;

    @BeforeEach
    void setUp() {
        store = new InMemoryGamelanExecutionBindingStore();
        states = new InMemoryExecutionStateStore();
        gamelan = new GamelanClawDurableBridgeTest.RecordingWorkflowRunManager();
        bridge = new GamelanClawDurableBridge(store, gamelan.manager());
        reconciler = new DefaultGamelanTerminalResultReconciler(store, states, bridge);
    }

    private AgentExecutionBinding bindAndSave(String node, ExecutionStatus status) {
        AgentExecutionBinding binding =
                bridge.bindNodeExecution("tenant-1", "run-1", node, 1, "res-1");
        var ctx = new DefaultHarnessContext(
                DefaultHarnessIdentity.of("agent-1"), DefaultHarnessSession.createNew(), Map.of());
        states.save(new DefaultExecutionState(binding.executionId(), status, 0L, ctx,
                ExecutionCursor.initial(), new ExecutionData(Map.of("k", "v"), Map.of())), -1L);
        return binding;
    }

    @Test
    void completedExecution_reportsCompleted() {
        AgentExecutionBinding binding = bindAndSave("node-1", ExecutionStatus.COMPLETED);

        reconciler.reconcile(binding.executionId()).await().indefinitely();

        assertEquals(1, gamelan.nodeResults.size());
        assertEquals(NodeExecutionStatus.COMPLETED, gamelan.nodeResults.get(0).result().status());
        assertEquals("v", gamelan.nodeResults.get(0).result().output().get("k"));
        assertEquals(binding.attempt(), gamelan.nodeResults.get(0).result().attempt(),
                "the Gamelan attempt is reported, not Claw attempt ids (6.17.4.7)");
    }

    @Test
    void canceledExecution_reportsCancelled() {
        AgentExecutionBinding binding = bindAndSave("node-2", ExecutionStatus.CANCELED);

        reconciler.reconcile(binding.executionId()).await().indefinitely();

        assertEquals(NodeExecutionStatus.CANCELLED, gamelan.nodeResults.get(0).result().status());
    }

    @Test
    void timedOutExecution_reportsFailedWithTimeoutCode() {
        AgentExecutionBinding binding = bindAndSave("node-3", ExecutionStatus.TIMEOUT);

        reconciler.reconcile(binding.executionId()).await().indefinitely();

        var result = gamelan.nodeResults.get(0).result();
        assertEquals(NodeExecutionStatus.FAILED, result.status());
        assertEquals(GamelanNodeResultMapper.ERROR_CODE_TIMEOUT, result.error().code());
    }

    @Test
    void failedExecution_reportsFailedWithError() {
        AgentExecutionBinding binding = bindAndSave("node-4", ExecutionStatus.FAILED);

        reconciler.reconcile(binding.executionId()).await().indefinitely();

        var result = gamelan.nodeResults.get(0).result();
        assertEquals(NodeExecutionStatus.FAILED, result.status());
        assertEquals(GamelanNodeResultMapper.ERROR_CODE_FAILED, result.error().code());
    }

    @Test
    void nonTerminalExecution_producesNoGamelanResult() {
        AgentExecutionBinding waiting = bindAndSave("node-5", ExecutionStatus.WAITING);
        AgentExecutionBinding running = bindAndSave("node-6", ExecutionStatus.RUNNING);
        AgentExecutionBinding recovering = bindAndSave("node-7", ExecutionStatus.RECOVERING);

        reconciler.reconcile(waiting.executionId()).await().indefinitely();
        reconciler.reconcile(running.executionId()).await().indefinitely();
        reconciler.reconcile(recovering.executionId()).await().indefinitely();

        assertTrue(gamelan.nodeResults.isEmpty());
    }

    @Test
    void unboundExecution_isANoOp() {
        assertDoesNotThrow(() ->
                reconciler.reconcile(ExecutionId.of("exec-unbound")).await().indefinitely());
        assertTrue(gamelan.nodeResults.isEmpty());
    }

    @Test
    void reconcile_isIdempotent() {
        AgentExecutionBinding binding = bindAndSave("node-8", ExecutionStatus.COMPLETED);

        reconciler.reconcile(binding.executionId()).await().indefinitely();
        reconciler.reconcile(binding.executionId()).await().indefinitely();

        assertEquals(1, gamelan.nodeResults.size(),
                "the second report is absorbed by stale/duplicate protection");
    }

    @Test
    void crashAfterCompletion_recoveryReconciles() {
        // 6.17.4.12: Claw COMPLETED, crash before Gamelan was notified.
        AgentExecutionBinding binding = bindAndSave("node-9", ExecutionStatus.COMPLETED);

        // restart: fresh transient components over surviving durable state
        DefaultGamelanTerminalResultReconciler restarted =
                new DefaultGamelanTerminalResultReconciler(store, states, bridge);
        restarted.reconcile(binding.executionId()).await().indefinitely();

        assertEquals(1, gamelan.nodeResults.size());
    }
}
