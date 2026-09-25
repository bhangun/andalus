package tech.kayys.andalus.cli;

import tech.kayys.andalus.client.AndalusClient;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Runtime context shared by CLI command modules.
 *
 * <p>The context owns streams and lazy client resolution only; domain services
 * are reached through {@link AndalusClient} facades so the CLI remains a wrapper
 * around the SDK boundary.</p>
 */
public final class AndalusCliContext {

    private final Supplier<AndalusClient> clients;
    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;
    private AndalusClient client;

    public AndalusCliContext(
            Supplier<AndalusClient> clients,
            InputStream in,
            PrintStream out,
            PrintStream err) {
        this.clients = Objects.requireNonNull(clients, "clients");
        this.in = in == null ? InputStream.nullInputStream() : in;
        this.out = Objects.requireNonNull(out, "out");
        this.err = Objects.requireNonNull(err, "err");
    }

    public AndalusClient client() {
        if (client == null) {
            client = Objects.requireNonNull(clients.get(), "client");
        }
        return client;
    }

    public InputStream in() {
        return in;
    }

    public PrintStream out() {
        return out;
    }

    public PrintStream err() {
        return err;
    }

    public int commandFailure(RuntimeException e) {
        err.println("Andalus command failed: " + e.getMessage());
        return 2;
    }
}
