package tech.kayys.andalus.cli;

import tech.kayys.andalus.contract.AndalusContractDescriptor;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class AndalusCliGoldenFixtureManifest {

    private AndalusCliGoldenFixtureManifest() {
    }

    static List<Entry> entries() {
        return entries(AndalusCliGoldenFixtures.all());
    }

    static List<Entry> entries(List<AndalusCliGoldenFixtures.GoldenFixture> fixtures) {
        return fixtures.stream()
                .map(Entry::from)
                .toList();
    }

    record Entry(
            String name,
            String sdkSource,
            int expectedExitCode,
            List<String> args,
            List<String> commandIds,
            String commandLine,
            AndalusCliGoldenFixtures.SchemaMode mode,
            String schema,
            String envelope,
            String jsonSchemaId) {
        private static Entry from(AndalusCliGoldenFixtures.GoldenFixture fixture) {
            AndalusContractDescriptor descriptor = fixture.descriptor();
            return new Entry(
                    fixture.name(),
                    fixture.sdkSource(),
                    fixture.expectedExitCode(),
                    fixture.args(),
                    fixture.commandIds(),
                    fixture.commandLine(),
                    fixture.schemaMode(),
                    descriptor == null ? "" : descriptor.schema(),
                    descriptor == null ? "" : descriptor.envelope(),
                    descriptor == null ? "" : descriptor.jsonSchemaId());
        }

        Entry {
            args = List.copyOf(args);
            commandIds = List.copyOf(commandIds);
            mode = Objects.requireNonNull(mode, "mode");
        }

        String schemaMode() {
            return mode.name().toLowerCase(Locale.ROOT);
        }

        boolean explicitSchema() {
            return mode == AndalusCliGoldenFixtures.SchemaMode.EXPLICIT;
        }

        boolean selfDescribingSchema() {
            return mode == AndalusCliGoldenFixtures.SchemaMode.SELF_DESCRIBING;
        }

        boolean schemaValidated() {
            return mode.validated();
        }
    }
}
