package tech.kayys.andalus.tool.mcp;

import java.time.Instant;

public record McpToolCallHistoryClearPreview(
        int matched,
        Instant previewedAt) {
}
