package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import tech.kayys.andalus.cli.AndalusStandardAlignmentHealthTextFormat;
import tech.kayys.andalus.gollek.sdk.AndalusJson;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentDescriptor;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentHealthEnvelopes;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentHealthReport;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentHealthReports;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentPolicyConfig;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentPortfolio;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentProviderIssue;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentProviderSummary;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentSummary;
import tech.kayys.andalus.gollek.sdk.AndalusStandardDefinition;
import tech.kayys.andalus.gollek.sdk.AndalusStandardRegistry;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusStandardAlignmentHealthFormatsTest {

    @Test
    void textRendersBlockingPolicyAndRegistryDrift() {
        AndalusStandardAlignmentHealthReport health = AndalusStandardAlignmentHealthReports.builder()
                .config(AndalusStandardAlignmentPolicyConfig.builder()
                        .mode("pinned-registry")
                        .standardIds("a2a", "a2ui")
                        .registryDriftMode("block")
                        .build())
                .portfolio(AndalusStandardAlignmentPortfolio.builder()
                        .summary(new AndalusStandardAlignmentSummary(
                                new AndalusStandardAlignmentDescriptor(
                                        "a2a",
                                        "Agent2Agent Protocol",
                                        "0.9",
                                        "JSONRPC",
                                        "https://a2a-protocol.org/latest/specification/",
                                        Map.of()),
                                true,
                                2,
                                2,
                                0,
                                List.of(),
                                List.of()))
                        .build())
                .build();

        assertThat(AndalusStandardAlignmentHealthTextFormat.text("Andalus", health))
                .contains("Andalus standard alignment")
                .contains("status: blocked")
                .contains("ready: no")
                .contains("standards: 1")
                .contains("missing standards:")
                .contains("- a2ui")
                .contains("version mismatches:")
                .contains("- a2a expected=1.0 actual=0.9")
                .contains("registry drift detail:")
                .contains("- a2a version expected=1.0 actual=0.9")
                .contains("recommendations:");
    }

    @Test
    void jsonWrapsHealthPayloadWithProduct() {
        AndalusStandardDefinition definition = AndalusStandardRegistry.find("a2ui").orElseThrow();
        AndalusStandardAlignmentHealthReport health = AndalusStandardAlignmentHealthReports.builder()
                .config(AndalusStandardAlignmentPolicyConfig.strict("a2ui"))
                .reportMap(Map.of(
                        "standard",
                        definition.toDescriptor().toMap(),
                        "aligned",
                        true,
                        "requirementCount",
                        1,
                        "alignedCount",
                        1,
                        "gapCount",
                        0))
                .build();

        assertThat(AndalusJson.object(AndalusStandardAlignmentHealthEnvelopes.health("Andalus", health)))
                .startsWith("{")
                .contains("\"product\":\"Andalus\"")
                .contains("\"health\":")
                .contains("\"ready\":true")
                .contains("\"providerPolicyAssessment\":")
                .contains("\"providerDiagnostics\":")
                .contains("\"standardIds\":[\"a2ui\"]");
    }

    @Test
    void textRendersProviderIssuesAsWarnings() {
        AndalusStandardDefinition definition = AndalusStandardRegistry.find("a2a").orElseThrow();
        AndalusStandardAlignmentPortfolio portfolio = AndalusStandardAlignmentPortfolio.fromReportMaps(Map.of(
                "standard",
                definition.toDescriptor().toMap(),
                "aligned",
                true,
                "requirementCount",
                1,
                "alignedCount",
                1,
                "gapCount",
                0));
        AndalusStandardAlignmentHealthReport health = AndalusStandardAlignmentHealthReport.fromConfiguredPolicy(
                portfolio,
                AndalusStandardAlignmentPolicyConfig.strict("a2a"),
                List.of("a2a-provider"),
                List.of(AndalusStandardAlignmentProviderSummary.from(
                        "a2a-provider",
                        "example.A2aProvider",
                        10,
                        portfolio)),
                List.of(new AndalusStandardAlignmentProviderIssue(
                        "broken-provider",
                        "example.BrokenProvider",
                        "boom")));

        assertThat(AndalusStandardAlignmentHealthTextFormat.text("Andalus", health))
                .contains("status: warning")
                .contains("ready: yes")
                .contains("providers: 1")
                .contains("provider policy ready: yes")
                .contains("provider issue mode: warn")
                .contains("provider minimum: 0")
                .contains("provider ids:")
                .contains("- a2a-provider")
                .contains("provider detail:")
                .contains("- a2a-provider priority=10 standards=1 aligned=yes gaps=0 ids=a2a")
                .contains("provider issues: 1")
                .contains("provider issue detail:")
                .contains("- broken-provider example.BrokenProvider: boom")
                .contains("Review standard-alignment provider broken-provider: boom");
    }
}
