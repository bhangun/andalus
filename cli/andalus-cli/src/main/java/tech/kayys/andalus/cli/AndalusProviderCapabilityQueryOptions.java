package tech.kayys.andalus.cli;

import picocli.CommandLine.Option;
import tech.kayys.andalus.capability.AndalusProviderCapabilityQuery;
import tech.kayys.andalus.capability.AndalusProviderCapabilityState;

final class AndalusProviderCapabilityQueryOptions {

    @Option(names = {"--id", "--capability-id"}, description = "Filter by provider capability id.")
    String capabilityId;

    @Option(names = "--provider", description = "Filter by provider id, for example andalus-rag.")
    String providerId;

    @Option(names = "--namespace", description = "Filter by provider namespace.")
    String providerNamespace;

    @Option(names = "--module", description = "Filter by module id, for example rag or a2ui.")
    String moduleId;

    @Option(names = "--type", description = "Filter by capability type, for example rag or standard.")
    String capabilityType;

    @Option(names = "--state", description = "Filter by state: available, preview, disabled, or deprecated.")
    String state;

    @Option(names = "--surface", description = "Filter capabilities to a product surface.")
    String surfaceId;

    @Option(names = "--standard", description = "Filter capabilities by standard id or alias.")
    String standardId;

    @Option(names = "--tag", description = "Filter capabilities by tag.")
    String tag;

    AndalusProviderCapabilityQuery toQuery(String idOverride) {
        String resolvedCapabilityId = idOverride == null || idOverride.isBlank() ? capabilityId : idOverride;
        return new AndalusProviderCapabilityQuery(
                resolvedCapabilityId,
                providerId,
                providerNamespace,
                moduleId,
                capabilityType,
                state == null || state.isBlank() ? null : AndalusProviderCapabilityState.from(state),
                surfaceId,
                standardId,
                tag);
    }
}
