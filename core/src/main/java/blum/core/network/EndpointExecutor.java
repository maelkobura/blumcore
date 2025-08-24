package blum.core.network;

import blum.api.network.ResponseWrapper;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
public class EndpointExecutor {

    private static final Gson gson = new Gson();

    private Object target;
    private Method method;

    @SneakyThrows
    public void handle(HttpServletRequest request, HttpServletResponse response) {

        //TODO Params handling

        Object obj = method.invoke(target);
        adaptResponse(obj, response);

    }

    private void adaptResponse(Object result, HttpServletResponse response) throws Exception {
        if (result == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
            return;
        }

        if (result instanceof String str) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain; charset=utf-8");
            response.getWriter().print(str);
            return;
        }

        if (result instanceof byte[] bytes) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/octet-stream");
            response.getOutputStream().write(bytes);
            return;
        }

        if (result instanceof Number || result instanceof Boolean) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain; charset=utf-8");
            response.getWriter().print(result.toString());
            return;
        }

        // Exemple d'une classe maison pour contrôler le retour (style ResponseEntity)
        if (result instanceof ResponseWrapper wrapper) {
            response.setStatus(wrapper.status());
            wrapper.headers().forEach(response::setHeader);
            if (wrapper.body() != null) {
                adaptResponse(wrapper.body(), response);
            }
            return;
        }

        // Sinon → JSON par défaut
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().print(gson.toJson(result));
    }
}


