package tech.kayys.andalus.knowledge.exchange.protocol;

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

public final class InMemoryKnowledgeEvidenceExchangeProtocolSessionStore
        implements KnowledgeEvidenceExchangeProtocolSessionStore {

    private final ConcurrentMap<String,
            KnowledgeEvidenceExchangeProtocolSession> sessions =
            new ConcurrentHashMap<>();

    @Override
    public void save(
            KnowledgeEvidenceExchangeProtocolSession session
    ) {

        sessions.put(
                session.sessionId(),
                session
        );
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeProtocolSession> find(
            String sessionId
    ) {

        return Optional.ofNullable(
                sessions.get(sessionId)
        );
    }

    @Override
    public void remove(
            String sessionId
    ) {

        sessions.remove(sessionId);
    }
}
