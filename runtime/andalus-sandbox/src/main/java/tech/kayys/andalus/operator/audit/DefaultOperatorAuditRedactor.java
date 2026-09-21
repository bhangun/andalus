package tech.kayys.andalus.operator.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Standard audit redactor masking sensitive keys.
 */
public class DefaultOperatorAuditRedactor implements OperatorAuditRedactor {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "passwd",
            "secret",
            "client-secret",
            "client_secret",
            "api-key",
            "api_key",
            "access-token",
            "access_token",
            "refresh-token",
            "refresh_token",
            "authorization",
            "private-key",
            "private_key",
            "credential",
            "credentials"
    );

    @Override
    public Map<String, Object> redact(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (isSensitive(key)) {
                result.put(key, "[REDACTED]");
            } else {
                result.put(key, value);
            }
        });

        return Map.copyOf(result);
    }

    private boolean isSensitive(String key) {
        if (key == null) {
            return false;
        }

        String normalized = key
                .trim()
                .toLowerCase()
                .replace(' ', '-')
                .replace('_', '-');

        for (String s : SENSITIVE_KEYS) {
            if (normalized.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
