package tech.kayys.andalus.tool.mcp;

import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.error.AndalusException;
import tech.kayys.andalus.tool.dto.GenerateToolsRequest;
import tech.kayys.andalus.tool.dto.InvocationStatus;
import tech.kayys.andalus.tool.dto.ToolExecutionRequest;
import tech.kayys.andalus.tool.dto.ToolExecutionResult;
import tech.kayys.andalus.tool.dto.ToolGenerationResult;
import tech.kayys.andalus.tool.service.ToolExecutor;
import tech.kayys.andalus.tool.service.ToolGenerationService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.kayys.andalus.tool.mcp.McpResourceTestFixtures.generateToolsRequest;
import static tech.kayys.andalus.tool.mcp.McpResourceTestFixtures.toolExecutionRequest;
import static tech.kayys.andalus.tool.mcp.McpResourceTestFixtures.toolGenerationResult;

class McpResourceTest {

    @Test
    void generateToolsReturnsOkOnSuccess() {
        McpResource resource = resource(
                McpToolGenerationServiceTestDouble.succeeding(toolGenerationResult("default", "tool-a", "tool-b")),
                McpToolExecutorTestDouble.succeeding(
                        ToolExecutionResult.success("tool-a", Map.of("ok", true), 10)));

        GenerateToolsRequest request = generateToolsRequest("req-1", "user-1");

        RestResponse<ToolGenerationResult> response = resource.generateTools(request)
                .await().indefinitely();

        assertEquals(200, response.getStatus());
        assertEquals("default", response.getEntity().namespace());
        assertEquals(2, response.getEntity().toolsGenerated());
    }

    @Test
    void generateToolsWrapsFailureAsAndalusException() {
        McpResource resource = resource(
                McpToolGenerationServiceTestDouble.failing(new RuntimeException("bad source")),
                McpToolExecutorTestDouble.succeeding(ToolExecutionResult.success("tool-a", Map.of(), 1)));

        GenerateToolsRequest request = generateToolsRequest("req-2", "user-2");

        AndalusException error = assertThrows(
                AndalusException.class,
                () -> resource.generateTools(request).await().indefinitely());

        assertTrue(error.getMessage().contains("Generation failed: bad source"));
        assertNotNull(error.getErrorCode());
    }

    @Test
    void executeToolReturnsOkOnSuccess() {
        McpResource resource = resource(
                McpToolGenerationServiceTestDouble.succeeding(toolGenerationResult("default", "tool-a")),
                McpToolExecutorTestDouble.succeeding(
                        ToolExecutionResult.success("tool-a", Map.of("status", "done"), 15)));

        ToolExecutionRequest request = toolExecutionRequest(
                "req-3",
                "user-3",
                "tool-a",
                Map.of("q", "ai"),
                "run-1",
                "agent-1");

        RestResponse<ToolExecutionResult> response = resource.executeTool(request)
                .await().indefinitely();

        assertEquals(200, response.getStatus());
        assertEquals(InvocationStatus.SUCCESS, response.getEntity().status());
        assertEquals("done", response.getEntity().output().get("status"));
    }

    @Test
    void executeToolWrapsFailureAsAndalusException() {
        McpResource resource = resource(
                McpToolGenerationServiceTestDouble.succeeding(toolGenerationResult("default")),
                McpToolExecutorTestDouble.failing(new RuntimeException("executor failure")));

        ToolExecutionRequest request = toolExecutionRequest(
                "req-4",
                "user-4",
                "tool-x",
                Map.of(),
                "run-2",
                "agent-2");

        AndalusException error = assertThrows(
                AndalusException.class,
                () -> resource.executeTool(request).await().indefinitely());

        assertTrue(error.getMessage().contains("Execution failed: executor failure"));
        assertNotNull(error.getErrorCode());
    }

    private McpResource resource(
            ToolGenerationService toolGenerator,
            ToolExecutor toolExecutor) {
        McpResource resource = new McpResource();
        resource.toolGenerator = toolGenerator;
        resource.toolExecutor = toolExecutor;
        return resource;
    }

}
