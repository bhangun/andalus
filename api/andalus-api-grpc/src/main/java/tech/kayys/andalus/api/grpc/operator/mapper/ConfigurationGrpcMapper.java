package tech.kayys.andalus.api.grpc.operator.mapper;

import tech.kayys.andalus.operator.v1.ConfigurationSummary;
import tech.kayys.andalus.operator.v1.ListConfigurationsResponse;

import java.util.List;

public final class ConfigurationGrpcMapper {

    private ConfigurationGrpcMapper() {
    }

    public static ConfigurationSummary toProto(tech.kayys.andalus.spi.operator.configuration.ConfigurationSummary cs) {
        if (cs == null) {
            return ConfigurationSummary.getDefaultInstance();
        }
        var builder = ConfigurationSummary.newBuilder()
                .setId(cs.id() != null ? cs.id() : "")
                .setType(cs.type() != null ? cs.type() : "")
                .setSource(cs.source() != null ? cs.source() : "")
                .setStatus(cs.status() != null ? cs.status() : "")
                .setActive("ACTIVE".equalsIgnoreCase(cs.status()))
                .setLastModifiedAt(cs.lastModifiedAt() != null ? cs.lastModifiedAt().toString() : "");

        if (cs.values() != null) {
            cs.values().forEach((k, v) -> builder.putValues(k, String.valueOf(v)));
        }
        return builder.build();
    }

    public static ListConfigurationsResponse toListResponse(List<tech.kayys.andalus.spi.operator.configuration.ConfigurationSummary> list) {
        var builder = ListConfigurationsResponse.newBuilder();
        if (list != null) {
            for (var cs : list) {
                builder.addConfigurations(toProto(cs));
            }
        }
        return builder.build();
    }
}
