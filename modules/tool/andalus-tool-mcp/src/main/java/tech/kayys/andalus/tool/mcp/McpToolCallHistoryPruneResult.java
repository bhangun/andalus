package tech.kayys.andalus.tool.mcp;

import java.time.Instant;

public record McpToolCallHistoryPruneResult(
        int pruned,
        Instant prunedAt) {
}
