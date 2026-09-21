package tech.kayys.andalus.tool.mcp;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;
import tech.kayys.andalus.error.ErrorCode;
import tech.kayys.andalus.tool.dto.GenerateToolsRequest;
import tech.kayys.andalus.tool.dto.ToolExecutionRequest;
import tech.kayys.andalus.tool.dto.ToolExecutionResult;
import tech.kayys.andalus.tool.dto.ToolGenerationResult;
import tech.kayys.andalus.tool.service.ToolExecutor;
import tech.kayys.andalus.tool.service.ToolGenerationService;

/**
 * ============================================================================
 * MCP SERVER REST API
 * ============================================================================
 */
@Path("/mcp")
@Tag(name = "MCP", description = "MCP Server API")
public class McpResource {

    @Inject
    ToolGenerationService toolGenerator;

    @Inject
    ToolExecutor toolExecutor;

    /**
     * Generate tools from OpenAPI specification
     */
    @POST
    @Path("/tools/generate")
    @Authenticated
    @Operation(summary = "Generate tools from OpenAPI spec")
    public Uni<RestResponse<ToolGenerationResult>> generateTools(
            @Valid GenerateToolsRequest request) {

        return McpResourceSupport.ok(toolGenerator.generateTools(request))
                .onFailure().transform(McpResourceFailures.andalus(
                        ErrorCode.VALIDATION_FAILED,
                        "Generation failed"));
    }

    /**
     * Execute MCP tool
     */
    @POST
    @Path("/tools/execute")
    @Authenticated
    @Operation(summary = "Execute MCP tool")
    public Uni<RestResponse<ToolExecutionResult>> executeTool(
            @Valid ToolExecutionRequest request) {

        return McpResourceSupport.ok(toolExecutor.execute(request))
                .onFailure().transform(McpResourceFailures.andalus(
                        ErrorCode.TOOL_EXECUTION_FAILED,
                        "Execution failed"));
    }

}
