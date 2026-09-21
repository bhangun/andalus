package tech.kayys.andalus.api.grpc.operator.mapper;

import tech.kayys.andalus.operator.v1.ListSandboxesResponse;
import tech.kayys.andalus.operator.v1.SandboxDiagnostics;
import tech.kayys.andalus.operator.v1.SandboxHealth;
import tech.kayys.andalus.operator.v1.SandboxMetrics;
import tech.kayys.andalus.operator.v1.SandboxSummary;
import tech.kayys.andalus.spi.operator.sandbox.SandboxDiagnosticsSummary;
import tech.kayys.andalus.spi.operator.sandbox.SandboxHealthSummary;
import tech.kayys.andalus.spi.operator.sandbox.SandboxMetricsSummary;

import java.util.List;

public final class SandboxGrpcMapper {

    private SandboxGrpcMapper() {
    }

    public static SandboxSummary toProto(tech.kayys.andalus.spi.operator.sandbox.SandboxSummary sb) {
        if (sb == null) {
            return SandboxSummary.getDefaultInstance();
        }
        return SandboxSummary.newBuilder()
                .setId(sb.sandboxId() != null ? sb.sandboxId() : "")
                .setTenantId(sb.tenantId() != null ? sb.tenantId() : "")
                .setState(sb.state() != null ? sb.state().name() : "")
                .setProviderId(sb.providerId() != null ? sb.providerId() : "")
                .setCreatedAt(sb.createdAt() != null ? sb.createdAt().toString() : "")
                .build();
    }

    public static ListSandboxesResponse toListResponse(List<tech.kayys.andalus.spi.operator.sandbox.SandboxSummary> list) {
        var builder = ListSandboxesResponse.newBuilder();
        if (list != null) {
            for (var sb : list) {
                builder.addSandboxes(toProto(sb));
            }
        }
        return builder.build();
    }

    public static SandboxHealth toHealthProto(SandboxHealthSummary h) {
        if (h == null) {
            return SandboxHealth.getDefaultInstance();
        }
        return SandboxHealth.newBuilder()
                .setSandboxId(h.sandboxId() != null ? h.sandboxId() : "")
                .setStatus(h.status() != null ? h.status().name() : "")
                .setSummary(h.reason() != null ? h.reason() : "")
                .setCheckedAt(h.checkedAt() != null ? h.checkedAt().toString() : "")
                .build();
    }

    public static SandboxMetrics toMetricsProto(SandboxMetricsSummary m) {
        if (m == null) {
            return SandboxMetrics.getDefaultInstance();
        }
        var builder = SandboxMetrics.newBuilder()
                .setSandboxId(m.sandboxId() != null ? m.sandboxId() : "");

        if (m.resource() != null) {
            builder.putMetrics("cpuMillisUsed", (double) m.resource().cpuMillisUsed());
            builder.putMetrics("memoryBytesUsed", (double) m.resource().memoryBytesUsed());
        }
        if (m.network() != null) {
            builder.putMetrics("bytesSent", (double) m.network().bytesSent());
            builder.putMetrics("bytesReceived", (double) m.network().bytesReceived());
        }
        return builder.build();
    }

    public static SandboxDiagnostics toDiagnosticsProto(SandboxDiagnosticsSummary d) {
        if (d == null) {
            return SandboxDiagnostics.getDefaultInstance();
        }
        return SandboxDiagnostics.newBuilder()
                .setSandboxId(d.sandboxId() != null ? d.sandboxId() : "")
                .setStatus(d.health() != null ? d.health() : "UNKNOWN")
                .build();
    }
}
