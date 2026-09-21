package tech.kayys.andalus.knowledge.audit;

import tech.kayys.andalus.knowledge.decision.KnowledgeDecisionTrace;

import java.util.Objects;

public final class DefaultKnowledgeAuditPolicy implements KnowledgeAuditPolicy {

    @Override
    public boolean shouldAudit(KnowledgeDecisionTrace trace) {
        return Objects.nonNull(trace);
    }
}
