package tech.kayys.andalus.knowledge.exchange.fusion;

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


public final class DefaultKnowledgeEvidenceFusionScorer
        implements KnowledgeEvidenceFusionScorer {

    private final KnowledgeEvidenceAuthorityScorer authority;

    private final KnowledgeEvidenceTrustScorer trust;

    private final KnowledgeEvidenceFreshnessScorer freshness;

    public DefaultKnowledgeEvidenceFusionScorer(
            KnowledgeEvidenceAuthorityScorer authority,
            KnowledgeEvidenceTrustScorer trust,
            KnowledgeEvidenceFreshnessScorer freshness
    ) {

        this.authority = authority;
        this.trust = trust;
        this.freshness = freshness;
    }

    @Override
    public KnowledgeEvidenceFusionCandidate score(
            KnowledgeEvidenceReference evidence,
            String runtimeId,
            KnowledgeEvidenceSemanticQuery query
    ) {

        double retrieval =
                clamp(evidence.relevance());

        double authorityScore =
                clamp(
                        authority.score(evidence)
                );

        double trustScore =
                clamp(
                        trust.score(evidence)
                );

        double freshnessScore =
                clamp(
                        freshness.score(
                                evidence,
                                query.effectiveAt()
                        )
                );

        /*
         * Default weights are deliberately transparent.
         * Domain implementations may override this SPI.
         */
        double finalScore =
                retrieval * 0.45
                        + authorityScore * 0.25
                        + trustScore * 0.20
                        + freshnessScore * 0.10;

        return new KnowledgeEvidenceFusionCandidate(
                evidence,
                runtimeId,
                retrieval,
                retrieval,
                authorityScore,
                trustScore,
                freshnessScore,
                finalScore,
                java.util.Map.of()
        );
    }

    private double clamp(double value) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {

            return 0.0;
        }

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        value
                )
        );
    }
}
