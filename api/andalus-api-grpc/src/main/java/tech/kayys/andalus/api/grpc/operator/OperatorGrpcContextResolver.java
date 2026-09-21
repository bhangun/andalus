package tech.kayys.andalus.api.grpc.operator;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.andalus.operator.v1.OperatorRequestContext;
import tech.kayys.andalus.spi.operator.OperatorContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves unified OperatorContext from gRPC requests and metadata.
 */
@ApplicationScoped
public class OperatorGrpcContextResolver {

    public OperatorContext resolve(OperatorRequestContext protoCtx) {
        String corrId = protoCtx != null && !protoCtx.getCorrelationId().isBlank()
                ? protoCtx.getCorrelationId()
                : UUID.randomUUID().toString();

        String reqId = protoCtx != null && !protoCtx.getRequestId().isBlank()
                ? protoCtx.getRequestId()
                : UUID.randomUUID().toString();

        Map<String, Object> attrs = new HashMap<>();
        if (protoCtx != null) {
            attrs.putAll(protoCtx.getAttributesMap());
        }

        String tenantId = attrs.containsKey("tenantId")
                ? String.valueOf(attrs.get("tenantId"))
                : "default";

        String userId = attrs.containsKey("userId")
                ? String.valueOf(attrs.get("userId"))
                : "grpc-operator";

        return new OperatorContext(tenantId, userId, corrId, reqId, attrs);
    }
}
