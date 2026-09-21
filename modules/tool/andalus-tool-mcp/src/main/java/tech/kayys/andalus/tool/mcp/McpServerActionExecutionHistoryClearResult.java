package tech.kayys.andalus.tool.mcp;

import java.time.Instant;

public record McpServerActionExecutionHistoryClearResult(
        int cleared,
        Instant clearedAt) {
}
