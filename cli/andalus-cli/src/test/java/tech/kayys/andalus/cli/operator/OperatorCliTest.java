package tech.kayys.andalus.cli.operator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import tech.kayys.andalus.cli.operator.grpc.OperatorGrpcClient;
import tech.kayys.andalus.operator.v1.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OperatorCliTest {

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private MockOperatorGrpcClient mockClient;
    private OperatorCommand operatorCommand;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outContent, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errContent, true, StandardCharsets.UTF_8);

        mockClient = new MockOperatorGrpcClient();
        operatorCommand = new OperatorCommand(mockClient, CliOutput.of("table", out, err));
        operatorCommand.setStreams(System.in, out, err);
    }

    private int execute(String... args) {
        CommandLine cmd = new CommandLine(operatorCommand);
        cmd.setOut(new java.io.PrintWriter(new PrintStream(outContent, true, StandardCharsets.UTF_8), true));
        cmd.setErr(new java.io.PrintWriter(new PrintStream(errContent, true, StandardCharsets.UTF_8), true));
        return cmd.execute(args);
    }

    @Test
    void testPluginsListAndInspect() {
        int exitCode = execute("plugins", "list");
        assertEquals(0, exitCode);
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("test-plugin"));

        outContent.reset();
        exitCode = execute("plugins", "inspect", "test-plugin");
        assertEquals(0, exitCode);
        output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("test-plugin"));
    }

    @Test
    void testPluginsLifecycle() {
        int exitCode = execute("plugins", "enable", "test-plugin");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("enabled"));

        outContent.reset();
        exitCode = execute("plugins", "disable", "test-plugin");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("disabled"));

        outContent.reset();
        exitCode = execute("plugins", "unload", "test-plugin");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("unloaded"));
    }

    @Test
    void testSandboxesListAndInspect() {
        int exitCode = execute("sandboxes", "list");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("sb-1"));

        outContent.reset();
        exitCode = execute("sandboxes", "inspect", "sb-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("sb-1"));
    }

    @Test
    void testSandboxesHealthAndMetrics() {
        int exitCode = execute("sandboxes", "health", "sb-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("HEALTHY"));

        outContent.reset();
        exitCode = execute("sandboxes", "metrics", "sb-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("sb-1"));
    }

    @Test
    void testSandboxDestroyRequiresConfirmation() {
        // Without --yes flag, destructive operation must be blocked
        int exitCode = execute("sandboxes", "destroy", "sb-1");
        assertEquals(1, exitCode);
        assertTrue(errContent.toString(StandardCharsets.UTF_8).contains("--yes"));

        errContent.reset();
        exitCode = execute("sandboxes", "destroy", "sb-1", "--yes");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("destroyed"));
    }

    @Test
    void testToolsAndCapabilities() {
        int exitCode = execute("tools", "list");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("tool-1"));

        outContent.reset();
        exitCode = execute("capabilities", "list");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("cap-1"));

        outContent.reset();
        exitCode = execute("capabilities", "type", "LLM");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("cap-1"));
    }

    @Test
    void testExecutionsControl() {
        int exitCode = execute("executions", "list");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("exec-1"));

        outContent.reset();
        exitCode = execute("executions", "pause", "exec-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("paused"));

        outContent.reset();
        exitCode = execute("executions", "resume", "exec-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("resumed"));

        outContent.reset();
        exitCode = execute("executions", "cancel", "exec-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("cancelled"));
    }

    @Test
    void testSessionsControl() {
        int exitCode = execute("sessions", "list");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("sess-1"));

        outContent.reset();
        exitCode = execute("sessions", "suspend", "sess-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("suspended"));

        outContent.reset();
        exitCode = execute("sessions", "close", "sess-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("closed"));
    }

    @Test
    void testDiagnostics() {
        int exitCode = execute("diagnostics", "all");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("HEALTHY"));

        outContent.reset();
        exitCode = execute("diagnostics", "type", "SANDBOX");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("HEALTHY"));
    }

    @Test
    void testConfigurations() {
        int exitCode = execute("configurations", "list");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("cfg-1"));

        outContent.reset();
        exitCode = execute("configurations", "active");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("cfg-1"));

        outContent.reset();
        exitCode = execute("configurations", "activate", "cfg-1");
        assertEquals(0, exitCode);
        assertTrue(outContent.toString(StandardCharsets.UTF_8).contains("activated"));
    }

    @Test
    void testJsonOutputFormat() {
        OperatorCommand jsonCmd = new OperatorCommand(mockClient, CliOutput.of("json", new PrintStream(outContent, true, StandardCharsets.UTF_8), new PrintStream(errContent, true, StandardCharsets.UTF_8)));
        CommandLine cmd = new CommandLine(jsonCmd);
        int exitCode = cmd.execute("plugins", "list");
        assertEquals(0, exitCode);
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("\"id\" : \"test-plugin\""));
    }

    private static class MockOperatorGrpcClient implements OperatorGrpcClient {

        @Override
        public ListPluginsResponse listPlugins() {
            return ListPluginsResponse.newBuilder()
                    .addPlugins(PluginSummary.newBuilder().setId("test-plugin").setName("Test Plugin").setVersion("1.0").setState("ACTIVE").setDescription("A plugin").build())
                    .build();
        }

        @Override
        public PluginSummary getPlugin(String pluginId) {
            return PluginSummary.newBuilder().setId(pluginId).setName("Test Plugin").setVersion("1.0").setState("ACTIVE").setDescription("A plugin").build();
        }

        @Override
        public OperationResult enablePlugin(String pluginId) {
            return OperationResult.newBuilder().setOperationId("op-1").setResourceId(pluginId).build();
        }

        @Override
        public OperationResult disablePlugin(String pluginId) {
            return OperationResult.newBuilder().setOperationId("op-2").setResourceId(pluginId).build();
        }

        @Override
        public OperationResult unloadPlugin(String pluginId) {
            return OperationResult.newBuilder().setOperationId("op-3").setResourceId(pluginId).build();
        }

        @Override
        public ListSandboxesResponse listSandboxes() {
            return ListSandboxesResponse.newBuilder()
                    .addSandboxes(SandboxSummary.newBuilder().setId("sb-1").setTenantId("default").setState("RUNNING").setProviderId("docker").setCreatedAt("2026-09-20").build())
                    .build();
        }

        @Override
        public SandboxSummary getSandbox(String sandboxId) {
            return SandboxSummary.newBuilder().setId(sandboxId).setTenantId("default").setState("RUNNING").setProviderId("docker").setCreatedAt("2026-09-20").build();
        }

        @Override
        public SandboxHealth getSandboxHealth(String sandboxId) {
            return SandboxHealth.newBuilder().setSandboxId(sandboxId).setStatus("HEALTHY").setSummary("All good").setCheckedAt("2026-09-20").build();
        }

        @Override
        public SandboxMetrics getSandboxMetrics(String sandboxId) {
            return SandboxMetrics.newBuilder().setSandboxId(sandboxId).putMetrics("cpu", 0.15).build();
        }

        @Override
        public SandboxDiagnostics getSandboxDiagnostics(String sandboxId) {
            return SandboxDiagnostics.newBuilder().setSandboxId(sandboxId).setStatus("HEALTHY").build();
        }

        @Override
        public OperationResult stopSandbox(String sandboxId) {
            return OperationResult.newBuilder().setOperationId("op-sb-stop").setResourceId(sandboxId).build();
        }

        @Override
        public OperationResult destroySandbox(String sandboxId) {
            return OperationResult.newBuilder().setOperationId("op-sb-destroy").setResourceId(sandboxId).build();
        }

        @Override
        public ListToolsResponse listTools() {
            return ListToolsResponse.newBuilder()
                    .addTools(ToolSummary.newBuilder().setId("tool-1").setName("Tool One").setDescription("A tool").build())
                    .build();
        }

        @Override
        public ToolSummary getTool(String toolId) {
            return ToolSummary.newBuilder().setId(toolId).setName("Tool One").setDescription("A tool").build();
        }

        @Override
        public ListCapabilitiesResponse listCapabilities() {
            return ListCapabilitiesResponse.newBuilder()
                    .addCapabilities(CapabilitySummary.newBuilder().setId("cap-1").setType("LLM").setProviderId("provider-1").addTags("fast").build())
                    .build();
        }

        @Override
        public ListCapabilitiesResponse listCapabilitiesByType(String type) {
            return listCapabilities();
        }

        @Override
        public ListCapabilitiesResponse listCapabilitiesByTag(String tag) {
            return listCapabilities();
        }

        @Override
        public ListExecutionsResponse listExecutions() {
            return ListExecutionsResponse.newBuilder()
                    .addExecutions(ExecutionSummary.newBuilder().setExecutionId("exec-1").setTenantId("default").setState("RUNNING").setWorkflowId("wf-1").setCreatedAt("2026-09-20").build())
                    .build();
        }

        @Override
        public ExecutionSummary getExecution(String executionId) {
            return ExecutionSummary.newBuilder().setExecutionId(executionId).setTenantId("default").setState("RUNNING").setWorkflowId("wf-1").setCreatedAt("2026-09-20").build();
        }

        @Override
        public OperationResult pauseExecution(String executionId) {
            return OperationResult.newBuilder().setOperationId("op-pause").setResourceId(executionId).build();
        }

        @Override
        public OperationResult resumeExecution(String executionId) {
            return OperationResult.newBuilder().setOperationId("op-resume").setResourceId(executionId).build();
        }

        @Override
        public OperationResult cancelExecution(String executionId) {
            return OperationResult.newBuilder().setOperationId("op-cancel").setResourceId(executionId).build();
        }

        @Override
        public OperationResult retryExecution(String executionId) {
            return OperationResult.newBuilder().setOperationId("op-retry").setResourceId(executionId).build();
        }

        @Override
        public ListSessionsResponse listSessions() {
            return ListSessionsResponse.newBuilder()
                    .addSessions(SessionSummary.newBuilder().setSessionId("sess-1").setTenantId("default").setState("ACTIVE").setCreatedAt("2026-09-20").build())
                    .build();
        }

        @Override
        public SessionSummary getSession(String sessionId) {
            return SessionSummary.newBuilder().setSessionId(sessionId).setTenantId("default").setState("ACTIVE").setCreatedAt("2026-09-20").build();
        }

        @Override
        public OperationResult suspendSession(String sessionId) {
            return OperationResult.newBuilder().setOperationId("op-sess-suspend").setResourceId(sessionId).build();
        }

        @Override
        public OperationResult resumeSession(String sessionId) {
            return OperationResult.newBuilder().setOperationId("op-sess-resume").setResourceId(sessionId).build();
        }

        @Override
        public OperationResult closeSession(String sessionId) {
            return OperationResult.newBuilder().setOperationId("op-sess-close").setResourceId(sessionId).build();
        }

        @Override
        public DiagnosticReport diagnoseAll() {
            return DiagnosticReport.newBuilder()
                    .setOverallStatus("HEALTHY")
                    .setCheckedAt("2026-09-20")
                    .addResults(DiagnosticResult.newBuilder().setComponentType("PLUGIN").setComponentId("plug-1").setStatus("HEALTHY").setSummary("OK").build())
                    .build();
        }

        @Override
        public DiagnosticReport diagnoseType(String type) {
            return diagnoseAll();
        }

        @Override
        public DiagnosticResult diagnoseComponent(String type, String componentId) {
            return DiagnosticResult.newBuilder().setComponentType(type).setComponentId(componentId).setStatus("HEALTHY").setSummary("OK").build();
        }

        @Override
        public ListConfigurationsResponse listConfigurations(boolean includeValues) {
            return ListConfigurationsResponse.newBuilder()
                    .addConfigurations(ConfigurationSummary.newBuilder().setId("cfg-1").setPath("runtime.yaml").setType("YAML").setSource("file").setStatus("LOADED").setActive(true).build())
                    .build();
        }

        @Override
        public ConfigurationSummary getConfiguration(String configurationId) {
            return ConfigurationSummary.newBuilder().setId(configurationId).setPath("runtime.yaml").setType("YAML").setSource("file").setStatus("LOADED").setActive(true).build();
        }

        @Override
        public ConfigurationSummary getActiveConfiguration() {
            return getConfiguration("cfg-1");
        }

        @Override
        public ConfigurationSummary updateConfiguration(String configurationId, Map<String, String> values, boolean activate) {
            return getConfiguration(configurationId);
        }

        @Override
        public OperationResult activateConfiguration(String configurationId) {
            return OperationResult.newBuilder().setOperationId("op-cfg-act").setResourceId(configurationId).build();
        }

        @Override
        public void close() {
        }
    }
}
