package blum.api.exception;

public class DatabaseInitializationException extends RuntimeException {
    public DatabaseInitializationException(String message, Exception e) {
        super(message,e);
    }
}
