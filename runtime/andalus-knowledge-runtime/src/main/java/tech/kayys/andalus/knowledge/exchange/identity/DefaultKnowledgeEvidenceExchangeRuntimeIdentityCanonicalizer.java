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


import java.nio.charset.StandardCharsets;

public final class DefaultKnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer
        implements KnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer {

    @Override
    public byte[] canonicalize(
            KnowledgeEvidenceExchangeRuntimeIdentity identity
    ) {

        String value = String.join(
                "\n",
                identity.runtimeId(),
                identity.identityVersion(),
                nullSafe(identity.displayName()),
                nullSafe(identity.runtimeType()),
                nullSafe(identity.organizationId()),
                nullSafe(identity.tenantId()),
                nullSafe(identity.primaryKeyId()),
                nullSafe(identity.primaryKeyVersion()),
                nullSafe(identity.trustAnchorId())
        );

        return value.getBytes(StandardCharsets.UTF_8);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
