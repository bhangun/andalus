package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusCliGoldenFixtureContractsTest {

    @Test
    void resolvesExplicitAndSelfDescribingManifestContractIds() throws IOException {
        Set<String> jsonSchemaIds = AndalusCliGoldenFixtureContracts.jsonSchemaIds(List.of(
                entry("status-json.golden"),
                entry("run-result-json.golden")));

        assertThat(jsonSchemaIds)
                .containsExactlyInAnyOrder(
                        "urn:andalus:contract:andalus.platform.catalog:v1:platform-status",
                        "urn:andalus:contract:andalus.run.lifecycle:v1:run-result");
    }

    @Test
    void resolvesMultiEnvelopeSelfDescribingFixtures() throws IOException {
        assertThat(AndalusCliGoldenFixtureContracts.selfDescribingJsonSchemaIds("run-events-follow-json.golden"))
                .containsExactlyInAnyOrder(
                        "urn:andalus:contract:andalus.run.lifecycle:v1:run-events",
                        "urn:andalus:contract:andalus.run.lifecycle:v1:run-events-follow");
    }

    private static AndalusCliGoldenFixtureManifest.Entry entry(String name) {
        return AndalusCliGoldenFixtureManifest.entries().stream()
                .filter(candidate -> candidate.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing fixture manifest entry: " + name));
    }
}
