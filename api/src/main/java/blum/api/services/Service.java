package blum.api.services;

import blum.api.exception.ServiceInitializationException;
import blum.api.exception.ServiceStartException;

public interface Service {

    public void start() throws ServiceStartException;
    public void stop() throws ServiceStartException;

}
