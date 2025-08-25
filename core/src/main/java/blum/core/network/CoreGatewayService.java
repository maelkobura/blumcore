package blum.core.network;

import blum.api.core.Blum;
import blum.api.exception.ServiceStartException;
import blum.api.network.GatewayService;
import blum.api.network.annotations.Endpoints;
import blum.api.network.annotations.Request;
import blum.api.services.Service;
import blum.api.services.annotation.ServiceDescriptor;
import lombok.Getter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@ServiceDescriptor(
        name = "gateway",
        displayName = "Gateway Service",
        description = "Access point for frontend application",
        version = "1.0.0"
)
public class CoreGatewayService implements Service, GatewayService {

    private Map<String, EndpointHandler> endpoints;

    private Server server;
    private CoreHttpHandler httpHandler;



    @Override
    public void start() throws ServiceStartException {
        endpoints = new ConcurrentHashMap<>();

        server = new Server(Blum.getCoreConfiguration().getNetworkPort());
        httpHandler = new CoreHttpHandler(this);


        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(httpHandler);

        server.setHandler(handlers);

        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws ServiceStartException {
        endpoints.clear();
    }

    @Override
    public void registerEndpoints(Object obj) {
        if(!obj.getClass().isAnnotationPresent(Endpoints.class)) throw new IllegalArgumentException("The given object isn't a Endpoint class");

        String basePath = obj.getClass().getAnnotation(Endpoints.class).value().strip();

        for(Method mtd : obj.getClass().getDeclaredMethods()) {
            if(!mtd.canAccess(obj)) continue;
            if(!Request.hasRequestAnnotation(mtd)) continue;

            String path = basePath;

            Request.RequestInfo info = Request.extract(mtd);
            if(!info.path().isBlank()) {
                path = basePath + "\\" + info.path().strip();
            }

            saveEndpoint(path, info.type(), obj, mtd);
        }
    }

    private void saveEndpoint(String path, Request.Type type, Object obj, Method mtd) {
        if(!endpoints.containsKey(path)) {
            endpoints.put(path, new EndpointHandler(path));
        }

        endpoints.get(path).getExecutors().put(type, new EndpointExecutor(obj, mtd));
    }


}
