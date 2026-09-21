package tech.kayys.andalus.knowledge.exchange.transfer;

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


import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceTransferCheckpointStore
        implements KnowledgeEvidenceTransferCheckpointStore {

    private final ConcurrentMap<
            String,
            KnowledgeEvidenceTransferCheckpoint
            > checkpoints =
            new ConcurrentHashMap<>();

    @Override
    public void save(
            KnowledgeEvidenceTransferCheckpoint checkpoint
    ) {

        checkpoints.put(
                checkpoint.transferId(),
                checkpoint
        );
    }

    @Override
    public Optional<
            KnowledgeEvidenceTransferCheckpoint
            > find(
                    String transferId
            ) {

        return Optional.ofNullable(
                checkpoints.get(transferId)
        );
    }

    @Override
    public void delete(
            String transferId
    ) {

        checkpoints.remove(transferId);
    }
}
