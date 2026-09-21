package tech.kayys.andalus.operator.runtime.tool;

import tech.kayys.andalus.spi.capability.Capability;
import tech.kayys.andalus.spi.capability.CapabilityDescriptor;
import tech.kayys.andalus.spi.operator.tool.CapabilitySummary;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class CapabilitySummaryMapper {

    private CapabilitySummaryMapper() {
    }

    public static CapabilitySummary map(Capability capability) {
        if (capability == null) {
            return null;
        }

        CapabilityDescriptor desc = capability.descriptor();
        String name = desc != null && desc.attributes().containsKey("name")
                ? String.valueOf(desc.attributes().get("name"))
                : capability.id();

        String description = desc != null ? desc.description() : null;

        String version = desc != null && desc.attributes().containsKey("version")
                ? String.valueOf(desc.attributes().get("version"))
                : "1.0.0";

        String providerId = desc != null && desc.attributes().containsKey("providerId")
                ? String.valueOf(desc.attributes().get("providerId"))
                : null;

        List<String> tags = List.of();
        if (desc != null && desc.attributes().containsKey("tags")) {
            Object tagsObj = desc.attributes().get("tags");
            if (tagsObj instanceof Collection<?> col) {
                tags = col.stream().map(String::valueOf).toList();
            }
        }

        Map<String, Object> attrs = desc != null ? desc.attributes() : Map.of();

        return new CapabilitySummary(
                capability.id(),
                capability.type(),
                name,
                description,
                version,
                providerId,
                true,
                true,
                tags,
                attrs
        );
    }
}
