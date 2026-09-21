package tech.kayys.andalus.api.rest.state;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import tech.kayys.andalus.state.StateStore;
import tech.kayys.andalus.state.artifact.ArtifactMetadataStore;
import tech.kayys.andalus.state.artifact.ArtifactStore;
import tech.kayys.andalus.state.checkpoint.CheckpointStore;
import tech.kayys.andalus.state.context.ContextSnapshotStore;
import tech.kayys.andalus.state.core.*;
import tech.kayys.andalus.state.lineage.LineageResolver;
import tech.kayys.andalus.state.provenance.ProvenanceQueryService;
import tech.kayys.andalus.state.provenance.ProvenanceStore;
import tech.kayys.andalus.state.retention.GarbageCollector;
import tech.kayys.andalus.state.retention.RetentionPolicy;

/**
 * CDI Producer providing default implementations for state, context, artifact,
 * provenance, lineage, and retention SPIs.
 */
@ApplicationScoped
public class StateProducer {

    @Produces
    @ApplicationScoped
    public StateStore produceStateStore() {
        return new InMemoryStateStore();
    }

    @Produces
    @ApplicationScoped
    public ContextSnapshotStore produceContextSnapshotStore() {
        return new InMemoryContextSnapshotStore();
    }

    @Produces
    @ApplicationScoped
    public ArtifactStore produceArtifactStore() {
        return new InMemoryArtifactStore();
    }

    @Produces
    @ApplicationScoped
    public ArtifactMetadataStore produceArtifactMetadataStore() {
        return new InMemoryArtifactMetadataStore();
    }

    @Produces
    @ApplicationScoped
    public ProvenanceStore produceProvenanceStore() {
        return new InMemoryProvenanceStore();
    }

    @Produces
    @ApplicationScoped
    public LineageResolver produceLineageResolver(ProvenanceStore provenanceStore) {
        return new DefaultLineageResolver(provenanceStore);
    }

    @Produces
    @ApplicationScoped
    public ProvenanceQueryService produceProvenanceQueryService(ProvenanceStore provenanceStore, LineageResolver lineageResolver) {
        return new DefaultProvenanceQueryService(provenanceStore, lineageResolver);
    }

    @Produces
    @ApplicationScoped
    public CheckpointStore produceStateCheckpointStore() {
        return new InMemoryCheckpointStore();
    }

    @Produces
    @ApplicationScoped
    public RetentionPolicy produceRetentionPolicy() {
        return DefaultRetentionPolicy.standard();
    }

    @Produces
    @ApplicationScoped
    public GarbageCollector produceGarbageCollector() {
        return new DefaultGarbageCollector();
    }
}
