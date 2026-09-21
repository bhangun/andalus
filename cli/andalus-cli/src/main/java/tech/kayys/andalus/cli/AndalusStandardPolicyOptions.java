package tech.kayys.andalus.cli;

import picocli.CommandLine.Option;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentPolicyConfig;

import java.util.List;

final class AndalusStandardPolicyOptions {

    @Option(
            names = "--policy",
            description = "Alignment policy: none, strict, pinned-registry, or pinned-known.",
            defaultValue = "none")
    String policy;

    @Option(
            names = {"--standard", "--required-standard"},
            split = ",",
            description = "Required standard id or alias. Can be repeated or comma-separated.")
    List<String> standardIds;

    @Option(
            names = "--warning-gap-category",
            split = ",",
            description = "Gap category that should warn instead of block. Can be repeated or comma-separated.")
    List<String> warningGapCategories;

    @Option(
            names = {"--version", "--required-version"},
            description = "Required standard version as <standard>=<version>. Can be repeated.")
    List<String> versionEntries;

    @Option(
            names = "--registry-drift",
            description = "Registry drift behavior: ignore, warn, or block.",
            defaultValue = "ignore")
    String registryDriftMode;

    AndalusStandardAlignmentPolicyConfig toConfig() {
        AndalusStandardAlignmentPolicyConfig.Builder builder = AndalusStandardAlignmentPolicyConfig.builder()
                .mode(policy)
                .standardIds(standardIds)
                .warningGapCategories(warningGapCategories)
                .registryDriftMode(registryDriftMode);
        if (versionEntries != null) {
            versionEntries.forEach(entry -> addVersion(builder, entry));
        }
        return builder.build();
    }

    private void addVersion(AndalusStandardAlignmentPolicyConfig.Builder builder, String entry) {
        String normalized = entry == null ? "" : entry.trim();
        int separator = normalized.indexOf('=');
        if (separator <= 0 || separator == normalized.length() - 1) {
            throw new IllegalArgumentException("Required version entries must use standard=version: " + normalized);
        }
        builder.requiredStandardVersion(
                normalized.substring(0, separator).trim(),
                normalized.substring(separator + 1).trim());
    }
}
