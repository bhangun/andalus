package tech.kayys.andalus.rag.core.spi;

import tech.kayys.andalus.rag.core.RagChunk;
import tech.kayys.andalus.rag.core.RagDocument;

import java.util.List;

public interface Chunker {
    List<RagChunk> chunk(RagDocument document, ChunkingOptions options);
}
