package tech.kayys.andalus.knowledge.exchange.sync;

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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class DefaultKnowledgeEvidenceArtifactInventoryReconciler
        implements KnowledgeEvidenceArtifactInventoryReconciler {

    @Override
    public KnowledgeEvidenceArtifactReconciliationResult reconcile(
            KnowledgeEvidenceArtifactInventorySnapshot local,
            KnowledgeEvidenceArtifactInventorySnapshot remote
    ) {

        var localMap =
                new HashMap<String,
                        KnowledgeEvidenceArtifactInventoryEntry>();

        var remoteMap =
                new HashMap<String,
                        KnowledgeEvidenceArtifactInventoryEntry>();

        local.entries().forEach(
                e -> localMap.put(
                        e.artifactId(),
                        e
                )
        );

        remote.entries().forEach(
                e -> remoteMap.put(
                        e.artifactId(),
                        e
                )
        );

        var missingLocally =
                new ArrayList<String>();

        var missingRemotely =
                new ArrayList<String>();

        var divergent =
                new ArrayList<String>();

        var revoked =
                new ArrayList<String>();

        var allIds =
                new HashSet<String>();

        allIds.addAll(localMap.keySet());
        allIds.addAll(remoteMap.keySet());

        for (String id : allIds) {

            var l = localMap.get(id);
            var r = remoteMap.get(id);

            if (l == null) {

                missingLocally.add(id);
                continue;
            }

            if (r == null) {

                missingRemotely.add(id);
                continue;
            }

            if (l.revoked() || r.revoked()) {

                revoked.add(id);
                continue;
            }

            boolean same =
                    java.util.Objects.equals(
                            l.fingerprint(),
                            r.fingerprint()
                    )
                    &&
                    java.util.Objects.equals(
                            l.merkleRoot(),
                            r.merkleRoot()
                    )
                    &&
                    l.size() == r.size();

            if (!same) {
                divergent.add(id);
            }
        }

        KnowledgeEvidenceArtifactConsistencyState state;

        if (!divergent.isEmpty()) {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .DIVERGENT;

        } else if (!missingLocally.isEmpty()) {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .MISSING_LOCAL;

        } else if (!missingRemotely.isEmpty()) {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .MISSING_REMOTE;

        } else if (!revoked.isEmpty()) {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .REVOKED;

        } else {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .CONSISTENT;
        }

        return new KnowledgeEvidenceArtifactReconciliationResult(
                state,
                local.runtimeId(),
                remote.runtimeId(),
                missingLocally,
                missingRemotely,
                divergent,
                revoked,
                java.util.Map.of(
                        "localInventory",
                        String.valueOf(
                                local.inventoryFingerprint()
                        ),
                        "remoteInventory",
                        String.valueOf(
                                remote.inventoryFingerprint()
                        )
                )
        );
    }
}
