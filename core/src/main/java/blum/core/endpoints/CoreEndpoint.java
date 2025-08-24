package blum.core.endpoints;

import blum.api.network.ResponseWrapper;
import blum.api.network.annotations.Endpoints;
import blum.api.network.annotations.Request;
import blum.core.CoreSystem;

@Endpoints("/")
public class CoreEndpoint {

    @Request.Get
    public String version() {
        return CoreSystem.VERSION;
    }

}
