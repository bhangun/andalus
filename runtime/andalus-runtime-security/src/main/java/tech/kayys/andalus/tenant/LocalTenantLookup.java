package tech.kayys.andalus.tenant;

import io.smallrye.mutiny.Uni;

public interface LocalTenantLookup {
    Uni<TenantContext> resolve(String apiKey);
}
