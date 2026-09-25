package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.contract.AndalusContractCatalog;
import tech.kayys.andalus.contract.AndalusContractCommandCoverage;
import tech.kayys.andalus.contract.AndalusContractCommandCoverageEntry;
import tech.kayys.andalus.contract.AndalusContractCommandCoverageReport;
import tech.kayys.andalus.contract.AndalusContractDescriptor;
import tech.kayys.andalus.contract.AndalusContractIndex;
import tech.kayys.andalus.contract.AndalusContractKey;
import tech.kayys.andalus.workbench.AndalusWorkbenchCatalog;
import tech.kayys.andalus.workbench.WorkbenchCommand;
import tech.kayys.andalus.workbench.WorkbenchCommandContract;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AndalusCliGoldenFixtureCoverageTest {

    @Test
    void explicitSchemaManifestEntriesPointAtPublishedContracts() {
        AndalusContractIndex contracts = AndalusContractIndex.of(AndalusContractCatalog.defaultContracts());

        for (AndalusCliGoldenFixtureManifest.Entry entry : AndalusCliGoldenFixtureManifest.entries()) {
            if (!entry.explicitSchema()) {
                continue;
            }
            assertThat(entry.jsonSchemaId())
                    .as(entry.name() + " jsonSchemaId")
                    .isNotBlank();
            assertThat(contracts.contractByJsonSchemaId(entry.jsonSchemaId()))
                    .as(entry.name() + " should point at a published contract")
                    .isPresent()
                    .get()
                    .satisfies(contract -> assertThat(contract)
                            .extracting(
                                    AndalusContractDescriptor::schema,
                                    AndalusContractDescriptor::envelope)
                            .containsExactly(entry.schema(), entry.envelope()));
        }
    }

    @Test
    void manifestCoveredContractsHaveCompleteCommandCoverage() throws IOException {
        AndalusContractCommandCoverageReport coverage = AndalusContractCommandCoverage.defaultCoverage();
        Set<String> fixtureJsonSchemaIds = AndalusCliGoldenFixtureContracts.jsonSchemaIds();

        assertThat(fixtureJsonSchemaIds)
                .as("manifest contract schema ids")
                .isNotEmpty()
                .allSatisfy(jsonSchemaId -> assertThat(coverage.entryForJsonSchemaId(jsonSchemaId))
                        .as(jsonSchemaId + " coverage entry")
                        .isPresent()
                        .get()
                        .satisfies(AndalusCliGoldenFixtureCoverageTest::assertCompleteCoverage));
    }

    @Test
    void manifestCommandLinesMapToKnownCommandFamilies() {
        Set<String> commandFamilies = AndalusCliCommandFamilies.localWorkbenchCommandFamilies();
        List<String> unknown = AndalusCliGoldenFixtureManifest.entries().stream()
                .filter(entry -> !commandFamilies.contains(AndalusCliCommandFamilies.commandFamily(entry.args())))
                .map(AndalusCliGoldenFixtureManifest.Entry::commandLine)
                .toList();

        assertThat(unknown)
                .as("golden fixture args should map to known command families")
                .isEmpty();
    }

    @Test
    void manifestCommandIdsPointAtMatchingWorkbenchCommands() throws IOException {
        Map<String, WorkbenchCommand> commandsById = localCommandsById();

        for (AndalusCliGoldenFixtureManifest.Entry entry : AndalusCliGoldenFixtureManifest.entries()) {
            if (!entry.schemaValidated()) {
                continue;
            }
            Set<String> fixtureContractIds = fixtureJsonSchemaIds(entry);
            for (String commandId : entry.commandIds()) {
                assertThat(commandsById)
                        .as(entry.name() + " command id " + commandId)
                        .containsKey(commandId);
                WorkbenchCommand command = commandsById.get(commandId);
                assertThat(AndalusCliCommandFamilies.sameCommandFamily(command.command(), entry.args()))
                        .as(entry.name() + " should use the " + commandId + " command family")
                        .isTrue();
                List<String> commandContractIds = commandContractJsonSchemaIds(command);
                assertThat(commandContractIds)
                        .as(commandId + " declared contracts")
                        .isNotEmpty();
                assertThat(fixtureContractIds)
                        .as(entry.name() + " should emit every contract declared by " + commandId)
                        .containsAll(commandContractIds);
            }
        }
    }

    private static void assertCompleteCoverage(AndalusContractCommandCoverageEntry entry) {
        assertThat(entry.commandLinked())
                .as(entry.jsonSchemaId() + " should be linked to at least one command")
                .isTrue();
        assertThat(entry.complete())
                .as(entry.jsonSchemaId() + " coverage should be complete")
                .isTrue();
        assertThat(entry.unlinkedCommandIds())
                .as(entry.jsonSchemaId() + " unlinked command ids")
                .isEmpty();
        assertThat(entry.undeclaredLinkedCommandIds())
                .as(entry.jsonSchemaId() + " undeclared linked command ids")
                .isEmpty();
    }

    private static Map<String, WorkbenchCommand> localCommandsById() {
        Map<String, WorkbenchCommand> commands = new LinkedHashMap<>();
        for (WorkbenchCommand command : AndalusWorkbenchCatalog.localCommands()) {
            assertThat(commands)
                    .as("local workbench command ids should be unique")
                    .doesNotContainKey(command.id());
            commands.put(command.id(), command);
        }
        return Map.copyOf(commands);
    }

    private static List<String> commandContractJsonSchemaIds(WorkbenchCommand command) {
        return command.contracts().stream()
                .map(WorkbenchCommandContract::key)
                .map(AndalusContractKey::jsonSchemaId)
                .toList();
    }

    private static Set<String> fixtureJsonSchemaIds(AndalusCliGoldenFixtureManifest.Entry entry) throws IOException {
        if (entry.explicitSchema()) {
            return Set.of(entry.jsonSchemaId());
        }
        if (entry.selfDescribingSchema()) {
            return AndalusCliGoldenFixtureContracts.selfDescribingJsonSchemaIds(entry.name());
        }
        return Set.of();
    }
}
