package tech.kayys.andalus.knowledge.exchange.selection;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class
InMemoryKnowledgeAnswerResolutionDependencyGraph
        implements KnowledgeAnswerResolutionDependencyGraph {

    private final ConcurrentHashMap<
            String,
            CopyOnWriteArrayList<
                    KnowledgeAnswerResolutionDependency>>
            byResolution =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<
            String,
            CopyOnWriteArrayList<String>>
            byTarget =
            new ConcurrentHashMap<>();

    @Override
    public void add(
            KnowledgeAnswerResolutionDependency dependency) {

        byResolution
                .computeIfAbsent(
                        dependency.resolutionKeyFingerprint(),
                        ignored -> new CopyOnWriteArrayList<>()
                )
                .add(dependency);

        byTarget
                .computeIfAbsent(
                        targetKey(
                                dependency.type(),
                                dependency.targetId()
                        ),
                        ignored -> new CopyOnWriteArrayList<>()
                )
                .add(
                        dependency.resolutionKeyFingerprint()
                );
    }

    @Override
    public void remove(String dependencyId) {

        byResolution.values()
                .forEach(list ->
                        list.removeIf(
                                dependency ->
                                        dependency.dependencyId()
                                                .equals(dependencyId)
                        ));
    }

    @Override
    public List<
            KnowledgeAnswerResolutionDependency>
    dependenciesOf(
            String resolutionKeyFingerprint) {

        return List.copyOf(
                byResolution.getOrDefault(
                        resolutionKeyFingerprint,
                        new CopyOnWriteArrayList<>()
                )
        );
    }

    @Override
    public List<String>
    resolutionsDependingOn(
            KnowledgeAnswerResolutionDependencyType type,
            String targetId) {

        return List.copyOf(
                byTarget.getOrDefault(
                        targetKey(type, targetId),
                        new CopyOnWriteArrayList<>()
                )
        );
    }

    @Override
    public List<String>
    resolutionsDependingOnFingerprint(
            String fingerprint) {

        List<String> result =
                new ArrayList<>();

        byResolution.values()
                .forEach(dependencies -> {

                    if (dependencies.stream()
                            .anyMatch(d ->
                                    fingerprint.equals(
                                            d.targetFingerprint()
                                    ))) {

                        result.add(
                                dependencies.get(0)
                                        .resolutionKeyFingerprint()
                        );
                    }
                });

        return List.copyOf(result);
    }

    private String targetKey(
            KnowledgeAnswerResolutionDependencyType type,
            String targetId) {

        return type.name()
                + ":"
                + targetId;
    }
}
