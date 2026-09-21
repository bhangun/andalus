package tech.kayys.andalus.knowledge.exchange.recovery;

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
import java.util.List;

public final class DefaultKnowledgeAnswerResolutionRevalidationEngine
        implements KnowledgeAnswerResolutionRevalidationEngine {

    private final KnowledgeAnswerResolutionFreshnessService
            freshnessService;

    private final KnowledgeAnswerResolutionDependencyValidator
            dependencyValidator;

    private final KnowledgeAnswerResolutionSnapshotValidator
            snapshotValidator;

    private final KnowledgeAnswerResolutionAttestationValidator
            attestationValidator;

    private final KnowledgeAnswerResolutionLeaseStore leaseStore;

    public DefaultKnowledgeAnswerResolutionRevalidationEngine(
            KnowledgeAnswerResolutionFreshnessService freshnessService,
            KnowledgeAnswerResolutionDependencyValidator dependencyValidator,
            KnowledgeAnswerResolutionSnapshotValidator snapshotValidator,
            KnowledgeAnswerResolutionAttestationValidator attestationValidator,
            KnowledgeAnswerResolutionLeaseStore leaseStore) {

        this.freshnessService = freshnessService;
        this.dependencyValidator = dependencyValidator;
        this.snapshotValidator = snapshotValidator;
        this.attestationValidator = attestationValidator;
        this.leaseStore = leaseStore;
    }

    @Override
    public KnowledgeAnswerResolutionRevalidationResult revalidate(
            KnowledgeAnswerResolutionRevalidationRequest request,
            KnowledgeAnswerResolutionRevalidationContext context) {

        Instant now = context.effectiveAt();

        var leaseOptional = leaseStore.find(
                request.consensusId(),
                request.keyFingerprint());

        if (leaseOptional.isEmpty()) {

            return failed(
                    request,
                    KnowledgeAnswerResolutionRevalidationStatus.FAILED,
                    "No lease found");
        }

        var lease = leaseOptional.get();

        var freshnessPolicy = new KnowledgeAnswerResolutionFreshnessPolicy(
                context.policy().revalidationWindow(),
                context.policy().renewalWindow(),
                java.time.Duration.ofSeconds(30),
                java.time.Duration.ofSeconds(5),
                1,
                context.policy().requireParticipantLiveness(),
                true,
                context.policy().allowGracePeriodUse(),
                false
        );

        var freshness =
                freshnessService.evaluate(
                        lease,
                        context.participantRuntimeIds(),
                        freshnessPolicy,
                        now);

        boolean dependenciesValid =
                !context.policy().requireDependencyValidation()
                        || dependencyValidator.validate(
                        request.keyFingerprint(),
                        context.tenantId(),
                        context.workspaceId(),
                        context.projectId());

        boolean snapshotValid =
                !context.policy().requireSnapshotValidation()
                        || snapshotValidator.validate(
                        request.consensusId(),
                        request.keyFingerprint(),
                        context.tenantId(),
                        context.workspaceId(),
                        context.projectId());

        boolean attestationValid =
                !context.policy().requireAttestation()
                        || attestationValidator.validate(
                        request.consensusId(),
                        request.keyFingerprint());

        boolean participantsLive =
                !context.policy().requireParticipantLiveness()
                        || freshness.liveness()
                        == KnowledgeAnswerResolutionLivenessStatus.LIVE;

        boolean quorumValid =
                !context.policy().requireQuorum()
                        || freshness.liveParticipantCount() >= 1;

        List<String> diagnostics = new ArrayList<>(
                freshness.diagnostics());

        if (!dependenciesValid) {
            diagnostics.add("Dependencies are no longer valid");
        }

        if (!snapshotValid) {
            diagnostics.add("Decision snapshot is no longer valid");
        }

        if (!attestationValid) {
            diagnostics.add("Consensus attestation is invalid");
        }

        if (!participantsLive) {
            diagnostics.add("Required participants are not live");
        }

        if (!quorumValid) {
            diagnostics.add("Live quorum is unavailable");
        }

        boolean valid =
                dependenciesValid
                        && snapshotValid
                        && attestationValid
                        && participantsLive
                        && quorumValid;

        boolean usable =
                valid
                        && freshness.status()
                        == KnowledgeAnswerResolutionFreshnessStatus.FRESH;

        KnowledgeAnswerResolutionRevalidationStatus status;

        if (!valid) {

            status =
                    KnowledgeAnswerResolutionRevalidationStatus.INVALIDATED;

        } else if (usable) {

            status =
                    KnowledgeAnswerResolutionRevalidationStatus.REVALIDATED;

        } else {

            status =
                    KnowledgeAnswerResolutionRevalidationStatus.REQUIRED;
        }

        return new KnowledgeAnswerResolutionRevalidationResult(
                status,
                request.consensusId(),
                request.keyFingerprint(),
                freshness.status(),
                freshness.status(),
                dependenciesValid,
                snapshotValid,
                participantsLive,
                quorumValid,
                attestationValid,
                usable,
                now,
                diagnostics
        );
    }

    private KnowledgeAnswerResolutionRevalidationResult failed(
            KnowledgeAnswerResolutionRevalidationRequest request,
            KnowledgeAnswerResolutionRevalidationStatus status,
            String diagnostic) {

        return new KnowledgeAnswerResolutionRevalidationResult(
                status,
                request.consensusId(),
                request.keyFingerprint(),
                KnowledgeAnswerResolutionFreshnessStatus.UNKNOWN,
                KnowledgeAnswerResolutionFreshnessStatus.UNKNOWN,
                false,
                false,
                false,
                false,
                false,
                false,
                request.requestedAt(),
                List.of(diagnostic)
        );
    }
}
