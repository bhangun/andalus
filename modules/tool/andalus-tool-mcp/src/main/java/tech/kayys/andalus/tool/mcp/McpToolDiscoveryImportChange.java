package tech.kayys.andalus.tool.mcp;

record McpToolDiscoveryImportChange(
        tech.kayys.andalus.tool.entity.McpTool tool,
        boolean reactivated,
        String skippedRetiredToolId) {

    static McpToolDiscoveryImportChange imported(
            tech.kayys.andalus.tool.entity.McpTool tool,
            boolean reactivated) {
        return new McpToolDiscoveryImportChange(tool, reactivated, null);
    }

    static McpToolDiscoveryImportChange skippedRetired(String toolId) {
        return new McpToolDiscoveryImportChange(null, false, toolId);
    }

    boolean skippedRetired() {
        return skippedRetiredToolId != null;
    }
}
