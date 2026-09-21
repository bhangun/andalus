package tech.kayys.andalus.sdk.client;

import tech.kayys.andalus.state.artifact.*;
import tech.kayys.andalus.state.lineage.LineageGraph;
import tech.kayys.andalus.state.lineage.LineageResolver;
import tech.kayys.andalus.state.provenance.ProvenanceQueryService;

import java.util.Objects;
import java.util.Optional;

/**
 * Public SDK API for storing artifacts and resolving provenance lineage graphs.
 */
public final class AndalusArtifactApi {

    private final ArtifactStore artifactStore;
    private final ArtifactMetadataStore metadataStore;
    private final LineageResolver lineageResolver;
    private final ProvenanceQueryService provenanceService;

    public AndalusArtifactApi(
            ArtifactStore artifactStore,
            ArtifactMetadataStore metadataStore,
            LineageResolver lineageResolver,
            ProvenanceQueryService provenanceService) {
        this.artifactStore = Objects.requireNonNull(artifactStore, "artifactStore cannot be null");
        this.metadataStore = metadataStore;
        this.lineageResolver = lineageResolver;
        this.provenanceService = provenanceService;
    }

    public ArtifactReference put(ArtifactInput input) {
        ArtifactReference ref = artifactStore.put(input);
        if (metadataStore != null) {
            metadataStore.save(ref);
        }
        return ref;
    }

    public Optional<ArtifactContent> get(ArtifactReference reference) {
        return artifactStore.get(reference);
    }

    public Optional<ArtifactReference> getMetadata(ArtifactId id) {
        return metadataStore != null ? metadataStore.get(id) : Optional.empty();
    }

    public LineageGraph traceArtifact(ArtifactId id) {
        if (provenanceService != null) {
            return provenanceService.traceArtifact(id);
        }
        if (lineageResolver != null) {
            return lineageResolver.resolve(id);
        }
        return LineageGraph.empty();
    }

    public LineageGraph traceExecution(String executionId) {
        if (provenanceService != null) {
            return provenanceService.traceExecution(executionId);
        }
        if (lineageResolver != null) {
            return lineageResolver.resolveExecution(executionId);
        }
        return LineageGraph.empty();
    }
}
