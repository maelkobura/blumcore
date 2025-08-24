package blum.api.exception;

import blum.api.services.Service;

public class ServiceRegisteringException extends RuntimeException {

    private Class<?> service;

    public ServiceRegisteringException(String message, Class<?> service) {
        super(message);
        this.service = service;
    }
}
