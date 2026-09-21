package tech.kayys.andalus.knowledge.exchange.factuality;

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

public final class DefaultKnowledgeVerifiedAnswerArtifactVerifier
        implements KnowledgeVerifiedAnswerArtifactVerifier {

    private final KnowledgeVerifiedAnswerArtifactFingerprinter
            fingerprinter;

    private final KnowledgeAnswerProvenanceValidator
            provenanceValidator;

    public DefaultKnowledgeVerifiedAnswerArtifactVerifier() {

        this(
                new Sha256KnowledgeVerifiedAnswerArtifactFingerprinter(),
                new DefaultKnowledgeAnswerProvenanceValidator()
        );
    }

    public DefaultKnowledgeVerifiedAnswerArtifactVerifier(
            KnowledgeVerifiedAnswerArtifactFingerprinter fingerprinter,
            KnowledgeAnswerProvenanceValidator provenanceValidator) {

        this.fingerprinter = fingerprinter;
        this.provenanceValidator = provenanceValidator;
    }

    @Override
    public KnowledgeVerifiedAnswerExchangeVerificationResult verify(
            KnowledgeVerifiedAnswerArtifact artifact,
            KnowledgeVerifiedAnswerExchangeRequest request) {

        var issues = new ArrayList<
                KnowledgeVerifiedAnswerVerificationIssue>();

        if (artifact == null) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "MISSING_ARTIFACT",
                            "Answer artifact is missing",
                            true
                    )
            );

            return result(
                    KnowledgeVerifiedAnswerExchangeVerificationStatus
                            .FAILED,
                    null,
                    null,
                    null,
                    false,
                    false,
                    false,
                    false,
                    false,
                    issues
            );
        }

        if (request.expiredAt(Instant.now())) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "REQUEST_EXPIRED",
                            "Exchange request has expired",
                            true
                    )
            );
        }

        if (!artifact.artifactId()
                .equals(request.artifactId())) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "ARTIFACT_ID_MISMATCH",
                            "Artifact identity does not match request",
                            true
                    )
            );
        }

        String fingerprint =
                fingerprinter.fingerprint(artifact);

        boolean artifactIntegrity =
                fingerprint.equals(
                        artifact.artifactId()
                );

        if (!artifactIntegrity) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "ARTIFACT_FINGERPRINT_MISMATCH",
                            "Artifact fingerprint mismatch",
                            true
                    )
            );
        }

        var provenance =
                provenanceValidator.validate(
                        artifact.provenance()
                );

        boolean provenanceValid =
                provenance.valid();

        if (!provenanceValid) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "PROVENANCE_INVALID",
                            "Artifact provenance graph is invalid",
                            true
                    )
            );
        }

        if (request.requireSnapshot()
                && (artifact.snapshotId() == null
                || artifact.snapshotId().isBlank())) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "SNAPSHOT_REQUIRED",
                            "A decision snapshot is required",
                            true
                    )
            );
        }

        boolean accepted =
                issues.stream()
                        .noneMatch(
                                KnowledgeVerifiedAnswerVerificationIssue
                                        ::blocking
                        );

        return result(
                accepted
                        ? KnowledgeVerifiedAnswerExchangeVerificationStatus
                                .VERIFIED
                        : KnowledgeVerifiedAnswerExchangeVerificationStatus
                                .INTEGRITY_FAILED,
                artifact.artifactId(),
                artifact.responseId(),
                artifact.snapshotId(),
                artifactIntegrity,
                provenanceValid,
                artifact.snapshotId() != null,
                artifactIntegrity,
                false,
                issues
        );
    }

    private KnowledgeVerifiedAnswerExchangeVerificationResult result(
            KnowledgeVerifiedAnswerExchangeVerificationStatus status,
            String artifactId,
            String responseId,
            String snapshotId,
            boolean authorization,
            boolean provenance,
            boolean snapshot,
            boolean integrity,
            boolean seal,
            java.util.List<
                    KnowledgeVerifiedAnswerVerificationIssue> issues) {

        return new KnowledgeVerifiedAnswerExchangeVerificationResult(
                status,
                artifactId,
                responseId,
                snapshotId,
                artifactId,
                authorization,
                provenance,
                snapshot,
                integrity,
                seal,
                issues,
                java.util.Map.of()
        );
    }
}
