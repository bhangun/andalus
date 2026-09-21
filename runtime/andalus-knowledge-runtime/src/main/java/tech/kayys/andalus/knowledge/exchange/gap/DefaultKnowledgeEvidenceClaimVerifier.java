package tech.kayys.andalus.knowledge.exchange.gap;

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
import java.util.Comparator;
import java.util.List;

public final class DefaultKnowledgeEvidenceClaimVerifier
        implements KnowledgeEvidenceClaimVerifier {

    @Override
    public KnowledgeEvidenceClaimVerification verify(
            KnowledgeEvidenceClaim claim,
            KnowledgeEvidenceClaimGraph graph) {

        List<KnowledgeEvidenceClaimSupport> supports =
                graph.supports()
                        .stream()
                        .filter(s ->
                                s.claimId()
                                        .equals(claim.claimId()))
                        .sorted(
                                Comparator.comparingDouble(
                                        KnowledgeEvidenceClaimSupport
                                                ::supportScore
                                ).reversed()
                        )
                        .toList();

        List<KnowledgeEvidenceClaimContradiction>
                contradictions =
                    graph.contradictions()
                            .stream()
                            .filter(c ->
                                    c.claimId()
                                            .equals(claim.claimId()))
                            .toList();

        if (!contradictions.isEmpty()) {

            return new KnowledgeEvidenceClaimVerification(
                    claim.claimId(),
                    KnowledgeEvidenceClaimStatus.CONTRADICTED,
                    contradictions.stream()
                            .mapToDouble(
                                    KnowledgeEvidenceClaimContradiction
                                            ::confidence
                            )
                            .max()
                            .orElse(0.0),
                    supports.stream()
                            .map(
                                    KnowledgeEvidenceClaimSupport
                                            ::evidenceId
                            )
                            .toList(),
                    contradictions.stream()
                            .map(
                                    KnowledgeEvidenceClaimContradiction
                                            ::evidenceId
                            )
                            .toList(),
                    "Claim has contradictory evidence",
                    java.util.Map.of()
            );
        }

        if (supports.isEmpty()) {

            return new KnowledgeEvidenceClaimVerification(
                    claim.claimId(),
                    KnowledgeEvidenceClaimStatus.UNSUPPORTED,
                    0.0,
                    List.of(),
                    List.of(),
                    "No supporting evidence",
                    java.util.Map.of()
            );
        }

        double best =
                supports.get(0).supportScore();

        List<String> supporting =
                supports.stream()
                        .filter(s ->
                                s.supportScore() >= 0.40)
                        .map(
                                KnowledgeEvidenceClaimSupport
                                        ::evidenceId
                        )
                        .toList();

        KnowledgeEvidenceClaimStatus status;

        if (best >= 0.80) {
            status = KnowledgeEvidenceClaimStatus.SUPPORTED;
        } else if (best >= 0.40) {
            status =
                    KnowledgeEvidenceClaimStatus.PARTIALLY_SUPPORTED;
        } else {
            status =
                    KnowledgeEvidenceClaimStatus.UNSUPPORTED;
        }

        return new KnowledgeEvidenceClaimVerification(
                claim.claimId(),
                status,
                best,
                supporting,
                List.of(),
                status
                        == KnowledgeEvidenceClaimStatus.SUPPORTED
                        ? "Claim is directly supported"
                        : "Claim has insufficient direct support",
                java.util.Map.of()
        );
    }
}
