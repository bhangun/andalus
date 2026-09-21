package tech.kayys.andalus.knowledge.exchange.validity;

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
import java.util.Map;

public final class
DefaultKnowledgeAnswerResolutionConsensusLifecycleService
        implements
        KnowledgeAnswerResolutionConsensusLifecycleService {

    private final
    KnowledgeAnswerResolutionConsensusLifecycleStore store;

    private final
    KnowledgeAnswerResolutionConsensusLifecyclePolicy policy;

    public
    DefaultKnowledgeAnswerResolutionConsensusLifecycleService(
            KnowledgeAnswerResolutionConsensusLifecycleStore store,
            KnowledgeAnswerResolutionConsensusLifecyclePolicy policy) {

        this.store = store;
        this.policy = policy;
    }

    @Override
    public KnowledgeAnswerResolutionConsensusLifecycle activate(
            String consensusId,
            String keyFingerprint,
            Instant effectiveFrom,
            Instant effectiveUntil,
            String actorRuntimeId) {

        Instant now = Instant.now();

        if (effectiveFrom == null) {
            effectiveFrom = now;
        }

        if (effectiveUntil == null) {

            effectiveUntil =
                    now.plus(
                            policy.maximumConsensusLifetime()
                    );
        }

        if (!effectiveUntil.isAfter(effectiveFrom)) {

            throw new IllegalArgumentException(
                    "Consensus effectiveUntil must be after effectiveFrom"
            );
        }

        var lifecycle =
                new KnowledgeAnswerResolutionConsensusLifecycle(
                        consensusId,
                        keyFingerprint,
                        KnowledgeAnswerResolutionConsensusLifecycleState
                                .ACTIVE,
                        null,
                        null,
                        effectiveFrom,
                        effectiveUntil,
                        null,
                        null,
                        actorRuntimeId,
                        Map.of()
                );

        store.save(lifecycle);

        return lifecycle;
    }

    @Override
    public KnowledgeAnswerResolutionConsensusLifecycle supersede(
            String consensusId,
            String replacementConsensusId,
            String actorRuntimeId) {

        if (!policy.allowSupersession()) {

            throw new IllegalStateException(
                    "Consensus supersession is disabled"
            );
        }

        var existing =
                store.find(consensusId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Consensus not found: "
                                                        + consensusId
                                        )
                        );

        var replacement =
                new KnowledgeAnswerResolutionConsensusLifecycle(
                        existing.consensusId(),
                        existing.keyFingerprint(),
                        KnowledgeAnswerResolutionConsensusLifecycleState
                                .SUPERSEDED,
                        null,
                        replacementConsensusId,
                        existing.effectiveFrom(),
                        Instant.now(),
                        null,
                        null,
                        actorRuntimeId,
                        existing.metadata()
                );

        store.save(replacement);

        return replacement;
    }

    @Override
    public KnowledgeAnswerResolutionConsensusLifecycle revoke(
            String consensusId,
            KnowledgeAnswerResolutionConsensusRevocationReason reason,
            String actorRuntimeId,
            String evidenceFingerprint) {

        if (!policy.allowRevocation()) {

            throw new IllegalStateException(
                    "Consensus revocation is disabled"
            );
        }

        var existing =
                store.find(consensusId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Consensus not found: "
                                                        + consensusId
                                        )
                        );

        var revoked =
                new KnowledgeAnswerResolutionConsensusLifecycle(
                        existing.consensusId(),
                        existing.keyFingerprint(),
                        KnowledgeAnswerResolutionConsensusLifecycleState
                                .REVOKED,
                        reason,
                        null,
                        existing.effectiveFrom(),
                        existing.effectiveUntil(),
                        Instant.now(),
                        null,
                        actorRuntimeId,
                        Map.of(
                                "evidenceFingerprint",
                                evidenceFingerprint == null
                                        ? ""
                                        : evidenceFingerprint
                        )
                );

        store.save(revoked);

        return revoked;
    }

    @Override
    public KnowledgeAnswerResolutionConsensusLifecycle expire(
            String consensusId,
            String actorRuntimeId) {

        var existing =
                store.find(consensusId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Consensus not found: "
                                                        + consensusId
                                        )
                        );

        var expired =
                new KnowledgeAnswerResolutionConsensusLifecycle(
                        existing.consensusId(),
                        existing.keyFingerprint(),
                        KnowledgeAnswerResolutionConsensusLifecycleState
                                .EXPIRED,
                        null,
                        null,
                        existing.effectiveFrom(),
                        existing.effectiveUntil(),
                        null,
                        null,
                        actorRuntimeId,
                        existing.metadata()
                );

        store.save(expired);

        return expired;
    }

    @Override
    public java.util.Optional<
            KnowledgeAnswerResolutionConsensusLifecycle>
    current(
            String consensusId,
            Instant now) {

        return store.find(consensusId)
                .filter(
                        lifecycle ->
                                lifecycle.activeAt(now)
                );
    }
}
