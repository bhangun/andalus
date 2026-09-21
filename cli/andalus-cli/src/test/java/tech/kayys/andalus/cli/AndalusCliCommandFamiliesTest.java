package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusCliCommandFamiliesTest {

    @Test
    void normalizesRunTaskCommandsWithoutTreatingPromptAsSubcommand() {
        assertThat(AndalusCliCommandFamilies.commandFamily("run contract prompt --json"))
                .isEqualTo("run <task>");
        assertThat(AndalusCliCommandFamilies.sameCommandFamily(
                        "run <task> --json",
                        "run contract prompt --json"))
                .isTrue();
    }

    @Test
    void preservesRunSubcommandFamilies() {
        assertThat(AndalusCliCommandFamilies.commandFamily("run status contract-run-1 --json"))
                .isEqualTo("run status");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of(
                        "run",
                        "cancel",
                        "contract-run-1",
                        "--reason",
                        "contract stop",
                        "--json")))
                .isEqualTo("run cancel");
        assertThat(AndalusCliCommandFamilies.sameCommandFamily(
                        "run <task> --json",
                        "run status contract-run-1 --json"))
                .isFalse();
        assertThat(AndalusCliCommandFamilies.sameCommandFamily(
                        "run cancel <run-id> --reason <text> --json",
                        List.of("run", "cancel", "contract-run-1", "--reason", "contract stop", "--json")))
                .isTrue();
    }

    @Test
    void normalizesOptionLedCatalogCommands() {
        assertThat(AndalusCliCommandFamilies.commandFamily("contracts --domain planning --index --json"))
                .isEqualTo("contracts");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of(
                        "contracts",
                        "--domain",
                        "planning",
                        "--index",
                        "--json")))
                .isEqualTo("contracts");
        assertThat(AndalusCliCommandFamilies.commandFamily("skills list --surface assistant-agent --json"))
                .isEqualTo("skills list");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of(
                        "skills",
                        "search",
                        "gamelan",
                        "--profile",
                        "workflow-agent",
                        "--json")))
                .isEqualTo("skills search");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of("providers", "--json")))
                .isEqualTo("providers");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of("providers", "inspect", "storage.hybrid-persistence")))
                .isEqualTo("providers inspect");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of(
                        "status",
                        "--readiness-profile",
                        "default",
                        "--json")))
                .isEqualTo("status");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of(
                        "commands",
                        "--contract-json-schema-id",
                        "urn:andalus:contract:andalus.run.lifecycle:v1:run-result",
                        "--json")))
                .isEqualTo("commands");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of(
                        "workbench",
                        "--profile",
                        "assistant-agent",
                        "--json")))
                .isEqualTo("workbench");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of(
                        "profiles",
                        "--surface",
                        "assistant-agent",
                        "--json")))
                .isEqualTo("profiles");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of(
                        "profiles",
                        "inspect",
                        "assistant-agent",
                        "--json")))
                .isEqualTo("profiles inspect");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of("products", "--json")))
                .isEqualTo("products");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of("sdk-boundaries", "--json")))
                .isEqualTo("sdk-boundaries");
        assertThat(AndalusCliCommandFamilies.commandFamily(List.of("sdk-boundaries", "run", "--json")))
                .isEqualTo("sdk-boundaries");
    }
}
