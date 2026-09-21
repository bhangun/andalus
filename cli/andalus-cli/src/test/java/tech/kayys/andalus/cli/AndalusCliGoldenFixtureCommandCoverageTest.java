package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusCliGoldenFixtureCommandCoverageTest {

    @Test
    void manifestCoveredCommandIdsArePublishedContractCommandIds() {
        AndalusCliGoldenFixtureCommandCoverage.Report report =
                AndalusCliGoldenFixtureCommandCoverage.defaultReport();

        assertThat(report.coveredCommandIds())
                .as("covered command ids")
                .isNotEmpty();
        assertThat(report.undeclaredCoveredCommandIds())
                .as("fixtures should not claim unpublished contract command ids")
                .isEmpty();
    }

    @Test
    void manifestCommandCoverageCoversPublishedCommandIds() {
        AndalusCliGoldenFixtureCommandCoverage.Report report =
                AndalusCliGoldenFixtureCommandCoverage.defaultReport();

        assertThat(report.missingCommandIds())
                .as("contract command ids without golden fixtures")
                .isEmpty();
    }
}
