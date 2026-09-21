package tech.kayys.andalus.knowledge.exchange.fusion;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DefaultKnowledgeEvidenceCoherenceSelector
        implements KnowledgeEvidenceCoherenceSelector {

    @Override
    public List<KnowledgeEvidenceFusionCandidate> select(
            List<KnowledgeEvidenceFusionCandidate> candidates,
            List<KnowledgeEvidenceFusionRelation> relations,
            List<KnowledgeEvidenceFusionConflict> conflicts,
            KnowledgeEvidenceSemanticQuery query
    ) {

        var selected =
                new ArrayList<
                        KnowledgeEvidenceFusionCandidate
                        >();

        var seenKnowledge =
                new HashSet<String>();

        for (var candidate : candidates) {

            if (selected.size() >= query.limit()) {
                break;
            }

            var evidence =
                    candidate.evidence();

            String key =
                    evidence.knowledgeId()
                            + ":"
                            + evidence.versionId()
                            + ":"
                            + evidence.fragmentId();

            if (!seenKnowledge.add(key)) {
                continue;
            }

            /*
             * Avoid flooding the context with extremely weak evidence.
             */
            if (candidate.finalScore()
                    < query.minScore()) {

                continue;
            }

            selected.add(candidate);
        }

        return List.copyOf(selected);
    }
}
