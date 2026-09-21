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
import java.util.UUID;

public final class
DefaultKnowledgeAnswerResolutionAttestationService
        implements
        KnowledgeAnswerResolutionAttestationService {

    private final
    KnowledgeAnswerResolutionAttestationCanonicalizer
            canonicalizer;

    private final
    KnowledgeAnswerResolutionAttestationSigner signer;

    public
    DefaultKnowledgeAnswerResolutionAttestationService(
            KnowledgeAnswerResolutionAttestationCanonicalizer
                    canonicalizer,
            KnowledgeAnswerResolutionAttestationSigner signer) {

        this.canonicalizer = canonicalizer;
        this.signer = signer;
    }

    @Override
    public KnowledgeAnswerResolutionAttestation attest(
            KnowledgeAnswerResolutionConsensusProposal proposal,
            KnowledgeAnswerResolutionConsensusResult result,
            String runtimeId) {

        if (result.status()
                != KnowledgeAnswerResolutionConsensusStatus
                        .CONSENSUS_REACHED) {

            throw new IllegalArgumentException(
                    "Only reached consensus may be attested"
            );
        }

        byte[] payload =
                canonicalizer.canonicalize(
                        proposal.consensusId(),
                        proposal.keyFingerprint(),
                        result.winningResolutionFingerprint(),
                        result.winningDependencyFingerprint(),
                        runtimeId,
                        signer.keyReference(),
                        signer.algorithm()
                );

        byte[] signature =
                signer.sign(payload);

        return new KnowledgeAnswerResolutionAttestation(
                UUID.randomUUID().toString(),
                proposal.consensusId(),
                proposal.keyFingerprint(),
                result.winningResolutionFingerprint(),
                result.winningDependencyFingerprint(),
                runtimeId,
                signer.keyReference(),
                signer.algorithm(),
                signature,
                Instant.now(),
                proposal.expiresAt(),
                java.util.Map.of()
        );
    }
}
