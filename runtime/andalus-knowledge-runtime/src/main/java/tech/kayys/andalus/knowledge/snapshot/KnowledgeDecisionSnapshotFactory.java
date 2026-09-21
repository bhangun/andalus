package tech.kayys.andalus.knowledge.snapshot;

import tech.kayys.andalus.knowledge.decision.KnowledgeDecisionTrace;
import tech.kayys.andalus.knowledge.replay.KnowledgeFingerprint;

import java.time.Instant;
import java.util.List;

public final class KnowledgeDecisionSnapshotFactory {

    private final KnowledgeSnapshotCanonicalizer canonicalizer;

    public KnowledgeDecisionSnapshotFactory(KnowledgeSnapshotCanonicalizer canonicalizer) {
        this.canonicalizer = canonicalizer != null ? canonicalizer : new DefaultKnowledgeSnapshotCanonicalizer();
    }

    public KnowledgeDecisionSnapshot create(
            KnowledgeDecisionTrace trace,
            List<KnowledgeSnapshotEntry> knowledge,
            KnowledgePolicySnapshot policies,
            KnowledgeRuleSnapshot rules,
            KnowledgeGovernanceSnapshot governance,
            KnowledgeRuntimeSnapshot runtime) {

        KnowledgeDecisionSnapshot preliminary = new KnowledgeDecisionSnapshot(
                null,
                trace.executionId(),
                trace.id(),
                trace.agentId(),
                trace.operation(),
                trace.query(),
                trace.createdAt(),
                knowledge,
                policies,
                rules,
                governance,
                runtime,
                "",
                Instant.now(),
                java.util.Map.of()
        );

        String canonical = canonicalizer.canonicalize(preliminary);
        String fingerprint = KnowledgeFingerprint.sha256(canonical);
        KnowledgeSnapshotId snapshotId = new KnowledgeSnapshotId(fingerprint, "SHA-256", Instant.now());

        return new KnowledgeDecisionSnapshot(
                snapshotId,
                trace.executionId(),
                trace.id(),
                trace.agentId(),
                trace.operation(),
                trace.query(),
                trace.createdAt(),
                knowledge,
                policies,
                rules,
                governance,
                runtime,
                fingerprint,
                preliminary.createdAt(),
                java.util.Map.of()
        );
    }
}
