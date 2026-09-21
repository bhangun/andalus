package tech.kayys.andalus.knowledge.exchange.capability;

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


import java.time.Instant;
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeCapabilityNegotiationService
        implements KnowledgeEvidenceExchangeCapabilityNegotiationService {

    private final KnowledgeEvidenceExchangeCapabilityNegotiator negotiator;

    public DefaultKnowledgeEvidenceExchangeCapabilityNegotiationService(
            KnowledgeEvidenceExchangeCapabilityNegotiator negotiator
    ) {
        this.negotiator =
                Objects.requireNonNull(negotiator);
    }

    @Override
    public KnowledgeEvidenceExchangeCapabilityNegotiationResult negotiate(
            KnowledgeEvidenceExchangeCapabilityNegotiationRequest request
    ) {

        Objects.requireNonNull(request);

        return negotiator.negotiate(
                request.localManifest(),
                request.remoteManifest(),
                request.requestedAt()
        );
    }

    @Override
    public boolean isAllowed(
            KnowledgeEvidenceExchangeCapabilityNegotiationResult result,
            KnowledgeEvidenceExchangeCapabilityType capability
    ) {

        if (!result.successful()) {
            return false;
        }

        return result.capabilities()
                .stream()
                .anyMatch(
                        value -> value.type() == capability
                );
    }

    @Override
    public Instant negotiatedAt(
            KnowledgeEvidenceExchangeCapabilityNegotiationResult result
    ) {

        return Instant.now();
    }
}
