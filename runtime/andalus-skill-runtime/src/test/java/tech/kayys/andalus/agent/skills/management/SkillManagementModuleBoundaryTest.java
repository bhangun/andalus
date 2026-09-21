package tech.kayys.andalus.agent.skills.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkillManagementModuleBoundaryTest {

    @Test
    void namesTargetPackageSkeleton() {
        assertThat(SkillManagementModuleBoundary.basePackage())
                .isEqualTo("tech.kayys.andalus.agent.skills.management");
        assertThat(SkillManagementModuleBoundary.targetSubpackages())
                .extracting(SkillManagementModuleBoundary::packageName)
                .containsExactly(
                        "tech.kayys.andalus.agent.skills.management.config",
                        "tech.kayys.andalus.agent.skills.management.contracts",
                        "tech.kayys.andalus.agent.skills.management.preflight",
                        "tech.kayys.andalus.agent.skills.management.runtime",
                        "tech.kayys.andalus.agent.skills.management.store",
                        "tech.kayys.andalus.agent.skills.management.workflow",
                        "tech.kayys.andalus.agent.skills.management.events",
                        "tech.kayys.andalus.agent.skills.management.admin",
                        "tech.kayys.andalus.agent.skills.management.support");
    }

    @Test
    void exposesUniqueBoundaryLabelsAndResponsibilities() {
        assertThat(SkillManagementModuleBoundary.values())
                .extracting(SkillManagementModuleBoundary::label)
                .doesNotHaveDuplicates();
        assertThat(SkillManagementModuleBoundary.values())
                .allSatisfy(boundary -> assertThat(boundary.responsibility()).isNotBlank());
        assertThat(SkillManagementModuleBoundary.FACADE.rootPackage()).isTrue();
        assertThat(SkillManagementModuleBoundary.CONFIG.targetSubpackage()).isTrue();
    }
}
