package tech.kayys.andalus.tool.mcp;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.tool.dto.ToolExecutionRequest;
import tech.kayys.andalus.tool.dto.ToolExecutionResult;
import tech.kayys.andalus.tool.service.ToolExecutor;

final class McpToolExecutorTestDouble extends ToolExecutor {
    private final Uni<ToolExecutionResult> result;
    private int calls;
    private ToolExecutionRequest lastRequest;

    private McpToolExecutorTestDouble(Uni<ToolExecutionResult> result) {
        this.result = result;
    }

    static McpToolExecutorTestDouble succeeding(ToolExecutionResult result) {
        return new McpToolExecutorTestDouble(Uni.createFrom().item(result));
    }

    static McpToolExecutorTestDouble failing(RuntimeException failure) {
        return new McpToolExecutorTestDouble(Uni.createFrom().failure(failure));
    }

    int calls() {
        return calls;
    }

    ToolExecutionRequest lastRequest() {
        return lastRequest;
    }

    @Override
    public Uni<ToolExecutionResult> execute(ToolExecutionRequest request) {
        calls++;
        lastRequest = request;
        return result;
    }
}
