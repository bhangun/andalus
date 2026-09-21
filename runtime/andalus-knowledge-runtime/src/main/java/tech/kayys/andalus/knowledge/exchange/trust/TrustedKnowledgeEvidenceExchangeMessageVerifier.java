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


import java.time.Instant;
import java.util.Objects;

public final class TrustedKnowledgeEvidenceExchangeMessageVerifier {

    private final KnowledgeEvidenceExchangeMessageVerifier verifier;
    private final KnowledgeEvidenceExchangeKeyTrustService trustService;

    public TrustedKnowledgeEvidenceExchangeMessageVerifier(
            KnowledgeEvidenceExchangeMessageVerifier verifier,
            KnowledgeEvidenceExchangeKeyTrustService trustService
    ) {

        this.verifier = Objects.requireNonNull(verifier);
        this.trustService = Objects.requireNonNull(trustService);
    }

    public KnowledgeEvidenceExchangeMessageAuthenticationStatus verify(
            KnowledgeEvidenceExchangeSignedMessage<?> message,
            String expectedRuntimeId,
            String expectedTenantId,
            Instant at
    ) {

        var envelope = message.envelope();

        var trustDecision = trustService.verify(
                envelope.keyId(),
                envelope.keyVersion(),
                expectedRuntimeId,
                expectedTenantId,
                at
        );

        if (trustDecision
                instanceof KnowledgeEvidenceExchangeKeyTrustDecision.Denied) {

            return mapDenied(
                    (KnowledgeEvidenceExchangeKeyTrustDecision.Denied)
                            trustDecision
            );
        }

        return verifier.verify(message, expectedRuntimeId, expectedTenantId, at);
    }

    private KnowledgeEvidenceExchangeMessageAuthenticationStatus mapDenied(
            KnowledgeEvidenceExchangeKeyTrustDecision.Denied denied
    ) {

        return switch (denied.status()) {

            case UNKNOWN ->
                    KnowledgeEvidenceExchangeMessageAuthenticationStatus.UNKNOWN_KEY;

            case REVOKED ->
                    KnowledgeEvidenceExchangeMessageAuthenticationStatus.REVOKED_KEY;

            case EXPIRED ->
                    KnowledgeEvidenceExchangeMessageAuthenticationStatus.EXPIRED_KEY;

            default ->
                    KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_MESSAGE;
        };
    }
}
