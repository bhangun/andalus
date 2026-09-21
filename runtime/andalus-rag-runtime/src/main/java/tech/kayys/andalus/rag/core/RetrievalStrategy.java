package tech.kayys.andalus.rag.core;

import tech.kayys.andalus.rag.core.store.VectorStore;
import java.util.List;

public interface RetrievalStrategy {
    List<ScoredDocument> retrieve(
            String query,
            VectorStore<RagChunk> store,
            RetrievalConfig config);
}