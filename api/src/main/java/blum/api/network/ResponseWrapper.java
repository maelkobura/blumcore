package blum.api.network;

import java.util.Map;

public record ResponseWrapper(int status, Map<String, String> headers, Object body) {
    public static ResponseWrapper ok(Object body) {
        return new ResponseWrapper(200, Map.of(), body);
    }
    public static ResponseWrapper created(Object body) {
        return new ResponseWrapper(201, Map.of(), body);
    }
}
