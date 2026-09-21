package tech.kayys.andalus.operator.authorization;

import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.authorization.OperatorSubjectResolver;

import java.util.HashSet;
import java.util.Set;

/**
 * Default subject resolver extracting identity, roles, and permissions from OperatorContext.
 */
public class DefaultOperatorSubjectResolver implements OperatorSubjectResolver {

    private final Set<String> defaultRoles = new HashSet<>();
    private final Set<String> defaultPermissions = new HashSet<>();

    public DefaultOperatorSubjectResolver() {
        defaultRoles.add("andalus-platform-admin");
        defaultRoles.add("andalus-operator");
    }

    public void grantPermission(String permission) {
        defaultPermissions.add(permission);
    }

    public void grantRole(String role) {
        defaultRoles.add(role);
    }

    @Override
    public String subject(OperatorContext context) {
        return context != null && context.userId() != null ? context.userId() : "anonymous-operator";
    }

    @Override
    public Set<String> roles(OperatorContext context) {
        if (context != null) {
            var attr = context.attribute("roles");
            if (attr.isPresent() && attr.get() instanceof Set<?> r) {
                Set<String> res = new HashSet<>();
                for (Object obj : r) {
                    if (obj != null) res.add(obj.toString());
                }
                return res;
            }
        }
        return Set.copyOf(defaultRoles);
    }

    @Override
    public Set<String> permissions(OperatorContext context) {
        if (context != null) {
            var attr = context.attribute("permissions");
            if (attr.isPresent() && attr.get() instanceof Set<?> p) {
                Set<String> res = new HashSet<>();
                for (Object obj : p) {
                    if (obj != null) res.add(obj.toString());
                }
                return res;
            }
        }
        return defaultPermissions.isEmpty() ? Set.of("*") : Set.copyOf(defaultPermissions);
    }
}
