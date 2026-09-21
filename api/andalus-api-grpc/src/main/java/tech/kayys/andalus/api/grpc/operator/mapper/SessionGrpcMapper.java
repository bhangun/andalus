package tech.kayys.andalus.api.grpc.operator.mapper;

import tech.kayys.andalus.operator.v1.ListSessionsResponse;
import tech.kayys.andalus.operator.v1.SessionSummary;
import tech.kayys.andalus.spi.session.SessionInfo;

import java.util.List;

public final class SessionGrpcMapper {

    private SessionGrpcMapper() {
    }

    public static SessionSummary toProto(SessionInfo info) {
        if (info == null) {
            return SessionSummary.getDefaultInstance();
        }
        return SessionSummary.newBuilder()
                .setSessionId(info.sessionId() != null ? info.sessionId().value() : "")
                .setTenantId(info.tenantId() != null ? info.tenantId() : "")
                .setState(info.state() != null ? info.state().name() : "")
                .setCreatedAt(info.createdAt() != null ? info.createdAt().toString() : "")
                .setUpdatedAt(info.lastActivityAt() != null ? info.lastActivityAt().toString() : "")
                .build();
    }

    public static ListSessionsResponse toListResponse(List<SessionInfo> list) {
        var builder = ListSessionsResponse.newBuilder();
        if (list != null) {
            for (var info : list) {
                builder.addSessions(toProto(info));
            }
        }
        return builder.build();
    }
}
