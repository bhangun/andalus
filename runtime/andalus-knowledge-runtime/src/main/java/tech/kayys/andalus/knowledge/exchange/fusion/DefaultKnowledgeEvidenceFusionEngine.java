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

public final class DefaultKnowledgeEvidenceFusionEngine
        implements KnowledgeEvidenceFusionEngine {

    private final KnowledgeEvidenceFusionScorer scorer;

    private final KnowledgeEvidenceReranker reranker;

    private final KnowledgeEvidenceDuplicateDetector duplicateDetector;

    private final KnowledgeEvidenceConflictDetector conflictDetector;

    private final KnowledgeEvidenceConflictResolutionPolicy
            conflictPolicy;

    private final KnowledgeEvidenceCoherenceSelector selector;

    public DefaultKnowledgeEvidenceFusionEngine(
            KnowledgeEvidenceFusionScorer scorer,
            KnowledgeEvidenceReranker reranker,
            KnowledgeEvidenceDuplicateDetector duplicateDetector,
            KnowledgeEvidenceConflictDetector conflictDetector,
            KnowledgeEvidenceConflictResolutionPolicy conflictPolicy,
            KnowledgeEvidenceCoherenceSelector selector
    ) {

        this.scorer = scorer;
        this.reranker = reranker;
        this.duplicateDetector = duplicateDetector;
        this.conflictDetector = conflictDetector;
        this.conflictPolicy = conflictPolicy;
        this.selector = selector;
    }

    @Override
    public KnowledgeEvidenceFusionResult fuse(
            List<KnowledgeEvidenceFederationQueryResult> results,
            KnowledgeEvidenceSemanticQuery query
    ) {

        var candidates =
                new ArrayList<
                        KnowledgeEvidenceFusionCandidate
                        >();

        for (var result : results) {

            if (result == null
                    || !result.authorized()) {

                continue;
            }

            if (query.requireVerification()
                    && !result.verified()) {

                continue;
            }

            for (var evidence : result.evidence()) {

                candidates.add(
                        scorer.score(
                                evidence,
                                result.runtimeId(),
                                query
                        )
                );
            }
        }

        /*
         * Remove duplicates before conflict analysis.
         */
        var unique =
                new ArrayList<
                        KnowledgeEvidenceFusionCandidate
                        >();

        for (var candidate : candidates) {

            boolean duplicate = false;

            for (var existing : unique) {

                if (duplicateDetector.duplicate(
                        candidate.evidence(),
                        existing.evidence()
                )) {

                    duplicate = true;
                    break;
                }
            }

            if (!duplicate) {
                unique.add(candidate);
            }
        }

        var ranked =
                reranker.rerank(unique);

        var conflicts =
                conflictDetector.detect(ranked);

        boolean ambiguous = false;

        for (var conflict : conflicts) {

            var decision =
                    conflictPolicy.resolve(
                            conflict
                    );

            if (decision
                    instanceof KnowledgeEvidenceConflictResolutionDecision
                            .Ambiguous) {

                ambiguous = true;
            }
        }

        var relations =
                buildRelations(ranked);

        var selected =
                selector.select(
                        ranked,
                        relations,
                        conflicts,
                        query
                );

        return new KnowledgeEvidenceFusionResult(
                ranked,
                selected,
                relations,
                conflicts,
                !selected.isEmpty()
                        && !ambiguous,
                ambiguous,
                java.util.Map.of(
                        "candidateCount",
                        Integer.toString(
                                candidates.size()
                        ),
                        "uniqueCount",
                        Integer.toString(
                                unique.size()
                        ),
                        "selectedCount",
                        Integer.toString(
                                selected.size()
                        ),
                        "conflictCount",
                        Integer.toString(
                                conflicts.size()
                        )
                )
        );
    }

    private List<KnowledgeEvidenceFusionRelation> buildRelations(
            List<KnowledgeEvidenceFusionCandidate> candidates
    ) {

        var relations =
                new ArrayList<
                        KnowledgeEvidenceFusionRelation
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

                if (left.evidence()
                        .knowledgeId()
                        .equals(
                                right.evidence()
                                        .knowledgeId()
                        )) {

                    relations.add(
                            new KnowledgeEvidenceFusionRelation(
                                    evidenceId(left),
                                    evidenceId(right),
                                    KnowledgeEvidenceFusionRelationType
                                            .RELATED,
                                    0.5,
                                    "Same knowledge lineage",
                                    java.util.Map.of()
                            )
                    );
                }
            }
        }

        return List.copyOf(relations);
    }

    private String evidenceId(
            KnowledgeEvidenceFusionCandidate candidate
    ) {

        var evidence =
                candidate.evidence();

        return evidence.knowledgeId()
                + ":"
                + evidence.versionId()
                + ":"
                + evidence.fragmentId();
    }
}
