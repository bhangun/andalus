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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceExchangeKeyTrustAnchorRegistry
        implements KnowledgeEvidenceExchangeKeyTrustAnchorRegistry {

    private final ConcurrentMap<String,
            KnowledgeEvidenceExchangeKeyTrustAnchor> anchors =
            new ConcurrentHashMap<>();

    @Override
    public void register(
            KnowledgeEvidenceExchangeKeyTrustAnchor anchor
    ) {

        anchors.compute(anchor.anchorId(), (id, existing) -> {

            if (existing == null) {
                return anchor;
            }

            if (!existing.equals(anchor)) {
                throw new IllegalStateException(
                        "Trust anchor already exists: " + id
                );
            }

            return existing;
        });
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeKeyTrustAnchor> find(
            String anchorId
    ) {

        return Optional.ofNullable(
                anchors.get(anchorId)
        );
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeKeyTrustAnchor> resolve(
            String anchorId,
            Instant at
    ) {

        return find(anchorId)
                .filter(anchor -> anchor.activeAt(at));
    }
}
