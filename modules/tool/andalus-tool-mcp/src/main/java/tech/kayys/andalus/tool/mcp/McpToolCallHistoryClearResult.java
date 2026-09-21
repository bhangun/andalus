package tech.kayys.andalus.tool.mcp;

import java.time.Instant;

public record McpToolCallHistoryClearResult(
        int cleared,
        Instant clearedAt) {
}
