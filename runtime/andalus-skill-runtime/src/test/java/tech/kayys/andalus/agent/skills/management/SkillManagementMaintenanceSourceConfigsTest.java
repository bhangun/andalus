package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SkillManagementMaintenanceSourceConfigsTest {

    @Test
    void parsesDefinitionAndArtifactSourcesFromProperties(@TempDir Path tempDir) {
        Properties properties = new Properties();
        properties.setProperty("andalus.skills.maintenance.source.definitions.store.kind", "filesystem");
        properties.setProperty(
                "andalus.skills.maintenance.source.definitions.store.directory",
                tempDir.resolve("source-definitions").toString());
        properties.setProperty("andalus.skills.maintenance.source.artifacts.store.kind", "s3");
        properties.setProperty(
                "andalus.skills.maintenance.source.artifacts.store.object-prefix",
                "tenants/acme/source-artifacts");

        SkillManagementMaintenanceSourceConfig config =
                SkillManagementMaintenanceSourceConfigs.fromProperties(properties);

        assertThat(config.hasDefinitionStore()).isTrue();
        assertThat(config.definitionStore().kind()).isEqualTo(SkillDefinitionStoreConfig.Kind.FILESYSTEM);
        assertThat(config.definitionStore().directory()).isEqualTo(tempDir.resolve("source-definitions"));
        assertThat(config.hasArtifactStore()).isTrue();
        assertThat(config.artifactStore().kind()).isEqualTo(SkillArtifactStoreConfig.Kind.OBJECT_STORAGE);
        assertThat(config.artifactStore().objectPrefix()).isEqualTo("tenants/acme/source-artifacts");
    }

    @Test
    void parsesSourceStoresFromEnvironment() {
        SkillManagementMaintenanceSourceConfig config =
                SkillManagementMaintenanceSourceConfigs.fromEnvironment(Map.of(
                        "ANDALUS_SKILLS_MAINTENANCE_SOURCE_DEFINITION_STORE_KIND", "jdbc",
                        "ANDALUS_SKILLS_MAINTENANCE_SOURCE_DEFINITION_STORE_JDBC_TABLE_NAME", "source_skills",
                        "ANDALUS_SKILLS_MAINTENANCE_SOURCE_DEFINITION_STORE_JDBC_INITIALIZE_SCHEMA", "false",
                        "ANDALUS_SKILLS_MAINTENANCE_SOURCE_ARTIFACTS_STORE_KIND", "rustfs",
                        "ANDALUS_SKILLS_MAINTENANCE_SOURCE_ARTIFACTS_STORE_OBJECT_PREFIX",
                        "tenants/acme/source-artifacts"));

        assertThat(config.definitionStore().kind()).isEqualTo(SkillDefinitionStoreConfig.Kind.JDBC);
        assertThat(config.definitionStore().jdbcTableName()).isEqualTo("source_skills");
        assertThat(config.definitionStore().initializeJdbcSchema()).isFalse();
        assertThat(config.artifactStore().kind()).isEqualTo(SkillArtifactStoreConfig.Kind.OBJECT_STORAGE);
        assertThat(config.artifactStore().objectPrefix()).isEqualTo("tenants/acme/source-artifacts");
    }

    @Test
    void defaultsToNoExplicitSources() {
        SkillManagementMaintenanceSourceConfig config =
                SkillManagementMaintenanceSourceConfigs.fromMap(Map.of());

        assertThat(config.hasDefinitionStore()).isFalse();
        assertThat(config.hasArtifactStore()).isFalse();
        assertThat(config.validate().validConfiguration()).isTrue();
    }

    @Test
    void validatesExplicitSourceStoresWithoutThrowing() {
        SkillStoreConfigValidationResult result =
                SkillManagementMaintenanceSourceConfigs.validateMap(Map.of(
                        "andalus.skills.maintenance.source.artifacts.store.kind", "filesystem"));

        assertThat(result.validConfiguration()).isFalse();
        assertThat(result.errors()).containsExactly("Filesystem artifact store requires a directory");
    }
}
