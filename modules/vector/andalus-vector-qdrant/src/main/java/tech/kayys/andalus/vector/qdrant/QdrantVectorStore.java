package tech.kayys.andalus.vector.qdrant;

import io.smallrye.mutiny.Uni;
import tech.kayys.andalus.vector.AbstractVectorStore;
import tech.kayys.andalus.vector.VectorEntry;
import tech.kayys.andalus.vector.VectorQuery;

import java.util.List;
import java.util.Map;

/**
 * Qdrant implementation of VectorStore.
 * This would be replaced with actual Qdrant implementation.
 */
public class QdrantVectorStore extends AbstractVectorStore {
    
    @Override
    public Uni<Void> store(List<VectorEntry> entries) {
        // Actual implementation would connect to Qdrant
        throw new UnsupportedOperationException("QdrantVectorStore not yet implemented");
    }

    @Override
    public Uni<List<VectorEntry>> search(VectorQuery query) {
        // Actual implementation would perform vector similarity search in Qdrant
        throw new UnsupportedOperationException("QdrantVectorStore not yet implemented");
    }

    @Override
    public Uni<Void> delete(List<String> ids) {
        // Actual implementation would delete from Qdrant
        throw new UnsupportedOperationException("QdrantVectorStore not yet implemented");
    }
}