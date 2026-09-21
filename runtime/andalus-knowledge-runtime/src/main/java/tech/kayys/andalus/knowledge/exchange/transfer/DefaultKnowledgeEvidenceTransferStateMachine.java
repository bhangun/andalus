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


public final class DefaultKnowledgeEvidenceTransferStateMachine
        implements KnowledgeEvidenceTransferStateMachine {

    @Override
    public void transition(
            KnowledgeEvidenceTransferSession session,
            KnowledgeEvidenceTransferState target
    ) {

        var current = session.state();

        boolean valid =
                switch (current) {

                    case CREATED ->
                            target == KnowledgeEvidenceTransferState.AUTHORIZING
                                    || target == KnowledgeEvidenceTransferState.CANCELLED;

                    case AUTHORIZING ->
                            target == KnowledgeEvidenceTransferState.AUTHORIZED
                                    || target == KnowledgeEvidenceTransferState.FAILED;

                    case AUTHORIZED ->
                            target == KnowledgeEvidenceTransferState.NEGOTIATING
                                    || target == KnowledgeEvidenceTransferState.CANCELLED;

                    case NEGOTIATING ->
                            target == KnowledgeEvidenceTransferState.TRANSFERRING
                                    || target == KnowledgeEvidenceTransferState.FAILED;

                    case TRANSFERRING ->
                            target == KnowledgeEvidenceTransferState.TRANSFERRING
                                    || target == KnowledgeEvidenceTransferState.PAUSED
                                    || target == KnowledgeEvidenceTransferState.VERIFYING
                                    || target == KnowledgeEvidenceTransferState.CANCELLED
                                    || target == KnowledgeEvidenceTransferState.FAILED;

                    case PAUSED ->
                            target == KnowledgeEvidenceTransferState.TRANSFERRING
                                    || target == KnowledgeEvidenceTransferState.CANCELLED
                                    || target == KnowledgeEvidenceTransferState.EXPIRED;

                    case VERIFYING ->
                            target == KnowledgeEvidenceTransferState.COMPLETED
                                    || target == KnowledgeEvidenceTransferState.FAILED;

                    default -> false;
                };

        if (!valid) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Invalid transfer transition: "
                            + current
                            + " -> "
                            + target
            );
        }
    }
}
