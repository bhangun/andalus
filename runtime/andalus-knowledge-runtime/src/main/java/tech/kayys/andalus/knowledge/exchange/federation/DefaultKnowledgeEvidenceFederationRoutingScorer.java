package tech.kayys.andalus.knowledge.exchange.federation;

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


public final class DefaultKnowledgeEvidenceFederationRoutingScorer
        implements KnowledgeEvidenceFederationRoutingScorer {

    @Override
    public double score(
            KnowledgeEvidenceRuntimeLocation location,
            KnowledgeEvidenceRuntimeQueryCapability capability,
            KnowledgeEvidenceFederatedQuery query
    ) {

        double score = 0.0;

        if (location.local()) {
            score += 100.0;
        }

        if (location.trusted()) {
            score += 50.0;
        }

        if (location.verified()) {
            score += 30.0;
        }

        if (capability.governedRetrieval()) {
            score += 25.0;
        }

        if (query.requireVerification()
                && capability.verification()) {

            score += 20.0;
        }

        score -= Math.min(
                location.estimatedLatencyMs() / 10.0,
                50.0
        );

        return score;
    }
}
