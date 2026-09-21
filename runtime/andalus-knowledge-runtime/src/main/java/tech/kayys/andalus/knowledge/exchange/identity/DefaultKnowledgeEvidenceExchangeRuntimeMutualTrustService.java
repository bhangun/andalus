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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeEvidenceExchangeRuntimeMutualTrustService
        implements KnowledgeEvidenceExchangeRuntimeMutualTrustService {

    private final KnowledgeEvidenceExchangeRuntimeTrustService trustService;
    private final String localTenantId;

    public DefaultKnowledgeEvidenceExchangeRuntimeMutualTrustService(
            KnowledgeEvidenceExchangeRuntimeTrustService trustService,
            String localTenantId
    ) {
        this.trustService =
                Objects.requireNonNull(trustService);

        this.localTenantId = localTenantId;
    }

    @Override
    public KnowledgeEvidenceExchangeRuntimeHandshakeResult establish(
            KnowledgeEvidenceExchangeRuntimeIdentity localIdentity,
            KnowledgeEvidenceExchangeRuntimeIdentity remoteIdentity,
            String localKeyId,
            String localKeyVersion,
            String remoteKeyId,
            String remoteKeyVersion,
            String localNonce,
            String remoteNonce,
            Instant now
    ) {

        Objects.requireNonNull(localIdentity);
        Objects.requireNonNull(remoteIdentity);
        Objects.requireNonNull(now);

        /*
         * Local runtime must be active too.
         */
        if (!localIdentity.activeAt(now)) {

            return denied(
                    localIdentity,
                    remoteIdentity,
                    localKeyId,
                    localKeyVersion,
                    remoteKeyId,
                    remoteKeyVersion,
                    localNonce,
                    remoteNonce,
                    now,
                    "Local runtime identity is not active"
            );
        }

        /*
         * Remote runtime must be independently trusted.
         */
        var remoteTrust =
                trustService.verify(
                        remoteIdentity.runtimeId(),
                        localTenantId,
                        now
                );

        if (remoteTrust
                instanceof KnowledgeEvidenceExchangeRuntimeTrustDecision.Denied denied) {

            return denied(
                    localIdentity,
                    remoteIdentity,
                    localKeyId,
                    localKeyVersion,
                    remoteKeyId,
                    remoteKeyVersion,
                    localNonce,
                    remoteNonce,
                    now,
                    denied.reason()
            );
        }

        var handshake =
                new KnowledgeEvidenceExchangeRuntimeHandshake(
                        UUID.randomUUID().toString(),
                        localIdentity.runtimeId(),
                        remoteIdentity.runtimeId(),
                        localIdentity.identityFingerprint(),
                        remoteIdentity.identityFingerprint(),
                        localKeyId,
                        localKeyVersion,
                        remoteKeyId,
                        remoteKeyVersion,
                        localNonce,
                        remoteNonce,
                        now,
                        now.plusSeconds(300),
                        true,
                        true,
                        true,
                        Map.of()
                );

        return new KnowledgeEvidenceExchangeRuntimeHandshakeResult(
                KnowledgeEvidenceExchangeRuntimeHandshakeStatus
                        .MUTUALLY_TRUSTED,
                handshake,
                null,
                Map.of()
        );
    }

    private KnowledgeEvidenceExchangeRuntimeHandshakeResult denied(
            KnowledgeEvidenceExchangeRuntimeIdentity localIdentity,
            KnowledgeEvidenceExchangeRuntimeIdentity remoteIdentity,
            String localKeyId,
            String localKeyVersion,
            String remoteKeyId,
            String remoteKeyVersion,
            String localNonce,
            String remoteNonce,
            Instant now,
            String reason
    ) {

        var handshake =
                new KnowledgeEvidenceExchangeRuntimeHandshake(
                        UUID.randomUUID().toString(),
                        localIdentity.runtimeId(),
                        remoteIdentity.runtimeId(),
                        localIdentity.identityFingerprint(),
                        remoteIdentity.identityFingerprint(),
                        localKeyId,
                        localKeyVersion,
                        remoteKeyId,
                        remoteKeyVersion,
                        localNonce,
                        remoteNonce,
                        now,
                        now.plusSeconds(300),
                        true,
                        false,
                        false,
                        Map.of()
                );

        return new KnowledgeEvidenceExchangeRuntimeHandshakeResult(
                KnowledgeEvidenceExchangeRuntimeHandshakeStatus.DENIED,
                handshake,
                reason,
                Map.of()
        );
    }
}
