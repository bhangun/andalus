package tech.kayys.andalus.rag.core.impl;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.embedding.EmbeddingModuleConfig;
import tech.kayys.andalus.embedding.EmbeddingProviderRegistry;
import tech.kayys.andalus.embedding.EmbeddingService;
import tech.kayys.andalus.embedding.provider.CharNgramEmbeddingProvider;
import tech.kayys.andalus.embedding.provider.DeterministicHashEmbeddingProvider;
import tech.kayys.andalus.embedding.provider.TfIdfHashEmbeddingProvider;
import tech.kayys.andalus.rag.core.RagMetadataKeys;
import tech.kayys.andalus.rag.core.RagQuery;
import tech.kayys.andalus.rag.core.RagResult;
import tech.kayys.andalus.rag.core.spi.ChunkingOptions;
import tech.kayys.andalus.rag.core.store.InMemoryVectorStore;
import tech.kayys.andalus.rag.core.store.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagPipelineTest {

        @Test
        void shouldIngestAndRetrieveUsingOwnComponents() {
                EmbeddingProviderRegistry registry = new EmbeddingProviderRegistry(List.of(
                                new DeterministicHashEmbeddingProvider(),
                                new TfIdfHashEmbeddingProvider(),
                                new CharNgramEmbeddingProvider()));
                EmbeddingModuleConfig config = new EmbeddingModuleConfig();
                config.setDefaultModel("tfidf-512");
                EmbeddingService embeddingService = new EmbeddingService(registry, config);

                VectorStore<tech.kayys.andalus.rag.core.RagChunk> store = new InMemoryVectorStore<>();
                RagIndexer indexer = new RagIndexer(embeddingService, store, "tenant-a", "tfidf-512");
                VectorRetriever retriever = new VectorRetriever(embeddingService, store, "tenant-a", "tfidf-512");

                RagPipeline pipeline = new RagPipeline(
                                new SimpleTextDocumentParser(),
                                new SlidingWindowChunker(),
                                indexer,
                                retriever,
                                new TopKReranker(),
                                new ContextConcatenatingGenerator());

                List<tech.kayys.andalus.rag.core.RagChunk> chunks = pipeline.ingest(
                                "runbook",
                                "Payment timeout happens when gateway latency spikes. Retry policy should use exponential backoff.",
                                Map.of(RagMetadataKeys.COLLECTION, "ops"),
                                new ChunkingOptions(70, 10));

                RagResult result = pipeline.query(new RagQuery(
                                "How to handle payment timeout?",
                                3,
                                0.0,
                                Map.of()));

                assertFalse(result.chunks().isEmpty());
                assertTrue(chunks.stream().allMatch(chunk -> "runbook".equals(chunk.documentId())));
                assertTrue(result.answer().contains("payment") || result.answer().contains("Payment"));
        }
}
