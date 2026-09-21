package tech.kayys.andalus.tool.mcp;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.tool.entity.McpServerRegistry;
import tech.kayys.andalus.tool.repository.McpServerRegistryRepository;
import tech.kayys.andalus.tool.repository.RegistrySyncHistoryRepository;

final class McpToolDiscoveryRegisteredServerSync {

    private McpToolDiscoveryRegisteredServerSync() {
    }

    static Uni<McpToolDiscoverySyncResult> sync(
            McpServerRegistryRepository serverRegistryRepository,
            McpToolDiscoveryImportService importService,
            RegistrySyncHistoryRepository historyRepository,
            String requestId,
            String serverName) {
        if (serverName == null || serverName.isBlank()) {
            return Uni.createFrom().item(McpToolDiscoverySyncResults.warning(
                    McpToolDiscoverySyncMessages.serverNameRequired()));
        }
        if (serverRegistryRepository == null) {
            return Uni.createFrom().item(McpToolDiscoverySyncResults.warning(
                    McpToolDiscoverySyncMessages.serverRegistryNotConfigured(serverName)));
        }
        return serverRegistryRepository.findByRequestIdAndName(requestId, serverName)
                .flatMap(server -> syncResolvedServer(importService, historyRepository, server, serverName));
    }

    private static Uni<McpToolDiscoverySyncResult> syncResolvedServer(
            McpToolDiscoveryImportService importService,
            RegistrySyncHistoryRepository historyRepository,
            McpServerRegistry server,
            String serverName) {
        if (server == null) {
            return Uni.createFrom().item(McpToolDiscoverySyncResults.warning(
                    McpToolDiscoverySyncMessages.serverNotFound(serverName)));
        }
        if (!server.isEnabled()) {
            return McpToolDiscoverySyncRunner.recordServerError(
                    historyRepository,
                    server,
                    McpToolDiscoverySyncMessages.serverDisabled(server.getName()));
        }
        return McpToolDiscoverySyncRunner.syncServer(importService, historyRepository, server)
                .map(McpToolDiscoverySyncResults::singleServer);
    }
}
