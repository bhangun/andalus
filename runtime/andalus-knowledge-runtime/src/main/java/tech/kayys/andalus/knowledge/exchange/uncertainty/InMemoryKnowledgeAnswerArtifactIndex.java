package tech.kayys.andalus.knowledge.exchange.uncertainty;

import tech.kayys.andalus.knowledge.*;
import tech.kayys.andalus.knowledge.seal.*;
import tech.kayys.andalus.knowledge.snapshot.*;
import tech.kayys.andalus.knowledge.snapshot.pack.*;
import tech.kayys.andalus.knowledge.snapshot.artifact.*;
import tech.kayys.andalus.knowledge.snapshot.merkle.*;
import tech.kayys.andalus.knowledge.exchange.*;
import tech.kayys.andalus.knowledge.exchange.auth.*;
import tech.kayys.andalus.knowledge.exchange.session.*;
import tech.kayys.andalus.knowledge.exchange.binding.*;
import tech.kayys.andalus.knowledge.exchange.envelope.*;
import tech.kayys.andalus.knowledge.exchange.trust.*;
import tech.kayys.andalus.knowledge.exchange.identity.*;
import tech.kayys.andalus.knowledge.exchange.capability.*;
import tech.kayys.andalus.knowledge.exchange.protocol.*;
import tech.kayys.andalus.knowledge.exchange.transport.*;
import tech.kayys.andalus.knowledge.exchange.framing.*;
import tech.kayys.andalus.knowledge.exchange.transfer.*;
import tech.kayys.andalus.knowledge.exchange.replication.*;
import tech.kayys.andalus.knowledge.exchange.sync.*;
import tech.kayys.andalus.knowledge.exchange.federation.*;
import tech.kayys.andalus.knowledge.exchange.routing.*;
import tech.kayys.andalus.knowledge.exchange.fusion.*;
import tech.kayys.andalus.knowledge.exchange.coverage.*;
import tech.kayys.andalus.knowledge.exchange.gap.*;
import tech.kayys.andalus.knowledge.exchange.attribution.*;
import tech.kayys.andalus.knowledge.exchange.contradiction.*;
import tech.kayys.andalus.knowledge.exchange.factuality.*;
import tech.kayys.andalus.knowledge.exchange.uncertainty.*;
import tech.kayys.andalus.knowledge.exchange.compact.*;
import tech.kayys.andalus.knowledge.exchange.resolution.*;
import tech.kayys.andalus.knowledge.exchange.quorum.*;
import tech.kayys.andalus.knowledge.exchange.selection.*;
import tech.kayys.andalus.knowledge.exchange.coordination.*;
import tech.kayys.andalus.knowledge.exchange.attestation.*;
import tech.kayys.andalus.knowledge.exchange.proof.*;
import tech.kayys.andalus.knowledge.exchange.validity.*;
import tech.kayys.andalus.knowledge.exchange.lease.*;
import tech.kayys.andalus.knowledge.exchange.recovery.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeAnswerArtifactIndex
        implements KnowledgeAnswerArtifactIndex {

    private final ConcurrentHashMap<
            String,
            KnowledgeAnswerArtifactIndexEntry> entries =
            new ConcurrentHashMap<>();

    @Override
    public void index(
            KnowledgeAnswerArtifactIndexEntry entry) {

        if (entry == null
                || entry.artifactId() == null) {

            throw new IllegalArgumentException(
                    "Artifact index entry is required"
            );
        }

        entries.compute(
                entry.artifactId(),
                (id, existing) -> {

                    if (existing == null) {
                        return entry;
                    }

                    if (!existing
                            .responseFingerprint()
                            .equals(entry.responseFingerprint())) {

                        throw new IllegalStateException(
                                "Artifact identity collision"
                        );
                    }

                    return existing;
                }
        );
    }

    @Override
    public void remove(String artifactId) {
        entries.remove(artifactId);
    }

    @Override
    public Optional<
            KnowledgeAnswerArtifactIndexEntry> get(
            String artifactId) {

        return Optional.ofNullable(
                entries.get(artifactId)
        );
    }

    @Override
    public List<
            KnowledgeAnswerArtifactIndexEntry> search(
            KnowledgeAnswerArtifactQuery query) {

        List<KnowledgeAnswerArtifactIndexEntry> result =
                new ArrayList<>();

        for (KnowledgeAnswerArtifactIndexEntry entry
                : entries.values()) {

            if (!matches(query, entry)) {
                continue;
            }

            result.add(entry);

            if (result.size() >= query.limit()) {
                break;
            }
        }

        return List.copyOf(result);
    }

    private boolean matches(
            KnowledgeAnswerArtifactQuery query,
            KnowledgeAnswerArtifactIndexEntry entry) {

        if (!equalsNullable(
                query.tenantId(),
                entry.tenantId())) {

            return false;
        }

        if (query.workspaceId() != null
                && !equalsNullable(
                        query.workspaceId(),
                        entry.workspaceId())) {

            return false;
        }

        if (query.projectId() != null
                && !equalsNullable(
                        query.projectId(),
                        entry.projectId())) {

            return false;
        }

        if (query.requireVerified()
                && !entry.verified()) {

            return false;
        }

        if (query.requireSealed()
                && !entry.sealed()) {

            return false;
        }

        if (!query.requiredAgents().isEmpty()
                && !query.requiredAgents()
                        .contains(entry.agentId())) {

            return false;
        }

        if (!query.requiredStatuses().isEmpty()
                && !query.requiredStatuses()
                        .contains(entry.status())) {

            return false;
        }

        if (!query.requiredTags().isEmpty()
                && !entry.tags()
                        .containsAll(query.requiredTags())) {

            return false;
        }

        if (query.effectiveAt() != null
                && entry.effectiveAt() != null
                && entry.effectiveAt()
                        .isAfter(query.effectiveAt())) {

            return false;
        }

        return true;
    }

    private boolean equalsNullable(
            String left,
            String right) {

        return left == null
                ? right == null
                : left.equals(right);
    }
}
