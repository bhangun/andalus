package tech.kayys.andalus.knowledge.exchange.replication;

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


import java.util.Comparator;
import java.util.List;

public final class DefaultKnowledgeEvidenceArtifactReplicationPlanner
        implements KnowledgeEvidenceArtifactReplicationPlanner {

    private final List<
            KnowledgeEvidenceArtifactReplicaLocation
            > locations;

    public DefaultKnowledgeEvidenceArtifactReplicationPlanner(
            List<
                    KnowledgeEvidenceArtifactReplicaLocation
                    > locations
    ) {

        this.locations =
                locations == null
                        ? List.of()
                        : List.copyOf(locations);
    }

    @Override
    public List<
            KnowledgeEvidenceArtifactReplicaLocation
            > plan(
                    String artifactId,
                    String tenantId,
                    String workspaceId,
                    String projectId,
                    KnowledgeEvidenceArtifactReplicationPolicy
                            policy
            ) {

        return locations.stream()
                .filter(
                        KnowledgeEvidenceArtifactReplicaLocation::online
                )
                .sorted(
                        Comparator.comparing(
                                KnowledgeEvidenceArtifactReplicaLocation
                                        ::local
                        ).reversed()
                )
                .limit(
                        policy.targetReplicas()
                )
                .toList();
    }
}
