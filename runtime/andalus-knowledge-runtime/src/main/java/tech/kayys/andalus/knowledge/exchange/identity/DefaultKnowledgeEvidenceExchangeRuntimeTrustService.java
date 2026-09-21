package tech.kayys.andalus.knowledge.exchange.identity;

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

public final class DefaultKnowledgeEvidenceExchangeRuntimeTrustService
        implements KnowledgeEvidenceExchangeRuntimeTrustService {

    private final KnowledgeEvidenceExchangeRuntimeIdentityRegistry registry;
    private final String policyId;

    public DefaultKnowledgeEvidenceExchangeRuntimeTrustService(
            KnowledgeEvidenceExchangeRuntimeIdentityRegistry registry,
            String policyId
    ) {
        this.registry = Objects.requireNonNull(registry);
        this.policyId = Objects.requireNonNull(policyId);
    }

    @Override
    public KnowledgeEvidenceExchangeRuntimeTrustDecision verify(
            String runtimeId,
            String expectedTenantId,
            Instant at
    ) {

        var identity =
                registry.resolve(runtimeId, at);

        if (identity.isEmpty()) {

            return new KnowledgeEvidenceExchangeRuntimeTrustDecision.Denied(
                    policyId,
                    KnowledgeEvidenceExchangeRuntimeIdentityStatus.UNKNOWN,
                    "Runtime identity is unknown or inactive",
                    java.util.Map.of(
                            "runtimeId",
                            runtimeId
                    )
            );
        }

        var value = identity.get();

        if (expectedTenantId != null &&
                value.tenantId() != null &&
                !expectedTenantId.equals(value.tenantId())) {

            return new KnowledgeEvidenceExchangeRuntimeTrustDecision.Denied(
                    policyId,
                    KnowledgeEvidenceExchangeRuntimeIdentityStatus.UNTRUSTED,
                    "Runtime tenant mismatch",
                    java.util.Map.of(
                            "expectedTenantId",
                            expectedTenantId,
                            "actualTenantId",
                            value.tenantId()
                    )
            );
        }

        return new KnowledgeEvidenceExchangeRuntimeTrustDecision.Trusted(
                policyId,
                java.util.Map.of(
                        "runtimeId",
                        runtimeId,
                        "identityVersion",
                        value.identityVersion()
                )
        );
    }
}
