package blum.core.services;

import blum.api.annotation.Scheduled;
import blum.api.exception.ServiceInitializationException;
import blum.api.exception.ServiceRegisteringException;
import blum.api.exception.UnknownServiceException;
import blum.api.services.Service;
import blum.api.services.ServicesManager;
import blum.api.services.annotation.ServiceDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CoreServicesManager implements ServicesManager {

    private final Map<ServiceDescriptor, Service> services = new ConcurrentHashMap<>();
    private final Map<ServiceDescriptor, Class<? extends Service>> pendingServices = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;
    private final Map<String, List<ScheduledFuture<?>>> scheduledTasks = new ConcurrentHashMap<>();

    private volatile boolean shutdown = false;

    public CoreServicesManager(int schedulerThreads) {
        log.info("Initializing ServicesManager...");
        this.scheduler = Executors.newScheduledThreadPool(schedulerThreads);
    }

    @Override
    public synchronized void registerService(Class<?> service) {
        if(shutdown) {
            throw new IllegalStateException("ServicesManager has been shutdown");
        }

        validateServiceClass(service);

        ServiceDescriptor description = service.getAnnotation(ServiceDescriptor.class);
        validateServiceDescriptor(service, description);
        checkDuplicateService(service, description);

        log.info("Registering {} ({})...", description.displayName(), description.name());
        pendingServices.put(description, (Class<? extends Service>) service);
        log.debug("Added {} to pending services loading list", description.name());

        loadServices();
    }

    private void validateServiceClass(Class<?> service) {
        if(service == null) {
            log.error("Trying to register service but given class is null");
            throw new ServiceRegisteringException("Given class is null", null);
        }

        if(!Service.class.isAssignableFrom(service)) {
            log.error("Trying to register service but class doesn't implement Service interface ({})", service.getName());
            throw new ServiceRegisteringException("Service must implement Service interface (" + service.getName()+")", service);
        }
    }

    private void validateServiceDescriptor(Class<?> service, ServiceDescriptor description) {
        if(description == null) {
            log.error("Trying to register service but @ServiceDescriptor is missing ({})", service.getName());
            throw new ServiceRegisteringException("Service must have @ServiceDescriptor annotation (" + service.getName()+")", service);
        }
    }

    private void checkDuplicateService(Class<?> service, ServiceDescriptor description) {
        boolean exists = Stream.concat(
                services.keySet().stream(),
                pendingServices.keySet().stream()
        ).anyMatch(desc -> desc.name().equals(description.name()));

        if(exists) {
            log.error("Trying to register service but service with same name already exists ({})", service.getName());
            throw new ServiceRegisteringException("Service already exists (" + service.getName()+")", service);
        }
    }

    private void loadServices() {
        log.debug("Loading all pending services...");

        List<ServiceDescriptor> loaded = new ArrayList<>();
        boolean progress;

        do {
            progress = false;
            for (Map.Entry<ServiceDescriptor, Class<? extends Service>> entry : pendingServices.entrySet()) {
                if(canLoadService(entry.getKey()) && !loaded.contains(entry.getKey())) {
                    try {
                        loadService(entry.getKey(), entry.getValue());
                        loaded.add(entry.getKey());
                        progress = true;
                    }catch (ServiceInitializationException e) {
                        log.error("Failed to start service ({})", entry.getKey().name(), e);
                    }
                }
            }
        } while (progress && !loaded.isEmpty());

        loaded.forEach(pendingServices::remove);

        if(!pendingServices.isEmpty()) {
            log.warn("Some services could not be loaded due to unsatisfied dependencies: {}",
                    pendingServices.keySet().stream()
                            .map(ServiceDescriptor::name)
                            .collect(Collectors.joining(", ")));
        }
    }



    private boolean canLoadService(ServiceDescriptor descriptor) {
        return Arrays.stream(descriptor.dependencies()).allMatch(this::hasService);
    }

    private void loadService(ServiceDescriptor descriptor, Class<? extends Service> clazz) throws ServiceInitializationException {
        log.info("Loading {} ({})...", descriptor.displayName(), descriptor.name());

        Service service;
        try {
            service = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Failed to initialize service ({})", descriptor.name(), e);
            throw new ServiceInitializationException("Failed to initialize service", clazz, e);
        }

        try {
            log.debug("Starting {} ({})...", descriptor.displayName(), descriptor.name());
            service.start();

            scheduleServiceTasks(service, clazz, descriptor.name());

            services.put(descriptor, service);
            log.info("Loaded {} ({})", descriptor.displayName(), descriptor.name());

        } catch (Exception e) {
            log.error("Failed to start service ({})", descriptor.name(), e);
            try {
                service.stop();
            } catch (Exception stopException) {
                log.warn("Error while stopping failed service", stopException);
            }
            throw new ServiceInitializationException("Failed to start service", clazz, e);
        }
    }

    private void scheduleServiceTasks(Service service, Class<? extends Service> clazz, String serviceName) {
        List<ScheduledFuture<?>> futures = new ArrayList<>();

        for(Method method : clazz.getDeclaredMethods()) {
            if(method.isAnnotationPresent(Scheduled.class)) {
                Scheduled annotation = method.getAnnotation(Scheduled.class);

                ServiceScheduledTask task = new ServiceScheduledTask(this, serviceName, method);

                int time = annotation.value();
                TimeUnit unit = annotation.unit();

                ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0, time, unit);
                futures.add(future);

                log.debug("Scheduled task for method {} of service {} every {} {}",
                        method.getName(), serviceName, time, unit);
            }
        }

        if(!futures.isEmpty()) {
            scheduledTasks.put(serviceName, futures);
        }
    }

    @Override
    public Service getService(String name) {
        Service service = services.entrySet().stream()
                .filter(entry -> entry.getKey().name().equals(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if(service == null) {
            throw new UnknownServiceException("Service not found: " + name);
        }

        return service;
    }

    @Override
    public <T extends Service> T getService(String name, Class<T> clazz) {
        Service service = getService(name);

        if(clazz.isAssignableFrom(service.getClass())) {
            return clazz.cast(service);
        }

        throw new UnknownServiceException("Service " + name + " is not of type " + clazz.getName());
    }

    @Override
    public Iterator<Service> getServices() {
        return new ArrayList<>(services.values()).iterator();
    }

    @Override
    public boolean hasService(String serviceName) {
        return services.keySet().stream()
                .anyMatch(descriptor -> descriptor.name().equals(serviceName));
    }

    public void shutdown() {
        log.info("Shutting down services manager...");
        shutdown = true;

        scheduledTasks.values().forEach(futures ->
                futures.forEach(future -> future.cancel(true)));

        for(Service service : services.values()) {
            try {
                service.stop();
            } catch (Exception e) {
                log.warn("Error stopping service", e);
            }
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        services.clear();
        pendingServices.clear();
        scheduledTasks.clear();

        log.info("Services manager shutdown complete");
    }
}
