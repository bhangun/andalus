package tech.kayys.andalus.cli;

import tech.kayys.andalus.contract.AndalusContractCommandCoverage;
import tech.kayys.andalus.contract.AndalusContractCommandCoverageEntry;
import tech.kayys.andalus.contract.AndalusContractCommandCoverageReport;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class AndalusCliGoldenFixtureCommandCoverage {
    private AndalusCliGoldenFixtureCommandCoverage() {
    }

    static Report defaultReport() {
        return report(
                AndalusCliGoldenFixtureManifest.entries(),
                AndalusContractCommandCoverage.defaultCoverage());
    }

    static Report report(
            List<AndalusCliGoldenFixtureManifest.Entry> entries,
            AndalusContractCommandCoverageReport coverage) {
        LinkedHashSet<String> declaredCommandIds = declaredCommandIds(coverage);
        LinkedHashSet<String> coveredCommandIds = coveredCommandIds(entries);
        return new Report(
                List.copyOf(declaredCommandIds),
                List.copyOf(coveredCommandIds),
                missingCommandIds(declaredCommandIds, coveredCommandIds),
                undeclaredCoveredCommandIds(declaredCommandIds, coveredCommandIds));
    }

    static List<String> coveredCommandIds(AndalusCliGoldenFixtureManifest.Entry entry) {
        if (!entry.schemaValidated()) {
            return List.of();
        }
        return entry.commandIds();
    }

    private static LinkedHashSet<String> declaredCommandIds(AndalusContractCommandCoverageReport coverage) {
        LinkedHashSet<String> commandIds = new LinkedHashSet<>();
        for (AndalusContractCommandCoverageEntry entry : coverage.entries()) {
            commandIds.addAll(entry.declaredCommandIds());
        }
        return commandIds;
    }

    private static LinkedHashSet<String> coveredCommandIds(List<AndalusCliGoldenFixtureManifest.Entry> entries) {
        LinkedHashSet<String> commandIds = new LinkedHashSet<>();
        for (AndalusCliGoldenFixtureManifest.Entry entry : entries) {
            commandIds.addAll(coveredCommandIds(entry));
        }
        return commandIds;
    }

    private static List<String> missingCommandIds(Set<String> declaredCommandIds, Set<String> coveredCommandIds) {
        return declaredCommandIds.stream()
                .filter(commandId -> !coveredCommandIds.contains(commandId))
                .toList();
    }

    private static List<String> undeclaredCoveredCommandIds(Set<String> declaredCommandIds, Set<String> coveredCommandIds) {
        return coveredCommandIds.stream()
                .filter(commandId -> !declaredCommandIds.contains(commandId))
                .toList();
    }

    record Report(
            List<String> declaredCommandIds,
            List<String> coveredCommandIds,
            List<String> missingCommandIds,
            List<String> undeclaredCoveredCommandIds) {
        Report {
            declaredCommandIds = List.copyOf(declaredCommandIds);
            coveredCommandIds = List.copyOf(coveredCommandIds);
            missingCommandIds = List.copyOf(missingCommandIds);
            undeclaredCoveredCommandIds = List.copyOf(undeclaredCoveredCommandIds);
        }
    }
}
