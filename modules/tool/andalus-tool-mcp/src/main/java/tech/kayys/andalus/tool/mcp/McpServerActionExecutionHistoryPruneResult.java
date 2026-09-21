package tech.kayys.andalus.tool.mcp;

import java.time.Instant;

public record McpServerActionExecutionHistoryPruneResult(
        int pruned,
        Instant prunedAt) {
}
