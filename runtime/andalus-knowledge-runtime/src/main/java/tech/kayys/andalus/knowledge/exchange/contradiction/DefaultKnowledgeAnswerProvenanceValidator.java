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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DefaultKnowledgeAnswerProvenanceValidator
        implements KnowledgeAnswerProvenanceValidator {

    @Override
    public KnowledgeAnswerProvenanceValidationResult validate(
            KnowledgeAnswerProvenanceGraph graph) {

        Set<String> nodeIds =
                graph.nodes()
                        .stream()
                        .map(
                                KnowledgeAnswerProvenanceNode
                                        ::nodeId
                        )
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        List<String> invalidEdges =
                new ArrayList<>();

        for (KnowledgeAnswerProvenanceEdge edge
                : graph.edges()) {

            if (!nodeIds.contains(edge.sourceNodeId())
                    || !nodeIds.contains(edge.targetNodeId())) {

                invalidEdges.add(
                        edge.edgeId()
                );
            }
        }

        Set<String> referenced =
                new HashSet<>();

        for (KnowledgeAnswerProvenanceEdge edge
                : graph.edges()) {

            referenced.add(edge.sourceNodeId());
            referenced.add(edge.targetNodeId());
        }

        List<String> orphan =
                graph.nodes()
                        .stream()
                        .map(
                                KnowledgeAnswerProvenanceNode
                                        ::nodeId
                        )
                        .filter(id ->
                                !referenced.contains(id))
                        .toList();

        return new KnowledgeAnswerProvenanceValidationResult(
                invalidEdges.isEmpty(),
                List.of(),
                invalidEdges,
                orphan,
                java.util.Map.of(
                        "nodeCount",
                        Integer.toString(
                                graph.nodes().size()
                        ),
                        "edgeCount",
                        Integer.toString(
                                graph.edges().size()
                        )
                )
        );
    }
}
