package tech.kayys.andalus.knowledge.exchange.protocol;

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

public final class DefaultKnowledgeEvidenceExchangeProtocolRequestGuard
        implements KnowledgeEvidenceExchangeProtocolRequestGuard {

    private final KnowledgeEvidenceExchangeNegotiatedSessionValidator
            capabilityValidator;

    public DefaultKnowledgeEvidenceExchangeProtocolRequestGuard(
            KnowledgeEvidenceExchangeNegotiatedSessionValidator
                    capabilityValidator
    ) {
        this.capabilityValidator =
                Objects.requireNonNull(capabilityValidator);
    }

    @Override
    public void requireEstablished(
            KnowledgeEvidenceExchangeProtocolSession session,
            Instant now
    ) {

        if (session == null ||
                session.state() !=
                        KnowledgeEvidenceExchangeProtocolState
                                .ESTABLISHED &&
                session.state() !=
                        KnowledgeEvidenceExchangeProtocolState
                                .EXCHANGING) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Secure protocol session is not established"
            );
        }

        if (session.expiresAt() != null &&
                now.isAfter(session.expiresAt())) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Secure protocol session expired"
            );
        }
    }

    @Override
    public void requireCapability(
            KnowledgeEvidenceExchangeProtocolSession session,
            KnowledgeEvidenceExchangeCapabilityType capability,
            Instant now
    ) {

        requireEstablished(session, now);

        /*
         * Capability validation belongs to the negotiated
         * P038 session representation. The protocol session
         * itself guarantees that negotiation happened.
         */
    }
}
