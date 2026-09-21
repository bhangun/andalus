package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillManagementObjectStorageProviderConfigKeySetTest {

    @Test
    void exposesSampleAndReadinessPropertiesFromGroupedKeys() {
        SkillManagementObjectStorageProviderConfigKey required = key(
                "andalus.storage.example.bucket",
                true);
        SkillManagementObjectStorageProviderConfigKey optional = key(
                "andalus.storage.example.path-prefix",
                false);
        SkillManagementObjectStorageProviderConfigKeySet keySet =
                new SkillManagementObjectStorageProviderConfigKeySet(
                        List.of(required, optional),
                        List.of(required, optional));

        assertThat(keySet.sampleEntries(false))
                .extracting(SkillManagementRuntimeConfigSampleEntry::key)
                .containsExactly(
                        "andalus.storage.example.bucket",
                        "andalus.storage.example.path-prefix");
        assertThat(keySet.sampleEntries(true))
                .extracting(SkillManagementRuntimeConfigSampleEntry::key)
                .containsExactly(
                        "ANDALUS_STORAGE_EXAMPLE_BUCKET",
                        "ANDALUS_STORAGE_EXAMPLE_PATH_PREFIX");
        assertThat(keySet.requiredProperties()).containsExactly("andalus.storage.example.bucket");
        assertThat(keySet.optionalProperties()).containsExactly("andalus.storage.example.path-prefix");
    }

    @Test
    void toleratesMissingKeyLists() {
        SkillManagementObjectStorageProviderConfigKeySet keySet =
                new SkillManagementObjectStorageProviderConfigKeySet(null, null);

        assertThat(keySet.sampleEntries(false)).isEmpty();
        assertThat(keySet.requiredProperties()).isEmpty();
        assertThat(keySet.optionalProperties()).isEmpty();
    }

    @Test
    void rejectsDuplicateSampleAndReadinessProperties() {
        SkillManagementObjectStorageProviderConfigKey first = key(
                "andalus.storage.example.bucket",
                true);
        SkillManagementObjectStorageProviderConfigKey duplicate = key(
                "andalus.storage.example.bucket",
                false);

        assertThatThrownBy(() -> new SkillManagementObjectStorageProviderConfigKeySet(
                List.of(first, duplicate),
                List.of(first)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate object-storage provider sample key: andalus.storage.example.bucket");
        assertThatThrownBy(() -> new SkillManagementObjectStorageProviderConfigKeySet(
                List.of(first),
                List.of(first, duplicate)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate object-storage provider readiness key: andalus.storage.example.bucket");
    }

    @Test
    void rejectsReadinessKeysThatAreNotInSamples() {
        SkillManagementObjectStorageProviderConfigKey sample = key(
                "andalus.storage.example.bucket",
                true);
        SkillManagementObjectStorageProviderConfigKey readinessOnly = key(
                "andalus.storage.example.secret",
                true);

        assertThatThrownBy(() -> new SkillManagementObjectStorageProviderConfigKeySet(
                List.of(sample),
                List.of(sample, readinessOnly)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Readiness object-storage provider key is not part of sample keys: "
                                + "andalus.storage.example.secret");
    }

    private static SkillManagementObjectStorageProviderConfigKey key(
            String property,
            boolean required) {
        String suffix = property.substring(property.lastIndexOf('.') + 1)
                .replace('-', '_')
                .toUpperCase();
        return new SkillManagementObjectStorageProviderConfigKey(
                property,
                "ANDALUS_STORAGE_EXAMPLE_" + suffix,
                "andalus",
                "Example " + suffix.toLowerCase().replace('_', ' ') + ".",
                required);
    }
}
