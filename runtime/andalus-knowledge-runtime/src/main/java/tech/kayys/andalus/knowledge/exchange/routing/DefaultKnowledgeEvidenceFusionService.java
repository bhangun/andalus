package tech.kayys.andalus.knowledge.exchange.routing;

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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public final class DefaultKnowledgeEvidenceFusionService
        implements KnowledgeEvidenceFusionService {

    @Override
    public List<KnowledgeEvidenceReference> fuse(
            List<KnowledgeEvidenceFederationQueryResult> results
    ) {

        var merged =
                new LinkedHashMap<
                        String,
                        KnowledgeEvidenceReference
                        >();

        for (var result : results) {

            if (result == null || !result.authorized()) {
                continue;
            }

            for (var evidence : result.evidence()) {

                String key =
                        evidence.knowledgeId()
                                + ":"
                                + String.valueOf(
                                        evidence.versionId()
                                )
                                + ":"
                                + String.valueOf(
                                        evidence.fragmentId()
                                );

                merged.merge(
                        key,
                        evidence,
                        (existing, incoming) ->
                                incoming.relevance()
                                        > existing.relevance()
                                        ? incoming
                                        : existing
                );
            }
        }

        return merged.values()
                .stream()
                .sorted(
                        Comparator.comparingDouble(
                                        KnowledgeEvidenceReference
                                                ::relevance
                                )
                                .reversed()
                )
                .toList();
    }
}
