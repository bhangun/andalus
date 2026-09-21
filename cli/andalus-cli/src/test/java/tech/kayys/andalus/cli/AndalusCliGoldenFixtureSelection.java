package tech.kayys.andalus.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class AndalusCliGoldenFixtureSelection {
    static final String UPDATE_INCLUDE_PROPERTY = "andalus.golden.update.include";

    private final Set<String> includes;

    private AndalusCliGoldenFixtureSelection(Set<String> includes) {
        this.includes = Collections.unmodifiableSet(new LinkedHashSet<>(includes));
    }

    static AndalusCliGoldenFixtureSelection fromSystemProperties() {
        return from(System.getProperty(UPDATE_INCLUDE_PROPERTY, ""));
    }

    static AndalusCliGoldenFixtureSelection from(String raw) {
        if (raw == null || raw.isBlank()) {
            return new AndalusCliGoldenFixtureSelection(Set.of());
        }
        Set<String> names = new LinkedHashSet<>();
        for (String value : raw.split(",")) {
            String normalized = value.trim();
            if (!normalized.isEmpty()) {
                names.add(normalized);
            }
        }
        return new AndalusCliGoldenFixtureSelection(names);
    }

    boolean selected(String name) {
        return includes.isEmpty()
                || includes.contains(name)
                || includes.contains(withoutGoldenSuffix(name));
    }

    List<String> unknownIncludes(Set<String> fixtureNames) {
        List<String> unknown = new ArrayList<>();
        for (String include : includes) {
            if (!fixtureNames.contains(include) && !fixtureNames.contains(include + ".golden")) {
                unknown.add(include);
            }
        }
        return List.copyOf(unknown);
    }

    private static String withoutGoldenSuffix(String name) {
        return name != null && name.endsWith(".golden")
                ? name.substring(0, name.length() - ".golden".length())
                : name;
    }
}
