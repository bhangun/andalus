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


import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class
InMemoryKnowledgeAnswerResolutionMemoryStore
        implements KnowledgeAnswerResolutionMemoryStore {

    private final ConcurrentMap<
            String,
            KnowledgeAnswerResolutionMemoryEntry
            > entries =
            new ConcurrentHashMap<>();

    @Override
    public void save(
            KnowledgeAnswerResolutionMemoryEntry entry) {

        entries.putIfAbsent(
                entry.keyFingerprint(),
                entry
        );
    }

    @Override
    public Optional<
            KnowledgeAnswerResolutionMemoryEntry> get(
            String keyFingerprint) {

        return Optional.ofNullable(
                entries.get(keyFingerprint)
        );
    }

    @Override
    public void invalidate(
            String keyFingerprint) {

        entries.computeIfPresent(
                keyFingerprint,
                (key, entry) ->
                        new KnowledgeAnswerResolutionMemoryEntry(
                                entry.memoryId(),
                                entry.keyFingerprint(),
                                entry.key(),
                                entry.resolution(),
                                entry.snapshotFingerprint(),
                                entry.evidenceFingerprint(),
                                entry.graphFingerprint(),
                                entry.createdAt(),
                                entry.expiresAt(),
                                true,
                                entry.metadata()
                        )
        );
    }

    @Override
    public void delete(
            String keyFingerprint) {

        entries.remove(keyFingerprint);
    }
}
