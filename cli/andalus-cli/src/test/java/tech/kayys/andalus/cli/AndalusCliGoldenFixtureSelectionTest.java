package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusCliGoldenFixtureSelectionTest {

    @Test
    void defaultsToAllFixturesWhenNoIncludePropertyIsSet() {
        String previous = System.getProperty(AndalusCliGoldenFixtureSelection.UPDATE_INCLUDE_PROPERTY);
        try {
            System.clearProperty(AndalusCliGoldenFixtureSelection.UPDATE_INCLUDE_PROPERTY);

            assertThat(AndalusCliGoldenFixtureSelection.fromSystemProperties()
                    .selected("status-json.golden")).isTrue();
        } finally {
            restoreProperty(AndalusCliGoldenFixtureSelection.UPDATE_INCLUDE_PROPERTY, previous);
        }
    }

    @Test
    void acceptsGoldenNamesAndBareNames() {
        AndalusCliGoldenFixtureSelection selection = AndalusCliGoldenFixtureSelection.from(
                "status-json.golden, readiness-profiles-check-json");

        assertThat(selection.selected("status-json.golden")).isTrue();
        assertThat(selection.selected("readiness-profiles-check-json.golden")).isTrue();
        assertThat(selection.selected("commands-index-json.golden")).isFalse();
    }

    @Test
    void reportsUnknownSelectionsInInputOrder() {
        AndalusCliGoldenFixtureSelection selection = AndalusCliGoldenFixtureSelection.from(
                "missing-a, status-json, missing-b.golden");

        assertThat(selection.unknownIncludes(Set.of("status-json.golden")))
                .containsExactly("missing-a", "missing-b.golden");
    }

    private static void restoreProperty(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }
}
