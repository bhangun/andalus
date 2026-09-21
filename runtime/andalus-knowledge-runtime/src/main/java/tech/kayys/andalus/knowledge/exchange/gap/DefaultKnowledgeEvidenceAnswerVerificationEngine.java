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
import java.util.List;
import java.util.UUID;

public final class DefaultKnowledgeEvidenceAnswerVerificationEngine
        implements KnowledgeEvidenceAnswerVerificationEngine {

    private final KnowledgeEvidenceClaimExtractor extractor;
    private final KnowledgeEvidenceClaimMatcher matcher;
    private final KnowledgeEvidenceClaimVerifier verifier;

    public DefaultKnowledgeEvidenceAnswerVerificationEngine() {
        this(
                new DefaultKnowledgeEvidenceClaimExtractor(),
                new DefaultKnowledgeEvidenceClaimMatcher(),
                new DefaultKnowledgeEvidenceClaimVerifier()
        );
    }

    public DefaultKnowledgeEvidenceAnswerVerificationEngine(
            KnowledgeEvidenceClaimExtractor extractor,
            KnowledgeEvidenceClaimMatcher matcher,
            KnowledgeEvidenceClaimVerifier verifier) {

        this.extractor = extractor;
        this.matcher = matcher;
        this.verifier = verifier;
    }

    @Override
    public KnowledgeEvidenceAnswerVerification verify(
            String answer,
            KnowledgeEvidenceSemanticQuery query,
            KnowledgeEvidenceFusionResult evidence) {

        List<KnowledgeEvidenceClaim> claims =
                extractor.extract(answer);

        List<KnowledgeEvidenceClaimSupport> supports =
                new ArrayList<>();

        for (KnowledgeEvidenceClaim claim : claims) {

            for (KnowledgeEvidenceFusionCandidate candidate
                    : evidence.selected()) {

                KnowledgeEvidenceClaimSupport support =
                        matcher.match(
                                claim,
                                candidate
                        );

                if (support.supportScore() >= 0.20) {
                    supports.add(support);
                }
            }
        }

        KnowledgeEvidenceClaimGraph graph =
                new KnowledgeEvidenceClaimGraph(
                        "claim-graph-"
                                + UUID.randomUUID(),
                        query.queryId(),
                        claims,
                        supports,
                        List.of(),
                        java.util.Map.of()
                );

        List<KnowledgeEvidenceClaimVerification>
                verifications =
                    claims.stream()
                            .map(claim ->
                                    verifier.verify(
                                            claim,
                                            graph
                                    ))
                            .toList();

        boolean hasContradiction =
                verifications.stream()
                        .anyMatch(v ->
                                v.status()
                                        == KnowledgeEvidenceClaimStatus
                                        .CONTRADICTED);

        boolean hasUnsupported =
                verifications.stream()
                        .anyMatch(v ->
                                v.status()
                                        == KnowledgeEvidenceClaimStatus
                                        .UNSUPPORTED);

        boolean hasPartial =
                verifications.stream()
                        .anyMatch(v ->
                                v.status()
                                        == KnowledgeEvidenceClaimStatus
                                        .PARTIALLY_SUPPORTED);

        KnowledgeEvidenceClaimStatus overall;

        if (hasContradiction) {
            overall =
                    KnowledgeEvidenceClaimStatus.CONTRADICTED;
        } else if (hasUnsupported) {
            overall =
                    KnowledgeEvidenceClaimStatus.UNSUPPORTED;
        } else if (hasPartial) {
            overall =
                    KnowledgeEvidenceClaimStatus.PARTIALLY_SUPPORTED;
        } else {
            overall =
                    KnowledgeEvidenceClaimStatus.SUPPORTED;
        }

        double confidence =
                verifications.isEmpty()
                        ? 0.0
                        : verifications.stream()
                                .mapToDouble(
                                        KnowledgeEvidenceClaimVerification
                                                ::confidence
                                )
                                .average()
                                .orElse(0.0);

        List<String> blocked =
                verifications.stream()
                        .filter(v ->
                                v.status()
                                        != KnowledgeEvidenceClaimStatus
                                        .SUPPORTED)
                        .map(
                                KnowledgeEvidenceClaimVerification
                                        ::claimId
                        )
                        .toList();

        boolean releasable =
                blocked.isEmpty();

        return new KnowledgeEvidenceAnswerVerification(
                "answer-verification-"
                        + UUID.randomUUID(),
                graph,
                verifications,
                overall,
                confidence,
                releasable,
                blocked,
                java.util.Map.of(
                        "claimCount",
                        Integer.toString(claims.size())
                )
        );
    }
}
