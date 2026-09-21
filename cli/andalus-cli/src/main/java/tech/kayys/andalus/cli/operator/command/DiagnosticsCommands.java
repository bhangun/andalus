package tech.kayys.andalus.cli.operator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tech.kayys.andalus.cli.operator.OperatorCommand;
import tech.kayys.andalus.operator.v1.DiagnosticReport;
import tech.kayys.andalus.operator.v1.DiagnosticResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "diagnostics",
        description = "System diagnostics operations",
        mixinStandardHelpOptions = true,
        subcommands = {
                DiagnosticsCommands.AllCommand.class,
                DiagnosticsCommands.TypeCommand.class,
                DiagnosticsCommands.InspectCommand.class
        }
)
public class DiagnosticsCommands {

    @Command(name = "all", description = "Run diagnostics across all platform subsystems")
    public static class AllCommand implements Callable<Integer> {
        @ParentCommand
        private DiagnosticsCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            DiagnosticReport report = root.client().diagnoseAll();
            printReport(root, report);
            return 0;
        }
    }

    @Command(name = "type", description = "Run diagnostics for a subsystem type (e.g. SANDBOX, PLUGIN, MODEL)")
    public static class TypeCommand implements Callable<Integer> {
        @ParentCommand
        private DiagnosticsCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Subsystem component type")
        private String type;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            DiagnosticReport report = root.client().diagnoseType(type);
            printReport(root, report);
            return 0;
        }
    }

    @Command(name = "inspect", description = "Inspect a specific component's diagnostics")
    public static class InspectCommand implements Callable<Integer> {
        @ParentCommand
        private DiagnosticsCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Subsystem component type")
        private String type;

        @Parameters(index = "1", description = "Component identifier")
        private String componentId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            DiagnosticResult res = root.client().diagnoseComponent(type, componentId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("componentType", res.getComponentType());
            map.put("componentId", res.getComponentId());
            map.put("status", res.getStatus());
            map.put("summary", res.getSummary());
            map.put("checkedAt", res.getCheckedAt());
            root.output().print(map);
            return 0;
        }
    }

    private static void printReport(OperatorCommand root, DiagnosticReport report) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("overallStatus", report.getOverallStatus());
        map.put("checkedAt", report.getCheckedAt());
        List<Map<String, Object>> results = new ArrayList<>();
        for (DiagnosticResult r : report.getResultsList()) {
            results.add(Map.of(
                    "componentType", r.getComponentType(),
                    "componentId", r.getComponentId(),
                    "status", r.getStatus(),
                    "summary", r.getSummary()
            ));
        }
        map.put("results", results);
        root.output().print(map);
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
