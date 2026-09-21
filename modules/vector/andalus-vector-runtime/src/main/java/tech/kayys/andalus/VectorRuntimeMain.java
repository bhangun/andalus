package tech.kayys.andalus;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.andalus.vector.VectorStore;
import tech.kayys.andalus.vector.runtime.VectorStoreProvider;

/**
 * Main entry point for the Vector Store Runtime module.
 */
public class VectorRuntimeMain {
    public static void main(String... args) {
        Quarkus.run(VectorRuntimeApp.class, args);
    }
}

@ApplicationScoped
class VectorRuntimeApp implements QuarkusApplication {

    private static final Logger LOG = LoggerFactory.getLogger(VectorRuntimeApp.class);

    @Inject
    VectorStore vectorStore;

    @Inject
    VectorStoreProvider vectorStoreProvider;

    @Override
    public int run(String... args) throws Exception {
        LOG.info("Starting Vector Store Runtime...");

        // Initialize the vector store
        vectorStoreProvider.initialize()
                .await().indefinitely();

        LOG.info("Vector Store Runtime initialized with: {}", vectorStore.getClass().getSimpleName());

        // Keep the application running
        Thread.currentThread().join();

        return 0;
    }
}
