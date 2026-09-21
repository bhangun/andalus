package tech.kayys.andalus.sdk.client;

import tech.kayys.andalus.sdk.provider.Provider;

/**
 * API for managing inference providers.
 */
public final class AndalusProviderApi {

    private Provider defaultProvider;

    public AndalusProviderApi() {
    }

    public void setDefaultProvider(Provider provider) {
        this.defaultProvider = provider;
    }

    public Provider getDefaultProvider() {
        return defaultProvider;
    }
}
