package tech.kayys.andalus.api.grpc.operator.mapper;

import tech.kayys.andalus.operator.v1.DiagnosticIssue;
import tech.kayys.andalus.operator.v1.DiagnosticReport;
import tech.kayys.andalus.operator.v1.DiagnosticResult;

import java.util.List;

public final class DiagnosticsGrpcMapper {

    private DiagnosticsGrpcMapper() {
    }

    public static DiagnosticResult toProto(tech.kayys.andalus.spi.diagnostics.DiagnosticResult dr) {
        if (dr == null) {
            return DiagnosticResult.getDefaultInstance();
        }
        String compType = dr.component() != null && dr.component().type() != null ? dr.component().type().name() : "";
        String compId = dr.component() != null && dr.component().id() != null ? dr.component().id() : "";

        var builder = DiagnosticResult.newBuilder()
                .setComponentType(compType)
                .setComponentId(compId)
                .setStatus(dr.status() != null ? dr.status().name() : "UNKNOWN")
                .setCheckedAt(dr.checkedAt() != null ? dr.checkedAt().toString() : "")
                .setSummary(dr.summary() != null ? dr.summary() : "");

        if (dr.issues() != null) {
            for (var issue : dr.issues()) {
                builder.addIssues(DiagnosticIssue.newBuilder()
                        .setCode(issue.code() != null ? issue.code() : "")
                        .setSeverity(issue.severity() != null ? issue.severity().name() : "")
                        .setMessage(issue.message() != null ? issue.message() : "")
                        .build());
            }
        }
        return builder.build();
    }

    public static DiagnosticReport toReportProto(tech.kayys.andalus.spi.diagnostics.DiagnosticReport report) {
        if (report == null) {
            return DiagnosticReport.getDefaultInstance();
        }
        var builder = DiagnosticReport.newBuilder()
                .setCheckedAt(report.checkedAt() != null ? report.checkedAt().toString() : "")
                .setOverallStatus(report.overallStatus() != null ? report.overallStatus().name() : "UNKNOWN");

        if (report.results() != null) {
            for (var r : report.results()) {
                builder.addResults(toProto(r));
            }
        }
        return builder.build();
    }

    public static DiagnosticReport toReportProto(List<tech.kayys.andalus.spi.diagnostics.DiagnosticResult> list) {
        var builder = DiagnosticReport.newBuilder();
        boolean allHealthy = true;
        if (list != null) {
            for (var dr : list) {
                builder.addResults(toProto(dr));
                if (dr.status() != tech.kayys.andalus.spi.diagnostics.DiagnosticStatus.HEALTHY) {
                    allHealthy = false;
                }
            }
        }
        builder.setOverallStatus(allHealthy ? "HEALTHY" : "DEGRADED");
        return builder.build();
    }
}
