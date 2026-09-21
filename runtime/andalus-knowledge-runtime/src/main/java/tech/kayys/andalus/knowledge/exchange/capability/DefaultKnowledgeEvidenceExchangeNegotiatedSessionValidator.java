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

public final class DefaultKnowledgeEvidenceExchangeNegotiatedSessionValidator
        implements KnowledgeEvidenceExchangeNegotiatedSessionValidator {

    @Override
    public boolean supports(
            KnowledgeEvidenceExchangeNegotiatedSession session,
            KnowledgeEvidenceExchangeCapabilityType capability,
            Instant at
    ) {

        return session != null &&
                session.activeAt(at) &&
                session.supports(capability);
    }

    @Override
    public void require(
            KnowledgeEvidenceExchangeNegotiatedSession session,
            KnowledgeEvidenceExchangeCapabilityType capability,
            Instant at
    ) {

        if (!supports(session, capability, at)) {

            throw new IllegalStateException(
                    "Negotiated session does not support capability: "
                            + capability
            );
        }
    }
}
