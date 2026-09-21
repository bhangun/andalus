package tech.kayys.andalus.operator.authorization;

import tech.kayys.andalus.spi.operator.authorization.OperatorPermissionDefinition;
import tech.kayys.andalus.spi.operator.authorization.OperatorPermissionRegistry;
import tech.kayys.andalus.spi.operator.authorization.OperatorScope;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standard thread-safe operator permission registry preloaded with system definitions.
 */
public class DefaultOperatorPermissionRegistry implements OperatorPermissionRegistry {

    private final Map<String, OperatorPermissionDefinition> permissions = new ConcurrentHashMap<>();

    public DefaultOperatorPermissionRegistry() {
        registerDefaults();
    }

    @Override
    public void register(OperatorPermissionDefinition permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is required");
        }
        if (permission.id() == null || permission.id().isBlank()) {
            throw new IllegalArgumentException("permission id is required");
        }
        permissions.put(permission.id(), permission);
    }

    @Override
    public Optional<OperatorPermissionDefinition> find(String permissionId) {
        if (permissionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(permissions.get(permissionId));
    }

    @Override
    public List<OperatorPermissionDefinition> list() {
        return permissions.values()
                .stream()
                .sorted(Comparator.comparing(OperatorPermissionDefinition::id))
                .toList();
    }

    private void registerDefaults() {
        // Plugin
        register(new OperatorPermissionDefinition(
                "operator.plugins.read",
                "Read plugin information",
                OperatorScope.PLATFORM,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.plugins.enable",
                "Enable a plugin",
                OperatorScope.PLATFORM,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.plugins.disable",
                "Disable a plugin",
                OperatorScope.PLATFORM,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.plugins.unload",
                "Unload a plugin",
                OperatorScope.PLATFORM,
                true,
                true
        ));

        // Sandbox
        register(new OperatorPermissionDefinition(
                "operator.sandboxes.read",
                "Read sandbox information",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.sandboxes.health",
                "Read sandbox health",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.sandboxes.metrics",
                "Read sandbox metrics",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.sandboxes.diagnostics",
                "Read sandbox diagnostics",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.sandboxes.stop",
                "Stop a sandbox",
                OperatorScope.TENANT,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.sandboxes.destroy",
                "Destroy a sandbox",
                OperatorScope.TENANT,
                true,
                true
        ));

        // Execution
        register(new OperatorPermissionDefinition(
                "operator.executions.read",
                "Read execution information",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.executions.inspect",
                "Inspect execution details",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.executions.pause",
                "Pause an execution",
                OperatorScope.TENANT,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.executions.resume",
                "Resume an execution",
                OperatorScope.TENANT,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.executions.cancel",
                "Cancel an execution",
                OperatorScope.TENANT,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.executions.retry",
                "Retry an execution",
                OperatorScope.TENANT,
                true,
                true
        ));

        // Session
        register(new OperatorPermissionDefinition(
                "operator.sessions.read",
                "Read session metadata",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.sessions.inspect",
                "Inspect session details",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.sessions.suspend",
                "Suspend a session",
                OperatorScope.TENANT,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.sessions.resume",
                "Resume a session",
                OperatorScope.TENANT,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.sessions.close",
                "Close a session",
                OperatorScope.TENANT,
                true,
                true
        ));

        // Tool & Capability
        register(new OperatorPermissionDefinition(
                "operator.tools.read",
                "Read tool descriptions",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.tools.inspect",
                "Inspect tool metadata",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.capabilities.read",
                "Read capabilities",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.capabilities.inspect",
                "Inspect capability details",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.capabilities.enable",
                "Enable capability",
                OperatorScope.TENANT,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.capabilities.disable",
                "Disable capability",
                OperatorScope.TENANT,
                true,
                true
        ));

        // Diagnostics
        register(new OperatorPermissionDefinition(
                "operator.diagnostics.read",
                "Read diagnostic summaries",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.diagnostics.inspect",
                "Inspect component diagnostics",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.diagnostics.platform",
                "Inspect platform-wide diagnostics",
                OperatorScope.PLATFORM,
                false,
                true
        ));

        // Configuration
        register(new OperatorPermissionDefinition(
                "operator.configuration.read",
                "Read configuration metadata",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.configuration.inspect",
                "Inspect configuration values",
                OperatorScope.TENANT,
                false,
                false
        ));
        register(new OperatorPermissionDefinition(
                "operator.configuration.update",
                "Update runtime configuration",
                OperatorScope.TENANT,
                true,
                true
        ));
        register(new OperatorPermissionDefinition(
                "operator.configuration.activate",
                "Activate configuration",
                OperatorScope.TENANT,
                true,
                true
        ));
    }
}
