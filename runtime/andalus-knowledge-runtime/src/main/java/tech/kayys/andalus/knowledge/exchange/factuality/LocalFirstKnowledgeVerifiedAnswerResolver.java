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


import java.util.concurrent.CompletableFuture;

public final class LocalFirstKnowledgeVerifiedAnswerResolver
        implements KnowledgeVerifiedAnswerRemoteResolver {

    private final KnowledgeVerifiedAnswerArtifactStore localStore;

    private final KnowledgeVerifiedAnswerRemoteResolver remote;

    public LocalFirstKnowledgeVerifiedAnswerResolver(
            KnowledgeVerifiedAnswerArtifactStore localStore,
            KnowledgeVerifiedAnswerRemoteResolver remote) {

        this.localStore = localStore;
        this.remote = remote;
    }

    @Override
    public CompletableFuture<
            KnowledgeVerifiedAnswerExchangeResponse> resolve(
            KnowledgeVerifiedAnswerExchangeRequest request) {

        var local =
                localStore.get(request.artifactId());

        if (local.isPresent()) {

            return CompletableFuture.completedFuture(
                    new KnowledgeVerifiedAnswerExchangeResponse(
                            true,
                            request.operation(),
                            request.artifactId(),
                            local.get(),
                            null,
                            null,
                            java.util.Map.of(
                                    "source",
                                    "local"
                            )
                    )
            );
        }

        if (!request.allowRemoteFetch()) {

            return CompletableFuture.completedFuture(
                    KnowledgeVerifiedAnswerExchangeResponse.failure(
                            request.operation(),
                            request.artifactId(),
                            "REMOTE_FETCH_DISABLED",
                            "Artifact is not locally available"
                    )
            );
        }

        return remote.resolve(request);
    }
}
