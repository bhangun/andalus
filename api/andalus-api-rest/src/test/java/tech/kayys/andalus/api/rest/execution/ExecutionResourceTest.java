package tech.kayys.andalus.api.rest.execution;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.execution.checkpoint.CheckpointStore;
import tech.kayys.andalus.execution.lifecycle.ExecutionState;
import tech.kayys.andalus.execution.recovery.RecoveryManager;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionResourceTest {

    private ExecutionResource resource;
    private ExecutionProducer producer;

    @BeforeEach
    void setUp() {
        producer = new ExecutionProducer();
        resource = new ExecutionResource();
        resource.stateMachine = producer.produceExecutionStateMachine();

        CheckpointStore cpStore = producer.produceCheckpointStore();
        RecoveryManager recManager = producer.produceRecoveryManager(cpStore);
        resource.recoveryCoordinator = producer.produceExecutionRecoveryCoordinator(recManager);
    }

    @Test
    void getsInitialStateAndExecutesValidTransitions() {
        Response stateResp = resource.getState("exec-test-1");
        assertEquals(200, stateResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> stateMap = (Map<String, Object>) stateResp.getEntity();
        assertEquals("CREATED", stateMap.get("state"));

        // Transition: CREATED -> ADMITTED
        Response t1 = resource.transition("exec-test-1", new ExecutionResource.TransitionRequest("ADMITTED", "Admitted to queue"));
        assertEquals(200, t1.getStatus());

        // Transition: ADMITTED -> STARTING
        Response t2 = resource.transition("exec-test-1", new ExecutionResource.TransitionRequest("STARTING", "Worker assigned"));
        assertEquals(200, t2.getStatus());

        // Transition: STARTING -> RUNNING
        Response t3 = resource.transition("exec-test-1", new ExecutionResource.TransitionRequest("RUNNING", "Started main loop"));
        assertEquals(200, t3.getStatus());

        // Transition: RUNNING -> PAUSED
        Response t4 = resource.transition("exec-test-1", new ExecutionResource.TransitionRequest("PAUSED", "User requested pause"));
        assertEquals(200, t4.getStatus());

        // Invalid transition: PAUSED -> COMPLETED (not allowed directly)
        Response t5 = resource.transition("exec-test-1", new ExecutionResource.TransitionRequest("COMPLETED", "Force complete"));
        assertEquals(409, t5.getStatus());

        // Check history
        Response histResp = resource.getHistory("exec-test-1");
        assertEquals(200, histResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> histMap = (Map<String, Object>) histResp.getEntity();
        assertEquals(4, histMap.get("totalTransitions"));
    }

    @Test
    void assessesAndExecutesRecovery() {
        String execId = "exec-crash-1";

        // Assess recovery when no checkpoint exists
        Response assessResp = resource.assessRecovery(execId);
        assertEquals(200, assessResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> assessMap = (Map<String, Object>) assessResp.getEntity();
        assertTrue((Boolean) assessMap.get("recoverable"));
        assertEquals("RESTART", assessMap.get("recommendedMode"));

        // Plan recovery
        Response planResp = resource.planRecovery(execId);
        assertEquals(200, planResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> planMap = (Map<String, Object>) planResp.getEntity();
        assertEquals("RESTART", planMap.get("mode"));

        // Execute recovery
        Response execResp = resource.executeRecovery(execId);
        assertEquals(200, execResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> execMap = (Map<String, Object>) execResp.getEntity();
        assertTrue((Boolean) execMap.get("success"));
        assertEquals("RESTART", execMap.get("mode"));
        assertNotNull(execMap.get("newAttemptId"));
    }
}
