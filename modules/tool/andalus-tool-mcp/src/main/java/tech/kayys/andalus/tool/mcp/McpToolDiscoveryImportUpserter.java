package tech.kayys.andalus.tool.mcp;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.tool.repository.ToolRepository;

import java.util.List;

final class McpToolDiscoveryImportUpserter {

    private McpToolDiscoveryImportUpserter() {
    }

    static Uni<McpToolDiscoveryImportChanges> upsertTools(
            ToolRepository toolRepository,
            String requestId,
            McpToolDiscoveryImportRequest request,
            String namespace,
            List<McpDiscoveredTool> tools) {
        Uni<McpToolDiscoveryImportChanges> chain = Uni.createFrom().item(McpToolDiscoveryImportChanges.empty());
        for (McpDiscoveredTool discoveredTool : tools) {
            tech.kayys.andalus.tool.entity.McpTool source = McpToolDiscoveryImportMapper.toEntity(
                    requestId,
                    request,
                    namespace,
                    discoveredTool);
            chain = chain.flatMap(changes -> upsertTool(toolRepository, requestId, request, source)
                    .map(change -> {
                        changes.add(change);
                        return changes;
                    }));
        }
        return chain.map(McpToolDiscoveryImportChanges::copy);
    }

    private static Uni<McpToolDiscoveryImportChange> upsertTool(
            ToolRepository toolRepository,
            String requestId,
            McpToolDiscoveryImportRequest request,
            tech.kayys.andalus.tool.entity.McpTool source) {
        return toolRepository.findByRequestIdAndToolId(requestId, source.getToolId())
                .flatMap(existing -> importOrSkipRetired(toolRepository, request, existing, source));
    }

    private static Uni<McpToolDiscoveryImportChange> importOrSkipRetired(
            ToolRepository toolRepository,
            McpToolDiscoveryImportRequest request,
            tech.kayys.andalus.tool.entity.McpTool existing,
            tech.kayys.andalus.tool.entity.McpTool source) {
        if (McpToolDiscoveryImportPolicy.shouldSkipRetired(existing, request)) {
            return Uni.createFrom().item(McpToolDiscoveryImportChange.skippedRetired(source.getToolId()));
        }
        boolean reactivated = McpToolDiscoveryImportPolicy.isReactivationCandidate(existing);
        return (existing == null
                ? toolRepository.save(source)
                : Uni.createFrom().item(McpToolDiscoveryImportMapper.updateExisting(existing, source)))
                .map(saved -> McpToolDiscoveryImportChange.imported(saved, reactivated));
    }
}
