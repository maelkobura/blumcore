package blum.core.network;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

public class CoreHttpHandler extends AbstractHandler {

    private CoreGatewayService service;

    public CoreHttpHandler(CoreGatewayService service) {
        this.service = service;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        EndpointHandler endpoint = service.getEndpoints().get(target);
        if(endpoint == null) {
            response.setContentType("text/plain; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        endpoint.handle(request, response);
        baseRequest.setHandled(true);
    }
}
