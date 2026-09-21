package tech.kayys.andalus.operator.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import tech.kayys.andalus.spi.operator.OperatorContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves OperatorContext from incoming HTTP request headers and security context.
 */
@RequestScoped
public class OperatorRestContextResolver {

    @Context
    HttpHeaders headers;

    public OperatorContext resolve() {
        String tenantId = getHeader("X-Tenant-ID", "default");
        String userId = getHeader("X-User-ID", "operator");
        String correlationId = getHeader("X-Correlation-ID", UUID.randomUUID().toString());
        String requestId = getHeader("X-Request-ID", UUID.randomUUID().toString());

        Map<String, Object> attributes = new HashMap<>();
        if (headers != null) {
            String userAgent = headers.getHeaderString("User-Agent");
            if (userAgent != null) {
                attributes.put("userAgent", userAgent);
            }
            String forwardedFor = headers.getHeaderString("X-Forwarded-For");
            if (forwardedFor != null) {
                attributes.put("forwardedFor", forwardedFor);
            }
        }

        return new OperatorContext(
                tenantId,
                userId,
                correlationId,
                requestId,
                attributes
        );
    }

    private String getHeader(String name, String defaultValue) {
        if (headers != null) {
            String val = headers.getHeaderString(name);
            if (val != null && !val.isBlank()) {
                return val;
            }
        }
        return defaultValue;
    }
}
