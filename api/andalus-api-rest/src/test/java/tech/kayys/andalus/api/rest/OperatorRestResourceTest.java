package tech.kayys.andalus.api.rest;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.operator.rest.OperatorResponseMapper;
import tech.kayys.andalus.operator.rest.OperatorRestContextResolver;
import tech.kayys.andalus.operator.rest.configuration.ConfigurationOperatorResource;
import tech.kayys.andalus.operator.rest.dto.OperatorErrorResponse;
import tech.kayys.andalus.operator.rest.plugin.PluginOperatorResource;
import tech.kayys.andalus.spi.operator.OperatorContext;
import tech.kayys.andalus.spi.operator.OperatorResult;
import tech.kayys.andalus.spi.operator.configuration.ConfigurationOperatorService;
import tech.kayys.andalus.spi.operator.configuration.ConfigurationSummary;
import tech.kayys.andalus.spi.operator.plugin.PluginOperatorService;
import tech.kayys.andalus.spi.operator.plugin.PluginSummary;
import tech.kayys.andalus.spi.plugin.PluginState;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OperatorRestResourceTest {

    private OperatorRestContextResolver contextResolver;

    @BeforeEach
    void setUp() {
        contextResolver = new OperatorRestContextResolver();
    }

    @Test
    void testPluginOperatorResourceListAndInspect() {
        PluginOperatorService mockService = (PluginOperatorService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{PluginOperatorService.class},
                (proxy, method, args) -> {
                    if ("list".equals(method.getName())) {
                        return OperatorResult.success(List.of(
                                new PluginSummary("p-1", "Plugin 1", "1.0", "Desc", PluginState.ACTIVE, List.of())
                        ));
                    }
                    if ("inspect".equals(method.getName())) {
                        String id = (String) args[1];
                        if ("p-1".equals(id)) {
                            return OperatorResult.success(
                                    new PluginSummary("p-1", "Plugin 1", "1.0", "Desc", PluginState.ACTIVE, List.of())
                            );
                        }
                        return OperatorResult.failure("PLUGIN_NOT_FOUND", "Plugin not found");
                    }
                    return OperatorResult.success(null);
                }
        );

        var resource = new PluginOperatorResource(mockService, contextResolver);

        Response listResp = resource.list();
        assertEquals(200, listResp.getStatus());
        assertNotNull(listResp.getEntity());

        Response inspectResp = resource.inspect("p-1");
        assertEquals(200, inspectResp.getStatus());

        Response notFoundResp = resource.inspect("unknown");
        assertEquals(404, notFoundResp.getStatus());
    }

    @Test
    void testConfigurationOperatorResource() {
        ConfigurationOperatorService mockService = (ConfigurationOperatorService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ConfigurationOperatorService.class},
                (proxy, method, args) -> {
                    if ("active".equals(method.getName())) {
                        return OperatorResult.success(new ConfigurationSummary(
                                "default", "default-cfg", "DEFAULT", "APPLICATION", null, "ACTIVE",
                                Map.of("key", "val"), Instant.now()
                        ));
                    }
                    return OperatorResult.success(null);
                }
        );

        var resource = new ConfigurationOperatorResource(mockService, contextResolver);
        Response activeResp = resource.active();
        assertEquals(200, activeResp.getStatus());
    }

    @Test
    void testOperatorResponseMapperErrorCodes() {
        Response deniedResp = OperatorResponseMapper.toResponse(
                OperatorResult.failure("OPERATOR_PERMISSION_DENIED", "Access denied")
        );
        assertEquals(403, deniedResp.getStatus());
        assertInstanceOf(OperatorErrorResponse.class, deniedResp.getEntity());

        Response conflictResp = OperatorResponseMapper.toResponse(
                OperatorResult.failure("OPERATOR_CONFLICT", "State conflict")
        );
        assertEquals(409, conflictResp.getStatus());

        Response badRequestResp = OperatorResponseMapper.toResponse(
                OperatorResult.failure("OPERATOR_INVALID_REQUEST", "Invalid input")
        );
        assertEquals(400, badRequestResp.getStatus());
    }
}
