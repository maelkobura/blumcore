package blum.api.exception;

public class ServiceInitializationException extends RuntimeException {

    private Class<?> service;

    private Exception exception;

    public ServiceInitializationException(String message, Class<?> service, Exception exception) {
        super(message);
        this.service = service;
        this.exception = exception;
    }
}