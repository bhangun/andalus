package tech.kayys.andalus.knowledge.exchange.contradiction;

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

public final class DefaultKnowledgeAnswerProvenanceService
        implements KnowledgeAnswerProvenanceService {

    @Override
    public KnowledgeAnswerProvenanceGraph build(
            KnowledgeVerifiedResponse response,
            KnowledgeEvidenceSemanticQuery query,
            KnowledgeEvidenceAnswerVerification verification) {

        List<KnowledgeAnswerProvenanceNode> nodes =
                new ArrayList<>();

        List<KnowledgeAnswerProvenanceEdge> edges =
                new ArrayList<>();

        String queryNode =
                "query:" + query.queryId();

        String responseNode =
                "response:"
                        + response.metadata().responseId();

        nodes.add(
                new KnowledgeAnswerProvenanceNode(
                        queryNode,
                        KnowledgeAnswerProvenanceNodeType.QUERY,
                        query.queryId(),
                        null,
                        null,
                        query.tenantId(),
                        query.workspaceId(),
                        query.projectId(),
                        java.util.Map.of()
                )
        );

        nodes.add(
                new KnowledgeAnswerProvenanceNode(
                        responseNode,
                        KnowledgeAnswerProvenanceNodeType.RESPONSE,
                        response.metadata().responseId(),
                        null,
                        null,
                        response.metadata().tenantId(),
                        response.metadata().workspaceId(),
                        response.metadata().projectId(),
                        java.util.Map.of()
                )
        );

        edges.add(
                new KnowledgeAnswerProvenanceEdge(
                        UUID.randomUUID().toString(),
                        queryNode,
                        responseNode,
                        KnowledgeAnswerProvenanceRelationType.PRODUCED_BY,
                        "Response produced for query",
                        Instant.now(),
                        java.util.Map.of()
                )
        );

        for (KnowledgeEvidenceClaimVerification verificationItem
                : verification.claims()) {

            String claimNode =
                    "claim:" + verificationItem.claimId();

            nodes.add(
                    new KnowledgeAnswerProvenanceNode(
                            claimNode,
                            KnowledgeAnswerProvenanceNodeType.CLAIM,
                            verificationItem.claimId(),
                            null,
                            null,
                            response.metadata().tenantId(),
                            response.metadata().workspaceId(),
                            response.metadata().projectId(),
                            java.util.Map.of(
                                    "status",
                                    verificationItem.status()
                                            .name()
                            )
                    )
            );

            edges.add(
                    new KnowledgeAnswerProvenanceEdge(
                            UUID.randomUUID().toString(),
                            claimNode,
                            responseNode,
                            KnowledgeAnswerProvenanceRelationType
                                    .VERIFIED_BY,
                            verificationItem.reason(),
                            Instant.now(),
                            java.util.Map.of()
                    )
            );

            for (String evidenceId
                    : verificationItem.supportingEvidenceIds()) {

                String evidenceNode =
                        "evidence:" + evidenceId;

                nodes.add(
                        new KnowledgeAnswerProvenanceNode(
                                evidenceNode,
                                KnowledgeAnswerProvenanceNodeType
                                        .EVIDENCE,
                                evidenceId,
                                null,
                                null,
                                response.metadata().tenantId(),
                                response.metadata().workspaceId(),
                                response.metadata().projectId(),
                                java.util.Map.of()
                        )
                );

                edges.add(
                        new KnowledgeAnswerProvenanceEdge(
                                UUID.randomUUID().toString(),
                                evidenceNode,
                                claimNode,
                                KnowledgeAnswerProvenanceRelationType
                                        .SUPPORTS,
                                "Evidence supports verified claim",
                                Instant.now(),
                                java.util.Map.of()
                        )
                );
            }
        }

        return new KnowledgeAnswerProvenanceGraph(
                "provenance:"
                        + UUID.randomUUID(),
                response.metadata().responseId(),
                nodes,
                edges,
                java.util.Map.of()
        );
    }
}
