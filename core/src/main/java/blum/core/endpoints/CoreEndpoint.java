package blum.core.endpoints;

import blum.api.network.annotations.Endpoints;
import blum.api.network.annotations.Request;
import blum.core.system.BlumBoot;

@Endpoints("/")
public class CoreEndpoint {

    @Request.Get
    public String version() {
        return BlumBoot.VERSION;
    }

}
