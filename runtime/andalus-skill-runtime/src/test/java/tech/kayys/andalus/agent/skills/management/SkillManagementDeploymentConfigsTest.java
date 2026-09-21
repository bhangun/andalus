package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SkillManagementDeploymentConfigsTest {

    @Test
    void parsesFullDeploymentConfigFromProperties(@TempDir Path tempDir) {
        Properties properties = new Properties();
        properties.setProperty("andalus.skills.store.kind", "filesystem");
        properties.setProperty("andalus.skills.store.directory", tempDir.resolve("target-definitions").toString());
        properties.setProperty("andalus.skills.artifacts.store.kind", "filesystem");
        properties.setProperty("andalus.skills.artifacts.store.directory", tempDir.resolve("target-artifacts").toString());
        properties.setProperty("andalus.skills.maintenance.mode", "repair");
        properties.setProperty("andalus.skills.maintenance.source.definition.store.kind", "jdbc");
        properties.setProperty("andalus.skills.maintenance.source.definition.store.table", "source_skills");
        properties.setProperty("andalus.skills.maintenance.source.definition.store.initialize-schema", "false");
        properties.setProperty("andalus.skills.maintenance.source.artifact.store.kind", "rustfs");
        properties.setProperty(
                "andalus.skills.maintenance.source.artifact.store.object-prefix",
                "tenants/acme/source-artifacts");

        SkillManagementDeploymentConfig config = SkillManagementDeploymentConfigs.fromProperties(properties);

        assertThat(config.serviceConfig().definitionStore().kind())
                .isEqualTo(SkillDefinitionStoreConfig.Kind.FILESYSTEM);
        assertThat(config.serviceConfig().definitionStore().directory())
                .isEqualTo(tempDir.resolve("target-definitions"));
        assertThat(config.serviceConfig().artifactStore().kind())
                .isEqualTo(SkillArtifactStoreConfig.Kind.FILESYSTEM);
        assertThat(config.serviceConfig().artifactStore().directory())
                .isEqualTo(tempDir.resolve("target-artifacts"));
        assertThat(config.maintenancePlan().definitionSyncOptions().deleteMissingFromTarget()).isTrue();
        assertThat(config.maintenancePlan().artifactSyncOptions().overwriteExisting()).isTrue();
        assertThat(config.maintenanceSource().definitionStore().kind())
                .isEqualTo(SkillDefinitionStoreConfig.Kind.JDBC);
        assertThat(config.maintenanceSource().definitionStore().jdbcTableName()).isEqualTo("source_skills");
        assertThat(config.maintenanceSource().definitionStore().initializeJdbcSchema()).isFalse();
        assertThat(config.maintenanceSource().artifactStore().kind())
                .isEqualTo(SkillArtifactStoreConfig.Kind.OBJECT_STORAGE);
        assertThat(config.maintenanceSource().artifactStore().objectPrefix())
                .isEqualTo("tenants/acme/source-artifacts");
    }

    @Test
    void defaultsToSafeBootstrapDeployment() {
        SkillManagementDeploymentConfig config = SkillManagementDeploymentConfigs.fromMap(Map.of());

        assertThat(config.serviceConfig()).isEqualTo(SkillManagementServiceConfig.defaults());
        assertThat(config.maintenanceSource()).isEqualTo(SkillManagementMaintenanceSourceConfig.none());
        assertThat(config.maintenancePlan()).isEqualTo(SkillManagementMaintenancePlan.bootstrap());
        assertThat(config.validate().validConfiguration()).isTrue();
    }

    @Test
    void validatesServiceSourceAndPlanTogetherWithoutThrowing() {
        SkillStoreConfigValidationResult result = SkillManagementDeploymentConfigs.validateMap(Map.of(
                "andalus.skills.store.kind", "filesystem",
                "andalus.skills.maintenance.source.artifact.store.kind", "filesystem",
                "andalus.skills.maintenance.mode", "mystery"));

        assertThat(result.validConfiguration()).isFalse();
        assertThat(result.errors()).containsExactly(
                "Filesystem skill store requires a directory",
                "Filesystem artifact store requires a directory",
                "Unknown skill maintenance mode: mystery");
    }
}
