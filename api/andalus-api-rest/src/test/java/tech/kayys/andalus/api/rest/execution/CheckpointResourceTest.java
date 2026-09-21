package tech.kayys.andalus.api.rest.execution;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointResourceTest {

    private CheckpointResource resource;
    private ExecutionProducer producer;

    @BeforeEach
    void setUp() {
        producer = new ExecutionProducer();
        resource = new CheckpointResource();
        resource.checkpointStore = producer.produceCheckpointStore();
    }

    @Test
    void savesQueriesAndVerifiesCheckpointIntegrity() {
        CheckpointResource.SaveCheckpointRequest req = new CheckpointResource.SaveCheckpointRequest(
                "cp-001",
                "exec-100",
                "att-001",
                1L,
                null,
                "{\"step\": 1, \"memory\": \"ok\"}",
                List.of("state:var1"),
                List.of("art-001"),
                Map.of("env", "prod")
        );

        Response saveResp = resource.saveCheckpoint(req);
        assertEquals(201, saveResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> saveMap = (Map<String, Object>) saveResp.getEntity();
        assertEquals("cp-001", saveMap.get("checkpointId"));
        assertNotNull(saveMap.get("checksum"));

        // Query by id
        Response getResp = resource.getCheckpoint("cp-001");
        assertEquals(200, getResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> getMap = (Map<String, Object>) getResp.getEntity();
        assertEquals("exec-100", getMap.get("executionId"));

        // Verify checksum
        Response verifyResp = resource.verifyCheckpoint("cp-001");
        assertEquals(200, verifyResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> verifyMap = (Map<String, Object>) verifyResp.getEntity();
        assertTrue((Boolean) verifyMap.get("valid"));

        // List by execution
        Response listResp = resource.listByExecution("exec-100");
        assertEquals(200, listResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> listMap = (Map<String, Object>) listResp.getEntity();
        assertEquals(1, listMap.get("total"));

        // Get latest
        Response latestResp = resource.getLatest("exec-100");
        assertEquals(200, latestResp.getStatus());
    }
}
