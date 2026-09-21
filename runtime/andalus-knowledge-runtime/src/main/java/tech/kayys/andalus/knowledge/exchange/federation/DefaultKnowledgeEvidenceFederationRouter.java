package tech.kayys.andalus.knowledge.exchange.federation;

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


import java.util.Comparator;
import java.util.List;

public final class DefaultKnowledgeEvidenceFederationRouter
        implements KnowledgeEvidenceFederationRouter {

    private final KnowledgeEvidenceFederationDiscoveryService discovery;

    public DefaultKnowledgeEvidenceFederationRouter(
            KnowledgeEvidenceFederationDiscoveryService discovery
    ) {
        this.discovery = discovery;
    }

    @Override
    public List<KnowledgeEvidenceFederationCandidate> route(
            KnowledgeEvidenceFederatedQuery query
    ) {

        return discovery
                .discover(query)
                .stream()
                .filter(candidate ->
                        candidate.location().online()
                )
                .filter(candidate ->
                        candidate.location().trusted()
                )
                .filter(candidate ->
                        candidate.location().tenantId()
                                .equals(query.tenantId())
                )
                .sorted(
                        Comparator.comparingDouble(
                                KnowledgeEvidenceFederationCandidate
                                        ::routingScore
                        ).reversed()
                )
                .toList();
    }
}
