package tech.kayys.andalus.agent.api;

import tech.kayys.andalus.agent.hermes.HermesRuntimeEvent;
import tech.kayys.andalus.agent.hermes.HermesRuntimeJournalDirective;

/**
 * Maps journal request parameters to Hermes runtime directives.
 */
final class HermesJournalDirectiveMapper {

    private final HermesJournalQueryMapper queryMapper = new HermesJournalQueryMapper();

    HermesRuntimeJournalDirective directive(HermesJournalRequest request) {
        return HermesRuntimeJournalDirective.inspect(queryMapper.query(request));
    }

    HermesRuntimeJournalDirective learningAuditRetention(HermesJournalRequest request) {
        return HermesRuntimeJournalDirective.inspect(queryMapper.queryForType(
                request,
                HermesRuntimeEvent.TYPE_LEARNING_AUDIT_RETENTION_ATTENTION));
    }
}
