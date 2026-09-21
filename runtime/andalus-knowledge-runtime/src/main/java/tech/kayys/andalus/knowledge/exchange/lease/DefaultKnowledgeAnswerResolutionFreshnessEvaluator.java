package tech.kayys.andalus.knowledge.exchange.lease;

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

public final class DefaultKnowledgeAnswerResolutionFreshnessEvaluator
        implements KnowledgeAnswerResolutionFreshnessEvaluator {

    private final KnowledgeAnswerResolutionHeartbeatStore heartbeatStore;

    public DefaultKnowledgeAnswerResolutionFreshnessEvaluator(
            KnowledgeAnswerResolutionHeartbeatStore heartbeatStore) {

        this.heartbeatStore = heartbeatStore;
    }

    @Override
    public KnowledgeAnswerResolutionFreshnessDecision evaluate(
            KnowledgeAnswerResolutionLease lease,
            List<String> participantRuntimeIds,
            KnowledgeAnswerResolutionFreshnessPolicy policy,
            Instant now) {

        List<String> diagnostics = new ArrayList<>();
        List<String> liveRuntimeIds = new ArrayList<>();

        int participantCount = participantRuntimeIds.size();

        for (String runtimeId : participantRuntimeIds) {

            var heartbeat = heartbeatStore.find(runtimeId);

            if (heartbeat.isPresent()
                    && heartbeat.get().aliveAt(now)) {

                liveRuntimeIds.add(runtimeId);
            }
        }

        int liveCount = liveRuntimeIds.size();

        KnowledgeAnswerResolutionLivenessStatus liveness;

        if (liveCount >= policy.minimumLiveParticipants()) {
            liveness = KnowledgeAnswerResolutionLivenessStatus.LIVE;
        } else if (liveCount > 0) {
            liveness = KnowledgeAnswerResolutionLivenessStatus.DEGRADED;
        } else {
            liveness = KnowledgeAnswerResolutionLivenessStatus.OFFLINE;
        }

        Instant leaseExpiry = lease.expiresAt();
        Instant graceExpiry = leaseExpiry.plus(policy.gracePeriod());

        KnowledgeAnswerResolutionFreshnessStatus status;

        if (policy.requireLease()
                && now.isBefore(lease.issuedAt())) {

            status = KnowledgeAnswerResolutionFreshnessStatus.UNKNOWN;

            diagnostics.add("Lease is not yet effective");

        } else if (now.isBefore(leaseExpiry)) {

            if (policy.requireParticipantLiveness()
                    && liveCount < policy.minimumLiveParticipants()) {

                status = KnowledgeAnswerResolutionFreshnessStatus.STALE;

                diagnostics.add(
                        "Insufficient live consensus participants");

            } else {

                status = KnowledgeAnswerResolutionFreshnessStatus.FRESH;
            }

        } else if (policy.allowGracePeriod()
                && now.isBefore(graceExpiry)) {

            if (policy.allowOfflineGrace()
                    || liveCount > 0) {

                status =
                        KnowledgeAnswerResolutionFreshnessStatus.GRACE_PERIOD;

                diagnostics.add(
                        "Consensus lease expired but grace period remains");
            } else {

                status =
                        KnowledgeAnswerResolutionFreshnessStatus.STALE;

                diagnostics.add(
                        "Lease expired and participants are offline");
            }

        } else {

            status = KnowledgeAnswerResolutionFreshnessStatus.EXPIRED;

            diagnostics.add("Consensus lease expired");
        }

        return new KnowledgeAnswerResolutionFreshnessDecision(
                status,
                liveness,
                lease.consensusId(),
                lease.keyFingerprint(),
                participantCount,
                liveCount,
                policy.minimumLiveParticipants(),
                now,
                leaseExpiry,
                graceExpiry,
                liveRuntimeIds,
                diagnostics
        );
    }
}
