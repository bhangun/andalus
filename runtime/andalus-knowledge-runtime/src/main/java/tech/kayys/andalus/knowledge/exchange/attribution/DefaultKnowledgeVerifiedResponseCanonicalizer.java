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


public final class DefaultKnowledgeVerifiedResponseCanonicalizer
        implements KnowledgeVerifiedResponseCanonicalizer {

    @Override
    public String canonicalize(
            KnowledgeVerifiedResponse response) {

        StringBuilder builder = new StringBuilder();

        builder.append("responseId=")
                .append(response.metadata().responseId())
                .append('\n');

        builder.append("executionId=")
                .append(response.metadata().executionId())
                .append('\n');

        builder.append("agentId=")
                .append(response.metadata().agentId())
                .append('\n');

        builder.append("status=")
                .append(response.status())
                .append('\n');

        builder.append("disposition=")
                .append(response.disposition())
                .append('\n');

        builder.append("confidence=")
                .append(response.confidence())
                .append('\n');

        response.claims()
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                KnowledgeVerifiedClaim
                                        ::claimId
                        )
                )
                .forEach(claim -> {

                    builder.append("claim:")
                            .append(claim.claimId())
                            .append('|')
                            .append(claim.status())
                            .append('|')
                            .append(claim.confidence())
                            .append('|')
                            .append(claim.text())
                            .append('\n');
                });

        response.evidence()
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                KnowledgeVerifiedEvidenceReference
                                        ::evidenceId
                        )
                )
                .forEach(evidence -> {

                    builder.append("evidence:")
                            .append(evidence.evidenceId())
                            .append('|')
                            .append(evidence.versionId())
                            .append('|')
                            .append(evidence.artifactId())
                            .append('\n');
                });

        return builder.toString();
    }
}
