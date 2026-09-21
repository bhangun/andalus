package tech.kayys.andalus.knowledge.exchange.compact;

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


import java.util.HashMap;

public final class DefaultKnowledgeAnswerArtifactConflictDetector
        implements KnowledgeAnswerArtifactConflictDetector {

    @Override
    public KnowledgeAnswerArtifactConflict detect(
            KnowledgeVerifiedAnswerArtifact left,
            KnowledgeVerifiedAnswerArtifact right) {

        var leftClaims =
                left.response().claims();

        var rightClaims =
                right.response().claims();

        var rightByText =
                new HashMap<String,
                        KnowledgeVerifiedClaim>();

        for (var claim : rightClaims) {

            rightByText.put(
                    normalize(claim.text()),
                    claim
            );
        }

        for (var claim : leftClaims) {

            var opposite =
                    rightByText.get(
                            normalize(claim.text())
                    );

            if (opposite == null) {
                continue;
            }

            if (claim.status()
                    != opposite.status()) {

                return new KnowledgeAnswerArtifactConflict(
                        java.util.UUID.randomUUID()
                                .toString(),
                        left.artifactId(),
                        right.artifactId(),
                        KnowledgeAnswerArtifactConflictType
                                .CLAIM_CONFLICT,
                        0.85,
                        "Same normalized claim has different verification status",
                        java.util.Map.of()
                );
            }
        }

        return new KnowledgeAnswerArtifactConflict(
                java.util.UUID.randomUUID()
                        .toString(),
                left.artifactId(),
                right.artifactId(),
                KnowledgeAnswerArtifactConflictType
                        .NONE,
                0.0,
                "No detectable conflict",
                java.util.Map.of()
        );
    }

    private String normalize(String text) {

        return text == null
                ? ""
                : text.toLowerCase()
                    .replaceAll("\\s+", " ")
                    .trim();
    }
}
