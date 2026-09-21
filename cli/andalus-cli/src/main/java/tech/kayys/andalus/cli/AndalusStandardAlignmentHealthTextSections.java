package tech.kayys.andalus.cli;

import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentPolicyAssessment;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentPortfolio;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentProviderDiagnostics;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentProviderIssue;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentProviderSummary;
import tech.kayys.andalus.gollek.sdk.AndalusStandardAlignmentSummary;
import tech.kayys.andalus.gollek.sdk.AndalusStandardRegistryDriftIssue;
import tech.kayys.andalus.gollek.sdk.AndalusStandardRegistryDriftReport;

/**
 * Reusable section renderers for standard-alignment health terminal output.
 */
final class AndalusStandardAlignmentHealthTextSections {

    private static final String NL = System.lineSeparator();

    private AndalusStandardAlignmentHealthTextSections() {
    }

    static void appendStandards(StringBuilder output, AndalusStandardAlignmentPortfolio portfolio) {
        if (portfolio.standards().isEmpty()) {
            return;
        }
        output.append("standards detail:").append(NL);
        for (AndalusStandardAlignmentSummary summary : portfolio.standards()) {
            output.append("- ")
                    .append(summary.standardId())
                    .append(" ")
                    .append(summary.standard().version())
                    .append(" aligned=")
                    .append(CliText.yesNo(summary.aligned() && !summary.hasGaps()))
                    .append(" requirements=")
                    .append(summary.alignedCount())
                    .append("/")
                    .append(summary.requirementCount())
                    .append(" gaps=")
                    .append(summary.gapCount());
            if (!summary.gapCategories().isEmpty()) {
                output.append(" categories=").append(String.join(",", summary.gapCategories()));
            }
            output.append(NL);
        }
    }

    static void appendProviderSummaries(
            StringBuilder output,
            AndalusStandardAlignmentProviderDiagnostics providers) {
        if (providers.providers().isEmpty()) {
            return;
        }
        output.append("provider detail:").append(NL);
        for (AndalusStandardAlignmentProviderSummary summary : providers.providers()) {
            output.append("- ")
                    .append(summary.providerId())
                    .append(" priority=")
                    .append(summary.priority())
                    .append(" standards=")
                    .append(summary.standardCount())
                    .append(" aligned=")
                    .append(CliText.yesNo(summary.aligned() && !summary.hasGaps()))
                    .append(" gaps=")
                    .append(summary.gapCount());
            if (!summary.standardIds().isEmpty()) {
                output.append(" ids=").append(String.join(",", summary.standardIds()));
            }
            output.append(NL);
        }
    }

    static void appendVersionMismatches(
            StringBuilder output,
            AndalusStandardAlignmentPolicyAssessment policy) {
        if (policy.versionMismatchStandardIds().isEmpty()) {
            return;
        }
        output.append("version mismatches:").append(NL);
        for (String standardId : policy.versionMismatchStandardIds()) {
            output.append("- ")
                    .append(standardId)
                    .append(" expected=")
                    .append(policy.requiredVersions().getOrDefault(standardId, ""))
                    .append(" actual=")
                    .append(policy.actualVersions().getOrDefault(standardId, ""))
                    .append(NL);
        }
    }

    static void appendDriftIssues(StringBuilder output, AndalusStandardRegistryDriftReport drift) {
        CliText.appendBulletBlockIfAny(output, "registry unknown standards", drift.unknownStandardIds());
        if (drift.issues().isEmpty()) {
            return;
        }
        output.append("registry drift detail:").append(NL);
        for (AndalusStandardRegistryDriftIssue issue : drift.issues()) {
            output.append("- ")
                    .append(issue.standardId())
                    .append(" ")
                    .append(issue.field())
                    .append(" expected=")
                    .append(issue.expected())
                    .append(" actual=")
                    .append(issue.actual())
                    .append(NL);
        }
    }

    static void appendProviderIssues(
            StringBuilder output,
            AndalusStandardAlignmentProviderDiagnostics providers) {
        if (providers.issues().isEmpty()) {
            return;
        }
        output.append("provider issue detail:").append(NL);
        for (AndalusStandardAlignmentProviderIssue issue : providers.issues()) {
            output.append("- ")
                    .append(issue.providerId())
                    .append(" ")
                    .append(issue.providerClass())
                    .append(": ")
                    .append(issue.message())
                    .append(NL);
        }
    }

}
