package tech.kayys.andalus.knowledge.exchange.identity;

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


import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;

public final class Sha256KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter
        implements KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter {

    private final KnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer
            canonicalizer;

    public Sha256KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter(
            KnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer
                    canonicalizer
    ) {
        this.canonicalizer =
                Objects.requireNonNull(canonicalizer);
    }

    @Override
    public String fingerprint(
            KnowledgeEvidenceExchangeRuntimeIdentity identity
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(
                            canonicalizer.canonicalize(identity)
                    )
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to fingerprint runtime identity",
                    e
            );
        }
    }
}
