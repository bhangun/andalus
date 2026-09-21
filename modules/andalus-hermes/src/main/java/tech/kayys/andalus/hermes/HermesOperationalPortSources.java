package tech.kayys.andalus.agent.api;

import jakarta.enterprise.inject.Instance;
import tech.kayys.andalus.agent.hermes.HermesLearningAuditPort;
import tech.kayys.andalus.agent.hermes.HermesRuntimeDiagnosticsPort;
import tech.kayys.andalus.agent.hermes.HermesRuntimeJournalPort;

/**
 * CDI port sources plus optional test overrides for Hermes operational endpoints.
 */
record HermesOperationalPortSources(
        HermesRuntimeDiagnosticsPort runtimeDiagnosticsPort,
        Instance<HermesRuntimeDiagnosticsPort> runtimeDiagnosticsPorts,
        HermesRuntimeJournalPort runtimeJournalPort,
        Instance<HermesRuntimeJournalPort> runtimeJournalPorts,
        HermesLearningAuditPort learningAuditPort,
        Instance<HermesLearningAuditPort> learningAuditPorts) {

    HermesOperationalPortSources(
            HermesRuntimeDiagnosticsPort runtimeDiagnosticsPort,
            Instance<HermesRuntimeDiagnosticsPort> runtimeDiagnosticsPorts,
            HermesRuntimeJournalPort runtimeJournalPort,
            Instance<HermesRuntimeJournalPort> runtimeJournalPorts) {
        this(runtimeDiagnosticsPort, runtimeDiagnosticsPorts, runtimeJournalPort, runtimeJournalPorts, null, null);
    }
}
