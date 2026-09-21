package tech.kayys.andalus.tool.mcp;

import tech.kayys.andalus.tool.service.EditionModeService;

final class EditionModeServiceTestDouble extends EditionModeService {
    private final boolean mcpRegistryDatabaseSupported;

    private EditionModeServiceTestDouble(boolean mcpRegistryDatabaseSupported) {
        this.mcpRegistryDatabaseSupported = mcpRegistryDatabaseSupported;
    }

    static EditionModeServiceTestDouble mcpRegistryDatabaseSupported(boolean supported) {
        return new EditionModeServiceTestDouble(supported);
    }

    @Override
    public boolean supportsMcpRegistryDatabase() {
        return mcpRegistryDatabaseSupported;
    }
}
