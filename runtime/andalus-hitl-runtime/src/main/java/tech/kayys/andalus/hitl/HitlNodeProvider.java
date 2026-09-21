package tech.kayys.andalus.hitl;

import tech.kayys.andalus.plugin.spi.node.NodeDefinition;
import tech.kayys.andalus.plugin.spi.node.NodeProvider;
import tech.kayys.andalus.hitl.schema.*;
import tech.kayys.andalus.schema.generator.SchemaGeneratorUtils;

import java.util.List;
import java.util.Map;

/**
 * Contributes HITL-related node definitions to the unified catalog.
 */
public class HitlNodeProvider implements NodeProvider {

    @Override
    public String id() {
        return "tech.kayys.andalus.hitl";
    }

    @Override
    public String name() {
        return "Human-in-the-Loop Plugin";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Enables human review and approval steps in workflows.";
    }

    @Override
    public List<NodeDefinition> nodes() {
        return List.of(
                new NodeDefinition(
                        "hitl-human-task", "Human Task", "Human", "Approval",
                        "Human-in-the-loop task requiring manual intervention",
                        "user-check", "#3B82F6",
                        SchemaGeneratorUtils.generateSchema(HumanTaskConfig.class),
                        null, null, Map.of()));
    }
}
