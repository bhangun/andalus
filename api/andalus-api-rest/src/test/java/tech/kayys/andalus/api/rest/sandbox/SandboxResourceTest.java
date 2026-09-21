package tech.kayys.andalus.api.rest.sandbox;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.api.rest.execution.ExecutionProducer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SandboxResourceTest {

    private SandboxResource resource;
    private ExecutionProducer producer;

    @BeforeEach
    void setUp() {
        producer = new ExecutionProducer();
        resource = new SandboxResource();
        resource.sandboxManager = producer.produceSandboxManager();
        resource.profileRegistry = producer.produceSandboxProfileRegistry();
    }

    @Test
    void createsAndManagesSandboxLifecycle() {
        // Create sandbox
        SandboxResource.CreateSandboxRequest req = new SandboxResource.CreateSandboxRequest(
                "sbx-test-01",
                "CODING",
                Map.of()
        );

        Response createResp = resource.createSandbox(req);
        assertEquals(201, createResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> createMap = (Map<String, Object>) createResp.getEntity();
        assertEquals("sbx-test-01", createMap.get("sandboxId"));
        assertEquals("CODING", createMap.get("profile"));
        assertEquals("READY", createMap.get("state"));

        // List sandboxes
        Response listResp = resource.listSandboxes();
        assertEquals(200, listResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> listMap = (Map<String, Object>) listResp.getEntity();
        assertEquals(1, listMap.get("total"));

        // Start sandbox
        Response startResp = resource.startSandbox("sbx-test-01");
        assertEquals(200, startResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> startMap = (Map<String, Object>) startResp.getEntity();
        assertEquals("RUNNING", startMap.get("state"));

        // Health check
        Response healthResp = resource.getHealth("sbx-test-01");
        assertEquals(200, healthResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> healthMap = (Map<String, Object>) healthResp.getEntity();
        assertTrue((Boolean) healthMap.get("healthy"));

        // Pause sandbox
        Response pauseResp = resource.pauseSandbox("sbx-test-01");
        assertEquals(200, pauseResp.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> pauseMap = (Map<String, Object>) pauseResp.getEntity();
        assertEquals("PAUSED", pauseMap.get("state"));

        // Destroy sandbox
        Response destroyResp = resource.destroySandbox("sbx-test-01");
        assertEquals(200, destroyResp.getStatus());
    }
}
