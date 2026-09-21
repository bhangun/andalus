package tech.kayys.andalus.knowledge.exchange.framing;

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


import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceExchangeResumeService
        implements KnowledgeEvidenceExchangeResumeService {

    private final ConcurrentMap<
            String,
            KnowledgeEvidenceExchangeResumeToken
            > tokens =
            new ConcurrentHashMap<>();

    @Override
    public KnowledgeEvidenceExchangeResumeToken create(
            String sessionId,
            String streamId,
            String artifactId,
            String resourceId,
            long offset,
            long nextSequence,
            String resourceFingerprint,
            Instant now
    ) {

        var token =
                new KnowledgeEvidenceExchangeResumeToken(
                        UUID.randomUUID().toString(),
                        sessionId,
                        streamId,
                        artifactId,
                        resourceId,
                        offset,
                        nextSequence,
                        resourceFingerprint,
                        now,
                        now.plus(java.time.Duration.ofMinutes(10)),
                        java.util.Map.of()
                );

        tokens.put(
                token.tokenId(),
                token
        );

        return token;
    }

    @Override
    public KnowledgeEvidenceExchangeResumeToken resolve(
            String tokenId,
            Instant now
    ) {

        var token =
                tokens.get(tokenId);

        if (token == null) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Unknown resume token"
            );
        }

        if (!token.activeAt(now)) {

            tokens.remove(tokenId);

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Resume token expired"
            );
        }

        return token;
    }
}
