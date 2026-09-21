package tech.kayys.andalus.tool.mcp;

import tech.kayys.andalus.tool.entity.McpServerRegistry;

final class McpServerRegistryEntries {

    private McpServerRegistryEntries() {
    }

    static McpServerRegistryEntry from(McpServerRegistry server) {
        return new McpServerRegistryEntry(
                server.getName(),
                server.getTransport(),
                McpServerEndpoints.endpoint(server),
                server.isEnabled(),
                server.getSource(),
                server.getSyncSchedule(),
                server.getLastSyncAt(),
                server.getCreatedAt(),
                server.getUpdatedAt());
    }
}
