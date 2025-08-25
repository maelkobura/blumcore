package blum.api.core;

import blum.api.configuration.ConfigurationService;
import blum.api.services.ServicesManager;

public interface BlumCore {

    public ConfigurationService getConfigurationService();
    public ServicesManager getServiceManager();
    public BlumConfiguration getCoreConfiguration();

}
