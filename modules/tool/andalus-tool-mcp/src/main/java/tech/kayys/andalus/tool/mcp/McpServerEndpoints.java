package tech.kayys.andalus.tool.mcp;

import tech.kayys.andalus.tool.entity.McpServerRegistry;

final class McpServerEndpoints {

    private McpServerEndpoints() {
    }

    static String endpoint(McpServerRegistry server) {
        if (server == null) {
            return null;
        }
        String url = url(server);
        return url == null ? server.getCommand() : url;
    }

    static String url(McpServerRegistry server) {
        return server == null ? null : blankToNull(server.getUrl());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
