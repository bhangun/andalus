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

public final class InMemoryKnowledgeEvidenceTransferSessionStore
        implements KnowledgeEvidenceTransferSessionStore {

    private final ConcurrentMap<
            String,
            KnowledgeEvidenceTransferSession
            > sessions =
            new ConcurrentHashMap<>();

    @Override
    public void create(
            KnowledgeEvidenceTransferSession session
    ) {

        var existing =
                sessions.putIfAbsent(
                        session.transferId(),
                        session
                );

        if (existing != null) {

            throw new IllegalStateException(
                    "Transfer already exists: "
                            + session.transferId()
            );
        }
    }

    @Override
    public void update(
            KnowledgeEvidenceTransferSession session
    ) {

        sessions.compute(
                session.transferId(),
                (id, previous) -> {

                    if (previous == null) {

                        throw new IllegalStateException(
                                "Unknown transfer: " + id
                        );
                    }

                    return session;
                }
        );
    }

    @Override
    public Optional<
            KnowledgeEvidenceTransferSession
            > find(
                    String transferId
            ) {

        return Optional.ofNullable(
                sessions.get(transferId)
        );
    }
}
