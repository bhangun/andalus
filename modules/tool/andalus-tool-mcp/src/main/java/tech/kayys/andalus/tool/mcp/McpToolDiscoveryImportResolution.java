package tech.kayys.andalus.tool.mcp;

import tech.kayys.andalus.tool.entity.McpServerRegistry;

record McpToolDiscoveryImportResolution(
        McpToolDiscoveryImportRequest request,
        McpServerRegistry server) {
}
