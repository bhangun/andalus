package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillManagementRuntimeConfigSamplesTest {

    @Test
    void exposesDiscoverableSampleDescriptors() {
        assertThat(SkillManagementRuntimeConfigSamples.samples())
                .extracting(SkillManagementRuntimeConfigSampleDescriptor::name)
                .containsExactly(
                        "default",
                        "local-filesystem",
                        "object-storage",
                        "jdbc",
                        "hybrid-object-file",
                        "mirrored-object-file",
                        "gcs",
                        "azure",
                        "hybrid-gcs",
                        "hybrid-azure",
                        "mirrored-gcs",
                        "mirrored-azure");

        assertThat(SkillManagementRuntimeConfigSamples.samples())
                .filteredOn(sample -> sample.name().equals("gcs"))
                .singleElement()
                .satisfies(sample -> {
                    assertThat(sample.profile()).isEqualTo("object-storage");
                    assertThat(sample.objectStorageProvider()).isEqualTo("gcs");
                    assertThat(sample.aliases()).containsExactly(
                            "google-cloud-storage",
                            "gcs-object-storage");
                });
    }

    @Test
    void rendersObjectStorageSampleFromAlias() {
        SkillManagementRuntimeConfigSample sample =
                SkillManagementRuntimeConfigSamples.forProfile("rustfs");

        assertThat(sample.profile()).isEqualTo("object-storage");
        assertThat(sample.description()).contains("S3/RustFS-compatible");
        assertThat(lines(sample.properties()))
                .containsExactly(
                        "andalus.skills.profile=object-storage",
                        "andalus.skills.profile.object-prefix=andalus/skills",
                        "andalus.storage.s3.endpoint=http://localhost:9000",
                        "andalus.storage.s3.bucket=andalus",
                        "andalus.storage.s3.region=us-east-1",
                        "andalus.storage.s3.access-key-id=CHANGE_ME",
                        "andalus.storage.s3.secret-access-key=CHANGE_ME",
                        "andalus.storage.s3.path-style-access=true",
                        "andalus.storage.s3.path-prefix=tenants/acme",
                        "andalus.skills.profile.max-events=10000",
                        "andalus.skills.lifecycle.reconcile.mode=inspect-only");
        assertThat(lines(sample.environment()))
                .containsExactly(
                        "ANDALUS_SKILLS_PROFILE=object-storage",
                        "ANDALUS_SKILLS_PROFILE_OBJECT_PREFIX=andalus/skills",
                        "ANDALUS_STORAGE_S3_ENDPOINT=http://localhost:9000",
                        "ANDALUS_STORAGE_S3_BUCKET=andalus",
                        "ANDALUS_STORAGE_S3_REGION=us-east-1",
                        "ANDALUS_STORAGE_S3_ACCESS_KEY_ID=CHANGE_ME",
                        "ANDALUS_STORAGE_S3_SECRET_ACCESS_KEY=CHANGE_ME",
                        "ANDALUS_STORAGE_S3_PATH_STYLE_ACCESS=true",
                        "ANDALUS_STORAGE_S3_PATH_PREFIX=tenants/acme",
                        "ANDALUS_SKILLS_PROFILE_MAX_EVENTS=10000",
                        "ANDALUS_SKILLS_LIFECYCLE_RECONCILE_MODE=inspect-only");
    }

    @Test
    void rendersGcsObjectStorageSampleFromProviderAlias() {
        SkillManagementRuntimeConfigSample sample =
                SkillManagementRuntimeConfigSamples.forProfile("gcs");

        assertThat(sample.profile()).isEqualTo("object-storage");
        assertThat(sample.description()).contains("Google Cloud Storage");
        assertThat(lines(sample.properties()))
                .containsExactly(
                        "andalus.skills.profile=object-storage",
                        "andalus.skills.profile.object-prefix=andalus/skills",
                        "andalus.storage.gcs.bucket=andalus",
                        "andalus.storage.gcs.project-id=CHANGE_ME_PROJECT",
                        "andalus.storage.gcs.path-prefix=tenants/acme",
                        "andalus.skills.profile.max-events=10000",
                        "andalus.skills.lifecycle.reconcile.mode=inspect-only");
        assertThat(lines(sample.environment()))
                .containsExactly(
                        "ANDALUS_SKILLS_PROFILE=object-storage",
                        "ANDALUS_SKILLS_PROFILE_OBJECT_PREFIX=andalus/skills",
                        "ANDALUS_STORAGE_GCS_BUCKET=andalus",
                        "ANDALUS_STORAGE_GCS_PROJECT_ID=CHANGE_ME_PROJECT",
                        "ANDALUS_STORAGE_GCS_PATH_PREFIX=tenants/acme",
                        "ANDALUS_SKILLS_PROFILE_MAX_EVENTS=10000",
                        "ANDALUS_SKILLS_LIFECYCLE_RECONCILE_MODE=inspect-only");
    }

    @Test
    void rendersHybridSampleWithFileAndObjectDefaults() {
        SkillManagementRuntimeConfigSample sample =
                SkillManagementRuntimeConfigSamples.forProfile("hybrid");

        assertThat(sample.profile()).isEqualTo("hybrid-object-file");
        assertThat(lines(sample.properties()))
                .containsExactly(
                        "andalus.skills.profile=hybrid-object-file",
                        "andalus.skills.profile.base-directory=.andalus/skills",
                        "andalus.skills.profile.object-prefix=andalus/skills",
                        "andalus.storage.s3.endpoint=http://localhost:9000",
                        "andalus.storage.s3.bucket=andalus",
                        "andalus.storage.s3.region=us-east-1",
                        "andalus.storage.s3.access-key-id=CHANGE_ME",
                        "andalus.storage.s3.secret-access-key=CHANGE_ME",
                        "andalus.storage.s3.path-style-access=true",
                        "andalus.storage.s3.path-prefix=tenants/acme",
                        "andalus.skills.profile.max-events=10000",
                        "andalus.skills.lifecycle.reconcile.mode=inspect-only");
    }

    @Test
    void rendersAzureHybridSampleFromProviderAlias() {
        SkillManagementRuntimeConfigSample sample =
                SkillManagementRuntimeConfigSamples.forProfile("hybrid-azure");

        assertThat(sample.profile()).isEqualTo("hybrid-object-file");
        assertThat(sample.description()).contains("Azure Blob Storage");
        assertThat(lines(sample.properties()))
                .containsExactly(
                        "andalus.skills.profile=hybrid-object-file",
                        "andalus.skills.profile.base-directory=.andalus/skills",
                        "andalus.skills.profile.object-prefix=andalus/skills",
                        "andalus.storage.azure.connection-string=CHANGE_ME",
                        "andalus.storage.azure.container=andalus",
                        "andalus.storage.azure.path-prefix=tenants/acme",
                        "andalus.skills.profile.max-events=10000",
                        "andalus.skills.lifecycle.reconcile.mode=inspect-only");
        assertThat(lines(sample.environment()))
                .containsExactly(
                        "ANDALUS_SKILLS_PROFILE=hybrid-object-file",
                        "ANDALUS_SKILLS_PROFILE_BASE_DIRECTORY=.andalus/skills",
                        "ANDALUS_SKILLS_PROFILE_OBJECT_PREFIX=andalus/skills",
                        "ANDALUS_STORAGE_AZURE_CONNECTION_STRING=CHANGE_ME",
                        "ANDALUS_STORAGE_AZURE_CONTAINER=andalus",
                        "ANDALUS_STORAGE_AZURE_PATH_PREFIX=tenants/acme",
                        "ANDALUS_SKILLS_PROFILE_MAX_EVENTS=10000",
                        "ANDALUS_SKILLS_LIFECYCLE_RECONCILE_MODE=inspect-only");
    }

    @Test
    void rendersJdbcSampleWithSchemaInitialization() {
        SkillManagementRuntimeConfigSample sample =
                SkillManagementRuntimeConfigSamples.forProfile("db");

        assertThat(sample.profile()).isEqualTo("jdbc");
        assertThat(lines(sample.properties()))
                .containsExactly(
                        "andalus.skills.profile=jdbc",
                        "andalus.skills.profile.max-events=10000",
                        "andalus.skills.profile.initialize-jdbc-schema=true",
                        "andalus.skills.lifecycle.reconcile.mode=inspect-only");
    }

    @Test
    void rejectsUnknownProfile() {
        assertThatThrownBy(() -> SkillManagementRuntimeConfigSamples.forProfile("missing-profile"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown skill-management service profile: missing-profile");
    }

    private static java.util.List<String> lines(
            java.util.List<SkillManagementRuntimeConfigSampleEntry> entries) {
        return entries.stream()
                .map(entry -> entry.key() + "=" + entry.value())
                .toList();
    }
}
