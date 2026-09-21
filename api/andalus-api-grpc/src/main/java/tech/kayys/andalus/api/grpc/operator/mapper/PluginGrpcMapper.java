package tech.kayys.andalus.api.grpc.operator.mapper;

import tech.kayys.andalus.operator.v1.GetPluginResponse;
import tech.kayys.andalus.operator.v1.ListPluginsResponse;
import tech.kayys.andalus.operator.v1.PluginSummary;

import java.util.List;

public final class PluginGrpcMapper {

    private PluginGrpcMapper() {
    }

    public static PluginSummary toProto(tech.kayys.andalus.spi.operator.plugin.PluginSummary ps) {
        if (ps == null) {
            return PluginSummary.getDefaultInstance();
        }
        return PluginSummary.newBuilder()
                .setId(ps.id() != null ? ps.id() : "")
                .setName(ps.name() != null ? ps.name() : "")
                .setVersion(ps.version() != null ? ps.version() : "")
                .setDescription(ps.description() != null ? ps.description() : "")
                .setState(ps.state() != null ? ps.state().name() : "")
                .build();
    }

    public static GetPluginResponse toGetResponse(tech.kayys.andalus.spi.operator.plugin.PluginSummary ps) {
        return GetPluginResponse.newBuilder()
                .setPlugin(toProto(ps))
                .build();
    }

    public static ListPluginsResponse toListResponse(List<tech.kayys.andalus.spi.operator.plugin.PluginSummary> list) {
        var builder = ListPluginsResponse.newBuilder();
        if (list != null) {
            for (var ps : list) {
                builder.addPlugins(toProto(ps));
            }
        }
        return builder.build();
    }
}
