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


public final class KnowledgeVerifiedAnswerExchangeVerificationService {

    private final KnowledgeVerifiedAnswerArtifactVerifier
            artifactVerifier;

    private final KnowledgeVerifiedAnswerAuthorizer
            authorizer;

    public KnowledgeVerifiedAnswerExchangeVerificationService(
            KnowledgeVerifiedAnswerArtifactVerifier artifactVerifier,
            KnowledgeVerifiedAnswerAuthorizer authorizer) {

        this.artifactVerifier = artifactVerifier;
        this.authorizer = authorizer;
    }

    public KnowledgeVerifiedAnswerExchangeVerificationResult verify(
            KnowledgeVerifiedAnswerArtifact artifact,
            KnowledgeVerifiedAnswerExchangeRequest request) {

        KnowledgeVerifiedAnswerAuthorizationContext authContext =
                new KnowledgeVerifiedAnswerAuthorizationContext(
                        request.requestingRuntimeId(),
                        artifact.artifactId(),
                        request.tenantId(),
                        request.workspaceId(),
                        request.projectId(),
                        artifact.agentId(),
                        java.util.Set.of(),
                        java.time.Instant.now(),
                        java.util.Map.of(
                                "artifactTenantId",
                                artifact.tenantId(),
                                "artifactWorkspaceId",
                                artifact.workspaceId(),
                                "artifactProjectId",
                                artifact.projectId()
                        )
                );

        KnowledgeVerifiedAnswerAuthorizationDecision authorization =
                authorizer.authorize(authContext);

        if (authorization
                instanceof KnowledgeVerifiedAnswerAuthorizationDecision.Deny deny) {

            return new KnowledgeVerifiedAnswerExchangeVerificationResult(
                    KnowledgeVerifiedAnswerExchangeVerificationStatus
                            .AUTHORIZATION_FAILED,
                    artifact.artifactId(),
                    artifact.responseId(),
                    artifact.snapshotId(),
                    null,
                    false,
                    false,
                    false,
                    false,
                    false,
                    java.util.List.of(
                            new KnowledgeVerifiedAnswerVerificationIssue(
                                    "AUTHORIZATION_DENIED",
                                    deny.reason(),
                                    true
                            )
                    ),
                    java.util.Map.of(
                            "policyId",
                            deny.policyId()
                    )
            );
        }

        return artifactVerifier.verify(
                artifact,
                request
        );
    }
}
