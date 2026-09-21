package tech.kayys.andalus.operator.rest;

import jakarta.ws.rs.core.Response;
import tech.kayys.andalus.operator.rest.dto.OperatorErrorResponse;
import tech.kayys.andalus.spi.operator.OperatorResult;

import java.time.Instant;

/**
 * Maps OperatorResult envelopes into standard JAX-RS Response objects.
 */
public final class OperatorResponseMapper {

    private OperatorResponseMapper() {
    }

    public static Response toResponse(OperatorResult<?> result) {
        if (result instanceof OperatorResult.Success<?> success) {
            return Response.ok(success.value()).build();
        }

        if (result instanceof OperatorResult.Failure<?> failure) {
            int status = statusFor(failure.code());
            return Response.status(status)
                    .entity(new OperatorErrorResponse(
                            failure.code(),
                            failure.message(),
                            failure.metadata().getOrDefault("requestId", "unknown").toString(),
                            Instant.now()
                    ))
                    .build();
        }

        return Response.serverError().build();
    }

    private static int statusFor(String code) {
        if (code == null) {
            return 500;
        }
        return switch (code) {
            case "OPERATOR_PERMISSION_DENIED",
                 "OPERATOR_SCOPE_DENIED",
                 "OPERATOR_SCOPE_MISMATCH",
                 "OPERATOR_RESOURCE_ACCESS_DENIED",
                 "OPERATOR_PLATFORM_ACCESS_DENIED" -> 403;

            case "PLUGIN_NOT_FOUND",
                 "SANDBOX_NOT_FOUND",
                 "EXECUTION_NOT_FOUND",
                 "SESSION_NOT_FOUND",
                 "CONFIG_NOT_FOUND",
                 "OPERATOR_RESOURCE_NOT_FOUND" -> 404;

            case "INVALID_PLUGIN_ID",
                 "INVALID_SANDBOX_ID",
                 "INVALID_EXECUTION_ID",
                 "INVALID_SESSION_ID",
                 "INVALID_CONFIG_ID",
                 "OPERATOR_INVALID_REQUEST",
                 "OPERATOR_TENANT_REQUIRED" -> 400;

            case "OPERATOR_CONFLICT",
                 "PLUGIN_ALREADY_ACTIVE",
                 "PLUGIN_ALREADY_STOPPED" -> 409;

            default -> 500;
        };
    }
}
