package tech.kayys.andalus.cli.operator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tech.kayys.andalus.cli.operator.OperatorCommand;
import tech.kayys.andalus.operator.v1.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "sandboxes",
        description = "Manage and inspect execution sandboxes",
        mixinStandardHelpOptions = true,
        subcommands = {
                SandboxCommands.ListCommand.class,
                SandboxCommands.InspectCommand.class,
                SandboxCommands.HealthCommand.class,
                SandboxCommands.MetricsCommand.class,
                SandboxCommands.DiagnosticsCommand.class,
                SandboxCommands.StopCommand.class,
                SandboxCommands.DestroyCommand.class
        }
)
public class SandboxCommands {

    @Command(name = "list", description = "List sandboxes")
    public static class ListCommand implements Callable<Integer> {
        @ParentCommand
        private SandboxCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListSandboxesResponse resp = root.client().listSandboxes();
            List<Map<String, Object>> list = new ArrayList<>();
            for (SandboxSummary s : resp.getSandboxesList()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", s.getId());
                map.put("tenantId", s.getTenantId());
                map.put("state", s.getState());
                map.put("providerId", s.getProviderId());
                map.put("createdAt", s.getCreatedAt());
                list.add(map);
            }
            root.output().print(list);
            return 0;
        }
    }

    @Command(name = "inspect", description = "Inspect sandbox details")
    public static class InspectCommand implements Callable<Integer> {
        @ParentCommand
        private SandboxCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Sandbox identifier")
        private String sandboxId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            SandboxSummary s = root.client().getSandbox(sandboxId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("tenantId", s.getTenantId());
            map.put("state", s.getState());
            map.put("providerId", s.getProviderId());
            map.put("createdAt", s.getCreatedAt());
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "health", description = "Check sandbox health status")
    public static class HealthCommand implements Callable<Integer> {
        @ParentCommand
        private SandboxCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Sandbox identifier")
        private String sandboxId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            SandboxHealth h = root.client().getSandboxHealth(sandboxId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sandboxId", h.getSandboxId());
            map.put("status", h.getStatus());
            map.put("summary", h.getSummary());
            map.put("checkedAt", h.getCheckedAt());
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "metrics", description = "Fetch sandbox metrics")
    public static class MetricsCommand implements Callable<Integer> {
        @ParentCommand
        private SandboxCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Sandbox identifier")
        private String sandboxId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            SandboxMetrics m = root.client().getSandboxMetrics(sandboxId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sandboxId", m.getSandboxId());
            map.put("metrics", m.getMetricsMap());
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "diagnostics", description = "Fetch sandbox diagnostics")
    public static class DiagnosticsCommand implements Callable<Integer> {
        @ParentCommand
        private SandboxCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Sandbox identifier")
        private String sandboxId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            SandboxDiagnostics d = root.client().getSandboxDiagnostics(sandboxId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sandboxId", d.getSandboxId());
            map.put("status", d.getStatus());
            List<Map<String, Object>> issues = new ArrayList<>();
            for (DiagnosticIssue issue : d.getIssuesList()) {
                issues.add(Map.of("code", issue.getCode(), "message", issue.getMessage(), "severity", issue.getSeverity()));
            }
            map.put("issues", issues);
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "stop", description = "Stop a running sandbox")
    public static class StopCommand implements Callable<Integer> {
        @ParentCommand
        private SandboxCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Sandbox identifier")
        private String sandboxId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().stopSandbox(sandboxId);
            root.output().success("Sandbox " + sandboxId + " stopped (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    @Command(name = "destroy", description = "Destroy a sandbox (destructive)")
    public static class DestroyCommand implements Callable<Integer> {
        @ParentCommand
        private SandboxCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Sandbox identifier")
        private String sandboxId;

        @Option(names = {"--yes", "-y"}, description = "Confirm destruction without interactive prompt")
        private boolean confirmed;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            if (!confirmed) {
                // In non-interactive or unconfirmed mode, require confirmation flag
                root.output().error("Destructive operation requires --yes confirmation flag: destroy sandbox " + sandboxId);
                return 1;
            }
            OperationResult result = root.client().destroySandbox(sandboxId);
            root.output().success("Sandbox " + sandboxId + " destroyed (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    private static OperatorCommand findRoot(picocli.CommandLine.Model.CommandSpec spec) {
        picocli.CommandLine.Model.CommandSpec cur = spec;
        while (cur != null) {
            if (cur.userObject() instanceof OperatorCommand oc) {
                return oc;
            }
            cur = cur.parent();
        }
        throw new IllegalStateException("OperatorCommand root not found in command hierarchy");
    }
}
