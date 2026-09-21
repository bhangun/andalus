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


public final class DefaultKnowledgeEvidenceTransferVerifier
        implements KnowledgeEvidenceTransferVerifier {

    @Override
    public void verify(
            KnowledgeEvidenceTransferSession session,
            KnowledgeEvidenceTransferSource source
    ) {

        if (session.totalLength() >= 0 &&
                session.currentOffset()
                        != session.totalLength()) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Transfer incomplete: "
                            + session.currentOffset()
                            + "/"
                            + session.totalLength()
            );
        }

        if (session.artifactFingerprint() != null &&
                source.fingerprint() != null &&
                !session.artifactFingerprint().equals(
                        source.fingerprint()
                )) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Artifact fingerprint mismatch"
            );
        }

        if (session.merkleRoot() != null &&
                source.merkleRoot() != null &&
                !session.merkleRoot().equals(
                        source.merkleRoot()
                )) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Merkle root mismatch"
            );
        }
    }
}
