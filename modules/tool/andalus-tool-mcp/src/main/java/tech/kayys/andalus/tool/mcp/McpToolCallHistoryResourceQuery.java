package tech.kayys.andalus.tool.mcp;

import tech.kayys.andalus.tool.dto.ToolRequestContext;

record McpToolCallHistoryResourceQuery(
        String runId,
        McpToolCallHistoryQuery historyQuery,
        McpToolCallHistoryQuery summaryQuery) {

    static McpToolCallHistoryResourceQuery from(
            McpToolCallHistoryQueryParams query,
            ToolRequestContext requestContext) {
        McpToolCallHistoryQueryParams params =
                McpResourceSupport.beanParam(query, McpToolCallHistoryQueryParams::new);
        return new McpToolCallHistoryResourceQuery(
                params.runId(McpResourceSupport.currentRequestId(requestContext)),
                params.toQuery(),
                params.toUnpagedQuery());
    }
}
