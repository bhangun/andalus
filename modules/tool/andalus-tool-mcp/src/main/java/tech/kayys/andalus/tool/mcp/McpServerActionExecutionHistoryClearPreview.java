package tech.kayys.andalus.tool.mcp;

import java.time.Instant;

public record McpServerActionExecutionHistoryClearPreview(
        int matched,
        Instant previewedAt) {
}
