package tech.kayys.andalus.knowledge.exchange.compact;

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


public final class DefaultKnowledgeAnswerArtifactEquivalencePolicy
        implements KnowledgeAnswerArtifactEquivalencePolicy {

    @Override
    public KnowledgeAnswerArtifactComparison compare(
            KnowledgeAnswerArtifactIndexEntry left,
            KnowledgeAnswerArtifactIndexEntry right,
            double similarity) {

        if (left.artifactId()
                .equals(right.artifactId())) {

            return new KnowledgeAnswerArtifactComparison(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactComparisonType
                            .IDENTICAL,
                    1.0,
                    1.0,
                    "Same artifact identity",
                    java.util.Map.of()
            );
        }

        if (left.responseFingerprint() != null
                && left.responseFingerprint()
                        .equals(right.responseFingerprint())) {

            return new KnowledgeAnswerArtifactComparison(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactComparisonType
                            .DUPLICATE,
                    1.0,
                    1.0,
                    "Same response fingerprint",
                    java.util.Map.of()
            );
        }

        if (similarity >= 0.90) {

            return new KnowledgeAnswerArtifactComparison(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactComparisonType
                            .NEAR_DUPLICATE,
                    similarity,
                    0.80,
                    "Highly similar indexed concepts",
                    java.util.Map.of()
            );
        }

        if (similarity >= 0.65) {

            return new KnowledgeAnswerArtifactComparison(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactComparisonType
                            .RELATED,
                    similarity,
                    0.50,
                    "Related indexed concepts",
                    java.util.Map.of()
            );
        }

        return new KnowledgeAnswerArtifactComparison(
                left.artifactId(),
                right.artifactId(),
                KnowledgeAnswerArtifactComparisonType
                        .UNKNOWN,
                similarity,
                0.10,
                "Insufficient evidence for equivalence",
                java.util.Map.of()
        );
    }
}
