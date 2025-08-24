package blum.core.services;

import blum.api.services.Service;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceScheduledTask implements Runnable{

    private CoreServicesManager manager;
    private String serviceName;
    private Method method;



    @Override
    public void run() {
        try {
            Service service = manager.getService(serviceName);
            method.invoke(service);

        }catch (Exception e) {
            log.error("Failed to execute scheduled task ({} at {})", serviceName, method.getName(), e);
        }

    }
}
