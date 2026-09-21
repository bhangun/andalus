package tech.kayys.andalus.cli.operator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;

/**
 * Context for operator CLI executions containing connection, auth, and output parameters.
 */
public record OperatorCliContext(
        String server,
        String tenantId,
        String actorId,
        String token,
        String format,
        InputStream in,
        PrintStream out,
        PrintStream err
) {
    public OperatorCliContext {
        server = (server == null || server.isBlank()) ? "localhost:9090" : server;
        tenantId = (tenantId == null || tenantId.isBlank()) ? "default" : tenantId;
        actorId = (actorId == null || actorId.isBlank()) ? "system-operator" : actorId;
        format = (format == null || format.isBlank()) ? "table" : format;
        in = in != null ? in : System.in;
        out = out != null ? out : System.out;
        err = err != null ? err : System.err;
    }

    public CliOutput createOutput() {
        return CliOutput.of(format, out, err);
    }
}
