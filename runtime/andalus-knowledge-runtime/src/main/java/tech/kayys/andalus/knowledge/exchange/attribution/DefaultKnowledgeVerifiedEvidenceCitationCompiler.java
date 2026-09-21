package tech.kayys.andalus.knowledge.exchange.attribution;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DefaultKnowledgeVerifiedEvidenceCitationCompiler
        implements KnowledgeVerifiedEvidenceCitationCompiler {

    @Override
    public List<KnowledgeVerifiedEvidenceReference> compile(
            KnowledgeEvidenceAnswerVerification verification,
            KnowledgeEvidenceFusionResult fusionResult) {

        List<KnowledgeVerifiedEvidenceReference> result =
                new ArrayList<>();

        if (verification != null && verification.claims() != null) {
            for (var claimVerification : verification.claims()) {
                for (var evidenceId : claimVerification.supportingEvidenceIds()) {
                    if (fusionResult != null && fusionResult.selected() != null) {
                        fusionResult.selected()
                                .stream()
                                .filter(candidate -> candidate.evidence().knowledgeId().equals(evidenceId))
                                .findFirst()
                                .ifPresent(candidate -> {
                                    KnowledgeEvidenceReference evidence = candidate.evidence();
                                    Object artVal = evidence.metadata().get("artifactId");
                                    Object provVal = evidence.metadata().get("provenanceId");
                                    String artifactId = artVal != null ? artVal.toString() : "";
                                    String provenanceId = provVal != null ? provVal.toString() : "";

                                    result.add(
                                            new KnowledgeVerifiedEvidenceReference(
                                                    evidenceId,
                                                    evidence.knowledgeId(),
                                                    evidence.versionId(),
                                                    artifactId != null ? artifactId : "",
                                                    evidence.fragmentId(),
                                                    provenanceId != null ? provenanceId : "",
                                                    candidate.finalScore(),
                                                    candidate.authorityScore(),
                                                    candidate.trustScore(),
                                                    claimVerification.status() == KnowledgeEvidenceClaimStatus.SUPPORTED,
                                                    Map.of()
                                            )
                                    );
                                });
                    }
                }
            }
        }

        return result.stream()
                .distinct()
                .toList();
    }
}
