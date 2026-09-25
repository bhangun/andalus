package tech.kayys.andalus.cli;

import tech.kayys.andalus.contract.AndalusContractCatalog;
import tech.kayys.andalus.contract.AndalusContractDescriptor;
import tech.kayys.andalus.contract.AndalusContractQuery;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AndalusCliGoldenFixtureContracts {

    private AndalusCliGoldenFixtureContracts() {
    }

    static Set<String> jsonSchemaIds() throws IOException {
        return jsonSchemaIds(AndalusCliGoldenFixtureManifest.entries());
    }

    static Set<String> jsonSchemaIds(List<AndalusCliGoldenFixtureManifest.Entry> entries) throws IOException {
        Set<String> jsonSchemaIds = new HashSet<>();
        for (AndalusCliGoldenFixtureManifest.Entry entry : entries) {
            if (entry.explicitSchema()) {
                jsonSchemaIds.add(explicitJsonSchemaId(entry));
            } else if (entry.selfDescribingSchema()) {
                jsonSchemaIds.addAll(selfDescribingJsonSchemaIds(entry.name()));
            }
        }
        return Set.copyOf(jsonSchemaIds);
    }

    static Set<String> selfDescribingJsonSchemaIds(String fixture) throws IOException {
        Set<String> ids = new HashSet<>();
        List<String> lines = AndalusCliGoldenFixtureIO.readFixture(fixture)
                .lines()
                .filter(value -> !value.isBlank())
                .toList();
        for (String line : lines) {
            ids.add(jsonSchemaIdFromPayload(TestJson.parse(line), fixture));
        }
        return Set.copyOf(ids);
    }

    private static String explicitJsonSchemaId(AndalusCliGoldenFixtureManifest.Entry entry) {
        if (entry.jsonSchemaId().isBlank()) {
            throw new IllegalArgumentException("Missing explicit JSON Schema id for " + entry.name());
        }
        return entry.jsonSchemaId();
    }

    private static String jsonSchemaIdFromPayload(Object payload, String fixture) {
        Map<String, Object> object = objectValue(payload, fixture + " root JSON value");
        Map<String, Object> contract = objectValue(object.get("contract"), fixture + " contract");
        List<AndalusContractDescriptor> matches = AndalusContractCatalog.discover(AndalusContractQuery.of(
                        String.valueOf(contract.get("schema")),
                        String.valueOf(contract.get("envelope"))))
                .contracts();
        if (matches.size() != 1) {
            throw new IllegalArgumentException(
                    "Expected one published contract descriptor for " + fixture + " but found " + matches.size());
        }
        return matches.get(0).jsonSchemaId();
    }

    private static Map<String, Object> objectValue(Object value, String label) {
        if (!(value instanceof Map<?, ?> source)) {
            throw new IllegalArgumentException("Expected object for " + label);
        }
        Map<String, Object> object = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("Expected string object key for " + label);
            }
            object.put(key, entry.getValue());
        }
        return object;
    }
}
