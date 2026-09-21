package tech.kayys.andalus.api.grpc.operator.mapper;

import tech.kayys.andalus.operator.v1.ListToolsResponse;
import tech.kayys.andalus.operator.v1.ToolSummary;

import java.util.List;

public final class ToolGrpcMapper {

    private ToolGrpcMapper() {
    }

    public static ToolSummary toProto(tech.kayys.andalus.spi.operator.tool.ToolSummary ts) {
        if (ts == null) {
            return ToolSummary.getDefaultInstance();
        }
        return ToolSummary.newBuilder()
                .setId(ts.name() != null ? ts.name() : "")
                .setName(ts.name() != null ? ts.name() : "")
                .setDescription(ts.description() != null ? ts.description() : "")
                .build();
    }

    public static ListToolsResponse toListResponse(List<tech.kayys.andalus.spi.operator.tool.ToolSummary> list) {
        var builder = ListToolsResponse.newBuilder();
        if (list != null) {
            for (var ts : list) {
                builder.addTools(toProto(ts));
            }
        }
        return builder.build();
    }
}
