package tech.kayys.andalus.operator.runtime.configuration;

import tech.kayys.andalus.spi.operator.OperatorAuthorization;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.OperatorResult;
import tech.kayys.andalus.spi.operator.audit.OperatorAuditOutcome;
import tech.kayys.andalus.spi.operator.audit.OperatorAuditService;
import tech.kayys.andalus.spi.operator.authorization.OperatorAuthorizationRequest;
import tech.kayys.andalus.spi.operator.authorization.OperatorScope;
import tech.kayys.andalus.spi.operator.configuration.ConfigurationOperatorPermissions;
import tech.kayys.andalus.spi.operator.configuration.ConfigurationOperatorService;
import tech.kayys.andalus.spi.operator.configuration.ConfigurationSummary;
import tech.kayys.andalus.spi.operator.configuration.ConfigurationUpdateRequest;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standard implementation of ConfigurationOperatorService enforcing authorization,
 * secret masking, tenant isolation, and audit emission.
 */
public class DefaultConfigurationOperatorService implements ConfigurationOperatorService {

    private final OperatorAuthorization authorization;
    private final OperatorAuditService auditService;
    private final Map<String, ConfigRecord> configurations = new ConcurrentHashMap<>();
    private volatile String activeConfigId = "default";

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "secret", "token", "key", "credential", "auth", "private"
    );

    public DefaultConfigurationOperatorService(
            OperatorAuthorization authorization,
            OperatorAuditService auditService) {
        this.authorization = Objects.requireNonNull(authorization, "authorization");
        this.auditService = Objects.requireNonNull(auditService, "auditService");
        initializeDefaults();
    }

    @Override
    public String id() {
        return "configuration-operator";
    }

    @Override
    public String name() {
        return "Configuration Operator Service";
    }

    @Override
    public String description() {
        return "Operator control plane service for inspecting and modifying configuration";
    }

    private void initializeDefaults() {
        Map<String, Object> defaultValues = new LinkedHashMap<>();
        defaultValues.put("andalus.runtime.maxThreads", 200);
        defaultValues.put("andalus.runtime.defaultTimeout", 30000);
        defaultValues.put("andalus.security.apiKey", "super-secret-system-key-1234");
        defaultValues.put("andalus.database.password", "db-pass-5678");

        configurations.put("default", new ConfigRecord(
                "default",
                "default-config",
                "DEFAULT",
                "APPLICATION",
                null,
                "ACTIVE",
                defaultValues,
                Instant.now()
        ));
    }

    @Override
    public OperatorResult<List<ConfigurationSummary>> list(OperatorContext context) {
        try {
            authorization.require(context, ConfigurationOperatorPermissions.READ.id());

            List<ConfigurationSummary> list = configurations.values().stream()
                    .filter(c -> isVisibleToTenant(c, context))
                    .map(this::toSummaryMasked)
                    .toList();

            auditService.record(
                    context,
                    "operator.configuration.list",
                    ConfigurationOperatorPermissions.READ.id(),
                    OperatorScope.TENANT,
                    "configuration",
                    null,
                    false,
                    false,
                    OperatorAuditOutcome.SUCCESS,
                    "AUTHORIZED",
                    "Listed configurations"
            );

            return OperatorResult.success(list);
        } catch (Exception e) {
            auditService.record(
                    context,
                    "operator.configuration.list",
                    ConfigurationOperatorPermissions.READ.id(),
                    OperatorScope.TENANT,
                    "configuration",
                    null,
                    false,
                    false,
                    OperatorAuditOutcome.FAILURE,
                    "FAILURE",
                    e.getMessage()
            );
            return OperatorResult.failure("CONFIG_LIST_FAILED", e.getMessage());
        }
    }

    @Override
    public OperatorResult<ConfigurationSummary> inspect(OperatorContext context, String configurationId) {
        if (configurationId == null || configurationId.isBlank()) {
            return OperatorResult.failure("INVALID_CONFIG_ID", "configurationId must not be blank");
        }

        try {
            authorization.require(
                    OperatorAuthorizationRequest.tenant(
                            context,
                            ConfigurationOperatorPermissions.INSPECT.id(),
                            "configuration",
                            configurationId
                    )
            );

            ConfigRecord record = configurations.get(configurationId);
            if (record == null || !isVisibleToTenant(record, context)) {
                return OperatorResult.failure("CONFIG_NOT_FOUND", "Configuration not found: " + configurationId);
            }

            auditService.record(
                    context,
                    "operator.configuration.inspect",
                    ConfigurationOperatorPermissions.INSPECT.id(),
                    OperatorScope.TENANT,
                    "configuration",
                    configurationId,
                    false,
                    false,
                    OperatorAuditOutcome.SUCCESS,
                    "AUTHORIZED",
                    "Inspected configuration: " + configurationId
            );

            return OperatorResult.success(toSummaryMasked(record));
        } catch (Exception e) {
            auditService.record(
                    context,
                    "operator.configuration.inspect",
                    ConfigurationOperatorPermissions.INSPECT.id(),
                    OperatorScope.TENANT,
                    "configuration",
                    configurationId,
                    false,
                    false,
                    OperatorAuditOutcome.FAILURE,
                    "FAILURE",
                    e.getMessage()
            );
            return OperatorResult.failure("CONFIG_INSPECT_FAILED", e.getMessage());
        }
    }

    @Override
    public OperatorResult<ConfigurationSummary> active(OperatorContext context) {
        return inspect(context, activeConfigId);
    }

    @Override
    public OperatorResult<ConfigurationSummary> update(
            OperatorContext context,
            String configurationId,
            ConfigurationUpdateRequest request) {

        if (configurationId == null || configurationId.isBlank()) {
            return OperatorResult.failure("INVALID_CONFIG_ID", "configurationId must not be blank");
        }

        try {
            authorization.require(
                    OperatorAuthorizationRequest.tenant(
                            context,
                            ConfigurationOperatorPermissions.UPDATE.id(),
                            "configuration",
                            configurationId
                    )
            );

            ConfigRecord existing = configurations.get(configurationId);
            if (existing == null) {
                // Create tenant-specific config if requested
                Map<String, Object> newValues = new LinkedHashMap<>(request.values());
                existing = new ConfigRecord(
                        configurationId,
                        "config-" + configurationId,
                        "TENANT",
                        "TENANT",
                        context != null ? context.tenantId() : null,
                        request.activate() ? "ACTIVE" : "LOADED",
                        newValues,
                        Instant.now()
                );
            } else {
                Map<String, Object> merged = new LinkedHashMap<>(existing.values);
                merged.putAll(request.values());
                existing = new ConfigRecord(
                        existing.id,
                        existing.name,
                        existing.source,
                        existing.type,
                        existing.tenantId,
                        request.activate() ? "ACTIVE" : existing.status,
                        merged,
                        Instant.now()
                );
            }

            configurations.put(configurationId, existing);
            if (request.activate()) {
                activeConfigId = configurationId;
            }

            auditService.record(
                    context,
                    "operator.configuration.update",
                    ConfigurationOperatorPermissions.UPDATE.id(),
                    OperatorScope.TENANT,
                    "configuration",
                    configurationId,
                    true,
                    true,
                    OperatorAuditOutcome.SUCCESS,
                    "AUTHORIZED",
                    "Updated configuration: " + configurationId
            );

            return OperatorResult.success(toSummaryMasked(existing));
        } catch (Exception e) {
            auditService.record(
                    context,
                    "operator.configuration.update",
                    ConfigurationOperatorPermissions.UPDATE.id(),
                    OperatorScope.TENANT,
                    "configuration",
                    configurationId,
                    true,
                    true,
                    OperatorAuditOutcome.FAILURE,
                    "FAILURE",
                    e.getMessage()
            );
            return OperatorResult.failure("CONFIG_UPDATE_FAILED", e.getMessage());
        }
    }

    @Override
    public OperatorResult<Void> activate(OperatorContext context, String configurationId) {
        if (configurationId == null || configurationId.isBlank()) {
            return OperatorResult.failure("INVALID_CONFIG_ID", "configurationId must not be blank");
        }

        try {
            authorization.require(
                    OperatorAuthorizationRequest.tenant(
                            context,
                            ConfigurationOperatorPermissions.ACTIVATE.id(),
                            "configuration",
                            configurationId
                    )
            );

            ConfigRecord record = configurations.get(configurationId);
            if (record == null || !isVisibleToTenant(record, context)) {
                return OperatorResult.failure("CONFIG_NOT_FOUND", "Configuration not found: " + configurationId);
            }

            activeConfigId = configurationId;
            configurations.put(configurationId, new ConfigRecord(
                    record.id,
                    record.name,
                    record.source,
                    record.type,
                    record.tenantId,
                    "ACTIVE",
                    record.values,
                    Instant.now()
            ));

            auditService.record(
                    context,
                    "operator.configuration.activate",
                    ConfigurationOperatorPermissions.ACTIVATE.id(),
                    OperatorScope.TENANT,
                    "configuration",
                    configurationId,
                    true,
                    true,
                    OperatorAuditOutcome.SUCCESS,
                    "AUTHORIZED",
                    "Activated configuration: " + configurationId
            );

            return OperatorResult.success(null);
        } catch (Exception e) {
            auditService.record(
                    context,
                    "operator.configuration.activate",
                    ConfigurationOperatorPermissions.ACTIVATE.id(),
                    OperatorScope.TENANT,
                    "configuration",
                    configurationId,
                    true,
                    true,
                    OperatorAuditOutcome.FAILURE,
                    "FAILURE",
                    e.getMessage()
            );
            return OperatorResult.failure("CONFIG_ACTIVATE_FAILED", e.getMessage());
        }
    }

    private boolean isVisibleToTenant(ConfigRecord config, OperatorContext context) {
        if (config.tenantId == null) {
            return true; // Global / application configuration
        }
        return context != null && Objects.equals(config.tenantId, context.tenantId());
    }

    private ConfigurationSummary toSummaryMasked(ConfigRecord record) {
        Map<String, Object> masked = new LinkedHashMap<>();
        record.values.forEach((k, v) -> {
            if (isSecretKey(k)) {
                masked.put(k, "******");
            } else {
                masked.put(k, v);
            }
        });

        return new ConfigurationSummary(
                record.id,
                record.name,
                record.source,
                record.type,
                record.tenantId,
                record.status,
                masked,
                record.lastModifiedAt
        );
    }

    private boolean isSecretKey(String key) {
        if (key == null) return false;
        String lower = key.toLowerCase();
        for (String s : SENSITIVE_KEYS) {
            if (lower.contains(s)) return true;
        }
        return false;
    }

    private record ConfigRecord(
            String id,
            String name,
            String source,
            String type,
            String tenantId,
            String status,
            Map<String, Object> values,
            Instant lastModifiedAt
    ) {}
}
