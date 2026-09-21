package tech.kayys.andalus.api.grpc.operator.mapper;

import tech.kayys.andalus.operator.v1.CapabilitySummary;
import tech.kayys.andalus.operator.v1.ListCapabilitiesResponse;

import java.util.List;

public final class CapabilityGrpcMapper {

    private CapabilityGrpcMapper() {
    }

    public static CapabilitySummary toProto(tech.kayys.andalus.spi.operator.tool.CapabilitySummary cs) {
        if (cs == null) {
            return CapabilitySummary.getDefaultInstance();
        }
        var builder = CapabilitySummary.newBuilder()
                .setId(cs.id() != null ? cs.id() : "")
                .setType(cs.type() != null ? cs.type().qualifiedName() : "")
                .setProviderId(cs.providerId() != null ? cs.providerId() : "");

        if (cs.tags() != null) {
            builder.addAllTags(cs.tags());
        }
        return builder.build();
    }

    public static ListCapabilitiesResponse toListResponse(List<tech.kayys.andalus.spi.operator.tool.CapabilitySummary> list) {
        var builder = ListCapabilitiesResponse.newBuilder();
        if (list != null) {
            for (var cs : list) {
                builder.addCapabilities(toProto(cs));
            }
        }
        return builder.build();
    }
}
