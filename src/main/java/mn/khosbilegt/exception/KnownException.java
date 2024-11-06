package mn.khosbilegt.exception;

public class KnownException extends RuntimeException {
    public KnownException(String message) {
        super(message);
    }

    public KnownException(String message, Throwable cause) {
        super(message, cause);
    }
}
