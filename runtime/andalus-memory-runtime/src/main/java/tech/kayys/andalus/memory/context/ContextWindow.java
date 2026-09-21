package tech.kayys.andalus.memory.context;

import tech.kayys.andalus.memory.model.ConversationMemory;

import java.util.List;

public class ContextWindow {
    private final List<ConversationMemory> memories;
    private final int totalTokens;
    private final ContextWindowStrategy strategy;
    private final double qualityScore;

    public ContextWindow(List<ConversationMemory> memories, int totalTokens,
                        ContextWindowStrategy strategy, double qualityScore) {
        this.memories = memories;
        this.totalTokens = totalTokens;
        this.strategy = strategy;
        this.qualityScore = qualityScore;
    }

    public List<ConversationMemory> getMemories() { return memories; }
    public int getTotalTokens() { return totalTokens; }
    public ContextWindowStrategy getStrategy() { return strategy; }
    public double getQualityScore() { return qualityScore; }
}