package tech.kayys.andalus.knowledge.exchange.lease;

import tech.kayys.andalus.knowledge.*;
import tech.kayys.andalus.knowledge.seal.*;
import tech.kayys.andalus.knowledge.snapshot.*;
import tech.kayys.andalus.knowledge.snapshot.pack.*;
import tech.kayys.andalus.knowledge.snapshot.artifact.*;
import tech.kayys.andalus.knowledge.snapshot.merkle.*;
import tech.kayys.andalus.knowledge.exchange.*;
import tech.kayys.andalus.knowledge.exchange.auth.*;
import tech.kayys.andalus.knowledge.exchange.session.*;
import tech.kayys.andalus.knowledge.exchange.binding.*;
import tech.kayys.andalus.knowledge.exchange.envelope.*;
import tech.kayys.andalus.knowledge.exchange.trust.*;
import tech.kayys.andalus.knowledge.exchange.identity.*;
import tech.kayys.andalus.knowledge.exchange.capability.*;
import tech.kayys.andalus.knowledge.exchange.protocol.*;
import tech.kayys.andalus.knowledge.exchange.transport.*;
import tech.kayys.andalus.knowledge.exchange.framing.*;
import tech.kayys.andalus.knowledge.exchange.transfer.*;
import tech.kayys.andalus.knowledge.exchange.replication.*;
import tech.kayys.andalus.knowledge.exchange.sync.*;
import tech.kayys.andalus.knowledge.exchange.federation.*;
import tech.kayys.andalus.knowledge.exchange.routing.*;
import tech.kayys.andalus.knowledge.exchange.fusion.*;
import tech.kayys.andalus.knowledge.exchange.coverage.*;
import tech.kayys.andalus.knowledge.exchange.gap.*;
import tech.kayys.andalus.knowledge.exchange.attribution.*;
import tech.kayys.andalus.knowledge.exchange.contradiction.*;
import tech.kayys.andalus.knowledge.exchange.factuality.*;
import tech.kayys.andalus.knowledge.exchange.uncertainty.*;
import tech.kayys.andalus.knowledge.exchange.compact.*;
import tech.kayys.andalus.knowledge.exchange.resolution.*;
import tech.kayys.andalus.knowledge.exchange.quorum.*;
import tech.kayys.andalus.knowledge.exchange.selection.*;
import tech.kayys.andalus.knowledge.exchange.coordination.*;
import tech.kayys.andalus.knowledge.exchange.attestation.*;
import tech.kayys.andalus.knowledge.exchange.proof.*;
import tech.kayys.andalus.knowledge.exchange.validity.*;
import tech.kayys.andalus.knowledge.exchange.lease.*;
import tech.kayys.andalus.knowledge.exchange.recovery.*;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DefaultKnowledgeAnswerResolutionLeaseService
        implements KnowledgeAnswerResolutionLeaseService {

    private final KnowledgeAnswerResolutionLeaseStore leaseStore;
    private final KnowledgeAnswerResolutionFreshnessEvaluator evaluator;

    public DefaultKnowledgeAnswerResolutionLeaseService(
            KnowledgeAnswerResolutionLeaseStore leaseStore,
            KnowledgeAnswerResolutionFreshnessEvaluator evaluator) {

        this.leaseStore = leaseStore;
        this.evaluator = evaluator;
    }

    @Override
    public KnowledgeAnswerResolutionLease issue(
            String consensusId,
            String keyFingerprint,
            String runtimeId,
            KnowledgeAnswerResolutionFreshnessPolicy policy,
            Instant now) {

        KnowledgeAnswerResolutionLease lease =
                new KnowledgeAnswerResolutionLease(
                        UUID.randomUUID().toString(),
                        consensusId,
                        keyFingerprint,
                        runtimeId,
                        now,
                        now.plus(policy.leaseDuration()),
                        now,
                        0,
                        "issued"
                );

        leaseStore.put(lease);

        return lease;
    }

    @Override
    public KnowledgeAnswerResolutionLease renew(
            KnowledgeAnswerResolutionLease lease,
            KnowledgeAnswerResolutionFreshnessPolicy policy,
            Instant now) {

        if (now.isBefore(lease.issuedAt())) {
            throw new IllegalArgumentException(
                    "Cannot renew lease before issuance");
        }

        KnowledgeAnswerResolutionLease renewed =
                new KnowledgeAnswerResolutionLease(
                        lease.leaseId(),
                        lease.consensusId(),
                        lease.keyFingerprint(),
                        lease.runtimeId(),
                        lease.issuedAt(),
                        now.plus(policy.leaseDuration()),
                        now,
                        lease.renewalSequence() + 1,
                        "renewed"
                );

        leaseStore.put(renewed);

        return renewed;
    }

    @Override
    public KnowledgeAnswerResolutionFreshnessDecision evaluate(
            KnowledgeAnswerResolutionLease lease,
            List<String> participantRuntimeIds,
            KnowledgeAnswerResolutionFreshnessPolicy policy,
            Instant now) {

        return evaluator.evaluate(
                lease,
                participantRuntimeIds,
                policy,
                now
        );
    }
}
