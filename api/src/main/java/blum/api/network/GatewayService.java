package blum.api.network;

import blum.api.services.Service;

public interface GatewayService extends Service {

    public void registerEndpoints(Object obj);

}
