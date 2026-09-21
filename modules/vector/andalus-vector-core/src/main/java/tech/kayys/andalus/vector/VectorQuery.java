package tech.kayys.andalus.vector;

import java.util.List;

/**
 * Represents a query to the vector store.
 */
public record VectorQuery(
        List<Float> vector,
        int topK,
        float minScore) {
}
