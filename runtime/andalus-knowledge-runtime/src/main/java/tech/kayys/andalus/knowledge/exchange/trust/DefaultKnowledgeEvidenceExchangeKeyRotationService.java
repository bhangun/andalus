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

public final class DefaultKnowledgeEvidenceExchangeKeyRotationService
        implements KnowledgeEvidenceExchangeKeyRotationService {

    private final KnowledgeEvidenceExchangeKeyTrustRegistry registry;

    public DefaultKnowledgeEvidenceExchangeKeyRotationService(
            KnowledgeEvidenceExchangeKeyTrustRegistry registry
    ) {
        this.registry = Objects.requireNonNull(registry);
    }

    @Override
    public KnowledgeEvidenceExchangeKeyRotation rotate(
            KnowledgeEvidenceExchangeTrustedKey previousKey,
            KnowledgeEvidenceExchangeTrustedKey newKey,
            Instant activatedAt,
            String reason
    ) {

        Objects.requireNonNull(previousKey);
        Objects.requireNonNull(newKey);
        Objects.requireNonNull(activatedAt);

        if (!Objects.equals(
                previousKey.keyId(),
                newKey.keyId()
        )) {
            throw new IllegalArgumentException(
                    "Key rotation requires the same keyId"
            );
        }

        if (newKey.keyVersion()
                .equals(previousKey.keyVersion())) {

            throw new IllegalArgumentException(
                    "New key version must differ from previous version"
            );
        }

        if (newKey.validFrom() != null &&
                newKey.validFrom().isAfter(activatedAt)) {

            throw new IllegalArgumentException(
                    "New key is not valid at activation time"
            );
        }

        registry.register(newKey);

        return new KnowledgeEvidenceExchangeKeyRotation(
                newKey.keyId(),
                previousKey.keyVersion(),
                newKey.keyVersion(),
                activatedAt,
                previousKey.validUntil(),
                newKey.runtimeId(),
                reason
        );
    }
}
