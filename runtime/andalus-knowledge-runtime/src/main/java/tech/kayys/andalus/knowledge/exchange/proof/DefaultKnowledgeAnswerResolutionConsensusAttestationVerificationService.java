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


import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class
DefaultKnowledgeAnswerResolutionConsensusAttestationVerificationService
        implements
        KnowledgeAnswerResolutionConsensusAttestationVerificationService {

    private final
    KnowledgeAnswerResolutionAttestationCanonicalizer
            canonicalizer;

    private final
    KnowledgeAnswerResolutionAttestationVerifierResolver
            verifierResolver;

    public
    DefaultKnowledgeAnswerResolutionConsensusAttestationVerificationService(
            KnowledgeAnswerResolutionAttestationCanonicalizer
                    canonicalizer,
            KnowledgeAnswerResolutionAttestationVerifierResolver
                    verifierResolver) {

        this.canonicalizer = canonicalizer;
        this.verifierResolver = verifierResolver;
    }

    @Override
    public KnowledgeAnswerResolutionConsensusAttestationVerificationResult
    verify(
            KnowledgeAnswerResolutionConsensusAttestation certificate) {

        List<String> diagnostics =
                new ArrayList<>();

        if (certificate == null) {

            return failure(
                    "Certificate is null"
            );
        }

        if (certificate.expiresAt() != null
                && Instant.now()
                        .isAfter(certificate.expiresAt())) {

            return new KnowledgeAnswerResolutionConsensusAttestationVerificationResult(
                    KnowledgeAnswerResolutionAttestationStatus.EXPIRED,
                    certificate.attestations().size(),
                    0,
                    certificate.quorum(),
                    false,
                    false,
                    false,
                    List.of(),
                    List.of("Certificate expired")
            );
        }

        Set<String> runtimes =
                new HashSet<>();

        int valid = 0;

        boolean dependencyAgreement = true;
        String dependency =
                certificate.dependencyFingerprint();

        for (KnowledgeAnswerResolutionAttestation attestation
                : certificate.attestations()) {

            if (!certificate.consensusId()
                    .equals(attestation.consensusId())) {

                diagnostics.add(
                        "Consensus ID mismatch"
                );

                continue;
            }

            if (!certificate.keyFingerprint()
                    .equals(attestation.keyFingerprint())) {

                diagnostics.add(
                        "Key fingerprint mismatch"
                );

                continue;
            }

            if (!certificate.resolutionFingerprint()
                    .equals(attestation.resolutionFingerprint())) {

                diagnostics.add(
                        "Resolution fingerprint mismatch"
                );

                continue;
            }

            if (!java.util.Objects.equals(
                    dependency,
                    attestation.dependencyFingerprint())) {

                dependencyAgreement = false;

                diagnostics.add(
                        "Dependency fingerprint mismatch"
                );

                continue;
            }

            KnowledgeAnswerResolutionAttestationVerifier verifier =
                    verifierResolver.resolve(
                            attestation.algorithm(),
                            attestation.keyReference()
                    );

            if (verifier == null) {

                diagnostics.add(
                        "Unknown attestation key"
                );

                continue;
            }

            byte[] payload =
                    canonicalizer.canonicalize(
                            attestation.consensusId(),
                            attestation.keyFingerprint(),
                            attestation.resolutionFingerprint(),
                            attestation.dependencyFingerprint(),
                            attestation.runtimeId(),
                            attestation.keyReference(),
                            attestation.algorithm()
                    );

            if (!verifier.verify(
                    payload,
                    attestation.signature(),
                    attestation.keyReference())) {

                diagnostics.add(
                        "Invalid signature from "
                                + attestation.runtimeId()
                );

                continue;
            }

            if (!runtimes.add(
                    attestation.runtimeId())) {

                diagnostics.add(
                        "Duplicate runtime attestation: "
                                + attestation.runtimeId()
                );

                continue;
            }

            valid++;
        }

        boolean quorum =
                valid >= certificate.quorum();

        KnowledgeAnswerResolutionAttestationStatus status =
                quorum && dependencyAgreement
                        ? KnowledgeAnswerResolutionAttestationStatus
                                .VERIFIED
                        : KnowledgeAnswerResolutionAttestationStatus
                                .INVALID;

        return new KnowledgeAnswerResolutionConsensusAttestationVerificationResult(
                status,
                certificate.attestations().size(),
                valid,
                certificate.quorum(),
                quorum,
                valid > 0,
                dependencyAgreement,
                List.copyOf(runtimes),
                List.copyOf(diagnostics)
        );
    }

    private KnowledgeAnswerResolutionConsensusAttestationVerificationResult
    failure(String message) {

        return new KnowledgeAnswerResolutionConsensusAttestationVerificationResult(
                KnowledgeAnswerResolutionAttestationStatus.FAILED,
                0,
                0,
                0,
                false,
                false,
                false,
                List.of(),
                List.of(message)
        );
    }
}
