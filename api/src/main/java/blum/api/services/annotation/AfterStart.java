package blum.api.services.annotation;

import blum.api.services.Service;

public @interface AfterStart {

    Class<? extends Service> value();
}
