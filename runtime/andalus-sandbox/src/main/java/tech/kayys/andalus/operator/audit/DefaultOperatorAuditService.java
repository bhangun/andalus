package tech.kayys.andalus.operator.audit;

import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.audit.OperatorAuditEvent;
import tech.kayys.andalus.spi.operator.audit.OperatorAuditOutcome;
import tech.kayys.andalus.spi.operator.audit.OperatorAuditService;
import tech.kayys.andalus.spi.operator.audit.OperatorAuditSink;
import tech.kayys.andalus.spi.operator.authorization.OperatorScope;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Standard implementation of OperatorAuditService that redacts sensitive values,
 * isolates audit failures from caller operations, and dispatches to registered sinks.
 */
public class DefaultOperatorAuditService implements OperatorAuditService {

    private static final int MAX_BUFFER_SIZE = 1000;

    private final OperatorAuditRedactor redactor;
    private final List<OperatorAuditSink> sinks = new ArrayList<>();
    private final Deque<OperatorAuditEvent> recentEvents = new ConcurrentLinkedDeque<>();

    public DefaultOperatorAuditService() {
        this(new DefaultOperatorAuditRedactor());
    }

    public DefaultOperatorAuditService(OperatorAuditRedactor redactor) {
        this.redactor = redactor != null ? redactor : new DefaultOperatorAuditRedactor();
    }

    public void registerSink(OperatorAuditSink sink) {
        if (sink != null) {
            sinks.add(sink);
        }
    }

    public List<OperatorAuditEvent> getRecentEvents() {
        return List.copyOf(recentEvents);
    }

    public void clear() {
        recentEvents.clear();
    }

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

        try {
            var event = new OperatorAuditEvent(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    context != null ? context.tenantId() : null,
                    context != null ? context.userId() : null,
                    context != null ? context.correlationId() : null,
                    context != null ? context.requestId() : null,
                    operation,
                    permission,
                    scope != null ? scope : OperatorScope.TENANT,
                    resourceType,
                    resourceId,
                    destructive,
                    privileged,
                    outcome,
                    authorizationCode,
                    message,
                    redactor.redact(before),
                    redactor.redact(after),
                    redactor.redact(attributes)
            );

            recentEvents.addFirst(event);
            while (recentEvents.size() > MAX_BUFFER_SIZE) {
                recentEvents.pollLast();
            }

            for (OperatorAuditSink sink : sinks) {
                try {
                    sink.publish(event);
                } catch (Exception ignored) {
                    // Sinks must not throw back into the caller
                }
            }
        } catch (Exception ignored) {
            // Operator audit failures must never fail the underlying business operation
        }
    }
}
