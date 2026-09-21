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


import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeFrameIntegrityVerifier
        implements KnowledgeEvidenceExchangeFrameIntegrityVerifier {

    private final KnowledgeEvidenceExchangePayloadFingerprinter
            fingerprinter;

    public DefaultKnowledgeEvidenceExchangeFrameIntegrityVerifier(
            KnowledgeEvidenceExchangePayloadFingerprinter fingerprinter
    ) {
        this.fingerprinter =
                Objects.requireNonNull(fingerprinter);
    }

    @Override
    public void verify(
            KnowledgeEvidenceExchangeFrame frame
    ) {

        if (frame.payloadFingerprint() == null) {
            return;
        }

        String actual =
                fingerprinter.fingerprint(
                        frame.payload()
                );

        if (!actual.equals(
                frame.payloadFingerprint()
        )) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Frame payload integrity failure"
            );
        }
    }
}
