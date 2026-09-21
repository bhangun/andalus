package tech.kayys.andalus.knowledge.exchange.attestation;

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
import java.time.Instant;
import java.util.List;

public final class
KnowledgeAnswerResolutionConsensusCertificateFactory {

    public KnowledgeAnswerResolutionConsensusCertificate create(
            KnowledgeAnswerResolutionConsensusProposal proposal,
            KnowledgeAnswerResolutionConsensusResult result,
            List<String> agreeingRuntimeIds,
            Instant now) {

        if (result.status()
                != KnowledgeAnswerResolutionConsensusStatus
                        .CONSENSUS_REACHED) {

            throw new IllegalArgumentException(
                    "Cannot create certificate without consensus"
            );
        }

        String canonical =
                proposal.consensusId()
                + "|"
                + proposal.keyFingerprint()
                + "|"
                + result.winningResolutionFingerprint()
                + "|"
                + result.winningDependencyFingerprint()
                + "|"
                + String.join(
                        ",",
                        agreeingRuntimeIds
                );

        String fingerprint =
                sha256(canonical);

        return new KnowledgeAnswerResolutionConsensusCertificate(
                java.util.UUID.randomUUID().toString(),
                proposal.consensusId(),
                proposal.keyFingerprint(),
                result.winningResolutionFingerprint(),
                result.winningDependencyFingerprint(),
                proposal.participantRuntimeIds(),
                agreeingRuntimeIds,
                result.requiredVotes(),
                now,
                proposal.expiresAt(),
                fingerprint,
                java.util.Map.of()
        );
    }

    private String sha256(String value) {

        try {

            byte[] digest =
                    MessageDigest
                            .getInstance("SHA-256")
                            .digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            StringBuilder builder =
                    new StringBuilder();

            for (byte b : digest) {

                builder.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return "sha256:" + builder;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to fingerprint consensus certificate",
                    e
            );
        }
    }
}
