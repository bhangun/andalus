package tech.kayys.andalus.knowledge.exchange.selection;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class
KnowledgeSnapshotResolutionDependencyExtractor {

    public List<
            KnowledgeAnswerResolutionDependency>
    extract(
            String resolutionKeyFingerprint,
            KnowledgeDecisionSnapshot snapshot) {

        List<
                KnowledgeAnswerResolutionDependency>
                dependencies =
                new ArrayList<>();

        for (var entry :
                snapshot.knowledge()) {

            dependencies.add(
                    new KnowledgeAnswerResolutionDependency(
                            UUID.randomUUID().toString(),
                            resolutionKeyFingerprint,
                            KnowledgeAnswerResolutionDependencyType
                                    .KNOWLEDGE_VERSION,
                            entry.knowledgeId(),
                            entry.versionId(),
                            entry.fingerprint(),
                            true,
                            Instant.now(),
                            java.util.Map.of()
                    )
            );
        }

        if (snapshot.policies() != null) {

            dependencies.add(
                    new KnowledgeAnswerResolutionDependency(
                            UUID.randomUUID().toString(),
                            resolutionKeyFingerprint,
                            KnowledgeAnswerResolutionDependencyType
                                    .POLICY_SNAPSHOT,
                            snapshot.snapshotId().value(),
                            null,
                            snapshot.policies()
                                    .aggregateFingerprint(),
                            true,
                            Instant.now(),
                            java.util.Map.of()
                    )
            );
        }

        if (snapshot.governance() != null) {

            dependencies.add(
                    new KnowledgeAnswerResolutionDependency(
                            UUID.randomUUID().toString(),
                            resolutionKeyFingerprint,
                            KnowledgeAnswerResolutionDependencyType
                                    .GOVERNANCE_SNAPSHOT,
                            snapshot.snapshotId().value(),
                            null,
                            snapshot.governance()
                                    .scopeFingerprint(),
                            true,
                            Instant.now(),
                            java.util.Map.of()
                    )
            );
        }

        return List.copyOf(dependencies);
    }
}
