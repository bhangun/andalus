package tech.kayys.andalus.tenant;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.kayys.andalus.error.ErrorCode;
import tech.kayys.andalus.error.AndalusException;
import tech.kayys.andalus.tenant.cas.CasAccessClient;
import tech.kayys.andalus.tenant.cas.CasApiKeyValidationRequest;
import tech.kayys.andalus.tenant.cas.CasConsumerContext;

@ApplicationScoped
public class ApiKeyTenantResolver {

    @ConfigProperty(name = "andalus.multitenancy.enabled", defaultValue = "false")
    boolean multitenancyEnabled;

    @ConfigProperty(name = "andalus.cas.api-key.enabled", defaultValue = "false")
    boolean casApiKeyEnabled;

    @Inject
    @RestClient
    Instance<CasAccessClient> casAccessClient;

    @Inject
    Instance<LocalTenantLookup> localTenantLookup;

    public TenantContext resolveFromApiKey(String apiKey) {
        return resolveFromApiKeyAsync(apiKey).await().indefinitely();
    }

    public Uni<TenantContext> resolveFromApiKeyAsync(String apiKey) {
        if (!multitenancyEnabled) {
            return Uni.createFrom().item(TenantContext.builder()
                    .tenantId(ApiKeyHeader.COMMUNITY_API_KEY)
                    .build());
        }

        if (apiKey == null || apiKey.isBlank()) {
            return Uni.createFrom().failure(new AndalusException(ErrorCode.SECURITY_UNAUTHORIZED, "Missing API key"));
        }

        if (ApiKeyHeader.COMMUNITY_API_KEY.equals(apiKey)) {
            return Uni.createFrom().item(TenantContext.builder()
                    .tenantId(ApiKeyHeader.COMMUNITY_API_KEY)
                    .build());
        }

        if (casApiKeyEnabled && casAccessClient.isResolvable()) {
            return casAccessClient.get()
                    .validateApiKey(new CasApiKeyValidationRequest(apiKey))
                    .onItem().transform(this::toTenantContext)
                    .onFailure().transform(t -> new AndalusException(ErrorCode.SECURITY_UNAUTHORIZED, "Invalid API key", t));
        }

        if (localTenantLookup.isResolvable()) {
            return localTenantLookup.get()
                    .resolve(apiKey)
                    .onItem().transform(tenantContext -> {
                        if (tenantContext == null) {
                            throw new AndalusException(ErrorCode.SECURITY_UNAUTHORIZED, "Invalid API key");
                        }
                        return tenantContext;
                    });
        }

        return Uni.createFrom().failure(new AndalusException(
                ErrorCode.SECURITY_UNAUTHORIZED,
                "API key validation not configured"));
    }

    private TenantContext toTenantContext(CasConsumerContext context) {
        if (context == null || !context.active()) {
            throw new AndalusException(ErrorCode.SECURITY_UNAUTHORIZED, "Invalid API key");
        }

        TenantContext.Builder builder = TenantContext.builder()
                .tenantId(context.tenantId());

        if (context.consumerId() != null) {
            builder.attribute("consumerId", context.consumerId());
        }
        if (context.workspaceId() != null) {
            builder.attribute("workspaceId", context.workspaceId());
        }
        if (context.planId() != null) {
            builder.attribute("planId", context.planId());
        }
        if (context.scopes() != null && !context.scopes().isEmpty()) {
            builder.attribute("scopes", String.join(",", context.scopes()));
        }

        return builder.build();
    }
}
