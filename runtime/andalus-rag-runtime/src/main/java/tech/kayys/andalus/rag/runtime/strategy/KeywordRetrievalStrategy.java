package tech.kayys.andalus.rag.runtime.strategy;

import tech.kayys.andalus.rag.model.MultimodalRetrievalQuery;
import tech.kayys.andalus.rag.model.QueryPart;
import tech.kayys.andalus.rag.model.RagScoredChunk;
import tech.kayys.andalus.rag.spi.RetrievalStrategy;
import tech.kayys.andalus.rag.spi.VectorSearchHit;
import tech.kayys.andalus.rag.spi.VectorStore;

import java.util.List;
import java.util.Map;

/**
 * Lexical (keyword / BM25-like) retrieval strategy.
 * Delegates to VectorStore.keywordSearch().
 */
public class KeywordRetrievalStrategy implements RetrievalStrategy {

    @Override
    public List<RagScoredChunk> retrieve(MultimodalRetrievalQuery query, VectorStore store) {
        String text = extractText(query);
        if (text == null || text.isEmpty()) return List.of();
        
        List<VectorSearchHit> hits = store.keywordSearch("default", text, query.topK(), query.filters());
        return hits.stream()
                   .filter(h -> h.score() >= query.minScore())
                   .map(h -> new RagScoredChunk(h.payload(), h.score()))
                   .toList();
    }
    
    private String extractText(MultimodalRetrievalQuery query) {
        StringBuilder sb = new StringBuilder();
        for (QueryPart part : query.parts()) {
            if (part instanceof QueryPart.TextPart t) {
                sb.append(t.text()).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
