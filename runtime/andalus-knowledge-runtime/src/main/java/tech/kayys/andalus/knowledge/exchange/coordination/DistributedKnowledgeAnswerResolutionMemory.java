package tech.kayys.andalus.knowledge.exchange.coordination;

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
import java.util.List;

public final class
DistributedKnowledgeAnswerResolutionMemory {

    private final KnowledgeAnswerResolutionMemoryService
            localMemory;

    private final List<
            KnowledgeAnswerResolutionRemoteReplica>
            remotes;

    public DistributedKnowledgeAnswerResolutionMemory(
            KnowledgeAnswerResolutionMemoryService localMemory,
            List<
                    KnowledgeAnswerResolutionRemoteReplica>
                    remotes) {

        this.localMemory = localMemory;
        this.remotes = List.copyOf(remotes);
    }

    public KnowledgeAnswerResolutionMemoryEntry
    lookup(
            KnowledgeAnswerResolutionKey key) {

        var local =
                localMemory.lookup(
                        key,
                        Instant.now()
                );

        if (local.status()
                == KnowledgeAnswerResolutionCacheStatus.HIT) {

            return local.entry();
        }

        for (var remote : remotes) {

            try {

                var entry =
                        remote.fetch(
                                keyFingerprint(key)
                        );

                if (entry != null) {

                    return entry;
                }

            } catch (RuntimeException ignored) {
                // Continue to next replica.
            }
        }

        return null;
    }

    private String keyFingerprint(
            KnowledgeAnswerResolutionKey key) {

        return new
                Sha256KnowledgeAnswerResolutionKeyFingerprinter()
                .fingerprint(key);
    }
}
