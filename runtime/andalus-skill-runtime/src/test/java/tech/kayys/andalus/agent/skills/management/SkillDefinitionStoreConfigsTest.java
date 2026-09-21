package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillDefinitionStoreConfigsTest {

    @Test
    void parsesHybridJdbcPrimaryWithFileFallbackFromProperties(@TempDir Path tempDir) {
        Properties properties = new Properties();
        properties.setProperty("andalus.skills.store.kind", "hybrid");
        properties.setProperty("andalus.skills.store.primary.kind", "jdbc");
        properties.setProperty("andalus.skills.store.primary.jdbc.table-name", "skill_defs");
        properties.setProperty("andalus.skills.store.primary.jdbc.initialize-schema", "false");
        properties.setProperty("andalus.skills.store.fallback.kind", "filesystem");
        properties.setProperty("andalus.skills.store.fallback.directory", tempDir.toString());

        SkillDefinitionStoreConfig config = SkillDefinitionStoreConfigs.fromProperties(properties);

        assertThat(config.kind()).isEqualTo(SkillDefinitionStoreConfig.Kind.HYBRID);
        assertThat(config.primary().kind()).isEqualTo(SkillDefinitionStoreConfig.Kind.JDBC);
        assertThat(config.primary().jdbcTableName()).isEqualTo("skill_defs");
        assertThat(config.primary().initializeJdbcSchema()).isFalse();
        assertThat(config.fallback().kind()).isEqualTo(SkillDefinitionStoreConfig.Kind.FILESYSTEM);
        assertThat(config.fallback().directory()).isEqualTo(tempDir);
    }

    @Test
    void parsesRustFsAliasFromEnvironment() {
        SkillDefinitionStoreConfig config = SkillDefinitionStoreConfigs.fromEnvironment(Map.of(
                "ANDALUS_SKILLS_STORE_KIND", "rustfs",
                "ANDALUS_SKILLS_STORE_OBJECT_PREFIX", "tenants/acme/skills"));

        assertThat(config.kind()).isEqualTo(SkillDefinitionStoreConfig.Kind.OBJECT_STORAGE);
        assertThat(config.objectPrefix()).isEqualTo("tenants/acme/skills");
    }

    @Test
    void parsesNestedMapConfig() {
        SkillDefinitionStoreConfig config = SkillDefinitionStoreConfigs.fromMap(Map.of(
                "andalus", Map.of(
                        "skills", Map.of(
                                "store", Map.of(
                                        "kind", "custom",
                                        "custom", Map.of("name", "tenant-database"))))));

        assertThat(config.kind()).isEqualTo(SkillDefinitionStoreConfig.Kind.CUSTOM);
        assertThat(config.customStoreName()).isEqualTo("tenant-database");
    }

    @Test
    void rejectsHybridWithoutFallbackGroup() {
        assertThatThrownBy(() -> SkillDefinitionStoreConfigs.fromMap(Map.of(
                "andalus.skills.store.kind", "hybrid",
                "andalus.skills.store.primary.kind", "jdbc")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("primary and fallback");
    }

    @Test
    void validatesDefinitionStoreConfigWithoutConstructing() {
        SkillStoreConfigValidationResult result = SkillDefinitionStoreConfig.validate(
                SkillDefinitionStoreConfig.Kind.FILESYSTEM,
                null,
                null,
                null,
                null,
                false,
                null,
                null);

        assertThat(result.validConfiguration()).isFalse();
        assertThat(result.errors()).containsExactly("Filesystem skill store requires a directory");
    }
}
