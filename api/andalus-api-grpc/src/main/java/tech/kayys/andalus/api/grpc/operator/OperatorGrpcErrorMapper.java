package tech.kayys.andalus.api.grpc.operator;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import tech.kayys.andalus.spi.operator.OperatorResult;

/**
 * Maps OperatorResult failures into standard gRPC StatusRuntimeExceptions with error sanitization.
 */
public final class OperatorGrpcErrorMapper {

    private OperatorGrpcErrorMapper() {
    }

    public static StatusRuntimeException toStatusException(OperatorResult.Failure<?> failure) {
        if (failure == null) {
            return Status.INTERNAL.withDescription("Unknown internal operator error").asRuntimeException();
        }

        String code = failure.code() != null ? failure.code() : "UNKNOWN";
        String message = sanitizeMessage(failure.message());

        Status status = switch (code) {
            case "OPERATOR_INVALID_REQUEST", "INVALID_ARGUMENT", "VALIDATION_FAILED" ->
                    Status.INVALID_ARGUMENT;

            case "UNAUTHENTICATED", "OPERATOR_UNAUTHENTICATED" ->
                    Status.UNAUTHENTICATED;

            case "OPERATOR_PERMISSION_DENIED", "OPERATOR_SCOPE_DENIED", "PERMISSION_DENIED", "ACCESS_DENIED" ->
                    Status.PERMISSION_DENIED;

            case "PLUGIN_NOT_FOUND", "SANDBOX_NOT_FOUND", "EXECUTION_NOT_FOUND",
                 "SESSION_NOT_FOUND", "CONFIG_NOT_FOUND", "NOT_FOUND" ->
                    Status.NOT_FOUND;

            case "OPERATOR_CONFLICT", "CONFLICT", "ALREADY_EXISTS" ->
                    Status.ALREADY_EXISTS;

            case "LIFECYCLE_CONFLICT", "FAILED_PRECONDITION", "ILLEGAL_STATE" ->
                    Status.FAILED_PRECONDITION;

            case "TIMEOUT", "DEADLINE_EXCEEDED" ->
                    Status.DEADLINE_EXCEEDED;

            case "SERVICE_UNAVAILABLE", "UNAVAILABLE" ->
                    Status.UNAVAILABLE;

            default -> Status.INTERNAL;
        };

        return status.withDescription(message).asRuntimeException();
    }

    private static String sanitizeMessage(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Operation failed";
        }
        // Sanitize sensitive patterns
        String sanitized = raw.replaceAll("(?i)(password|token|secret|key|bearer)\\s*=\\s*[^,;\\s]+", "$1=******");
        // Sanitize stack trace references
        if (sanitized.contains("at tech.kayys.") || sanitized.contains("Exception:")) {
            return "An internal server error occurred while processing the request";
        }
        return sanitized;
    }
}
