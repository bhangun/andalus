package tech.kayys.andalus.knowledge.exchange.protocol;

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


import java.util.Comparator;
import java.util.Set;

public final class DefaultKnowledgeEvidenceExchangeProtocolVersionPolicy
        implements KnowledgeEvidenceExchangeProtocolVersionPolicy {

    @Override
    public KnowledgeEvidenceExchangeProtocolVersion negotiate(
            KnowledgeEvidenceExchangeProtocolVersion localPreferred,
            Set<KnowledgeEvidenceExchangeProtocolVersion> localSupported,
            KnowledgeEvidenceExchangeProtocolVersion remotePreferred,
            Set<KnowledgeEvidenceExchangeProtocolVersion> remoteSupported
    ) {

        return localSupported.stream()
                .filter(remoteSupported::contains)
                .filter(version ->
                        version.major() ==
                                localPreferred.major())
                .max(Comparator.naturalOrder())
                .orElseThrow(() ->
                        new KnowledgeEvidenceExchangeProtocolStateException(
                                "No compatible protocol version"
                        )
                );
    }

    @Override
    public void validateNoDowngrade(
            KnowledgeEvidenceExchangeProtocolVersion selected,
            Set<KnowledgeEvidenceExchangeProtocolVersion>
                    localSupported,
            Set<KnowledgeEvidenceExchangeProtocolVersion>
                    remoteSupported
    ) {

        var highestCommon =
                localSupported.stream()
                        .filter(remoteSupported::contains)
                        .max(Comparator.naturalOrder());

        if (highestCommon.isPresent() &&
                selected.compareTo(highestCommon.get()) < 0) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Protocol downgrade detected: selected="
                            + selected
                            + ", highestCommon="
                            + highestCommon.get()
            );
        }
    }
}
