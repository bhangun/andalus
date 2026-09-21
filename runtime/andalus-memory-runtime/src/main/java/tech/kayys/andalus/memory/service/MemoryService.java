package tech.kayys.andalus.memory.service;

import tech.kayys.andalus.memory.model.ConversationMemory;
import tech.kayys.andalus.memory.model.MemoryContext;
import tech.kayys.andalus.memory.model.AgentResponse;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface MemoryService {
    Uni<MemoryContext> getContext(String sessionId, String userId);
    Uni<Void> storeContext(MemoryContext context);
    Uni<Void> storeExecutionResult(String sessionId, AgentResponse result);
    Uni<List<AgentResponse>> getRecentResults(String sessionId, int limit);
    Uni<MemoryContext> summarizeAndCompact(String sessionId);
    Uni<List<ConversationMemory>> findSimilarMemories(String sessionId, String query, int limit);
}