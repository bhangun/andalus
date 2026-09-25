package tech.kayys.andalus.agent.backend.gamelan.claw;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.harness.context.DefaultHarnessContext;
import tech.kayys.andalus.harness.context.DefaultHarnessIdentity;
import tech.kayys.andalus.harness.context.DefaultHarnessSession;
import tech.kayys.andalus.harness.coordination.AgentExecutionBinding;
import tech.kayys.andalus.harness.coordination.GamelanExecutionBindingStore;
import tech.kayys.andalus.harness.coordination.InMemoryGamelanExecutionBindingStore;
import tech.kayys.andalus.harness.execution.state.DefaultExecutionState;
import tech.kayys.andalus.harness.execution.state.ExecutionCursor;
import tech.kayys.andalus.harness.execution.state.ExecutionData;
import tech.kayys.andalus.harness.execution.state.ExecutionId;
import tech.kayys.andalus.harness.execution.state.ExecutionState;
import tech.kayys.andalus.harness.execution.state.ExecutionStatus;
import tech.kayys.gamelan.engine.error.ErrorInfo;
import tech.kayys.gamelan.engine.node.NodeDispatchReservation;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Source-aligned completion-bridge tests (6.17.4): Claw terminal execution state →
 * Gamelan {@code NodeExecutionResult} → {@code WorkflowRunManager.handleNodeResult},
 * honoring Gamelan's contract (only COMPLETED/FAILED, attempt identity preserved,
 * cancellation expressed as a coded failure).
 */
class GamelanClawDurableBridgeTest {

    private InMemoryGamelanExecutionBindingStore store;
    private RecordingWorkflowRunManager gamelan;
    private GamelanClawDurableBridge bridge;

    @BeforeEach
    void setUp() {
        store = new InMemoryGamelanExecutionBindingStore();
        gamelan = new RecordingWorkflowRunManager();
        bridge = new GamelanClawDurableBridge(store, gamelan.manager());
    }

    private static NodeDispatchReservation reservation(String run, String node, int attempt) {
        return NodeDispatchReservation.reserved(
                WorkflowRunId.of(run), TenantId.of("tenant-1"), NodeId.of(node), attempt);
    }

    private static ExecutionState clawState(String executionId, ExecutionStatus status, Map<String, Object> data) {
        var ctx = new DefaultHarnessContext(
                DefaultHarnessIdentity.of("agent-1"), DefaultHarnessSession.createNew(), Map.of());
        return new DefaultExecutionState(ExecutionId.of(executionId), status, 1L, ctx,
                ExecutionCursor.initial(), new ExecutionData(data, Map.of()));
    }

    // -- dispatch -----------------------------------------------------------

    @Test
    void bindReservation_createsAttemptScopedBinding() {
        var binding = bridge.bindReservation(reservation("run-1", "node-A", 2)).orElseThrow();

        assertEquals("tenant-1:run-1:node-A:2", binding.dispatchId());
        assertEquals(2, binding.attempt());
        assertNotEquals("run-1", binding.executionId().value());
        assertNotEquals("node-A", binding.executionId().value());
    }

    @Test
    void bindReservation_skippedReservation_returnsEmpty() {
        var skipped = NodeDispatchReservation.skipped(
                WorkflowRunId.of("run-1"), TenantId.of("tenant-1"), NodeId.of("node-A"), "not ready");

        assertTrue(bridge.bindReservation(skipped).isEmpty());
        assertTrue(store.findActiveForNode("tenant-1", "run-1", "node-A").isEmpty());
    }

    @Test
    void bindReservation_gamelanRetry_createsDistinctDispatch() {
        var attempt1 = bridge.bindReservation(reservation("run-1", "node-A", 1)).orElseThrow();
        var attempt2 = bridge.bindReservation(reservation("run-1", "node-A", 2)).orElseThrow();

        assertNotEquals(attempt1.executionId(), attempt2.executionId());
        assertNotEquals(attempt1.dispatchId(), attempt2.dispatchId());
    }

    // -- completion (trusted execution plane) --------------------------------

    @Test
    void completedExecution_reportedAsCompletedWithOutput() {
        var binding = bridge.bindReservation(reservation("run-1", "node-A", 1)).orElseThrow();
        bridge.onExecutionCompleted(binding,
                clawState(binding.executionId().value(), ExecutionStatus.COMPLETED, Map.of("answer", 42)));

        assertEquals(1, gamelan.nodeResults.size());
        var recorded = gamelan.nodeResults.get(0);
        NodeExecutionResult result = recorded.result();
        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertNull(result.error(), "COMPLETED must not carry an error (Gamelan contract)");
        assertEquals(42, result.output().get("answer"));
        assertEquals(binding.executionId().value(),
                result.output().get(GamelanNodeResultMapper.META_EXECUTION_ID));
        assertEquals(binding.attempt(), result.attempt());
        assertEquals("run-1", recorded.runId());
        assertEquals("tenant-1", recorded.tenantId());
        // binding is completed but retained
        assertFalse(store.find("tenant-1", "run-1", "node-A", 1).orElseThrow().isActive());
    }

    @Test
    void failedExecution_reportedAsFailedWithError() {
        var binding = bridge.bindReservation(reservation("run-1", "node-F", 1)).orElseThrow();
        bridge.onExecutionFailed(binding, "tool exploded");

        assertEquals(1, gamelan.nodeResults.size());
        NodeExecutionResult result = gamelan.nodeResults.get(0).result();
        assertEquals(NodeExecutionStatus.FAILED, result.status());
        assertNotNull(result.error(), "FAILED must carry an error (Gamelan contract)");
        assertEquals(GamelanNodeResultMapper.ERROR_CODE_FAILED, result.error().code());
        assertEquals("tool exploded", result.error().message());
    }

    @Test
    void canceledExecution_reportedAsCancelled() {
        // 6.17.5.4: Gamelan supports CANCELLED at node level — cancellation is
        // reported as a terminal cancellation, not a failure.
        var binding = bridge.bindReservation(reservation("run-1", "node-C", 1)).orElseThrow();
        bridge.onExecutionCancelled(binding);

        NodeExecutionResult result = gamelan.nodeResults.get(0).result();
        assertEquals(NodeExecutionStatus.CANCELLED, result.status());
        assertEquals(GamelanNodeResultMapper.ERROR_CODE_CANCELED, result.error().code());
        assertInstanceOf(CancelledNodeExecutionResult.class, result);
    }

    @Test
    void timeoutExecution_reportedAsFailedWithTimeoutCode() {
        // 6.17.4.3: Gamelan has no TIMEOUT — map to FAILED + EXECUTION_TIMEOUT.
        var binding = bridge.bindReservation(reservation("run-1", "node-T2", 1)).orElseThrow();
        bridge.reportTerminal(binding,
                clawState(binding.executionId().value(), ExecutionStatus.TIMEOUT, Map.of()), null);

        NodeExecutionResult result = gamelan.nodeResults.get(0).result();
        assertEquals(NodeExecutionStatus.FAILED, result.status());
        assertEquals(GamelanNodeResultMapper.ERROR_CODE_TIMEOUT, result.error().code());
    }

    @Test
    void nonTerminalExecution_isRejectedAsGamelanResult() {
        // 6.17.4.2: WAITING is not node completion.
        var binding = bridge.bindReservation(reservation("run-1", "node-W", 1)).orElseThrow();
        var waiting = clawState(binding.executionId().value(), ExecutionStatus.WAITING, Map.of());

        assertThrows(IllegalStateException.class,
                () -> GamelanNodeResultMapper.toNodeResult(binding, waiting, null));
        assertTrue(gamelan.nodeResults.isEmpty());
    }

    @Test
    void staleCompletion_doesNotReachGamelan() {
        AgentExecutionBinding attempt1 = bridge.bindReservation(reservation("run-1", "node-S", 1)).orElseThrow();
        bridge.bindReservation(reservation("run-1", "node-S", 2));

        bridge.onExecutionCompleted(attempt1,
                clawState(attempt1.executionId().value(), ExecutionStatus.COMPLETED, Map.of()));

        assertTrue(gamelan.nodeResults.isEmpty(), "superseded attempt must not report to Gamelan");
    }

    @Test
    void nodeLost_reportsFailNodeExecution() {
        var binding = bridge.bindReservation(reservation("run-1", "node-L", 3)).orElseThrow();

        bridge.markNodeLost(binding, "worker disappeared");

        assertEquals(1, gamelan.lostNodes.size());
        var lost = gamelan.lostNodes.get(0);
        assertEquals("run-1", lost.runId());
        assertEquals(3, lost.attempt());
        assertEquals("worker disappeared", lost.reason());
    }

    @Test
    void gamelanNotificationFailure_isTolerated() {
        gamelan.failNext = "boom";
        var binding = bridge.bindReservation(reservation("run-1", "node-T", 1)).orElseThrow();

        assertDoesNotThrow(() -> bridge.onExecutionCompleted(binding,
                clawState(binding.executionId().value(), ExecutionStatus.COMPLETED, Map.of())));
        assertTrue(store.find("tenant-1", "run-1", "node-T", 1).isPresent(),
                "the durable binding survives so reconciliation can re-report");
    }

    // -- test double ---------------------------------------------------------

    /** Dynamic-proxy fake of the heavy WorkflowRunManager SPI, recording commit calls. */
    static final class RecordingWorkflowRunManager {
        record NodeResultCall(String runId, String tenantId, NodeExecutionResult result) {}
        record LostNodeCall(String runId, String tenantId, String nodeId, int attempt, String reason) {}

        final List<NodeResultCall> nodeResults = new ArrayList<>();
        final List<LostNodeCall> lostNodes = new ArrayList<>();
        volatile String failNext;

        WorkflowRunManager manager() {
            InvocationHandler handler = (Object proxy, Method method, Object[] args) -> {
                if (method.getName().equals("handleNodeResultWithOutcome")) {
                    if (failNext != null) {
                        throw new RuntimeException(failNext);
                    }
                    WorkflowRunId runId = (WorkflowRunId) args[0];
                    TenantId tenantId = (TenantId) args[1];
                    NodeExecutionResult result = (NodeExecutionResult) args[2];
                    nodeResults.add(new NodeResultCall(runId.value(), tenantId.value(), result));
                    return Uni.createFrom().item(
                            new tech.kayys.gamelan.engine.node.NodeResultHandlingOutcome(
                                    runId, tenantId, result.nodeId(), result.attempt(),
                                    tech.kayys.gamelan.engine.node.NodeExecutionResults.Acceptance.ACCEPT,
                                    true, true, true, false));
                }
                if (method.getName().equals("failNodeExecution")) {
                    WorkflowRunId runId = (WorkflowRunId) args[0];
                    TenantId tenantId = (TenantId) args[1];
                    tech.kayys.gamelan.engine.node.NodeId nodeId =
                            (tech.kayys.gamelan.engine.node.NodeId) args[2];
                    int attempt = (int) args[3];
                    String reason = (String) args[5];
                    lostNodes.add(new LostNodeCall(runId.value(), tenantId.value(), nodeId.value(), attempt, reason));
                    return Uni.createFrom().voidItem();
                }
                throw new UnsupportedOperationException("not stubbed: " + method.getName());
            };
            return (WorkflowRunManager) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[]{WorkflowRunManager.class}, handler);
        }
    }
}