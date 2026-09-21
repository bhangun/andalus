package tech.kayys.andalus.operator.authorization;

import tech.kayys.andalus.spi.operator.authorization.*;

import java.util.Set;

/**
 * Standard operator authorization policy implementation.
 */
public class DefaultOperatorAuthorizationPolicy implements OperatorAuthorizationPolicy {

    private final OperatorPermissionRegistry permissions;
    private final OperatorSubjectResolver subjects;

    public DefaultOperatorAuthorizationPolicy(
            OperatorPermissionRegistry permissions,
            OperatorSubjectResolver subjects) {
        this.permissions = permissions;
        this.subjects = subjects;
    }

    @Override
    public OperatorAuthorizationResult authorize(OperatorAuthorizationRequest request) {
        var definition = permissions.find(request.permission());

        if (definition.isEmpty()) {
            return OperatorAuthorizationResult.deny(
                    "OPERATOR_PERMISSION_UNKNOWN",
                    "Unknown operator permission: " + request.permission()
            );
        }

        OperatorPermissionDefinition permission = definition.get();

        if (permission.scope() != request.scope()) {
            return OperatorAuthorizationResult.deny(
                    "OPERATOR_SCOPE_MISMATCH",
                    "Requested authorization scope does not match permission scope"
            );
        }

        Set<String> granted = subjects.permissions(request.context());

        if (!granted.contains("*") && !granted.contains(request.permission())) {
            return OperatorAuthorizationResult.deny(
                    "OPERATOR_PERMISSION_DENIED",
                    "Operator permission denied"
            );
        }

        if (request.scope() == OperatorScope.TENANT) {
            if (request.context().tenantId() == null || request.context().tenantId().isBlank()) {
                return OperatorAuthorizationResult.deny(
                        "OPERATOR_TENANT_REQUIRED",
                        "Tenant context is required for tenant-scoped operations"
                );
            }
        }

        if (request.scope() == OperatorScope.PLATFORM) {
            if (!isPlatformOperator(subjects.roles(request.context()))) {
                return OperatorAuthorizationResult.deny(
                        "OPERATOR_PLATFORM_ACCESS_DENIED",
                        "Platform operator access denied"
                );
            }
        }

        return OperatorAuthorizationResult.allow(request.scope());
    }

    private boolean isPlatformOperator(Set<String> roles) {
        return roles.contains("andalus-platform-admin")
                || roles.contains("andalus-operator")
                || roles.contains("ADMIN");
    }
}
