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

public final class DefaultKnowledgeEvidenceTrustScorer
        implements KnowledgeEvidenceTrustScorer {

    @Override
    public double score(KnowledgeEvidenceReference evidence) {
        if (evidence == null) {
            return 0.0;
        }
        if (evidence.trust() > 0.0) {
            return evidence.trust();
        }
        Object metaTrust = evidence.metadata().get("trust");
        if (metaTrust != null) {
            String trust = metaTrust.toString().toLowerCase();
            return switch (trust) {
                case "verified", "attested", "trusted" -> 1.0;
                case "known" -> 0.75;
                case "unknown" -> 0.35;
                case "untrusted" -> 0.0;
                default -> 0.50;
            };
        }
        return 0.50;
    }
}
