package tech.kayys.andalus.knowledge.exchange.sync;

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


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class Sha256KnowledgeEvidenceArtifactInventoryFingerprinter
        implements KnowledgeEvidenceArtifactInventoryFingerprinter {

    @Override
    public String fingerprint(
            KnowledgeEvidenceArtifactInventorySnapshot snapshot
    ) {

        var entries =
                snapshot.entries()
                        .stream()
                        .sorted(
                                java.util.Comparator.comparing(
                                        KnowledgeEvidenceArtifactInventoryEntry
                                                ::artifactId
                                )
                        )
                        .map(
                                e ->
                                        String.join(
                                                "|",
                                                e.artifactId(),
                                                Long.toString(e.size()),
                                                String.valueOf(
                                                        e.fingerprint()
                                                ),
                                                String.valueOf(
                                                        e.merkleRoot()
                                                ),
                                                Boolean.toString(
                                                        e.revoked()
                                                )
                                        )
                        )
                        .toList();

        String canonical =
                String.join(
                        "\n",
                        entries
                );

        try {

            var digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return "sha256:" +
                    HexFormat.of().formatHex(
                            digest.digest(
                                    canonical.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            )
                    );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to fingerprint inventory",
                    e
            );
        }
    }
}
