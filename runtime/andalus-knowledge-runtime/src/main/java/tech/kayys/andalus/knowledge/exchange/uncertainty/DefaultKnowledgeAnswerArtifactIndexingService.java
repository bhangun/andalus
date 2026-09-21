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


import java.util.Set;

public final class DefaultKnowledgeAnswerArtifactIndexingService
        implements KnowledgeAnswerArtifactIndexingService {

    private final KnowledgeAnswerArtifactIndex index;

    public DefaultKnowledgeAnswerArtifactIndexingService(
            KnowledgeAnswerArtifactIndex index) {

        this.index = index;
    }

    @Override
    public void index(
            KnowledgeVerifiedAnswerArtifact artifact) {

        var response =
                artifact.response();

        Set<String> claimTypes =
                response.claims()
                        .stream()
                        .map(claim ->
                                claim.type().name())
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        Set<String> concepts =
                response.claims()
                        .stream()
                        .flatMap(claim ->
                                java.util.Arrays.stream(
                                        claim.text()
                                                .toLowerCase()
                                                .split("\\W+")
                                ))
                        .filter(s -> !s.isBlank())
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        var entry =
                new KnowledgeAnswerArtifactIndexEntry(
                        artifact.artifactId(),
                        artifact.responseId(),
                        artifact.snapshotId(),
                        "local",
                        artifact.tenantId(),
                        artifact.workspaceId(),
                        artifact.projectId(),
                        artifact.agentId(),
                        response.status().name(),
                        response.disposition().name(),
                        response.confidence(),
                        response.status()
                                == KnowledgeVerifiedResponseStatus
                                .VERIFIED,
                        false,
                        artifact.createdAt(),
                        artifact.createdAt(),
                        Set.of(),
                        concepts,
                        claimTypes,
                        artifact.responseFingerprint(),
                        java.util.Map.of()
                );

        index.index(entry);
    }

    @Override
    public void remove(
            String artifactId) {

        index.remove(artifactId);
    }
}
