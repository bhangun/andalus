package tech.kayys.andalus.tool.mcp;

import java.time.Instant;

record McpToolServerHealthSyncPolicyStatus(
        Instant nextSyncAt,
        boolean syncDue,
        String error) {
}
