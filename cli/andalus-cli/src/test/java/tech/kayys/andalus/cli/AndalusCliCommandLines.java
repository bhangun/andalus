package tech.kayys.andalus.cli;

import java.util.List;
import java.util.stream.Collectors;

final class AndalusCliCommandLines {

    private AndalusCliCommandLines() {
    }

    static String render(List<String> args) {
        return args.stream()
                .map(AndalusCliCommandLines::renderArg)
                .collect(Collectors.joining(" "));
    }

    private static String renderArg(String arg) {
        String value = arg == null ? "" : arg;
        if (value.matches("[A-Za-z0-9_@%+=:,./-]+")) {
            return value;
        }
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }
}
