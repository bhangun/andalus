package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusCliCommandLinesTest {

    @Test
    void rendersSimpleArgsWithoutQuotes() {
        assertThat(AndalusCliCommandLines.render(List.of("status", "--json")))
                .isEqualTo("status --json");
    }

    @Test
    void quotesArgsWithWhitespace() {
        assertThat(AndalusCliCommandLines.render(List.of("run", "cancel", "run-1", "--reason", "contract stop")))
                .isEqualTo("run cancel run-1 --reason 'contract stop'");
    }

    @Test
    void escapesSingleQuotesInsideQuotedArgs() {
        assertThat(AndalusCliCommandLines.render(List.of("run", "it isn't ready", "--json")))
                .isEqualTo("run 'it isn'\"'\"'t ready' --json");
    }
}
