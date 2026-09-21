package tech.kayys.andalus.sandbox.filesystem;

public final class FilesystemAccessException extends RuntimeException {

    public FilesystemAccessException(String message) {
        super(message);
    }

    public FilesystemAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
