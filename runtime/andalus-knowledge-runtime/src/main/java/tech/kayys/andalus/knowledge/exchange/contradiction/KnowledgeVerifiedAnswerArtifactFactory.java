package tech.kayys.andalus.knowledge.exchange.contradiction;

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

public final class KnowledgeVerifiedAnswerArtifactFactory {

    private final KnowledgeAnswerProvenanceService
            provenanceService;

    private final KnowledgeVerifiedResponseFingerprinter
            responseFingerprinter;

    private final KnowledgeVerifiedAnswerArtifactFingerprinter
            artifactFingerprinter;

    public KnowledgeVerifiedAnswerArtifactFactory() {

        this(
                new DefaultKnowledgeAnswerProvenanceService(),
                new Sha256KnowledgeVerifiedResponseFingerprinter(),
                new Sha256KnowledgeVerifiedAnswerArtifactFingerprinter()
        );
    }

    public KnowledgeVerifiedAnswerArtifactFactory(
            KnowledgeAnswerProvenanceService provenanceService,
            KnowledgeVerifiedResponseFingerprinter responseFingerprinter,
            KnowledgeVerifiedAnswerArtifactFingerprinter
                    artifactFingerprinter) {

        this.provenanceService = provenanceService;
        this.responseFingerprinter =
                responseFingerprinter;
        this.artifactFingerprinter =
                artifactFingerprinter;
    }

    public KnowledgeVerifiedAnswerArtifact create(
            KnowledgeVerifiedResponse response,
            KnowledgeEvidenceSemanticQuery query,
            KnowledgeEvidenceAnswerVerification verification,
            String snapshotId) {

        KnowledgeAnswerProvenanceGraph provenance =
                provenanceService.build(
                        response,
                        query,
                        verification
                );

        String responseFingerprint =
                responseFingerprinter.fingerprint(
                        response
                );

        String temporaryId =
                "answer:"
                        + UUID.randomUUID();

        KnowledgeVerifiedAnswerArtifact artifact =
                new KnowledgeVerifiedAnswerArtifact(
                        temporaryId,
                        response.metadata().responseId(),
                        response.metadata().executionId(),
                        response.metadata().agentId(),
                        response.metadata().tenantId(),
                        response.metadata().workspaceId(),
                        response.metadata().projectId(),
                        response,
                        provenance,
                        snapshotId,
                        responseFingerprint,
                        Instant.now(),
                        java.util.Map.of()
                );

        String fingerprint =
                artifactFingerprinter.fingerprint(
                        artifact
                );

        return new KnowledgeVerifiedAnswerArtifact(
                fingerprint,
                artifact.responseId(),
                artifact.executionId(),
                artifact.agentId(),
                artifact.tenantId(),
                artifact.workspaceId(),
                artifact.projectId(),
                artifact.response(),
                artifact.provenance(),
                artifact.snapshotId(),
                artifact.responseFingerprint(),
                artifact.createdAt(),
                artifact.metadata()
        );
    }
}
