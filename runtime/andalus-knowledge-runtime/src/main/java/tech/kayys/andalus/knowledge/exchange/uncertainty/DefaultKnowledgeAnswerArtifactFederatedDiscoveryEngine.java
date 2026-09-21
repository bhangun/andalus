package tech.kayys.andalus.knowledge.exchange.uncertainty;

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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DefaultKnowledgeAnswerArtifactFederatedDiscoveryEngine {

    private final KnowledgeAnswerArtifactDiscoveryEngine local;

    private final KnowledgeAnswerArtifactDiscoveryPlanner planner;

    private final List<KnowledgeAnswerArtifactRemoteIndex> remotes;

    public DefaultKnowledgeAnswerArtifactFederatedDiscoveryEngine(
            KnowledgeAnswerArtifactDiscoveryEngine local,
            KnowledgeAnswerArtifactDiscoveryPlanner planner,
            List<KnowledgeAnswerArtifactRemoteIndex> remotes) {

        this.local = local;
        this.planner = planner;
        this.remotes = List.copyOf(remotes);
    }

    public CompletableFuture<
            KnowledgeAnswerArtifactDiscoveryResult> discover(
            KnowledgeAnswerArtifactQuery query,
            List<KnowledgeAnswerArtifactIndexDescriptor> descriptors) {

        var localResult =
                local.discover(query);

        if (!query.allowRemote()) {
            return CompletableFuture.completedFuture(
                    localResult
            );
        }

        var selected =
                planner.select(
                        query,
                        descriptors
                );

        List<CompletableFuture<
                KnowledgeAnswerArtifactDiscoveryResult>> futures =
                new ArrayList<>();

        for (var descriptor : selected) {

            remotes.stream()
                    .filter(remote ->
                            remote.runtimeId()
                                    .equals(
                                            descriptor.runtimeId()
                                    ))
                    .findFirst()
                    .ifPresent(remote ->
                            futures.add(
                                    remote.search(query)
                            )
                    );
        }

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(
                    localResult
            );
        }

        return CompletableFuture
                .allOf(
                        futures.toArray(
                                new CompletableFuture[0]
                        )
                )
                .thenApply(ignored -> {

                    List<
                            KnowledgeAnswerArtifactCandidate>
                            all = new ArrayList<>();

                    all.addAll(
                            localResult.candidates()
                    );

                    for (var future : futures) {

                        var result =
                                future.join();

                        all.addAll(
                                result.candidates()
                        );
                    }

                    all.sort(
                            java.util.Comparator
                                    .comparingDouble(
                                            KnowledgeAnswerArtifactCandidate
                                                    ::finalScore
                                    )
                                    .reversed()
                    );

                    var selectedCandidates =
                            all.stream()
                                    .limit(query.limit())
                                    .toList();

                    return new KnowledgeAnswerArtifactDiscoveryResult(
                            query.queryId(),
                            all,
                            selectedCandidates,
                            true,
                            true,
                            java.util.Map.of(
                                    "runtimeCount",
                                    Integer.toString(
                                            selected.size()
                                    )
                            )
                    );
                });
    }
}
