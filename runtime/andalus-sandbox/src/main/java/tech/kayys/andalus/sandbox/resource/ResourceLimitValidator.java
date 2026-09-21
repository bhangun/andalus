package tech.kayys.andalus.sandbox.resource;

import tech.kayys.andalus.spi.sandbox.ResourceLimitFeature;
import tech.kayys.andalus.spi.sandbox.SandboxLimits;
import tech.kayys.andalus.spi.sandbox.SandboxProviderDescriptor;

import java.util.Set;

public final class ResourceLimitValidator {

    private ResourceLimitValidator() {
    }

    public static void validate(SandboxLimits limits, SandboxProviderDescriptor provider) {
        Set<ResourceLimitFeature> required = ResourceLimitRequirements.from(limits);

        if (!provider.supportsAllResourceLimits(required)) {
            throw new UnsupportedResourceLimitException(
                    "Sandbox provider '" + provider.id() + "' cannot enforce required resource limits: " + required);
        }
    }
}
