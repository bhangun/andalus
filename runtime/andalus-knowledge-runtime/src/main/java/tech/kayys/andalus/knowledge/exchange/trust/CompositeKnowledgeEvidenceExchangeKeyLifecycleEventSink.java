package tech.kayys.andalus.knowledge.exchange.trust;

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


import java.util.List;
import java.util.Objects;

public final class CompositeKnowledgeEvidenceExchangeKeyLifecycleEventSink
        implements KnowledgeEvidenceExchangeKeyLifecycleEventSink {

    private final List<KnowledgeEvidenceExchangeKeyLifecycleEventSink>
            sinks;

    public CompositeKnowledgeEvidenceExchangeKeyLifecycleEventSink(
            List<KnowledgeEvidenceExchangeKeyLifecycleEventSink> sinks
    ) {

        this.sinks = sinks == null
                ? List.of()
                : sinks.stream()
                        .filter(Objects::nonNull)
                        .toList();
    }

    @Override
    public void record(
            KnowledgeEvidenceExchangeKeyLifecycleEvent event
    ) {

        for (var sink : sinks) {
            sink.record(event);
        }
    }
}
