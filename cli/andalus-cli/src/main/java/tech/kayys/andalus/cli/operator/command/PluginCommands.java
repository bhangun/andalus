package tech.kayys.andalus.cli.operator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tech.kayys.andalus.cli.operator.OperatorCommand;
import tech.kayys.andalus.operator.v1.ListPluginsResponse;
import tech.kayys.andalus.operator.v1.OperationResult;
import tech.kayys.andalus.operator.v1.PluginSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "plugins",
        description = "Manage and inspect operator plugins",
        mixinStandardHelpOptions = true,
        subcommands = {
                PluginCommands.ListCommand.class,
                PluginCommands.InspectCommand.class,
                PluginCommands.EnableCommand.class,
                PluginCommands.DisableCommand.class,
                PluginCommands.UnloadCommand.class
        }
)
public class PluginCommands {

    @Command(name = "list", description = "List registered plugins")
    public static class ListCommand implements Callable<Integer> {
        @ParentCommand
        private PluginCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListPluginsResponse resp = root.client().listPlugins();
            List<Map<String, Object>> list = new ArrayList<>();
            for (PluginSummary p : resp.getPluginsList()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", p.getId());
                map.put("name", p.getName());
                map.put("version", p.getVersion());
                map.put("state", p.getState());
                map.put("description", p.getDescription());
                list.add(map);
            }
            root.output().print(list);
            return 0;
        }
    }

    @Command(name = "inspect", description = "Inspect detailed plugin state")
    public static class InspectCommand implements Callable<Integer> {
        @ParentCommand
        private PluginCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Plugin identifier")
        private String pluginId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            PluginSummary p = root.client().getPlugin(pluginId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("version", p.getVersion());
            map.put("state", p.getState());
            map.put("description", p.getDescription());
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "enable", description = "Enable a plugin")
    public static class EnableCommand implements Callable<Integer> {
        @ParentCommand
        private PluginCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Plugin identifier")
        private String pluginId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().enablePlugin(pluginId);
            root.output().success("Plugin " + pluginId + " enabled (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    @Command(name = "disable", description = "Disable a plugin")
    public static class DisableCommand implements Callable<Integer> {
        @ParentCommand
        private PluginCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Plugin identifier")
        private String pluginId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().disablePlugin(pluginId);
            root.output().success("Plugin " + pluginId + " disabled (operation: " + result.getOperationId() + ")");
            return 0;
        }
    }

    @Command(name = "unload", description = "Unload a plugin")
    public static class UnloadCommand implements Callable<Integer> {
        @ParentCommand
        private PluginCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Plugin identifier")
        private String pluginId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().unloadPlugin(pluginId);
            root.output().success("Plugin " + pluginId + " unloaded (operation: " + result.getOperationId() + ")");
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
