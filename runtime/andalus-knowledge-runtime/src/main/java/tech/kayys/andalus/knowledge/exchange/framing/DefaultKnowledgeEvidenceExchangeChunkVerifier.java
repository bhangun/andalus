package tech.kayys.andalus.knowledge.exchange.framing;

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


public final class DefaultKnowledgeEvidenceExchangeChunkVerifier
        implements KnowledgeEvidenceExchangeChunkVerifier {

    private final KnowledgeEvidenceExchangePayloadFingerprinter
            fingerprinter;

    public DefaultKnowledgeEvidenceExchangeChunkVerifier(
            KnowledgeEvidenceExchangePayloadFingerprinter
                    fingerprinter
    ) {
        this.fingerprinter = fingerprinter;
    }

    @Override
    public void verify(
            KnowledgeEvidenceExchangeChunk chunk
    ) {

        if (chunk.fingerprint() == null) {
            return;
        }

        String actual =
                fingerprinter.fingerprint(
                        chunk.data()
                );

        if (!actual.equals(
                chunk.fingerprint()
        )) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Chunk fingerprint mismatch"
            );
        }
    }
}
