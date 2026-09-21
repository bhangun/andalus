package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import tech.kayys.andalus.cli.AndalusStandardPolicyOptions;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentPolicyConfig;
import tech.kayys.andalus.gollek.sdk.AndalusStandardRegistryDriftMode;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AndalusCliPolicyOptionsTest {

    @Test
    void standardPolicyOptionsMapToSdkConfig() {
        AndalusStandardPolicyOptions options = new AndalusStandardPolicyOptions();
        options.policy = "pinned-registry";
        options.standardIds = List.of("a2a", "mcp");
        options.warningGapCategories = List.of("transport", "schema");
        options.versionEntries = List.of("a2a = 1.0", "mcp=2026-01");
        options.registryDriftMode = "warn";

        AndalusStandardAlignmentPolicyConfig config = options.toConfig();

        assertThat(config.mode()).isEqualTo(AndalusStandardAlignmentPolicyConfig.Mode.PINNED_REGISTRY);
        assertThat(config.standardIds()).containsExactly("a2a", "mcp");
        assertThat(config.warningGapCategories()).containsExactly("transport", "schema");
        assertThat(config.requiredVersions())
                .containsExactly(
                        Map.entry("a2a", "1.0"),
                        Map.entry("mcp", "2026-01"));
        assertThat(config.registryDriftMode()).isEqualTo(AndalusStandardRegistryDriftMode.WARN);
    }

    @Test
    void standardPolicyOptionsRejectMalformedVersionEntries() {
        AndalusStandardPolicyOptions options = new AndalusStandardPolicyOptions();
        options.versionEntries = List.of("a2a");

        assertThatThrownBy(options::toConfig)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Required version entries must use standard=version: a2a");
    }
}
