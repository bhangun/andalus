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


public final class DefaultKnowledgeVerifiedAnswerArtifactCanonicalizer
        implements KnowledgeVerifiedAnswerArtifactCanonicalizer {

    @Override
    public String canonicalize(
            KnowledgeVerifiedAnswerArtifact artifact) {

        StringBuilder b = new StringBuilder();

        b.append("artifactId=")
                .append(artifact.artifactId())
                .append('\n');

        b.append("responseId=")
                .append(artifact.responseId())
                .append('\n');

        b.append("executionId=")
                .append(artifact.executionId())
                .append('\n');

        b.append("agentId=")
                .append(artifact.agentId())
                .append('\n');

        b.append("tenantId=")
                .append(artifact.tenantId())
                .append('\n');

        b.append("workspaceId=")
                .append(artifact.workspaceId())
                .append('\n');

        b.append("projectId=")
                .append(artifact.projectId())
                .append('\n');

        b.append("responseFingerprint=")
                .append(artifact.responseFingerprint())
                .append('\n');

        b.append("snapshotId=")
                .append(artifact.snapshotId())
                .append('\n');

        b.append("responseStatus=")
                .append(artifact.response().status())
                .append('\n');

        b.append("disposition=")
                .append(artifact.response().disposition())
                .append('\n');

        artifact.provenance()
                .nodes()
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                KnowledgeAnswerProvenanceNode
                                        ::nodeId
                        )
                )
                .forEach(node ->
                        b.append("node:")
                                .append(node.nodeId())
                                .append('|')
                                .append(node.type())
                                .append('|')
                                .append(node.externalId())
                                .append('|')
                                .append(node.fingerprint())
                                .append('\n')
                );

        artifact.provenance()
                .edges()
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                KnowledgeAnswerProvenanceEdge
                                        ::edgeId
                        )
                )
                .forEach(edge ->
                        b.append("edge:")
                                .append(edge.edgeId())
                                .append('|')
                                .append(edge.sourceNodeId())
                                .append('|')
                                .append(edge.targetNodeId())
                                .append('|')
                                .append(edge.relation())
                                .append('\n')
                );

        return b.toString();
    }
}
