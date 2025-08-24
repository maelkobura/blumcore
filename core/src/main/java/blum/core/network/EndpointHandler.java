package blum.core.network;

import blum.api.network.annotations.Request;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class EndpointHandler {

    private String path;
    private Map<Request.Type, EndpointExecutor> executors;

    public EndpointHandler(String path) {
        this.path = path;
        this.executors = new ConcurrentHashMap<>();
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) {
        Request.Type type = Request.Type.valueOf(request.getMethod());
        EndpointExecutor endpoint = executors.get(type);
        if(endpoint == null) {
            response.setContentType("text/plain; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        endpoint.handle(request, response);
    }

}
