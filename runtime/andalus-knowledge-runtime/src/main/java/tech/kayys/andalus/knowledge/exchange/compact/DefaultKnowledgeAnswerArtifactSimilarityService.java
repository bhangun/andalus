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


import java.util.HashSet;

public final class DefaultKnowledgeAnswerArtifactSimilarityService
        implements KnowledgeAnswerArtifactSimilarityService {

    @Override
    public double similarity(
            KnowledgeAnswerArtifactIndexEntry left,
            KnowledgeAnswerArtifactIndexEntry right) {

        if (left.artifactId()
                .equals(right.artifactId())) {

            return 1.0;
        }

        if (left.responseFingerprint() != null
                && left.responseFingerprint()
                        .equals(right.responseFingerprint())) {

            return 1.0;
        }

        var leftTerms =
                new HashSet<String>(
                        left.concepts()
                );

        var rightTerms =
                new HashSet<String>(
                        right.concepts()
                );

        if (leftTerms.isEmpty()
                || rightTerms.isEmpty()) {

            return 0.0;
        }

        var intersection =
                new HashSet<>(leftTerms);

        intersection.retainAll(rightTerms);

        var union =
                new HashSet<>(leftTerms);

        union.addAll(rightTerms);

        return union.isEmpty()
                ? 0.0
                : (double) intersection.size()
                        / union.size();
    }
}
