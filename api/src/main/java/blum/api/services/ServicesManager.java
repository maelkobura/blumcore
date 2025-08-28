package blum.api.services;

import blum.api.exception.ServiceInitializationException;
import blum.api.exception.ServiceRegisteringException;
import blum.api.exception.ServiceStartException;
import blum.api.exception.UnknownServiceException;

import java.util.Iterator;

public interface ServicesManager {

    public void registerService(Class<? extends Service> service) throws ServiceRegisteringException;

    public Service getService(String name) throws UnknownServiceException;
    public <T extends Service> T getService(String name, Class<T> clazz) throws UnknownServiceException;

    public Iterator<Service> getServices();
    public boolean hasService(String service);

    public String getServiceName(Class<? extends Service> service);


}


