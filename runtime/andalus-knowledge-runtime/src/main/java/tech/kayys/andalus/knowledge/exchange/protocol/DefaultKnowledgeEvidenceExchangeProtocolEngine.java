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


import java.time.Instant;
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeProtocolEngine
        implements KnowledgeEvidenceExchangeProtocolEngine {

    private final KnowledgeEvidenceExchangeProtocolStateMachine
            stateMachine;

    private KnowledgeEvidenceExchangeProtocolSession session;

    public DefaultKnowledgeEvidenceExchangeProtocolEngine(
            KnowledgeEvidenceExchangeProtocolStateMachine stateMachine,
            KnowledgeEvidenceExchangeProtocolSession session
    ) {

        this.stateMachine =
                Objects.requireNonNull(stateMachine);

        this.session =
                Objects.requireNonNull(session);
    }

    @Override
    public KnowledgeEvidenceExchangeProtocolSession session() {
        return session;
    }

    @Override
    public KnowledgeEvidenceExchangeProtocolState state() {
        return stateMachine.state();
    }

    @Override
    public synchronized void send(
            KnowledgeEvidenceExchangeProtocolMessageType type,
            Instant now
    ) {

        validateTime(now);

        stateMachine.transition(type);

        session = updateState(
                stateMachine.state()
        );
    }

    @Override
    public synchronized void receive(
            KnowledgeEvidenceExchangeProtocolMessageType type,
            Instant now
    ) {

        validateTime(now);

        stateMachine.transition(type);

        session = updateState(
                stateMachine.state()
        );
    }

    @Override
    public synchronized void close(
            String reason,
            Instant now
    ) {

        validateTime(now);

        if (stateMachine.canAccept(
                KnowledgeEvidenceExchangeProtocolMessageType.CLOSE
        )) {

            stateMachine.transition(
                    KnowledgeEvidenceExchangeProtocolMessageType.CLOSE
            );

            session = updateState(
                    KnowledgeEvidenceExchangeProtocolState.CLOSED
            );
        }
    }

    private void validateTime(Instant now) {

        if (session.expiresAt() != null &&
                now.isAfter(session.expiresAt())) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Protocol session expired"
            );
        }
    }

    private KnowledgeEvidenceExchangeProtocolSession updateState(
            KnowledgeEvidenceExchangeProtocolState state
    ) {

        return new KnowledgeEvidenceExchangeProtocolSession(
                session.sessionId(),
                session.localRuntimeId(),
                session.remoteRuntimeId(),
                state,
                session.negotiatedProtocolVersion(),
                session.localNonce(),
                session.remoteNonce(),
                session.handshakeId(),
                session.localIdentityFingerprint(),
                session.remoteIdentityFingerprint(),
                session.localCapabilityFingerprint(),
                session.remoteCapabilityFingerprint(),
                session.sessionFingerprint(),
                session.createdAt(),
                session.expiresAt(),
                session.metadata()
        );
    }
}
