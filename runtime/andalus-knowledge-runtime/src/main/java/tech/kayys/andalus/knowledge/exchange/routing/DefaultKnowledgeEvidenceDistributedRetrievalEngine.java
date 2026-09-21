package tech.kayys.andalus.knowledge.exchange.routing;

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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class DefaultKnowledgeEvidenceDistributedRetrievalEngine
        implements KnowledgeEvidenceDistributedRetrievalEngine {

    private final KnowledgeEvidenceQueryAnalyzer analyzer;

    private final KnowledgeEvidenceQueryDecomposer decomposer;

    private final KnowledgeEvidenceRuntimeSelector selector;

    private final KnowledgeEvidenceDistributedRetrievalPlanner planner;

    private final KnowledgeEvidenceRuntimeRetrieverRegistry retrievers;

    private final KnowledgeEvidenceFusionService fusion;

    private final KnowledgeEvidenceFederationDeduplicator deduplicator;

    private final KnowledgeEvidenceFederationRanker ranker;

    private final KnowledgeEvidenceRetrievalStopPolicy stopPolicy;

    public DefaultKnowledgeEvidenceDistributedRetrievalEngine(
            KnowledgeEvidenceQueryAnalyzer analyzer,
            KnowledgeEvidenceQueryDecomposer decomposer,
            KnowledgeEvidenceRuntimeSelector selector,
            KnowledgeEvidenceDistributedRetrievalPlanner planner,
            KnowledgeEvidenceRuntimeRetrieverRegistry retrievers,
            KnowledgeEvidenceFusionService fusion,
            KnowledgeEvidenceFederationDeduplicator deduplicator,
            KnowledgeEvidenceFederationRanker ranker,
            KnowledgeEvidenceRetrievalStopPolicy stopPolicy
    ) {

        this.analyzer = analyzer;
        this.decomposer = decomposer;
        this.selector = selector;
        this.planner = planner;
        this.retrievers = retrievers;
        this.fusion = fusion;
        this.deduplicator = deduplicator;
        this.ranker = ranker;
        this.stopPolicy = stopPolicy;
    }

    @Override
    public CompletableFuture<KnowledgeEvidenceFederatedResult> retrieve(
            KnowledgeEvidenceSemanticQuery query
    ) {

        var intent =
                analyzer.analyze(query);

        var subQueries =
                decomposer.decompose(
                        query,
                        intent
                );

        if (subQueries.isEmpty()) {

            return CompletableFuture.completedFuture(
                    new KnowledgeEvidenceFederatedResult(
                            null,
                            null,
                            List.of()
                    )
            );
        }

        var candidates =
                selector.select(
                        query,
                        intent
                );

        var plan =
                planner.plan(
                        query,
                        intent,
                        candidates
                );

        return executePlan(
                query,
                intent,
                plan
        );
    }

    private CompletableFuture<
            KnowledgeEvidenceFederatedResult
            > executePlan(
                    KnowledgeEvidenceSemanticQuery query,
                    KnowledgeEvidenceQueryIntent intent,
                    KnowledgeEvidenceRetrievalPlan plan
            ) {

        var results =
                new ArrayList<
                        KnowledgeEvidenceFederationQueryResult
                        >();

        var queried =
                new ArrayList<String>();

        return executeTargets(
                query,
                intent,
                plan,
                0,
                results,
                queried
        );
    }

    private CompletableFuture<
            KnowledgeEvidenceFederatedResult
            > executeTargets(
                    KnowledgeEvidenceSemanticQuery query,
                    KnowledgeEvidenceQueryIntent intent,
                    KnowledgeEvidenceRetrievalPlan plan,
                    int index,
                    List<KnowledgeEvidenceFederationQueryResult> results,
                    List<String> queried
            ) {

        if (index >= plan.targets().size()) {

            return finish(
                    query,
                    results,
                    queried
            );
        }

        var target =
                plan.targets().get(index);

        var retriever =
                retrievers.get(
                        target.runtimeId()
                );

        if (retriever == null) {

            return executeTargets(
                    query,
                    intent,
                    plan,
                    index + 1,
                    results,
                    queried
            );
        }

        queried.add(
                target.runtimeId()
        );

        var request =
                new KnowledgeEvidenceRuntimeRetrievalRequest(
                        query.queryId(),
                        target.runtimeId(),
                        query,
                        intent,
                        target.limit(),
                        target.minScore(),
                        Map.of()
                );

        return retriever
                .retrieve(request)
                .exceptionally(error -> null)
                .thenCompose(result -> {

                    if (result != null) {
                        results.add(result);
                    }

                    var evidence =
                            fusion.fuse(results);

                    if (stopPolicy.shouldStop(
                            evidence,
                            query
                    )) {

                        return finish(
                                query,
                                results,
                                queried
                        );
                    }

                    return executeTargets(
                            query,
                            intent,
                            plan,
                            index + 1,
                            results,
                            queried
                    );
                });
    }

    private CompletableFuture<
            KnowledgeEvidenceFederatedResult
            > finish(
                    KnowledgeEvidenceSemanticQuery query,
                    List<KnowledgeEvidenceFederationQueryResult> results,
                    List<String> queried
            ) {

        var fused =
                fusion.fuse(results);

        var unique =
                deduplicator.deduplicate(
                        fused
                );

        var ranked =
                ranker.rank(
                        unique,
                        new KnowledgeEvidenceFederatedQuery(
                                query.queryId(),
                                query.tenantId(),
                                query.workspaceId(),
                                query.projectId(),
                                query.agentId(),
                                query.text(),
                                query.limit(),
                                query.minScore(),
                                query.effectiveAt(),
                                query.requireVerification(),
                                true,
                                query.allowRemote(),
                                Map.of(),
                                Map.of()
                        )
                );

        return CompletableFuture.completedFuture(
                new KnowledgeEvidenceFederatedResult(
                        new KnowledgeEvidenceFederationAggregateResult(
                                query.queryId(),
                                ranked,
                                queried,
                                List.of(),
                                true,
                                Map.of(
                                        "retrieval",
                                        "distributed"
                                )
                        ),
                        new KnowledgeEvidenceFederationContext(
                                query.queryId(),
                                "local",
                                java.time.Instant.now(),
                                queried.size() > 1,
                                queried.size(),
                                ranked.size(),
                                Map.of()
                        ),
                        ranked
                )
        );
    }
}
