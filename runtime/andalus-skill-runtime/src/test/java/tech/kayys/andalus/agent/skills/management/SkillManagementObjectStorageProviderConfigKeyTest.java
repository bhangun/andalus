package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillManagementObjectStorageProviderConfigKeyTest {

    @Test
    void createsPropertyAndEnvironmentSampleEntries() {
        SkillManagementObjectStorageProviderConfigKey key =
                new SkillManagementObjectStorageProviderConfigKey(
                        " andalus.storage.example.bucket ",
                        " ANDALUS_STORAGE_EXAMPLE_BUCKET ",
                        " andalus ",
                        " Example bucket. ",
                        true);

        assertThat(key.property()).isEqualTo("andalus.storage.example.bucket");
        assertThat(key.environment()).isEqualTo("ANDALUS_STORAGE_EXAMPLE_BUCKET");
        assertThat(key.defaultValue()).isEqualTo("andalus");
        assertThat(key.sampleDescription()).isEqualTo("Example bucket.");
        assertThat(key.required()).isTrue();
        assertThat(key.sampleEntry(false))
                .isEqualTo(new SkillManagementRuntimeConfigSampleEntry(
                        "andalus.storage.example.bucket",
                        "andalus",
                        "Example bucket."));
        assertThat(key.sampleEntry(true))
                .isEqualTo(new SkillManagementRuntimeConfigSampleEntry(
                        "ANDALUS_STORAGE_EXAMPLE_BUCKET",
                        "andalus",
                        "Example bucket."));
    }

    @Test
    void rejectsMissingPropertyAndEnvironmentKeys() {
        assertThatThrownBy(() -> new SkillManagementObjectStorageProviderConfigKey(
                "",
                "ANDALUS_STORAGE_EXAMPLE_BUCKET",
                "",
                "",
                false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("property is required");
        assertThatThrownBy(() -> new SkillManagementObjectStorageProviderConfigKey(
                "andalus.storage.example.bucket",
                "",
                "",
                "",
                false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("environment is required");
    }
}
