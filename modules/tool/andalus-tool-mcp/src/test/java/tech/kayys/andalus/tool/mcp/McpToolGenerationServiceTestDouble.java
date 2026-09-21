package tech.kayys.andalus.tool.mcp;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.tool.dto.GenerateToolsRequest;
import tech.kayys.andalus.tool.dto.ToolGenerationResult;
import tech.kayys.andalus.tool.service.ToolGenerationService;

final class McpToolGenerationServiceTestDouble implements ToolGenerationService {
    private final Uni<ToolGenerationResult> result;
    private int calls;
    private GenerateToolsRequest lastRequest;

    private McpToolGenerationServiceTestDouble(Uni<ToolGenerationResult> result) {
        this.result = result;
    }

    static McpToolGenerationServiceTestDouble succeeding(ToolGenerationResult result) {
        return new McpToolGenerationServiceTestDouble(Uni.createFrom().item(result));
    }

    static McpToolGenerationServiceTestDouble failing(RuntimeException failure) {
        return new McpToolGenerationServiceTestDouble(Uni.createFrom().failure(failure));
    }

    int calls() {
        return calls;
    }

    GenerateToolsRequest lastRequest() {
        return lastRequest;
    }

    @Override
    public Uni<ToolGenerationResult> generateTools(GenerateToolsRequest request) {
        calls++;
        lastRequest = request;
        return result;
    }
}
