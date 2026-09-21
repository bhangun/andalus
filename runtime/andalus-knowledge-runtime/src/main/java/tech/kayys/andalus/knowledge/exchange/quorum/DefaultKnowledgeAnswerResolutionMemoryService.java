package tech.kayys.andalus.knowledge.exchange.quorum;

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
import tech.kayys.andalus.knowledge.exchange.transfer.*;
import tech.kayys.andalus.knowledge.exchange.replication.*;
import tech.kayys.andalus.knowledge.exchange.sync.*;
import tech.kayys.andalus.knowledge.exchange.federation.*;
import tech.kayys.andalus.knowledge.exchange.routing.*;
import tech.kayys.andalus.knowledge.exchange.fusion.*;
import tech.kayys.andalus.knowledge.exchange.coverage.*;
import tech.kayys.andalus.knowledge.exchange.gap.*;
import tech.kayys.andalus.knowledge.exchange.attribution.*;
import tech.kayys.andalus.knowledge.exchange.contradiction.*;
import tech.kayys.andalus.knowledge.exchange.factuality.*;
import tech.kayys.andalus.knowledge.exchange.uncertainty.*;
import tech.kayys.andalus.knowledge.exchange.compact.*;
import tech.kayys.andalus.knowledge.exchange.resolution.*;
import tech.kayys.andalus.knowledge.exchange.quorum.*;
import tech.kayys.andalus.knowledge.exchange.selection.*;
import tech.kayys.andalus.knowledge.exchange.coordination.*;
import tech.kayys.andalus.knowledge.exchange.attestation.*;
import tech.kayys.andalus.knowledge.exchange.proof.*;
import tech.kayys.andalus.knowledge.exchange.validity.*;
import tech.kayys.andalus.knowledge.exchange.lease.*;
import tech.kayys.andalus.knowledge.exchange.recovery.*;


import java.time.Instant;

public final class
DefaultKnowledgeAnswerResolutionMemoryService
        implements KnowledgeAnswerResolutionMemoryService {

    private final KnowledgeAnswerResolutionMemoryStore store;

    private final KnowledgeAnswerResolutionKeyFingerprinter
            fingerprinter;

    public DefaultKnowledgeAnswerResolutionMemoryService(
            KnowledgeAnswerResolutionMemoryStore store) {

        this(
                store,
                new Sha256KnowledgeAnswerResolutionKeyFingerprinter()
        );
    }

    public DefaultKnowledgeAnswerResolutionMemoryService(
            KnowledgeAnswerResolutionMemoryStore store,
            KnowledgeAnswerResolutionKeyFingerprinter
                    fingerprinter) {

        this.store = store;
        this.fingerprinter = fingerprinter;
    }

    @Override
    public KnowledgeAnswerResolutionCacheLookup lookup(
            KnowledgeAnswerResolutionKey key,
            Instant now) {

        String fingerprint =
                fingerprinter.fingerprint(key);

        var entry =
                store.get(fingerprint);

        if (entry.isEmpty()) {

            return new KnowledgeAnswerResolutionCacheLookup(
                    KnowledgeAnswerResolutionCacheStatus.MISS,
                    null,
                    "No cached resolution"
            );
        }

        var value = entry.get();

        if (value.invalidated()) {

            return new KnowledgeAnswerResolutionCacheLookup(
                    KnowledgeAnswerResolutionCacheStatus
                            .INVALIDATED,
                    value,
                    "Resolution explicitly invalidated"
            );
        }

        if (!value.activeAt(now)) {

            return new KnowledgeAnswerResolutionCacheLookup(
                    KnowledgeAnswerResolutionCacheStatus.STALE,
                    value,
                    "Resolution expired"
            );
        }

        return new KnowledgeAnswerResolutionCacheLookup(
                KnowledgeAnswerResolutionCacheStatus.HIT,
                value,
                "Resolution cache hit"
        );
    }

    @Override
    public void remember(
            KnowledgeAnswerResolutionMemoryEntry entry) {

        store.save(entry);
    }

    @Override
    public void invalidate(
            KnowledgeAnswerResolutionKey key) {

        String fingerprint =
                fingerprinter.fingerprint(key);

        store.invalidate(fingerprint);
    }
}
