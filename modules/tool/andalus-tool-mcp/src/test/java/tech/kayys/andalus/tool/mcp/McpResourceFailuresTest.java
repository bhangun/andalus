package tech.kayys.andalus.tool.mcp;

import org.junit.jupiter.api.Test;
import tech.kayys.andalus.error.ErrorCode;
import tech.kayys.andalus.error.AndalusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class McpResourceFailuresTest {

    @Test
    void andalusMapsFailureWithCodeMessageAndCause() {
        RuntimeException cause = new RuntimeException("executor failure");

        Throwable mapped = McpResourceFailures.andalus(
                        ErrorCode.TOOL_EXECUTION_FAILED,
                        "Execution failed")
                .apply(cause);

        AndalusException error = assertInstanceOf(AndalusException.class, mapped);
        assertEquals(ErrorCode.TOOL_EXECUTION_FAILED, error.getErrorCode());
        assertEquals("Execution failed: executor failure", error.getMessage());
        assertSame(cause, error.getCause());
    }
}
