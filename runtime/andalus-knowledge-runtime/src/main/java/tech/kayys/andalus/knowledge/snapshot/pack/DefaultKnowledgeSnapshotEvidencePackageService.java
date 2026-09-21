package tech.kayys.andalus.knowledge.snapshot.pack;

import tech.kayys.andalus.knowledge.integrity.KnowledgeSnapshotIntegrityResult;
import tech.kayys.andalus.knowledge.seal.KnowledgeSnapshotSecureSeal;
import tech.kayys.andalus.knowledge.snapshot.KnowledgeDecisionSnapshot;

public final class DefaultKnowledgeSnapshotEvidencePackageService
        implements KnowledgeSnapshotEvidencePackageService {

    private final KnowledgeSnapshotVerificationManifestFactory manifestFactory;
    private final KnowledgeSnapshotEvidencePackageVerifier verifier;

    public DefaultKnowledgeSnapshotEvidencePackageService(
            KnowledgeSnapshotVerificationManifestFactory manifestFactory,
            KnowledgeSnapshotEvidencePackageVerifier verifier
    ) {
        this.manifestFactory = manifestFactory;
        this.verifier = verifier;
    }

    @Override
    public KnowledgeSnapshotEvidencePackage create(
            KnowledgeDecisionSnapshot snapshot,
            KnowledgeSnapshotIntegrityResult integrity,
            KnowledgeSnapshotSecureSeal seal
    ) {
        KnowledgeSnapshotVerificationManifest manifest =
                manifestFactory.create(snapshot, integrity, seal);

        return new KnowledgeSnapshotEvidencePackageBuilder()
                .manifest(manifest)
                .build();
    }

    @Override
    public KnowledgeSnapshotPackageVerificationResult verify(
            KnowledgeSnapshotEvidencePackage evidencePackage
    ) {
        return verifier.verify(evidencePackage);
    }

    @Override
    public KnowledgeSnapshotPackageVerificationResult verify(
            KnowledgeSnapshotEvidencePackage evidencePackage,
            KnowledgeSnapshotPackageVerificationContext context
    ) {
        return verifier.verify(evidencePackage, context);
    }
}
