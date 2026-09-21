package tech.kayys.andalus.cli.operator;

/**
 * Exception thrown when an operator CLI command fails.
 * Holds an exit code compliant with Phase 5.13 exit code specifications.
 */
public class OperatorCliException extends RuntimeException {

    public static final int SUCCESS = 0;
    public static final int GENERAL_ERROR = 1;
    public static final int INVALID_ARGUMENT = 2;
    public static final int UNAUTHENTICATED = 3;
    public static final int PERMISSION_DENIED = 4;
    public static final int NOT_FOUND = 5;
    public static final int CONFLICT = 6;
    public static final int TIMEOUT = 7;
    public static final int UNAVAILABLE = 8;
    public static final int CONFIGURATION_ERROR = 9;

    private final int exitCode;

    public OperatorCliException(String message) {
        this(message, GENERAL_ERROR);
    }

    public OperatorCliException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public OperatorCliException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
