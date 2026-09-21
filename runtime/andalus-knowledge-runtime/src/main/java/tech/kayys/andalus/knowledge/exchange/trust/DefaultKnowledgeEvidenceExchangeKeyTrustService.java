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

public final class DefaultKnowledgeEvidenceExchangeKeyTrustService
        implements KnowledgeEvidenceExchangeKeyTrustService {

    private final KnowledgeEvidenceExchangeKeyTrustRegistry registry;
    private final KnowledgeEvidenceExchangeKeyTrustPolicy policy;

    public DefaultKnowledgeEvidenceExchangeKeyTrustService(
            KnowledgeEvidenceExchangeKeyTrustRegistry registry,
            KnowledgeEvidenceExchangeKeyTrustPolicy policy
    ) {

        this.registry = Objects.requireNonNull(registry);
        this.policy = Objects.requireNonNull(policy);
    }

    @Override
    public KnowledgeEvidenceExchangeKeyTrustDecision verify(
            String keyId,
            String keyVersion,
            String expectedRuntimeId,
            String expectedTenantId,
            Instant at
    ) {

        var key = registry.find(
                keyId,
                keyVersion
        );

        if (key.isEmpty()) {

            return new KnowledgeEvidenceExchangeKeyTrustDecision.Denied(
                    "key-registry",
                    KnowledgeEvidenceExchangeKeyTrustStatus.UNKNOWN,
                    "Unknown key",
                    java.util.Map.of(
                            "keyId", keyId,
                            "keyVersion", keyVersion
                    )
            );
        }

        return policy.evaluate(
                key.get(),
                expectedRuntimeId,
                expectedTenantId,
                at
        );
    }
}
