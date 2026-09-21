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


public final class DefaultKnowledgeEvidenceDuplicateDetector
        implements KnowledgeEvidenceDuplicateDetector {

    private final KnowledgeEvidenceSimilarityService similarity;

    private final double threshold;

    public DefaultKnowledgeEvidenceDuplicateDetector(
            KnowledgeEvidenceSimilarityService similarity,
            double threshold
    ) {

        this.similarity = similarity;
        this.threshold = threshold;
    }

    @Override
    public boolean duplicate(
            KnowledgeEvidenceReference left,
            KnowledgeEvidenceReference right
    ) {

        if (left.knowledgeId()
                .equals(right.knowledgeId())
                && java.util.Objects.equals(
                        left.versionId(),
                        right.versionId()
                )
                && java.util.Objects.equals(
                        left.fragmentId(),
                        right.fragmentId()
                )) {

            return true;
        }

        return similarity.similarity(
                left,
                right
        ) >= threshold;
    }
}
