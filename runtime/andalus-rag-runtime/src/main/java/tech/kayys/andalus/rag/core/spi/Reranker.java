package tech.kayys.andalus.rag.core.spi;

import tech.kayys.andalus.rag.core.RagQuery;
import tech.kayys.andalus.rag.core.RagScoredChunk;

import java.util.List;

public interface Reranker {
    List<RagScoredChunk> rerank(RagQuery query, List<RagScoredChunk> candidates, int topK);
}
