package tech.kayys.andalus.cli.operator;

import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import tech.kayys.andalus.cli.operator.grpc.DefaultOperatorGrpcClient;
import tech.kayys.andalus.operator.v1.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class OperatorInProcessIntegrationTest {

    private Server server;
    private ManagedChannel channel;
    private final AtomicReference<String> capturedTenant = new AtomicReference<>();
    private final AtomicReference<String> capturedActor = new AtomicReference<>();
    private final AtomicReference<String> capturedAuth = new AtomicReference<>();

    private DefaultOperatorGrpcClient client;

    @BeforeEach
    void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();

        ServerInterceptor interceptor = new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                capturedTenant.set(headers.get(Metadata.Key.of("x-andalus-tenant-id", Metadata.ASCII_STRING_MARSHALLER)));
                capturedActor.set(headers.get(Metadata.Key.of("x-andalus-actor-id", Metadata.ASCII_STRING_MARSHALLER)));
                capturedAuth.set(headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)));
                return next.startCall(call, headers);
            }
        };

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .intercept(interceptor)
                .addService(new OperatorServiceGrpc.OperatorServiceImplBase() {
                    @Override
                    public void listPlugins(ListPluginsRequest request, StreamObserver<ListPluginsResponse> responseObserver) {
                        if ("denied-actor".equals(capturedActor.get())) {
                            responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Actor is denied").asRuntimeException());
                            return;
                        }
                        if (capturedAuth.get() == null || !capturedAuth.get().contains("valid-token")) {
                            responseObserver.onError(Status.UNAUTHENTICATED.withDescription("Missing or invalid token").asRuntimeException());
                            return;
                        }
                        responseObserver.onNext(ListPluginsResponse.newBuilder()
                                .addPlugins(PluginSummary.newBuilder().setId("grpc-plugin").setName("gRPC Plugin").build())
                                .build());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void pauseExecution(ExecutionRequest request, StreamObserver<OperationResult> responseObserver) {
                        if ("exec-not-found".equals(request.getExecutionId())) {
                            responseObserver.onError(Status.NOT_FOUND.withDescription("Execution not found").asRuntimeException());
                            return;
                        }
                        responseObserver.onNext(OperationResult.newBuilder()
                                .setOperationId("op-pause-123")
                                .setResourceId(request.getExecutionId())
                                .build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
    }

    @AfterEach
    void tearDown() {
        if (channel != null) channel.shutdownNow();
        if (server != null) server.shutdownNow();
    }

    @Test
    void testEndToEndMetadataPropagationAndSuccess() {
        OperatorCliContext ctx = new OperatorCliContext("inprocess:9090", "tenant-test", "actor-test", "valid-token", "table", null, null, null);
        client = new DefaultOperatorGrpcClient(channel, ctx);

        ListPluginsResponse response = client.listPlugins();
        assertNotNull(response);
        assertEquals(1, response.getPluginsCount());
        assertEquals("grpc-plugin", response.getPlugins(0).getId());

        assertEquals("tenant-test", capturedTenant.get());
        assertEquals("actor-test", capturedActor.get());
        assertEquals("Bearer valid-token", capturedAuth.get());
    }

    @Test
    void testPermissionDeniedErrorMapping() {
        OperatorCliContext ctx = new OperatorCliContext("inprocess:9090", "tenant-test", "denied-actor", "valid-token", "table", null, null, null);
        client = new DefaultOperatorGrpcClient(channel, ctx);

        OperatorCliException ex = assertThrows(OperatorCliException.class, () -> client.listPlugins());
        assertEquals(OperatorCliException.PERMISSION_DENIED, ex.getExitCode());
        assertTrue(ex.getMessage().contains("Actor is denied"));
    }

    @Test
    void testUnauthenticatedErrorMapping() {
        OperatorCliContext ctx = new OperatorCliContext("inprocess:9090", "tenant-test", "actor-test", "bad-token", "table", null, null, null);
        client = new DefaultOperatorGrpcClient(channel, ctx);

        OperatorCliException ex = assertThrows(OperatorCliException.class, () -> client.listPlugins());
        assertEquals(OperatorCliException.UNAUTHENTICATED, ex.getExitCode());
        assertTrue(ex.getMessage().contains("Missing or invalid token"));
    }

    @Test
    void testNotFoundErrorMapping() {
        OperatorCliContext ctx = new OperatorCliContext("inprocess:9090", "tenant-test", "actor-test", "valid-token", "table", null, null, null);
        client = new DefaultOperatorGrpcClient(channel, ctx);

        OperatorCliException ex = assertThrows(OperatorCliException.class, () -> client.pauseExecution("exec-not-found"));
        assertEquals(OperatorCliException.NOT_FOUND, ex.getExitCode());
        assertTrue(ex.getMessage().contains("Execution not found"));
    }

    @Test
    void testCliEndToEndExecutionWithInProcessServer() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outContent, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errContent, true, StandardCharsets.UTF_8);

        OperatorCliContext ctx = new OperatorCliContext("inprocess:9090", "tenant-omega", "operator-agent", "valid-token", "table", null, out, err);
        client = new DefaultOperatorGrpcClient(channel, ctx);

        OperatorCommand opCmd = new OperatorCommand(client, CliOutput.of("table", out, err));
        CommandLine cmd = new CommandLine(opCmd);
        cmd.setOut(new java.io.PrintWriter(out, true));
        cmd.setErr(new java.io.PrintWriter(err, true));

        int exitCode = cmd.execute("plugins", "list");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("grpc-plugin"));
        assertEquals("tenant-omega", capturedTenant.get());
        assertEquals("operator-agent", capturedActor.get());

        outContent.reset();
        exitCode = cmd.execute("executions", "pause", "exec-42");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("op-pause-123"));
    }
}
