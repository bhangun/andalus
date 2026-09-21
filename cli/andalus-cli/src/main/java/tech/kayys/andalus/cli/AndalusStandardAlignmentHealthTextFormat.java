package tech.kayys.andalus.cli;

import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentHealthReport;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentPolicyAssessment;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentPortfolio;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentProviderDiagnostics;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentProviderPolicyAssessment;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentHealthEnvelopes;
import tech.kayys.andalus.gollek.sdk.AndalusStandardRegistryDriftReport;

/**
 * Plain-text renderer for standards alignment health diagnostics.
 *
 * <p>The renderer keeps long-form terminal sections in the CLI while sharing
 * normalized health defaults with SDK-owned JSON envelopes.</p>
 */
final class AndalusStandardAlignmentHealthTextFormat {

    private static final String NL = System.lineSeparator();

    private AndalusStandardAlignmentHealthTextFormat() {
    }

    static String text(String productName, AndalusStandardAlignmentHealthReport health) {
        AndalusStandardAlignmentHealthReport model = AndalusStandardAlignmentHealthEnvelopes.normalize(health);
        AndalusStandardAlignmentPortfolio portfolio = model.portfolio();
        AndalusStandardAlignmentPolicyAssessment policy = model.policyAssessment();
        AndalusStandardAlignmentProviderDiagnostics providers = model.providerDiagnostics();
        AndalusStandardAlignmentProviderPolicyAssessment providerPolicy = model.providerPolicyAssessment();
        AndalusStandardRegistryDriftReport drift = model.registryDrift();
        StringBuilder output = new StringBuilder(productName).append(" standard alignment").append(NL);
        output.append("status: ").append(model.status()).append(NL);
        output.append("ready: ").append(CliText.yesNo(model.ready())).append(NL);
        output.append("standards: ").append(portfolio.standardCount()).append(NL);
        output.append("aligned standards: ").append(portfolio.alignedCount()).append(NL);
        output.append("gaps: ").append(portfolio.gapCount()).append(NL);
        output.append("providers: ").append(providers.providerCount()).append(NL);
        output.append("registry drift mode: ").append(model.registryDriftMode().id()).append(NL);
        output.append("registry drift free: ")
                .append(CliText.yesNo(drift.driftFree()))
                .append(NL);
        output.append("registry drift issues: ").append(drift.issues().size()).append(NL);
        output.append("provider policy ready: ")
                .append(CliText.yesNo(providerPolicy.ready()))
                .append(NL);
        output.append("provider issue mode: ").append(providerPolicy.issueMode().id()).append(NL);
        output.append("provider minimum: ").append(providerPolicy.minimumProviderCount()).append(NL);
        output.append("provider issues: ").append(providers.issueCount()).append(NL);
        output.append("unknown standards: ").append(drift.unknownStandardIds().size()).append(NL);
        CliText.appendBulletBlockIfAny(
                output, "required standards", policy.requiredStandardIds());
        CliText.appendBulletBlockIfAny(
                output, "present standards", policy.presentStandardIds());
        CliText.appendBulletBlockIfAny(
                output, "missing standards", policy.missingStandardIds());
        CliText.appendBulletBlockIfAny(
                output, "failing standards", policy.failingStandardIds());
        CliText.appendBulletBlockIfAny(
                output, "warning standards", policy.warningStandardIds());
        CliText.appendBulletBlockIfAny(
                output, "required providers", providerPolicy.requiredProviderIds());
        CliText.appendBulletBlockIfAny(
                output, "missing providers", providerPolicy.missingProviderIds());
        CliText.appendBulletBlockIfAny(output, "provider ids", providers.providerIds());
        AndalusStandardAlignmentHealthTextSections.appendProviderSummaries(output, providers);
        AndalusStandardAlignmentHealthTextSections.appendVersionMismatches(output, policy);
        AndalusStandardAlignmentHealthTextSections.appendStandards(output, portfolio);
        AndalusStandardAlignmentHealthTextSections.appendProviderIssues(output, providers);
        AndalusStandardAlignmentHealthTextSections.appendDriftIssues(output, drift);
        CliText.appendBulletBlockIfAny(output, "recommendations", model.recommendations());
        if (portfolio.standardCount() == 0 && model.recommendations().isEmpty()) {
            output.append("notes:").append(NL);
            output.append("- No standard-alignment reports are currently provided by this SDK.").append(NL);
        }
        return output.toString();
    }
}
