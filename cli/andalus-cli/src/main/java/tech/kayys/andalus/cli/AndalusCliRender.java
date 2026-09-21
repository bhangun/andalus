package tech.kayys.andalus.cli;

import java.io.PrintStream;
import java.util.function.Supplier;

final public class AndalusCliRender {

    private AndalusCliRender() {
    }

    public static void jsonOrText(
            PrintStream out,
            boolean json,
            Supplier<String> jsonOutput,
            Supplier<String> textOutput) {
        if (json) {
            out.println(jsonOutput.get());
        } else {
            out.print(textOutput.get());
        }
    }
}
