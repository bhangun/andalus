package tech.kayys.andalus.agent.backend.gamelan.claw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.harness.context.DefaultHarnessContext;
import tech.kayys.andalus.harness.context.DefaultHarnessIdentity;
import tech.kayys.andalus.harness.context.DefaultHarnessSession;
import tech.kayys.andalus.harness.coordination.AgentExecutionBinding;
import tech.kayys.andalus.harness.coordination.GamelanDispatchDescriptor;
import tech.kayys.andalus.harness.coordination.InMemoryGamelanExecutionBindingStore;
import tech.kayys.andalus.harness.execution.state.DefaultExecutionState;
import tech.kayys.andalus.harness.execution.state.ExecutionCursor;
import tech.kayys.andalus.harness.execution.state.ExecutionData;
import tech.kayys.andalus.harness.execution.state.ExecutionId;
import tech.kayys.andalus.harness.execution.state.ExecutionStatus;
import tech.kayys.andalus.harness.execution.state.InMemoryExecutionStateStore;
import tech.kayys.gamelan.engine.node.NodeDefinition;
import tech.kayys.gamelan.engine.node.NodeExecution;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.node.NodeType;
import tech.kayys.gamelan.engine.run.RunStatus;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunSnapshot;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Startup-adapter tests against real Gamelan snapshot types (6.17.3):
 * active-dispatch discovery from {@code WorkflowRunSnapshot} and application of
 * reconciliation decisions onto Gamelan's commit boundaries.
 */
class GamelanClawStartupAdapterTest {

    private InMemoryGamelanExecutionBindingStore store;
    private InMemoryExecutionStateStore states;
    private GamelanClawDurableBridgeTest.RecordingWorkflowRunManager gamelan;
    private GamelanClawDurableBridge bridge;
    private GamelanClawStartupAdapter adapter;

    @BeforeEach
    void setUp() {
        store = new InMemoryGamelanExecutionBindingStore();
        states = new InMemoryExecutionStateStore();
        gamelan = new GamelanClawDurableBridgeTest.RecordingWorkflowRunManager();
        bridge = new GamelanClawDurableBridge(store, gamelan.manager());
        adapter = new GamelanClawStartupAdapter(bridge, store, states);
    }

    private void saveClawState(ExecutionId id, ExecutionStatus status) {
        var ctx = new DefaultHarnessContext(
                DefaultHarnessIdentity.of("agent-1"), DefaultHarnessSession.createNew(), Map.of());
        states.save(new DefaultExecutionState(id, status, 0L, ctx,
                ExecutionCursor.initial(), ExecutionData.empty()), -1L);
    }

    private static NodeExecution nodeExecution(String node, NodeExecutionStatus status, int attempt) {
        NodeDefinition definition = NodeDefinition.builder()
                .id(NodeId.of(node)).name(node).type(NodeType.TASK).build();
        NodeExecution execution = NodeExecution.create(NodeId.of(node), definition);
        execution.setStatus(status);
        execution.setAttempt(attempt);
        return execution;
    }

    private static WorkflowRunSnapshot snapshot(Map<NodeId, NodeExecution> nodes) {
        return new WorkflowRunSnapshot(
                WorkflowRunId.of("run-1"), TenantId.of("tenant-1"), WorkflowDefinitionId.of("wf-1"),
                RunStatus.RUNNING, Map.of(), nodes, List.of(), null, Map.of(), null,
                Instant.now(), Instant.now(), null, 1L);
    }

    @Test
    void activeDispatches_scansSnapshotForInFlightAttempts() {
        Map<NodeId, NodeExecution> nodes = new LinkedHashMap<>();
        nodes.put(NodeId.of("node-A"), nodeExecution("node-A", NodeExecutionStatus.EXECUTING, 1));
        nodes.put(NodeId.of("node-B"), nodeExecution("node-B", NodeExecutionStatus.RUNNING, 2));
        nodes.put(NodeId.of("node-Done"), nodeExecution("node-Done", NodeExecutionStatus.COMPLETED, 1));
        nodes.put(NodeId.of("node-Idle"), nodeExecution("node-Idle", NodeExecutionStatus.PENDING, 0));

        List<GamelanDispatchDescriptor> dispatches = adapter.activeDispatches(snapshot(nodes));

        assertEquals(List.of("node-A:1", "node-B:2"),
                dispatches.stream().map(d -> d.nodeId() + ":" + d.attempt()).toList(),
                "only in-flight attempts with a reservation are discovered");
    }

    @Test
    void reconcileAndApply_reportsTerminalBindingToGamelan() {
        Map<NodeId, NodeExecution> nodes = new LinkedHashMap<>();
        nodes.put(NodeId.of("node-A"), nodeExecution("node-A", NodeExecutionStatus.EXECUTING, 1));
        AgentExecutionBinding binding =
                bridge.bindNodeExecution("tenant-1", "run-1", "node-A", 1, "res-1");
        saveClawState(binding.executionId(), ExecutionStatus.COMPLETED);

        var applied = adapter.reconcileAndApply(snapshot(nodes));

        assertEquals(1, applied.size());
        assertEquals(1, gamelan.nodeResults.size(), "completed Claw execution is re-reported to Gamelan");
        assertEquals(NodeExecutionStatus.COMPLETED, gamelan.nodeResults.get(0).result().status());
    }

    @Test
    void reconcileAndApply_marksLostBindingViaFailNodeExecution() {
        Map<NodeId, NodeExecution> nodes = new LinkedHashMap<>();
        nodes.put(NodeId.of("node-B"), nodeExecution("node-B", NodeExecutionStatus.EXECUTING, 1));
        bridge.bindNodeExecution("tenant-1", "run-1", "node-B", 1, "res-1");
        // the Claw worker disappeared — no execution state

        var applied = adapter.reconcileAndApply(snapshot(nodes));

        assertEquals(1, applied.size());
        assertEquals(1, gamelan.lostNodes.size());
        assertEquals("node-B", gamelan.lostNodes.get(0).nodeId());
        assertTrue(gamelan.nodeResults.isEmpty());
    }

    @Test
    void reconcileAndApply_leavesLiveExecutionsAndUnboundDispatchesAlone() {
        Map<NodeId, NodeExecution> nodes = new LinkedHashMap<>();
        nodes.put(NodeId.of("node-W"), nodeExecution("node-W", NodeExecutionStatus.EXECUTING, 1));
        nodes.put(NodeId.of("node-N"), nodeExecution("node-N", NodeExecutionStatus.RUNNING, 1));
        AgentExecutionBinding live =
                bridge.bindNodeExecution("tenant-1", "run-1", "node-W", 1, "res-1");
        saveClawState(live.executionId(), ExecutionStatus.WAITING);
        // node-N has no Claw binding at all

        var applied = adapter.reconcileAndApply(snapshot(nodes));

        assertTrue(applied.isEmpty(), "KEEP_WAITING and REBIND_REQUIRED require no Gamelan mutation");
        assertTrue(gamelan.nodeResults.isEmpty());
        assertTrue(gamelan.lostNodes.isEmpty());
    }

    @Test
    void reconcileAndApply_isRepeatable() {
        Map<NodeId, NodeExecution> nodes = new LinkedHashMap<>();
        nodes.put(NodeId.of("node-R"), nodeExecution("node-R", NodeExecutionStatus.EXECUTING, 1));
        AgentExecutionBinding binding =
                bridge.bindNodeExecution("tenant-1", "run-1", "node-R", 1, "res-1");
        saveClawState(binding.executionId(), ExecutionStatus.FAILED);

        var snap = snapshot(nodes);
        adapter.reconcileAndApply(snap);
        var second = adapter.reconcileAndApply(snap);

        assertEquals(1, second.size(), "repeat startup reconciliation reaches the same decision");
        assertEquals(1, gamelan.nodeResults.size(),
                "the second pass is absorbed by the bridge's stale/duplicate protection — "
                        + "exactly one Gamelan report ever lands");
        assertEquals(NodeExecutionStatus.FAILED, gamelan.nodeResults.get(0).result().status());
    }
}