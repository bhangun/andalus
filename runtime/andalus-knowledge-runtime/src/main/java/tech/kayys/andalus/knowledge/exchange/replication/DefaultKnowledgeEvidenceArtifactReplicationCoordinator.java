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


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DefaultKnowledgeEvidenceArtifactReplicationCoordinator
        implements KnowledgeEvidenceArtifactReplicationCoordinator {

    private final List<
            KnowledgeEvidenceArtifactRemoteReplica
            > replicas;

    public DefaultKnowledgeEvidenceArtifactReplicationCoordinator(
            List<
                    KnowledgeEvidenceArtifactRemoteReplica
                    > replicas
    ) {

        this.replicas =
                replicas == null
                        ? List.of()
                        : List.copyOf(replicas);
    }

    @Override
    public CompletableFuture<
            KnowledgeEvidenceArtifactReplicationResult
            > replicate(
                    KnowledgeEvidenceArtifactReplicationRequest request
            ) {

        return CompletableFuture.supplyAsync(() -> {

            if (request.expiredAt(
                    Instant.now()
            )) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Replication request expired"
                );
            }

            var successful =
                    new ArrayList<String>();

            var failed =
                    new ArrayList<String>();

            int verified = 0;

            for (
                    var runtimeId :
                            request.targetRuntimeIds()
            ) {

                var replica =
                        replicas.stream()
                                .filter(
                                        r ->
                                                r.runtimeId()
                                                        .equals(runtimeId)
                                )
                                .findFirst()
                                .orElse(null);

                if (replica == null) {

                    failed.add(runtimeId);

                    continue;
                }

                try {

                    var result =
                            replica.replicate(
                                    request
                            ).join();

                    if (result.quorumSatisfied()) {

                        successful.add(
                                runtimeId
                        );

                        verified +=
                                result.verifiedReplicas();

                    } else {

                        failed.add(
                                runtimeId
                        );
                    }

                } catch (Exception e) {

                    failed.add(
                            runtimeId
                    );
                }
            }

            int desired =
                    request.desiredReplicas();

            boolean quorum =
                    successful.size() >= desired;

            return new KnowledgeEvidenceArtifactReplicationResult(
                    request.replicationId(),
                    request.artifactId(),
                    request.targetRuntimeIds().size(),
                    successful.size(),
                    verified,
                    successful,
                    failed,
                    quorum,
                    java.util.Map.of(
                            "sourceRuntime",
                            String.valueOf(
                                    request.sourceRuntimeId()
                            )
                    )
            );
        });
    }
}
