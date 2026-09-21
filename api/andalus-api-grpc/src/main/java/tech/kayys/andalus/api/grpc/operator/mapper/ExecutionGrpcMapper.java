package tech.kayys.andalus.api.grpc.operator.mapper;

import tech.kayys.andalus.operator.v1.ExecutionSummary;
import tech.kayys.andalus.operator.v1.ListExecutionsResponse;
import tech.kayys.andalus.spi.execution.ExecutionInfo;

import java.util.List;

public final class ExecutionGrpcMapper {

    private ExecutionGrpcMapper() {
    }

    public static ExecutionSummary toProto(ExecutionInfo es) {
        if (es == null) {
            return ExecutionSummary.getDefaultInstance();
        }
        return ExecutionSummary.newBuilder()
                .setExecutionId(es.executionId() != null ? es.executionId() : "")
                .setTenantId(es.tenantId() != null ? es.tenantId() : "")
                .setState(es.state() != null ? es.state().name() : "")
                .setWorkflowId(es.workflowId() != null ? es.workflowId() : "")
                .setCreatedAt(es.createdAt() != null ? es.createdAt().toString() : "")
                .setUpdatedAt(es.updatedAt() != null ? es.updatedAt().toString() : "")
                .build();
    }

    public static ListExecutionsResponse toListResponse(List<ExecutionInfo> list) {
        var builder = ListExecutionsResponse.newBuilder();
        if (list != null) {
            for (var es : list) {
                builder.addExecutions(toProto(es));
            }
        }
        return builder.build();
    }
}
