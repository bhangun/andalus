package tech.kayys.andalus.knowledge.exchange.replication;

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

public final class DefaultKnowledgeEvidenceArtifactReplicationVerifier
        implements KnowledgeEvidenceArtifactReplicationVerifier {

    @Override
    public void verify(
            KnowledgeEvidenceArtifact artifact,
            String expectedFingerprint,
            String expectedMerkleRoot
    ) {
        if (expectedFingerprint != null &&
                !expectedFingerprint.equals(
                        artifact.metadata().artifactId().value()
                )) {
            throw new KnowledgeEvidenceExchangeTransportException(
                    "Replicated artifact identity mismatch"
            );
        }

        if (expectedMerkleRoot != null &&
                artifact.metadata() != null &&
                artifact.metadata().metadata() != null) {

            String actual =
                    artifact.metadata()
                            .metadata()
                            .get("merkleRoot");

            if (actual != null &&
                    !expectedMerkleRoot.equals(actual)) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Replicated Merkle root mismatch"
                );
            }
        }
    }
}
