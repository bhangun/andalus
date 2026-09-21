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


import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceExchangeRuntimePeerRegistry
        implements KnowledgeEvidenceExchangeRuntimePeerRegistry {

    private final ConcurrentMap<String,
            KnowledgeEvidenceExchangeRuntimePeer> peers =
            new ConcurrentHashMap<>();

    private String key(
            String localRuntimeId,
            String remoteRuntimeId
    ) {
        return localRuntimeId + "->" + remoteRuntimeId;
    }

    @Override
    public void trust(
            KnowledgeEvidenceExchangeRuntimePeer peer
    ) {

        peers.putIfAbsent(
                key(
                        peer.localRuntimeId(),
                        peer.remoteRuntimeId()
                ),
                peer
        );
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeRuntimePeer> find(
            String localRuntimeId,
            String remoteRuntimeId
    ) {

        return Optional.ofNullable(
                peers.get(
                        key(
                                localRuntimeId,
                                remoteRuntimeId
                        )
                )
        );
    }

    @Override
    public boolean trustedAt(
            String localRuntimeId,
            String remoteRuntimeId,
            java.time.Instant at
    ) {

        return find(
                localRuntimeId,
                remoteRuntimeId
        ).map(peer -> peer.trustedAt(at))
                .orElse(false);
    }

    @Override
    public void revoke(
            String localRuntimeId,
            String remoteRuntimeId,
            String reason
    ) {

        peers.computeIfPresent(
                key(localRuntimeId, remoteRuntimeId),
                (ignored, peer) ->
                        new KnowledgeEvidenceExchangeRuntimePeer(
                                peer.localRuntimeId(),
                                peer.remoteRuntimeId(),
                                peer.remoteIdentityFingerprint(),
                                peer.trustAnchorId(),
                                peer.trustedFrom(),
                                peer.trustedUntil(),
                                false,
                                mergeReason(
                                        peer.metadata(),
                                        reason
                                )
                        )
        );
    }

    private java.util.Map<String, String> mergeReason(
            java.util.Map<String, String> original,
            String reason
    ) {

        var result =
                new java.util.HashMap<>(original);

        if (reason != null) {
            result.put(
                    "revocationReason",
                    reason
            );
        }

        return result;
    }
}
