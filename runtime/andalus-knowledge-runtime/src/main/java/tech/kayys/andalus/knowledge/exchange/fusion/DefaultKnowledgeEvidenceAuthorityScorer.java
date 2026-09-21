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

public final class DefaultKnowledgeEvidenceAuthorityScorer
        implements KnowledgeEvidenceAuthorityScorer {

    @Override
    public double score(KnowledgeEvidenceReference evidence) {
        if (evidence == null) {
            return 0.0;
        }
        if (evidence.authority() > 0.0) {
            return evidence.authority();
        }
        Object metaAuthority = evidence.metadata().get("authority");
        if (metaAuthority != null) {
            String authority = metaAuthority.toString().toLowerCase();
            return switch (authority) {
                case "authoritative", "approved", "primary" -> 1.0;
                case "official" -> 0.95;
                case "secondary" -> 0.60;
                case "commentary" -> 0.40;
                case "unverified" -> 0.10;
                default -> 0.50;
            };
        }
        return 0.50;
    }
}
