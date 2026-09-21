package tech.kayys.andalus.operator.rest.dto;

import java.time.Instant;

/**
 * Standard error response envelope for operator REST APIs.
 */
public record OperatorErrorResponse(
        String code,
        String message,
        String requestId,
        Instant timestamp
) {
    public OperatorErrorResponse {
        timestamp = timestamp != null ? timestamp : Instant.now();
    }
}
