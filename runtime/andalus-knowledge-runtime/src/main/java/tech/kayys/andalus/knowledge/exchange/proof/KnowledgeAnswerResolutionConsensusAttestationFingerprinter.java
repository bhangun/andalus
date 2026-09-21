package tech.kayys.andalus.knowledge.exchange.proof;

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
import tech.kayys.andalus.knowledge.exchange.coverage.*;
import tech.kayys.andalus.knowledge.exchange.gap.*;
import tech.kayys.andalus.knowledge.exchange.attribution.*;
import tech.kayys.andalus.knowledge.exchange.contradiction.*;
import tech.kayys.andalus.knowledge.exchange.factuality.*;
import tech.kayys.andalus.knowledge.exchange.uncertainty.*;
import tech.kayys.andalus.knowledge.exchange.compact.*;
import tech.kayys.andalus.knowledge.exchange.resolution.*;
import tech.kayys.andalus.knowledge.exchange.quorum.*;
import tech.kayys.andalus.knowledge.exchange.selection.*;
import tech.kayys.andalus.knowledge.exchange.coordination.*;
import tech.kayys.andalus.knowledge.exchange.attestation.*;
import tech.kayys.andalus.knowledge.exchange.proof.*;
import tech.kayys.andalus.knowledge.exchange.validity.*;
import tech.kayys.andalus.knowledge.exchange.lease.*;
import tech.kayys.andalus.knowledge.exchange.recovery.*;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class
KnowledgeAnswerResolutionConsensusAttestationFingerprinter {

    public String fingerprint(
            KnowledgeAnswerResolutionConsensusAttestation certificate) {

        String attestations =
                certificate.attestations()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        KnowledgeAnswerResolutionAttestation
                                                ::runtimeId
                                )
                        )
                        .map(
                                a ->
                                        a.runtimeId()
                                        + "|"
                                        + a.keyReference()
                                        + "|"
                                        + a.algorithm()
                                        + "|"
                                        + bytes(a.signature())
                        )
                        .collect(
                                Collectors.joining(";")
                        );

        String canonical =
                certificate.consensusId()
                + "|"
                + certificate.keyFingerprint()
                + "|"
                + certificate.resolutionFingerprint()
                + "|"
                + certificate.dependencyFingerprint()
                + "|"
                + certificate.participantSetFingerprint()
                + "|"
                + certificate.quorum()
                + "|"
                + attestations;

        return "sha256:" + sha256(canonical);
    }

    private String bytes(byte[] bytes) {

        StringBuilder result =
                new StringBuilder();

        for (byte value : bytes) {

            result.append(
                    String.format(
                            "%02x",
                            value
                    )
            );
        }

        return result.toString();
    }

    private String sha256(String value) {

        try {

            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : digest) {

                result.append(
                        String.format(
                                "%02x",
                                value
                        )
                );
            }

            return result.toString();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to fingerprint consensus attestation",
                    e
            );
        }
    }
}
