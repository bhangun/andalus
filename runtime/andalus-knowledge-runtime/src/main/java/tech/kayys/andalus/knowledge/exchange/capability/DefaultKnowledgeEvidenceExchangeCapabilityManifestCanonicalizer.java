package tech.kayys.andalus.knowledge.exchange.capability;

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


import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class DefaultKnowledgeEvidenceExchangeCapabilityManifestCanonicalizer
        implements KnowledgeEvidenceExchangeCapabilityManifestCanonicalizer {

    @Override
    public byte[] canonicalize(
            KnowledgeEvidenceExchangeCapabilityManifest manifest
    ) {

        String capabilities =
                manifest.capabilities()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        c -> c.type().name()
                                )
                        )
                        .map(capability ->
                                capability.type().name()
                                        + ":"
                                        + capability.supported()
                                        + ":"
                                        + capability.algorithms()
                                                .stream()
                                                .sorted()
                                                .collect(
                                                        Collectors.joining(",")
                                                )
                                        + ":"
                                        + capability.formats()
                                                .stream()
                                                .sorted()
                                                .collect(
                                                        Collectors.joining(",")
                                                )
                                        + ":"
                                        + capability.maxArtifactBytes()
                                        + ":"
                                        + capability.required()
                        )
                        .collect(Collectors.joining("\n"));

        String canonical = String.join(
                "\n",
                manifest.runtimeId(),
                nullSafe(manifest.identityVersion()),
                nullSafe(manifest.identityFingerprint()),
                manifest.protocolVersion().toString(),
                capabilities,
                manifest.issuedAt() == null
                        ? ""
                        : manifest.issuedAt().toString(),
                manifest.expiresAt() == null
                        ? ""
                        : manifest.expiresAt().toString()
        );

        return canonical.getBytes(
                StandardCharsets.UTF_8
        );
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
