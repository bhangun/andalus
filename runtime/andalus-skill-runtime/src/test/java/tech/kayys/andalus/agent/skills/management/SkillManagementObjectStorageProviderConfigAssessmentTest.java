package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SkillManagementObjectStorageProviderConfigAssessmentTest {

    @Test
    void warnsWhenObjectStorageProviderSettingsAreMissing() {
        SkillManagementObjectStorageProviderConfigAssessment assessment =
                SkillManagementObjectStorageProviderConfigAssessment.fromMap(Map.of());

        assertThat(assessment.configuredProviders()).isEmpty();
        assertThat(assessment.warningCount()).isEqualTo(1);
        assertThat(assessment.warnings()).containsExactly(
                "Object-storage persistence is selected but no S3/RustFS, GCS, or Azure provider settings were detected.");
    }

    @Test
    void acceptsCompleteGcsProviderSettings() {
        SkillManagementObjectStorageProviderConfigAssessment assessment =
                SkillManagementObjectStorageProviderConfigAssessment.fromMap(Map.of(
                        "andalus.storage.gcs.bucket", "andalus-skills"));

        assertThat(assessment.configuredProviders()).containsExactly("gcs");
        assertThat(assessment.hasConfiguredProvider()).isTrue();
        assertThat(assessment.warnings()).isEmpty();
    }

    @Test
    void detectsEnvironmentStyleGcsProviderSettings() {
        SkillManagementObjectStorageProviderConfigAssessment assessment =
                SkillManagementObjectStorageProviderConfigAssessment.fromEnvironment(Map.of(
                        "ANDALUS_STORAGE_GCS_BUCKET", "andalus-skills"));

        assertThat(assessment.configuredProviders()).containsExactly("gcs");
        assertThat(assessment.warnings()).isEmpty();
    }

    @Test
    void warnsWhenAzureProviderSettingsAreIncomplete() {
        SkillManagementObjectStorageProviderConfigAssessment assessment =
                SkillManagementObjectStorageProviderConfigAssessment.fromMap(Map.of(
                        "andalus.storage.azure.container", "andalus"));

        assertThat(assessment.configuredProviders()).containsExactly("azure");
        assertThat(assessment.warnings()).containsExactly(
                "Azure Blob Storage object-storage provider settings are incomplete: missing "
                        + "andalus.storage.azure.connection-string.");
    }

    @Test
    void warnsWhenMultipleProviderFamiliesAreConfigured() {
        SkillManagementObjectStorageProviderConfigAssessment assessment =
                SkillManagementObjectStorageProviderConfigAssessment.fromMap(Map.of(
                        "andalus.storage.s3.access-key-id", "ak",
                        "andalus.storage.s3.secret-access-key", "sk",
                        "andalus.storage.s3.bucket", "andalus",
                        "andalus.storage.s3.region", "us-east-1",
                        "andalus.storage.gcs.bucket", "andalus"));

        assertThat(assessment.configuredProviders()).containsExactly("s3-rustfs", "gcs");
        assertThat(assessment.warnings()).containsExactly(
                "Multiple object-storage provider setting families detected: s3-rustfs, gcs. "
                        + "Keep only the active provider family configured.");
    }
}
