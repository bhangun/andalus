package tech.kayys.andalus.cli.operator.grpc;

import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import tech.kayys.andalus.cli.operator.OperatorCliContext;
import tech.kayys.andalus.cli.operator.OperatorCliException;
import tech.kayys.andalus.operator.v1.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Default gRPC client implementation for the Operator CLI.
 * Enforces metadata propagation, deadline policies, and exception mapping.
 */
public class DefaultOperatorGrpcClient implements OperatorGrpcClient {

    private final Channel channel;
    private final boolean managedChannel;
    private final OperatorServiceGrpc.OperatorServiceBlockingStub stub;

    public DefaultOperatorGrpcClient(OperatorCliContext context) {
        String server = context.server();
        String host = "localhost";
        int port = 9090;
        if (server.contains(":")) {
            String[] parts = server.split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        } else {
            host = server;
        }

        ManagedChannel mc = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.channel = mc;
        this.managedChannel = true;
        this.stub = configureStub(channel, context);
    }

    public DefaultOperatorGrpcClient(Channel channel, OperatorCliContext context) {
        this.channel = channel;
        this.managedChannel = false;
        this.stub = configureStub(channel, context);
    }

    private OperatorServiceGrpc.OperatorServiceBlockingStub configureStub(Channel ch, OperatorCliContext context) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("x-andalus-tenant-id", Metadata.ASCII_STRING_MARSHALLER), context.tenantId());
        metadata.put(Metadata.Key.of("x-andalus-actor-id", Metadata.ASCII_STRING_MARSHALLER), context.actorId());
        if (context.token() != null && !context.token().isBlank()) {
            String authHeader = context.token().startsWith("Bearer ") ? context.token() : "Bearer " + context.token();
            metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), authHeader);
        }

        return OperatorServiceGrpc.newBlockingStub(ch)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }

    private OperatorRequestContext newContext() {
        return OperatorRequestContext.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setCorrelationId(UUID.randomUUID().toString())
                .build();
    }

    private <T> T call(Supplier<T> action) {
        try {
            return action.get();
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            String description = status.getDescription() != null ? status.getDescription() : e.getMessage();
            int exitCode = mapStatusCode(status.getCode());
            throw new OperatorCliException(description, e, exitCode);
        } catch (Exception e) {
            throw new OperatorCliException("Operation failed: " + e.getMessage(), e, OperatorCliException.GENERAL_ERROR);
        }
    }

    private int mapStatusCode(Status.Code code) {
        return switch (code) {
            case INVALID_ARGUMENT -> OperatorCliException.INVALID_ARGUMENT;
            case UNAUTHENTICATED -> OperatorCliException.UNAUTHENTICATED;
            case PERMISSION_DENIED -> OperatorCliException.PERMISSION_DENIED;
            case NOT_FOUND -> OperatorCliException.NOT_FOUND;
            case ALREADY_EXISTS -> OperatorCliException.CONFLICT;
            case DEADLINE_EXCEEDED -> OperatorCliException.TIMEOUT;
            case UNAVAILABLE -> OperatorCliException.UNAVAILABLE;
            case FAILED_PRECONDITION -> OperatorCliException.CONFIGURATION_ERROR;
            default -> OperatorCliException.GENERAL_ERROR;
        };
    }

    // Plugins
    @Override
    public ListPluginsResponse listPlugins() {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listPlugins(ListPluginsRequest.newBuilder().setContext(newContext()).build()));
    }

    @Override
    public PluginSummary getPlugin(String pluginId) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getPlugin(GetPluginRequest.newBuilder().setContext(newContext()).setPluginId(pluginId).build()).getPlugin());
    }

    @Override
    public OperationResult enablePlugin(String pluginId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .enablePlugin(PluginOperationRequest.newBuilder().setContext(newContext()).setPluginId(pluginId).build()));
    }

    @Override
    public OperationResult disablePlugin(String pluginId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .disablePlugin(PluginOperationRequest.newBuilder().setContext(newContext()).setPluginId(pluginId).build()));
    }

    @Override
    public OperationResult unloadPlugin(String pluginId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .unloadPlugin(PluginOperationRequest.newBuilder().setContext(newContext()).setPluginId(pluginId).build()));
    }

    // Sandboxes
    @Override
    public ListSandboxesResponse listSandboxes() {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listSandboxes(ListSandboxesRequest.newBuilder().setContext(newContext()).build()));
    }

    @Override
    public SandboxSummary getSandbox(String sandboxId) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getSandbox(SandboxRequest.newBuilder().setContext(newContext()).setSandboxId(sandboxId).build()));
    }

    @Override
    public SandboxHealth getSandboxHealth(String sandboxId) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getSandboxHealth(SandboxRequest.newBuilder().setContext(newContext()).setSandboxId(sandboxId).build()));
    }

    @Override
    public SandboxMetrics getSandboxMetrics(String sandboxId) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getSandboxMetrics(SandboxRequest.newBuilder().setContext(newContext()).setSandboxId(sandboxId).build()));
    }

    @Override
    public SandboxDiagnostics getSandboxDiagnostics(String sandboxId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .getSandboxDiagnostics(SandboxRequest.newBuilder().setContext(newContext()).setSandboxId(sandboxId).build()));
    }

    @Override
    public OperationResult stopSandbox(String sandboxId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .stopSandbox(SandboxRequest.newBuilder().setContext(newContext()).setSandboxId(sandboxId).build()));
    }

    @Override
    public OperationResult destroySandbox(String sandboxId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .destroySandbox(SandboxRequest.newBuilder().setContext(newContext()).setSandboxId(sandboxId).build()));
    }

    // Tools & Capabilities
    @Override
    public ListToolsResponse listTools() {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listTools(ListToolsRequest.newBuilder().setContext(newContext()).build()));
    }

    @Override
    public ToolSummary getTool(String toolId) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getTool(ToolRequest.newBuilder().setContext(newContext()).setToolId(toolId).build()));
    }

    @Override
    public ListCapabilitiesResponse listCapabilities() {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listCapabilities(ListCapabilitiesRequest.newBuilder().setContext(newContext()).build()));
    }

    @Override
    public ListCapabilitiesResponse listCapabilitiesByType(String type) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listCapabilitiesByType(ListCapabilitiesByTypeRequest.newBuilder().setContext(newContext()).setType(type).build()));
    }

    @Override
    public ListCapabilitiesResponse listCapabilitiesByTag(String tag) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listCapabilitiesByTag(ListCapabilitiesByTagRequest.newBuilder().setContext(newContext()).setTag(tag).build()));
    }

    // Executions
    @Override
    public ListExecutionsResponse listExecutions() {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listExecutions(ListExecutionsRequest.newBuilder().setContext(newContext()).build()));
    }

    @Override
    public ExecutionSummary getExecution(String executionId) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getExecution(ExecutionRequest.newBuilder().setContext(newContext()).setExecutionId(executionId).build()));
    }

    @Override
    public OperationResult pauseExecution(String executionId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .pauseExecution(ExecutionRequest.newBuilder().setContext(newContext()).setExecutionId(executionId).build()));
    }

    @Override
    public OperationResult resumeExecution(String executionId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .resumeExecution(ExecutionRequest.newBuilder().setContext(newContext()).setExecutionId(executionId).build()));
    }

    @Override
    public OperationResult cancelExecution(String executionId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .cancelExecution(ExecutionRequest.newBuilder().setContext(newContext()).setExecutionId(executionId).build()));
    }

    @Override
    public OperationResult retryExecution(String executionId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .retryExecution(ExecutionRequest.newBuilder().setContext(newContext()).setExecutionId(executionId).build()));
    }

    // Sessions
    @Override
    public ListSessionsResponse listSessions() {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listSessions(ListSessionsRequest.newBuilder().setContext(newContext()).build()));
    }

    @Override
    public SessionSummary getSession(String sessionId) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getSession(SessionRequest.newBuilder().setContext(newContext()).setSessionId(sessionId).build()));
    }

    @Override
    public OperationResult suspendSession(String sessionId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .suspendSession(SessionRequest.newBuilder().setContext(newContext()).setSessionId(sessionId).build()));
    }

    @Override
    public OperationResult resumeSession(String sessionId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .resumeSession(SessionRequest.newBuilder().setContext(newContext()).setSessionId(sessionId).build()));
    }

    @Override
    public OperationResult closeSession(String sessionId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .closeSession(SessionRequest.newBuilder().setContext(newContext()).setSessionId(sessionId).build()));
    }

    // Diagnostics
    @Override
    public DiagnosticReport diagnoseAll() {
        return call(() -> stub.withDeadlineAfter(60, TimeUnit.SECONDS)
                .diagnoseAll(DiagnoseAllRequest.newBuilder().setContext(newContext()).build()));
    }

    @Override
    public DiagnosticReport diagnoseType(String type) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .diagnoseType(DiagnoseTypeRequest.newBuilder().setContext(newContext()).setType(type).build()));
    }

    @Override
    public DiagnosticResult diagnoseComponent(String type, String componentId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .diagnoseComponent(DiagnoseComponentRequest.newBuilder().setContext(newContext()).setType(type).setComponentId(componentId).build()));
    }

    // Configurations
    @Override
    public ListConfigurationsResponse listConfigurations(boolean includeValues) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .listConfigurations(ListConfigurationsRequest.newBuilder().setContext(newContext()).setIncludeValues(includeValues).build()));
    }

    @Override
    public ConfigurationSummary getConfiguration(String configurationId) {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getConfiguration(ConfigurationRequest.newBuilder().setContext(newContext()).setConfigurationId(configurationId).build()));
    }

    @Override
    public ConfigurationSummary getActiveConfiguration() {
        return call(() -> stub.withDeadlineAfter(10, TimeUnit.SECONDS)
                .getActiveConfiguration(newContext()));
    }

    @Override
    public ConfigurationSummary updateConfiguration(String configurationId, Map<String, String> values, boolean activate) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .updateConfiguration(UpdateConfigurationRequest.newBuilder()
                        .setContext(newContext())
                        .setConfigurationId(configurationId)
                        .putAllValues(values != null ? values : Map.of())
                        .setActivate(activate)
                        .build()));
    }

    @Override
    public OperationResult activateConfiguration(String configurationId) {
        return call(() -> stub.withDeadlineAfter(30, TimeUnit.SECONDS)
                .activateConfiguration(ConfigurationRequest.newBuilder().setContext(newContext()).setConfigurationId(configurationId).build()));
    }

    @Override
    public void close() {
        if (managedChannel && channel instanceof ManagedChannel mc) {
            mc.shutdown();
        }
    }
}
