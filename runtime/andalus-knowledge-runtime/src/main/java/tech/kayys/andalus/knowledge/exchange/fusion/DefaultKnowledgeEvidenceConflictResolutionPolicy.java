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


public final class DefaultKnowledgeEvidenceConflictResolutionPolicy
        implements KnowledgeEvidenceConflictResolutionPolicy {

    @Override
    public KnowledgeEvidenceConflictResolutionDecision resolve(
            KnowledgeEvidenceFusionConflict conflict
    ) {

        return switch (conflict.type()) {

            case NONE ->
                    new KnowledgeEvidenceConflictResolutionDecision
                            .KeepBoth();

            case VERSION_CONFLICT,
                 TEMPORAL_CONFLICT,
                 AUTHORITY_CONFLICT,
                 SCOPE_CONFLICT,
                 DIRECT_CONTRADICTION,
                 UNKNOWN ->
                    new KnowledgeEvidenceConflictResolutionDecision
                            .Ambiguous(
                                    "Core cannot safely resolve this conflict"
                            );
        };
    }
}
