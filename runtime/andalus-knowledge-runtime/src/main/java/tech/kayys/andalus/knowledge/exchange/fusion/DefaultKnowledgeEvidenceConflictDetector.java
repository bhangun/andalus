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


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DefaultKnowledgeEvidenceConflictDetector
        implements KnowledgeEvidenceConflictDetector {

    private final KnowledgeEvidenceSimilarityService similarity;

    public DefaultKnowledgeEvidenceConflictDetector(
            KnowledgeEvidenceSimilarityService similarity
    ) {

        this.similarity = similarity;
    }

    @Override
    public List<KnowledgeEvidenceFusionConflict> detect(
            List<KnowledgeEvidenceFusionCandidate> candidates
    ) {

        var conflicts =
                new ArrayList<
                        KnowledgeEvidenceFusionConflict
                        >();

        for (int i = 0;
             i < candidates.size();
             i++) {

            for (int j = i + 1;
                 j < candidates.size();
                 j++) {

                var left =
                        candidates.get(i);

                var right =
                        candidates.get(j);

                var a =
                        left.evidence();

                var b =
                        right.evidence();

                /*
                 * Same knowledge/version is not a conflict.
                 */
                if (a.knowledgeId()
                        .equals(b.knowledgeId())
                        && java.util.Objects.equals(
                                a.versionId(),
                                b.versionId()
                        )) {

                    continue;
                }

                /*
                 * Core cannot safely infer contradiction from
                 * arbitrary natural-language excerpts.
                 *
                 * A domain-specific conflict detector should
                 * implement semantic contradiction.
                 */
                double similarityScore =
                        similarity.similarity(
                                a,
                                b
                        );

                if (similarityScore >= 0.95
                        && !java.util.Objects.equals(
                                a.versionId(),
                                b.versionId()
                        )) {

                    conflicts.add(
                            new KnowledgeEvidenceFusionConflict(
                                    UUID.randomUUID()
                                            .toString(),
                                    evidenceId(a),
                                    evidenceId(b),
                                    KnowledgeEvidenceConflictType
                                            .VERSION_CONFLICT,
                                    similarityScore,
                                    "Highly similar evidence has different versions",
                                    java.util.Map.of()
                            )
                    );
                }
            }
        }

        return List.copyOf(conflicts);
    }

    private String evidenceId(
            KnowledgeEvidenceReference evidence
    ) {

        return evidence.knowledgeId()
                + ":"
                + evidence.versionId()
                + ":"
                + evidence.fragmentId();
    }
}
