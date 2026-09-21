package tech.kayys.andalus.operator.audit;

import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.audit.OperatorAuditOutcome;
import tech.kayys.andalus.spi.operator.audit.OperatorAuditService;
import tech.kayys.andalus.spi.operator.authorization.OperatorScope;

import java.util.Map;

/**
 * Zero-overhead no-op implementation when operator audit is disabled.
 */
public final class NoOpOperatorAuditService implements OperatorAuditService {

    @Override
    public void record(
            OperatorContext context,
            String operation,
            String permission,
            OperatorScope scope,
            String resourceType,
            String resourceId,
            boolean destructive,
            boolean privileged,
            OperatorAuditOutcome outcome,
            String authorizationCode,
            String message,
            Map<String, Object> before,
            Map<String, Object> after,
            Map<String, Object> attributes) {
        // Zero-overhead no-op
    }
}
