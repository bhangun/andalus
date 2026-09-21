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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeEvidenceRuntimeRetrieverRegistry
        implements KnowledgeEvidenceRuntimeRetrieverRegistry {

    private final Map<
            String,
            KnowledgeEvidenceRuntimeRetriever
            > retrievers =
            new ConcurrentHashMap<>();

    @Override
    public KnowledgeEvidenceRuntimeRetriever get(
            String runtimeId
    ) {

        return retrievers.get(runtimeId);
    }

    @Override
    public void register(
            KnowledgeEvidenceRuntimeRetriever retriever
    ) {

        if (retriever == null) {
            throw new IllegalArgumentException(
                    "retriever is required"
            );
        }

        retrievers.put(
                retriever.runtimeId(),
                retriever
        );
    }

    @Override
    public void remove(
            String runtimeId
    ) {

        retrievers.remove(runtimeId);
    }
}
