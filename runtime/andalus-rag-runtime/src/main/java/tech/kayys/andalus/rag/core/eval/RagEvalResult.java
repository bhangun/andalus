package tech.kayys.andalus.rag.core.eval;

public record RagEvalResult(
        int totalQueries,
        int topK,
        double recallAtK,
        double mrr,
        long latencyP95Ms,
        long latencyAvgMs) {
}
