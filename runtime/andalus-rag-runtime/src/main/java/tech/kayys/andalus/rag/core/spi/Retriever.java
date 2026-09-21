package tech.kayys.andalus.rag.core.spi;

import tech.kayys.andalus.rag.core.RagQuery;
import tech.kayys.andalus.rag.core.RagScoredChunk;

import java.util.List;

public interface Retriever {
    List<RagScoredChunk> retrieve(RagQuery query);
}
