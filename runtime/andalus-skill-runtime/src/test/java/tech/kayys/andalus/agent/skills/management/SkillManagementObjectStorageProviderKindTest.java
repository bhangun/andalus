package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SkillManagementObjectStorageProviderKindTest {

    @Test
    void exposesStableProviderVocabulary() {
        assertThat(Arrays.stream(SkillManagementObjectStorageProviderKind.values()))
                .extracting(SkillManagementObjectStorageProviderKind::configName)
                .containsExactly("s3-rustfs", "gcs", "azure");
        assertThat(SkillManagementObjectStorageProviderKind.S3_RUSTFS.sampleLabel())
                .isEqualTo("S3/RustFS-compatible object storage");
        assertThat(SkillManagementObjectStorageProviderKind.AZURE.readinessLabel())
                .isEqualTo("Azure Blob Storage");
        assertThat(SkillManagementObjectStorageProviderKind.AZURE.summaryLabel())
                .isEqualTo("Azure");
    }

    @Test
    void sampleProvidersReuseProviderKinds() {
        assertThat(SkillManagementRuntimeConfigSampleProvider.NONE.kind()).isNull();
        assertThat(SkillManagementRuntimeConfigSampleProvider.S3_RUSTFS.kind())
                .isEqualTo(SkillManagementObjectStorageProviderKind.S3_RUSTFS);
        assertThat(SkillManagementRuntimeConfigSampleProvider.GCS.kind())
                .isEqualTo(SkillManagementObjectStorageProviderKind.GCS);
        assertThat(SkillManagementRuntimeConfigSampleProvider.AZURE.kind())
                .isEqualTo(SkillManagementObjectStorageProviderKind.AZURE);
    }

    @Test
    void sampleProvidersOwnProviderSpecificSampleEntries() {
        assertThat(SkillManagementRuntimeConfigSampleProvider.NONE.sampleEntries(false)).isEmpty();
        assertThat(SkillManagementRuntimeConfigSampleProvider.S3_RUSTFS.sampleEntries(false))
                .extracting(SkillManagementRuntimeConfigSampleEntry::key)
                .containsExactly(
                        "andalus.storage.s3.endpoint",
                        "andalus.storage.s3.bucket",
                        "andalus.storage.s3.region",
                        "andalus.storage.s3.access-key-id",
                        "andalus.storage.s3.secret-access-key",
                        "andalus.storage.s3.path-style-access",
                        "andalus.storage.s3.path-prefix");
        assertThat(SkillManagementRuntimeConfigSampleProvider.GCS.sampleEntries(true))
                .extracting(SkillManagementRuntimeConfigSampleEntry::key)
                .containsExactly(
                        "ANDALUS_STORAGE_GCS_BUCKET",
                        "ANDALUS_STORAGE_GCS_PROJECT_ID",
                        "ANDALUS_STORAGE_GCS_PATH_PREFIX");
        assertThat(SkillManagementRuntimeConfigSampleProvider.AZURE.sampleEntries(true))
                .extracting(SkillManagementRuntimeConfigSampleEntry::key)
                .containsExactly(
                        "ANDALUS_STORAGE_AZURE_CONNECTION_STRING",
                        "ANDALUS_STORAGE_AZURE_CONTAINER",
                        "ANDALUS_STORAGE_AZURE_PATH_PREFIX");
    }

    @Test
    void readinessFamiliesReuseProviderKinds() {
        assertThat(SkillManagementObjectStorageProviderConfigFamilies.all())
                .extracting(SkillManagementObjectStorageProviderConfigFamily::kind)
                .containsExactly(
                        SkillManagementObjectStorageProviderKind.S3_RUSTFS,
                        SkillManagementObjectStorageProviderKind.GCS,
                        SkillManagementObjectStorageProviderKind.AZURE);
    }
}
