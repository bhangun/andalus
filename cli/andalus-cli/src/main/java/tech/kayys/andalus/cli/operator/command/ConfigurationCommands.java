package tech.kayys.andalus.cli.operator.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tech.kayys.andalus.cli.operator.OperatorCommand;
import tech.kayys.andalus.operator.v1.ConfigurationSummary;
import tech.kayys.andalus.operator.v1.ListConfigurationsResponse;
import tech.kayys.andalus.operator.v1.OperationResult;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;

@Command(
        name = "configurations",
        description = "Manage and inspect runtime configurations",
        mixinStandardHelpOptions = true,
        subcommands = {
                ConfigurationCommands.ListCommand.class,
                ConfigurationCommands.InspectCommand.class,
                ConfigurationCommands.ActiveCommand.class,
                ConfigurationCommands.UpdateCommand.class,
                ConfigurationCommands.ActivateCommand.class
        }
)
public class ConfigurationCommands {

    @Command(name = "list", description = "List configurations")
    public static class ListCommand implements Callable<Integer> {
        @ParentCommand
        private ConfigurationCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Option(names = "--include-values", description = "Include configuration value maps")
        private boolean includeValues;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListConfigurationsResponse resp = root.client().listConfigurations(includeValues);
            List<Map<String, Object>> list = new ArrayList<>();
            for (ConfigurationSummary c : resp.getConfigurationsList()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", c.getId());
                map.put("path", c.getPath());
                map.put("type", c.getType());
                map.put("source", c.getSource());
                map.put("status", c.getStatus());
                map.put("active", c.getActive());
                if (includeValues) {
                    map.put("values", c.getValuesMap());
                }
                list.add(map);
            }
            root.output().print(list);
            return 0;
        }
    }

    @Command(name = "inspect", description = "Inspect configuration details")
    public static class InspectCommand implements Callable<Integer> {
        @ParentCommand
        private ConfigurationCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Configuration identifier")
        private String configId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ConfigurationSummary c = root.client().getConfiguration(configId);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("path", c.getPath());
            map.put("type", c.getType());
            map.put("source", c.getSource());
            map.put("status", c.getStatus());
            map.put("active", c.getActive());
            map.put("mutable", c.getMutable());
            map.put("values", c.getValuesMap());
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "active", description = "Get the currently active configuration")
    public static class ActiveCommand implements Callable<Integer> {
        @ParentCommand
        private ConfigurationCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ConfigurationSummary c = root.client().getActiveConfiguration();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("path", c.getPath());
            map.put("type", c.getType());
            map.put("source", c.getSource());
            map.put("status", c.getStatus());
            map.put("active", c.getActive());
            map.put("mutable", c.getMutable());
            map.put("values", c.getValuesMap());
            root.output().print(map);
            return 0;
        }
    }

    @Command(name = "update", description = "Update configuration values")
    public static class UpdateCommand implements Callable<Integer> {
        @ParentCommand
        private ConfigurationCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Configuration identifier")
        private String configId;

        @Option(names = {"--file", "-f"}, description = "Path to JSON file containing updated key-value pairs")
        private File file;

        @Option(names = "--activate", description = "Activate configuration immediately after update")
        private boolean activate;

        @Override
        public Integer call() throws Exception {
            OperatorCommand root = findRoot(spec);
            Map<String, String> values = new HashMap<>();
            if (file != null && file.exists()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                ObjectMapper om = new ObjectMapper();
                values = om.readValue(bytes, new TypeReference<Map<String, String>>() {});
            }
            ConfigurationSummary updated = root.client().updateConfiguration(configId, values, activate);
            root.output().success("Configuration " + configId + " updated (active: " + updated.getActive() + ")");
            return 0;
        }
    }

    @Command(name = "activate", description = "Activate a configuration version")
    public static class ActivateCommand implements Callable<Integer> {
        @ParentCommand
        private ConfigurationCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Configuration identifier")
        private String configId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            OperationResult result = root.client().activateConfiguration(configId);
            root.output().success("Configuration " + configId + " activated (operation: " + result.getOperationId() + ")");
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
