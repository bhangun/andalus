package tech.kayys.andalus.cli.contract;

import tech.kayys.andalus.gollek.sdk.AndalusContractIntegrityIssue;
import tech.kayys.andalus.gollek.sdk.AndalusContractIntegrityReport;
import tech.kayys.andalus.gollek.sdk.AndalusContractEnvelopes;

/**
 * Plain-text renderer for contract integrity diagnostics.
 *
 * <p>The renderer owns terminal-facing labels while SDK envelopes provide the
 * normalized integrity report model used by JSON and text surfaces.</p>
 */
final class AndalusContractIntegrityTextFormat {

    private AndalusContractIntegrityTextFormat() {
    }

    static String text(String productName, AndalusContractIntegrityReport report) {
        AndalusContractIntegrityReport model = AndalusContractEnvelopes.normalizeIntegrityReport(report);
        StringBuilder output = new StringBuilder(productName).append(" contract integrity").append(System.lineSeparator());
        output.append("valid: ").append(model.valid()).append(System.lineSeparator());
        output.append("issues: ").append(model.issueCount()).append(System.lineSeparator());
        output.append("contracts: ").append(model.totalContracts()).append(System.lineSeparator());
        output.append("commands: ").append(model.totalCommands()).append(System.lineSeparator());
        output.append("contractCommandLinks: ").append(model.contractCommandLinks()).append(System.lineSeparator());
        output.append("commandContractLinks: ").append(model.commandContractLinks()).append(System.lineSeparator());
        if (!model.issues().isEmpty()) {
            output.append(System.lineSeparator()).append("Issues").append(System.lineSeparator());
            for (AndalusContractIntegrityIssue issue : model.issues()) {
                output.append("  - ")
                        .append(issue.kind())
                        .append(": ")
                        .append(issue.message())
                        .append(System.lineSeparator());
            }
        }
        return output.toString();
    }
}
