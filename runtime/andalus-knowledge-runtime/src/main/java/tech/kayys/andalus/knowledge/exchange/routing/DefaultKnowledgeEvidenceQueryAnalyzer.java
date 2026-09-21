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


import java.util.Arrays;
import java.util.List;

public final class DefaultKnowledgeEvidenceQueryAnalyzer
        implements KnowledgeEvidenceQueryAnalyzer {

    @Override
    public KnowledgeEvidenceQueryIntent analyze(
            KnowledgeEvidenceSemanticQuery query
    ) {

        List<String> keywords =
                Arrays.stream(
                                query.text()
                                        .toLowerCase()
                                        .split("\\s+")
                        )
                        .map(token ->
                                token.replaceAll(
                                        "[^\\p{L}\\p{N}_-]",
                                        ""
                                )
                        )
                        .filter(token ->
                                !token.isBlank()
                        )
                        .distinct()
                        .toList();

        return new KnowledgeEvidenceQueryIntent(
                query.text(),
                keywords,
                List.of(),
                keywords,
                query.requiredTags(),
                java.util.Map.of()
        );
    }
}
