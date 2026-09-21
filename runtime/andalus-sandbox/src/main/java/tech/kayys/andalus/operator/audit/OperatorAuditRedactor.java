package tech.kayys.andalus.operator.audit;

import java.util.Map;

/**
 * Redactor interface to ensure no plaintext passwords, secrets, or tokens enter audit logs.
 */
public interface OperatorAuditRedactor {

    Map<String, Object> redact(Map<String, Object> values);
}
