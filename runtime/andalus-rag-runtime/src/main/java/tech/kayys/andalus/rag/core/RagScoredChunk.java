package tech.kayys.andalus.rag.core;

public record RagScoredChunk(
        RagChunk chunk,
        double score) {
}
