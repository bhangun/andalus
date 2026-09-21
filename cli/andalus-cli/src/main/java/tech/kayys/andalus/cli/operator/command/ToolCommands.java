package tech.kayys.andalus.cli.operator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tech.kayys.andalus.cli.operator.OperatorCommand;
import tech.kayys.andalus.operator.v1.ListToolsResponse;
import tech.kayys.andalus.operator.v1.ToolSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "tools",
        description = "Inspect and list operator tools",
        mixinStandardHelpOptions = true,
        subcommands = {
                ToolCommands.ListCommand.class,
                ToolCommands.InspectCommand.class
        }
)
public class ToolCommands {

    @Command(name = "list", description = "List registered tools")
    public static class ListCommand implements Callable<Integer> {
        @ParentCommand
        private ToolCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListToolsResponse resp = root.client().listTools();
            List<Map<String, Object>> list = new ArrayList<>();
            for (ToolSummary t : resp.getToolsList()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", t.getId());
                map.put("name", t.getName());
                map.put("description", t.getDescription());
                list.add(map);
            }
            root.output().print(list);
            return 0;
        }
    }

    @Command(name = "inspect", description = "Inspect tool details")
    public static class InspectCommand implements Callable<Integer> {
        @ParentCommand
        private ToolCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Tool identifier")
        private String toolId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ToolSummary t = root.client().getTool(toolId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getName());
            map.put("description", t.getDescription());
            root.output().print(map);
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
