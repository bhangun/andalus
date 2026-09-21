package tech.kayys.andalus.tool.nono;

public class NonoException extends RuntimeException {
    public NonoException(String message) {
        super(message);
    }

    public NonoException(String message, Throwable cause) {
        super(message, cause);
    }
}
