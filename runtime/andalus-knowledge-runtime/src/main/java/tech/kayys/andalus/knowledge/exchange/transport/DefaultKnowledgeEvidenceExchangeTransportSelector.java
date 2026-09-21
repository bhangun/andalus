package tech.kayys.andalus.knowledge.exchange.transport;

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


import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeTransportSelector
        implements KnowledgeEvidenceExchangeTransportSelector {

    private final KnowledgeEvidenceExchangeTransportRegistry registry;

    public DefaultKnowledgeEvidenceExchangeTransportSelector(
            KnowledgeEvidenceExchangeTransportRegistry registry
    ) {
        this.registry =
                Objects.requireNonNull(registry);
    }

    @Override
    public KnowledgeEvidenceExchangeTransport select(
            KnowledgeEvidenceExchangeTransportType preferred,
            KnowledgeEvidenceExchangeTransportDescriptor remote
    ) {

        var transport =
                registry.find(preferred)
                        .orElseThrow(() ->
                                new KnowledgeEvidenceExchangeTransportException(
                                        "Transport unavailable: "
                                                + preferred
                                )
                        );

        return transport;
    }
}
