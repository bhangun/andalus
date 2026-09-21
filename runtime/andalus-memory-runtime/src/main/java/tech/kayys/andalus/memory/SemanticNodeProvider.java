package tech.kayys.andalus.memory;

import tech.kayys.andalus.memory.node.MemoryNodeTypes;
import tech.kayys.andalus.memory.node.MemorySchemas;
import tech.kayys.andalus.plugin.spi.node.NodeDefinition;
import tech.kayys.andalus.plugin.spi.node.NodeProvider;

import java.util.List;
import java.util.Map;

/**
 * Exposes the Semantic Memory Executor capability as a Andalus node.
 */
public class SemanticNodeProvider implements NodeProvider {

    @Override
    public String id() {
        return "tech.kayys.andalus.memory.semantic";
    }

    @Override
    public String name() {
        return "Semantic Memory Plugin";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Stores and retrieves generalized facts and knowledge.";
    }

    @Override
    public List<NodeDefinition> nodes() {
        return List.of(
                // Semantic Memory Node
                new NodeDefinition(
                        MemoryNodeTypes.SEMANTIC,
                        "Semantic Memory",
                        "Memory",
                        "Semantic",
                        "Stores and retrieves generalized facts and knowledge.",
                        "database", // Icon
                        "#8B5CF6",
                        MemorySchemas.MEMORY_CONFIG,
                        "{}",
                        "{}",
                        Map.of(
                                "operation", "RETRIEVE",
                                "minConfidence", 0.7,
                                "limit", 10,
                                "minSimilarity", 0.0)));
    }
}
