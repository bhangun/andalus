package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import tech.kayys.andalus.cli.AndalusGollekCli;
import tech.kayys.andalus.cli.AndalusTuiCommands;
import tech.kayys.andalus.gollek.sdk.AndalusGollekSdk;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusTuiCommandsTest {

    @Test
    void tuiCommandIsRegisteredFromDedicatedModule() {
        CommandLine root = commandLine();

        assertThat((Object) root.getSubcommands().get("tui").getCommand())
                .isInstanceOf(AndalusTuiCommands.TuiCommand.class);
    }

    private static CommandLine commandLine() {
        return new CommandLine(new AndalusGollekCli(
                AndalusGollekSdk.local(),
                stream(new ByteArrayOutputStream()),
                stream(new ByteArrayOutputStream())));
    }

    private static PrintStream stream(ByteArrayOutputStream output) {
        return new PrintStream(output, true, StandardCharsets.UTF_8);
    }
}
