package blum.api.exception;

public class ServiceStartException extends RuntimeException {
    public ServiceStartException(String message, Exception e) {
        super(message,e);
    }
}
