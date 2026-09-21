package tech.kayys.andalus.tool.mcp;

import io.smallrye.mutiny.Uni;

public interface McpToolDiscoveryClient {

    Uni<McpToolDiscoveryResult> discoverTools(McpToolDiscoveryRequest request);
}
