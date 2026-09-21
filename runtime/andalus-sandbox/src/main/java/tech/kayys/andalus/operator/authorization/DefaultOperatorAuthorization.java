package tech.kayys.andalus.operator.authorization;

import tech.kayys.andalus.spi.operator.OperatorAuthorization;
import tech.kayys.andalus.spi.operator.authorization.OperatorAuthorizationPolicy;
import tech.kayys.andalus.spi.operator.authorization.OperatorAuthorizationRequest;
import tech.kayys.andalus.spi.operator.authorization.OperatorAuthorizationResult;

/**
 * Implementation of OperatorAuthorization delegating to OperatorAuthorizationPolicy.
 */
public class DefaultOperatorAuthorization implements OperatorAuthorization {

    private final OperatorAuthorizationPolicy policy;

    public DefaultOperatorAuthorization(OperatorAuthorizationPolicy policy) {
        this.policy = policy;
    }

    @Override
    public OperatorAuthorizationResult authorize(OperatorAuthorizationRequest request) {
        return policy.authorize(request);
    }
}
