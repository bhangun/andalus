package tech.kayys.andalus.tool.mcp;

import tech.kayys.andalus.error.ErrorCode;
import tech.kayys.andalus.error.AndalusException;

import java.util.function.Function;

final class McpResourceFailures {

    private McpResourceFailures() {
    }

    static Function<Throwable, Throwable> andalus(
            ErrorCode errorCode,
            String messagePrefix) {
        return throwable -> new AndalusException(
                errorCode,
                messagePrefix + ": " + throwable.getMessage(),
                throwable);
    }
}
