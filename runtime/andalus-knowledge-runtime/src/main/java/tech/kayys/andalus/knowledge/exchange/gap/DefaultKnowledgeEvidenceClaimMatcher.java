package tech.kayys.andalus.knowledge.exchange.gap;

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


import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class DefaultKnowledgeEvidenceClaimMatcher
        implements KnowledgeEvidenceClaimMatcher {

    @Override
    public KnowledgeEvidenceClaimSupport match(
            KnowledgeEvidenceClaim claim,
            KnowledgeEvidenceFusionCandidate candidate) {

        String claimText =
                normalize(claim.text());

        String evidenceText =
                normalize(
                        candidate.evidence().excerpt()
                );

        Set<String> claimTokens =
                tokens(claimText);

        Set<String> evidenceTokens =
                tokens(evidenceText);

        if (claimTokens.isEmpty()
                || evidenceTokens.isEmpty()) {

            return new KnowledgeEvidenceClaimSupport(
                    claim.claimId(),
                    candidate.evidence().knowledgeId(),
                    0.0,
                    false,
                    candidate.authorityScore() >= 0.80,
                    candidate.trustScore() >= 0.80,
                    "No lexical evidence match",
                    java.util.Map.of()
            );
        }

        Set<String> intersection =
                new HashSet<>(claimTokens);

        intersection.retainAll(evidenceTokens);

        double lexicalScore =
                (double) intersection.size()
                        / claimTokens.size();

        double score =
                Math.min(
                        1.0,
                        lexicalScore * 0.60
                                + candidate.finalScore() * 0.40
                );

        boolean direct = score >= 0.80;

        return new KnowledgeEvidenceClaimSupport(
                claim.claimId(),
                candidate.evidence().knowledgeId(),
                score,
                direct,
                candidate.authorityScore() >= 0.80,
                candidate.trustScore() >= 0.80,
                direct
                        ? "Evidence directly matches claim"
                        : "Evidence partially matches claim",
                java.util.Map.of()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ");
    }

    private Set<String> tokens(String value) {
        Set<String> result = new HashSet<>();

        for (String token : value.split("\\s+")) {
            if (token.length() >= 3) {
                result.add(token);
            }
        }

        return result;
    }
}
