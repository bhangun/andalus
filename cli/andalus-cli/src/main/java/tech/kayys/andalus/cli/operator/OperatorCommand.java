package tech.kayys.andalus.cli.operator;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import tech.kayys.andalus.cli.operator.command.*;
import tech.kayys.andalus.cli.operator.grpc.DefaultOperatorGrpcClient;
import tech.kayys.andalus.cli.operator.grpc.OperatorGrpcClient;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;

/**
 * Root command for the Andalus Operator control-plane CLI.
 */
@Command(
        name = "operator",
        description = "Andalus operator control-plane operations (plugins, sandboxes, executions, etc.)",
        mixinStandardHelpOptions = true,
        subcommands = {
                PluginCommands.class,
                SandboxCommands.class,
                ToolCommands.class,
                CapabilityCommands.class,
                ExecutionCommands.class,
                SessionCommands.class,
                DiagnosticsCommands.class,
                ConfigurationCommands.class
        }
)
public class OperatorCommand implements Callable<Integer>, AutoCloseable {

    @Option(names = {"--server", "-s"}, description = "Operator gRPC server endpoint", defaultValue = "${env:ANDALUS_OPERATOR_SERVER:-localhost:9090}")
    private String server = "localhost:9090";

    @Option(names = {"--tenant", "-t"}, description = "Tenant identifier", defaultValue = "${env:ANDALUS_OPERATOR_TENANT:-default}")
    private String tenant = "default";

    @Option(names = "--actor", description = "Operator subject / actor identifier", defaultValue = "${env:ANDALUS_OPERATOR_ACTOR:-system-operator}")
    private String actor = "system-operator";

    @Option(names = "--token", description = "Bearer authentication token", defaultValue = "${env:ANDALUS_OPERATOR_TOKEN:-}")
    private String token;

    @Option(names = {"--format", "-f", "--output", "-o"}, description = "Output format (table, json)", defaultValue = "table")
    private String format = "table";

    private OperatorGrpcClient client;
    private CliOutput output;
    private InputStream in = System.in;
    private PrintStream out = System.out;
    private PrintStream err = System.err;

    public OperatorCommand() {
    }

    public OperatorCommand(OperatorGrpcClient client, CliOutput output) {
        this.client = client;
        this.output = output;
    }

    public void setStreams(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public synchronized OperatorGrpcClient client() {
        if (client == null) {
            OperatorCliContext ctx = new OperatorCliContext(server, tenant, actor, token, format, in, out, err);
            client = new DefaultOperatorGrpcClient(ctx);
        }
        return client;
    }

    public synchronized CliOutput output() {
        if (output == null) {
            output = CliOutput.of(format, out, err);
        }
        return output;
    }

    public String tenant() {
        return tenant;
    }

    public String actor() {
        return actor;
    }

    @Override
    public Integer call() {
        CommandLine.usage(this, out);
        return 0;
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
