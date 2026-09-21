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


import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class KnowledgeVerifiedAnswerDiscoveryService {

    private final DefaultKnowledgeAnswerArtifactFederatedDiscoveryEngine
            discovery;

    private final DefaultKnowledgeVerifiedAnswerExchangeService
            exchange;

    public KnowledgeVerifiedAnswerDiscoveryService(
            DefaultKnowledgeAnswerArtifactFederatedDiscoveryEngine discovery,
            DefaultKnowledgeVerifiedAnswerExchangeService exchange) {

        this.discovery = discovery;
        this.exchange = exchange;
    }

    public CompletableFuture<
            List<KnowledgeAnswerArtifactCandidate>> discoverVerified(
            KnowledgeAnswerArtifactQuery query,
            List<KnowledgeAnswerArtifactIndexDescriptor> descriptors) {

        return discovery
                .discover(query, descriptors)
                .thenCompose(result -> {

                    List<CompletableFuture<
                            KnowledgeAnswerArtifactCandidate>>
                            verified =
                            result.selected()
                                    .stream()
                                    .map(this::verify)
                                    .toList();

                    return CompletableFuture
                            .allOf(
                                    verified.toArray(
                                            new CompletableFuture[0]
                                    )
                            )
                            .thenApply(ignored ->
                                    verified.stream()
                                            .map(CompletableFuture::join)
                                            .filter(
                                                    java.util.Objects
                                                            ::nonNull
                                            )
                                            .toList()
                            );
                });
    }

    private CompletableFuture<
            KnowledgeAnswerArtifactCandidate> verify(
            KnowledgeAnswerArtifactCandidate candidate) {

        var request =
                new KnowledgeVerifiedAnswerExchangeRequest(
                        java.util.UUID.randomUUID()
                                .toString(),
                        KnowledgeVerifiedAnswerExchangeOperation
                                .GET_ARTIFACT,
                        candidate.artifactId(),
                        candidate.responseId(),
                        null,
                        candidate.tenantId(),
                        candidate.workspaceId(),
                        candidate.projectId(),
                        candidate.runtimeId(),
                        true,
                        true,
                        true,
                        candidate.sealed(),
                        true,
                        java.time.Instant.now(),
                        java.time.Instant.now()
                                .plusSeconds(60),
                        java.util.Map.of()
                );

        return exchange.fetch(request)
                .thenApply(result ->
                        result.accepted()
                                ? candidate
                                : null
                );
    }
}
