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


import java.io.ByteArrayOutputStream;

public final class DefaultKnowledgeEvidenceExchangeChunkAssembler
        implements KnowledgeEvidenceExchangeChunkAssembler {

    private final ByteArrayOutputStream output =
            new ByteArrayOutputStream();

    private final String streamId;

    private long expectedOffset;

    private boolean complete;

    public DefaultKnowledgeEvidenceExchangeChunkAssembler(
            String streamId
    ) {
        this.streamId = streamId;
    }

    @Override
    public synchronized void accept(
            KnowledgeEvidenceExchangeChunk chunk
    ) {

        if (!streamId.equals(
                chunk.streamId()
        )) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Chunk stream mismatch"
            );
        }

        if (chunk.offset() != expectedOffset) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Chunk offset mismatch: expected="
                            + expectedOffset
                            + ", actual="
                            + chunk.offset()
            );
        }

        try {

            output.write(
                    chunk.data()
            );

        } catch (java.io.IOException e) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Unable to assemble chunk",
                    e
            );
        }

        expectedOffset +=
                chunk.data().length;

        if (chunk.last()) {
            complete = true;
        }
    }

    @Override
    public synchronized byte[] complete() {

        if (!complete) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Stream is not complete"
            );
        }

        return output.toByteArray();
    }

    @Override
    public synchronized boolean completeState() {
        return complete;
    }

    @Override
    public synchronized long receivedBytes() {
        return expectedOffset;
    }
}
