package tech.kayys.andalus.cli.operator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import tech.kayys.andalus.cli.operator.OperatorCommand;
import tech.kayys.andalus.operator.v1.CapabilitySummary;
import tech.kayys.andalus.operator.v1.ListCapabilitiesResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "capabilities",
        description = "Inspect and query operator capabilities",
        mixinStandardHelpOptions = true,
        subcommands = {
                CapabilityCommands.ListCommand.class,
                CapabilityCommands.TypeCommand.class,
                CapabilityCommands.TagCommand.class,
                CapabilityCommands.InspectCommand.class
        }
)
public class CapabilityCommands {

    @Command(name = "list", description = "List registered capabilities")
    public static class ListCommand implements Callable<Integer> {
        @ParentCommand
        private CapabilityCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListCapabilitiesResponse resp = root.client().listCapabilities();
            printList(root, resp);
            return 0;
        }
    }

    @Command(name = "type", description = "List capabilities filtered by type")
    public static class TypeCommand implements Callable<Integer> {
        @ParentCommand
        private CapabilityCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Capability type (e.g. LLM, TOOL, EMBEDDING)")
        private String type;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListCapabilitiesResponse resp = root.client().listCapabilitiesByType(type);
            printList(root, resp);
            return 0;
        }
    }

    @Command(name = "tag", description = "List capabilities filtered by tag")
    public static class TagCommand implements Callable<Integer> {
        @ParentCommand
        private CapabilityCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Tag string")
        private String tag;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListCapabilitiesResponse resp = root.client().listCapabilitiesByTag(tag);
            printList(root, resp);
            return 0;
        }
    }

    @Command(name = "inspect", description = "Inspect a specific capability")
    public static class InspectCommand implements Callable<Integer> {
        @ParentCommand
        private CapabilityCommands parent;
        @picocli.CommandLine.Spec
        private picocli.CommandLine.Model.CommandSpec spec;

        @Parameters(index = "0", description = "Capability identifier")
        private String capabilityId;

        @Override
        public Integer call() {
            OperatorCommand root = findRoot(spec);
            ListCapabilitiesResponse resp = root.client().listCapabilities();
            for (CapabilitySummary c : resp.getCapabilitiesList()) {
                if (c.getId().equals(capabilityId)) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", c.getId());
                    map.put("type", c.getType());
                    map.put("providerId", c.getProviderId());
                    map.put("tags", c.getTagsList());
                    root.output().print(map);
                    return 0;
                }
            }
            root.output().error("Capability " + capabilityId + " not found");
            return 5;
        }
    }

    private static void printList(OperatorCommand root, ListCapabilitiesResponse resp) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CapabilitySummary c : resp.getCapabilitiesList()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("type", c.getType());
            map.put("providerId", c.getProviderId());
            map.put("tags", c.getTagsList());
            list.add(map);
        }
        root.output().print(list);
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
