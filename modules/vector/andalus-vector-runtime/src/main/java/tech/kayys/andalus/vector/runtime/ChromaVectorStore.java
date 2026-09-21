package tech.kayys.andalus.vector.runtime;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.vector.AbstractVectorStore;
import tech.kayys.andalus.vector.VectorEntry;
import tech.kayys.andalus.vector.VectorQuery;
import tech.kayys.andalus.vector.VectorStore;

import java.util.List;
import java.util.Map;

/**
 * Placeholder implementation for ChromaVectorStore.
 * This would be replaced with actual ChromaDB implementation.
 */
public class ChromaVectorStore extends AbstractVectorStore {

    @Override
    public Uni<Void> store(List<VectorEntry> entries) {
        // Actual implementation would connect to ChromaDB
        throw new UnsupportedOperationException("ChromaVectorStore not yet implemented");
    }

    @Override
    public Uni<List<VectorEntry>> search(VectorQuery query) {
        // Actual implementation would perform vector similarity search in ChromaDB
        throw new UnsupportedOperationException("ChromaVectorStore not yet implemented");
    }

    @Override
    public Uni<Void> delete(List<String> ids) {
        // Actual implementation would delete from ChromaDB
        throw new UnsupportedOperationException("ChromaVectorStore not yet implemented");
    }
}