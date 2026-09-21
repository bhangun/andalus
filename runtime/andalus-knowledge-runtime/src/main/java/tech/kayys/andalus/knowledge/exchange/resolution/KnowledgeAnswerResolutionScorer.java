package tech.kayys.andalus.knowledge.exchange.resolution;

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


public final class KnowledgeAnswerResolutionScorer {

    public KnowledgeAnswerResolutionCandidate score(
            KnowledgeVerifiedAnswerArtifact artifact,
            KnowledgeAnswerResolutionContext context,
            java.util.List<
                    KnowledgeAnswerArtifactRelation> relations,
            KnowledgeAnswerEligibilityService eligibility,
            KnowledgeAnswerAuthorityResolver authority,
            KnowledgeAnswerTrustResolver trust,
            KnowledgeAnswerProvenanceScorer provenance,
            KnowledgeAnswerScopeScorer scope,
            KnowledgeAnswerSupportScorer support) {

        boolean eligible =
                eligibility.eligible(
                        artifact,
                        context
                );

        double authorityScore =
                authority.score(artifact);

        double trustScore =
                trust.score(artifact);

        double provenanceScore =
                provenance.score(artifact);

        double scopeScore =
                scope.score(
                        artifact,
                        context
                );

        double supportScore =
                support.score(
                        artifact.artifactId(),
                        relations
                );

        double freshnessScore =
                freshness(
                        artifact,
                        context
                );

        double conflictPenalty =
                conflictPenalty(
                        artifact.artifactId(),
                        relations
                );

        double finalScore =
                authorityScore * 0.25
                + trustScore * 0.25
                + provenanceScore * 0.10
                + scopeScore * 0.15
                + supportScore * 0.10
                + freshnessScore * 0.15
                - conflictPenalty;

        if (!eligible) {
            finalScore = 0.0;
        }

        return new KnowledgeAnswerResolutionCandidate(
                artifact.artifactId(),
                "local",
                authorityScore,
                trustScore,
                freshnessScore,
                provenanceScore,
                scopeScore,
                supportScore,
                conflictPenalty,
                Math.max(
                        0.0,
                        Math.min(1.0, finalScore)
                ),
                artifact.response()
                        .status()
                        == KnowledgeVerifiedResponseStatus
                        .VERIFIED,
                eligible,
                java.util.Map.of()
        );
    }

    private double freshness(
            KnowledgeVerifiedAnswerArtifact artifact,
            KnowledgeAnswerResolutionContext context) {

        if (context.effectiveAt() == null) {
            return 0.5;
        }

        if (artifact.createdAt() == null) {
            return 0.5;
        }

        return artifact.createdAt()
                .isAfter(context.effectiveAt())
                ? 0.0
                : 1.0;
    }

    private double conflictPenalty(
            String artifactId,
            java.util.List<
                    KnowledgeAnswerArtifactRelation> relations) {

        return relations.stream()
                .filter(r ->
                        r.type()
                                == KnowledgeAnswerArtifactRelationType
                                .CONTRADICTS)
                .filter(r ->
                        r.sourceArtifactId()
                                .equals(artifactId)
                        || r.targetArtifactId()
                                .equals(artifactId))
                .mapToDouble(r ->
                        0.25 * r.confidence())
                .sum();
    }
}
