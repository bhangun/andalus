package tech.kayys.andalus.cli.operator.grpc;

import tech.kayys.andalus.operator.v1.*;

import java.util.Map;

/**
 * Facade contract for communicating with the Andalus Operator gRPC API.
 */
public interface OperatorGrpcClient extends AutoCloseable {

    // Plugins
    ListPluginsResponse listPlugins();
    PluginSummary getPlugin(String pluginId);
    OperationResult enablePlugin(String pluginId);
    OperationResult disablePlugin(String pluginId);
    OperationResult unloadPlugin(String pluginId);

    // Sandboxes
    ListSandboxesResponse listSandboxes();
    SandboxSummary getSandbox(String sandboxId);
    SandboxHealth getSandboxHealth(String sandboxId);
    SandboxMetrics getSandboxMetrics(String sandboxId);
    SandboxDiagnostics getSandboxDiagnostics(String sandboxId);
    OperationResult stopSandbox(String sandboxId);
    OperationResult destroySandbox(String sandboxId);

    // Tools & Capabilities
    ListToolsResponse listTools();
    ToolSummary getTool(String toolId);
    ListCapabilitiesResponse listCapabilities();
    ListCapabilitiesResponse listCapabilitiesByType(String type);
    ListCapabilitiesResponse listCapabilitiesByTag(String tag);

    // Executions
    ListExecutionsResponse listExecutions();
    ExecutionSummary getExecution(String executionId);
    OperationResult pauseExecution(String executionId);
    OperationResult resumeExecution(String executionId);
    OperationResult cancelExecution(String executionId);
    OperationResult retryExecution(String executionId);

    // Sessions
    ListSessionsResponse listSessions();
    SessionSummary getSession(String sessionId);
    OperationResult suspendSession(String sessionId);
    OperationResult resumeSession(String sessionId);
    OperationResult closeSession(String sessionId);

    // Diagnostics
    DiagnosticReport diagnoseAll();
    DiagnosticReport diagnoseType(String type);
    DiagnosticResult diagnoseComponent(String type, String componentId);

    // Configurations
    ListConfigurationsResponse listConfigurations(boolean includeValues);
    ConfigurationSummary getConfiguration(String configurationId);
    ConfigurationSummary getActiveConfiguration();
    ConfigurationSummary updateConfiguration(String configurationId, Map<String, String> values, boolean activate);
    OperationResult activateConfiguration(String configurationId);

    @Override
    void close();
}
