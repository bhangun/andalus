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


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceExchangeSequenceValidator
        implements KnowledgeEvidenceExchangeSequenceValidator {

    private final ConcurrentMap<String, Long> lastSeen =
            new ConcurrentHashMap<>();

    @Override
    public void accept(
            String streamId,
            long sequence
    ) {

        lastSeen.compute(
                streamId,
                (id, previous) -> {

                    if (previous != null &&
                            sequence <= previous) {

                        throw new KnowledgeEvidenceExchangeTransportException(
                                "Invalid stream sequence: "
                                        + sequence
                        );
                    }

                    return sequence;
                }
        );
    }
}
