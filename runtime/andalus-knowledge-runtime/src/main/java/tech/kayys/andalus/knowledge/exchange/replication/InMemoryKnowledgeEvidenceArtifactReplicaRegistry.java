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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceArtifactReplicaRegistry
        implements KnowledgeEvidenceArtifactReplicaRegistry {

    private final ConcurrentMap<
            String,
            KnowledgeEvidenceArtifactReplica
            > replicas =
            new ConcurrentHashMap<>();

    private String key(
            String artifactId,
            String runtimeId
    ) {
        return artifactId + ":" + runtimeId;
    }

    @Override
    public void register(
            KnowledgeEvidenceArtifactReplica replica
    ) {

        replicas.put(
                key(
                        replica.artifactId(),
                        replica.runtimeId()
                ),
                replica
        );
    }

    @Override
    public KnowledgeEvidenceArtifactReplica find(
            String artifactId,
            String runtimeId
    ) {

        return replicas.get(
                key(
                        artifactId,
                        runtimeId
                )
        );
    }

    @Override
    public List<KnowledgeEvidenceArtifactReplica>
    findByArtifact(
            String artifactId
    ) {

        var result =
                new ArrayList<
                        KnowledgeEvidenceArtifactReplica
                        >();

        replicas.values().forEach(
                replica -> {

                    if (artifactId.equals(
                            replica.artifactId()
                    )) {
                        result.add(replica);
                    }
                }
        );

        return List.copyOf(result);
    }

    @Override
    public List<KnowledgeEvidenceArtifactReplica>
    findByRuntime(
            String runtimeId
    ) {

        var result =
                new ArrayList<
                        KnowledgeEvidenceArtifactReplica
                        >();

        replicas.values().forEach(
                replica -> {

                    if (runtimeId.equals(
                            replica.runtimeId()
                    )) {
                        result.add(replica);
                    }
                }
        );

        return List.copyOf(result);
    }

    @Override
    public void remove(
            String artifactId,
            String runtimeId
    ) {

        replicas.remove(
                key(
                        artifactId,
                        runtimeId
                )
        );
    }
}
