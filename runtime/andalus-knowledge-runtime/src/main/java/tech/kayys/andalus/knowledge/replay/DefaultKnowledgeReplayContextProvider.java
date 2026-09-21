package tech.kayys.andalus.knowledge.replay;

import tech.kayys.andalus.knowledge.decision.KnowledgeDecisionTrace;

public final class DefaultKnowledgeReplayContextProvider implements KnowledgeReplayContextProvider {

    @Override
    public KnowledgeReplayContext context(
            KnowledgeDecisionTrace trace,
            KnowledgeReplayRequest request) {

        return new KnowledgeReplayContext(
                null,
                null,
                null,
                request.effectiveAt() != null ? request.effectiveAt() : (trace != null ? trace.createdAt() : java.time.Instant.now()),
                java.util.Map.of()
        );
    }
}
