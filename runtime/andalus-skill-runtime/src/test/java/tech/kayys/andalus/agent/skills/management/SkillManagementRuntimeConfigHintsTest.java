package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkillManagementRuntimeConfigHintsTest {

    @Test
    void exposesProfileSelectorsAndDefaults() {
        SkillManagementRuntimeConfigCatalog catalog = SkillManagementRuntimeConfigHints.catalog();

        assertThat(catalog.groups())
                .extracting(SkillManagementRuntimeConfigGroup::name)
                .containsExactly(
                        "runtime-sources",
                        "profile-selectors",
                        "profile-options",
                        "object-storage-provider",
                        "store-overrides",
                        "store-option-suffixes");
        assertThat(catalog.hintCount()).isEqualTo(33);
        assertThat(catalog.group("profile-selectors").hints())
                .singleElement()
                .satisfies(hint -> {
                    assertThat(hint.properties())
                            .containsExactly(
                                    "andalus.skills.profile",
                                    "andalus.skills.runtime.profile",
                                    "andalus.skills.service.profile",
                                    "andalus.skills.persistence.profile");
                    assertThat(hint.environment())
                            .containsExactly(
                                    "ANDALUS_SKILLS_PROFILE",
                                    "ANDALUS_SKILLS_SERVICE_PROFILE",
                                    "ANDALUS_SKILLS_PERSISTENCE_PROFILE");
                    assertThat(hint.defaultValue()).isEqualTo("default");
                    assertThat(hint.notes()).singleElement().asString().contains("rustfs");
                });
        assertThat(catalog.group("profile-options").hints())
                .filteredOn(hint -> hint.name().equals("object-prefix"))
                .singleElement()
                .satisfies(hint -> {
                    assertThat(hint.properties())
                            .contains("andalus.skills.profile.object-prefix");
                    assertThat(hint.environment())
                            .containsExactly("ANDALUS_SKILLS_PROFILE_OBJECT_PREFIX");
                    assertThat(hint.defaultValue()).isEqualTo("andalus/skills");
                });
    }

    @Test
    void exposesObjectStorageProviderSettings() {
        SkillManagementRuntimeConfigCatalog catalog = SkillManagementRuntimeConfigHints.catalog();

        assertThat(catalog.group("object-storage-provider").hints())
                .extracting(SkillManagementRuntimeConfigHint::name)
                .containsExactly(
                        "endpoint",
                        "bucket",
                        "region",
                        "access-key-id",
                        "secret-access-key",
                        "path-style-access",
                        "path-prefix",
                        "gcs.bucket",
                        "gcs.project-id",
                        "gcs.path-prefix",
                        "azure.connection-string",
                        "azure.container",
                        "azure.path-prefix");
        assertThat(catalog.group("object-storage-provider").hints())
                .filteredOn(hint -> hint.name().equals("endpoint"))
                .singleElement()
                .satisfies(hint -> {
                    assertThat(hint.properties())
                            .containsExactly("andalus.storage.s3.endpoint");
                    assertThat(hint.environment())
                            .containsExactly("ANDALUS_STORAGE_S3_ENDPOINT");
                    assertThat(hint.defaultValue()).isEqualTo("http://localhost:9000");
                    assertThat(hint.notes()).singleElement().asString().contains("AWS S3");
                });
        assertThat(catalog.group("object-storage-provider").hints())
                .filteredOn(hint -> hint.name().equals("path-style-access"))
                .singleElement()
                .satisfies(hint -> {
                    assertThat(hint.defaultValue()).isEqualTo("true");
                    assertThat(hint.description()).contains("RustFS");
                });
        assertThat(catalog.group("object-storage-provider").hints())
                .filteredOn(hint -> hint.name().equals("gcs.project-id"))
                .singleElement()
                .satisfies(hint -> {
                    assertThat(hint.properties())
                            .containsExactly("andalus.storage.gcs.project-id");
                    assertThat(hint.environment())
                            .containsExactly("ANDALUS_STORAGE_GCS_PROJECT_ID");
                    assertThat(hint.defaultValue()).isEqualTo("CHANGE_ME_PROJECT");
                    assertThat(hint.notes()).singleElement().asString().contains("default project");
                });
        assertThat(catalog.group("object-storage-provider").hints())
                .filteredOn(hint -> hint.name().equals("azure.connection-string"))
                .singleElement()
                .satisfies(hint -> {
                    assertThat(hint.properties())
                            .containsExactly("andalus.storage.azure.connection-string");
                    assertThat(hint.environment())
                            .containsExactly("ANDALUS_STORAGE_AZURE_CONNECTION_STRING");
                    assertThat(hint.defaultValue()).isEqualTo("CHANGE_ME");
                    assertThat(hint.notes()).singleElement().asString().contains("secret injection");
                });
    }

    @Test
    void exposesObjectStorageProviderReadinessKeysFromHintMetadata() {
        assertThat(SkillManagementObjectStorageProviderConfigHints.requiredProperties(
                SkillManagementObjectStorageProviderKind.S3_RUSTFS))
                .containsExactly(
                        "andalus.storage.s3.access-key-id",
                        "andalus.storage.s3.secret-access-key",
                        "andalus.storage.s3.bucket",
                        "andalus.storage.s3.region");
        assertThat(SkillManagementObjectStorageProviderConfigHints.optionalProperties(
                SkillManagementObjectStorageProviderKind.S3_RUSTFS))
                .containsExactly(
                        "andalus.storage.s3.endpoint",
                        "andalus.storage.s3.path-style-access",
                        "andalus.storage.s3.path-prefix");
        assertThat(SkillManagementObjectStorageProviderConfigHints.requiredProperties(
                SkillManagementObjectStorageProviderKind.AZURE))
                .containsExactly(
                        "andalus.storage.azure.connection-string",
                        "andalus.storage.azure.container");
        assertThat(SkillManagementObjectStorageProviderConfigHints.optionalProperties(null)).isEmpty();
    }

    @Test
    void exposesObjectStorageProviderSamplesFromHintMetadata() {
        assertThat(SkillManagementObjectStorageProviderConfigHints.sampleEntries(
                SkillManagementObjectStorageProviderKind.GCS,
                false))
                .extracting(SkillManagementRuntimeConfigSampleEntry::key)
                .containsExactly(
                        "andalus.storage.gcs.bucket",
                        "andalus.storage.gcs.project-id",
                        "andalus.storage.gcs.path-prefix");
        assertThat(SkillManagementObjectStorageProviderConfigHints.sampleEntries(
                SkillManagementObjectStorageProviderKind.AZURE,
                true))
                .extracting(SkillManagementRuntimeConfigSampleEntry::key)
                .containsExactly(
                        "ANDALUS_STORAGE_AZURE_CONNECTION_STRING",
                        "ANDALUS_STORAGE_AZURE_CONTAINER",
                        "ANDALUS_STORAGE_AZURE_PATH_PREFIX");
        assertThat(SkillManagementObjectStorageProviderConfigHints.sampleEntries(null, false)).isEmpty();
    }

    @Test
    void exposesRoleOverridePrefixesAndStoreSuffixes() {
        SkillManagementRuntimeConfigCatalog catalog = SkillManagementRuntimeConfigHints.catalog();

        assertThat(catalog.group("store-overrides").hints())
                .filteredOn(hint -> hint.name().equals("definition"))
                .singleElement()
                .satisfies(hint -> {
                    assertThat(hint.properties()).containsExactly("andalus.skills.store.");
                    assertThat(hint.environment()).containsExactly("ANDALUS_SKILLS_STORE_");
                    assertThat(hint.defaultValue()).isEqualTo("registry");
                });
        assertThat(catalog.group("store-overrides").hints())
                .filteredOn(hint -> hint.name().equals("lifecycle-reconcile"))
                .singleElement()
                .satisfies(hint -> {
                    assertThat(hint.properties()).containsExactly("andalus.skills.lifecycle.reconcile.");
                    assertThat(hint.environment()).containsExactly("ANDALUS_SKILLS_LIFECYCLE_RECONCILE_");
                    assertThat(hint.notes()).singleElement().asString().contains("sync");
                });
        assertThat(catalog.group("store-option-suffixes").hints())
                .filteredOn(hint -> hint.name().equals("primary-and-fallback"))
                .singleElement()
                .satisfies(hint -> assertThat(hint.properties()).containsExactly("primary.*", "fallback.*"));
        assertThat(catalog.group("store-option-suffixes").hints())
                .filteredOn(hint -> hint.name().equals("max-events"))
                .singleElement()
                .satisfies(hint -> assertThat(hint.defaultValue()).isEqualTo("10000"));
    }

    @Test
    void selectsOneRuntimeConfigHintGroup() {
        SkillManagementRuntimeConfigCatalog catalog = SkillManagementRuntimeConfigHints.catalog();

        SkillManagementRuntimeConfigCatalog selected = catalog.selectGroup("store-overrides");

        assertThat(selected.groups())
                .extracting(SkillManagementRuntimeConfigGroup::name)
                .containsExactly("store-overrides");
        assertThat(selected.hintCount()).isEqualTo(5);
    }

    @Test
    void summarizesRuntimeConfigHintGroups() {
        SkillManagementRuntimeConfigCatalog catalog = SkillManagementRuntimeConfigHints.catalog();

        assertThat(catalog.groupSummaries())
                .extracting(SkillManagementRuntimeConfigGroupSummary::name)
                .containsExactly(
                        "runtime-sources",
                        "profile-selectors",
                        "profile-options",
                        "object-storage-provider",
                        "store-overrides",
                        "store-option-suffixes");
        assertThat(catalog.groupSummaries())
                .filteredOn(summary -> summary.name().equals("profile-options"))
                .singleElement()
                .satisfies(summary -> {
                    assertThat(summary.label()).isEqualTo("profile option defaults");
                    assertThat(summary.hintCount()).isEqualTo(4);
                });
    }
}
