package blum.api.core;

import blum.api.configuration.ConfigurationRoot;
import blum.api.configuration.ConfigurationService;
import blum.api.services.Service;
import blum.api.services.ServicesManager;

public class Blum {

    private static BlumCore core;

    public static BlumCore getCore() {
        return core;
    }

    public static void init(BlumCore core) {
        if(Blum.core != null) return;
        Blum.core = core;
    }

    public static ConfigurationService getConfigurationService() {
        return core.getConfigurationService();
    }

    public static <T extends ConfigurationRoot> T loadConfiguration(String name, Class<T> type) throws Exception {
        return core.getConfigurationService().loadConfiguration(name, type);
    }

    public static ServicesManager getServiceManager() {
        return core.getServiceManager();
    }

    public static <T extends Service> T getService(String name, Class<T> clazz) {
        return core.getServiceManager().getService(name, clazz);
    }

    public static BlumConfiguration getCoreConfiguration() {
        return core.getCoreConfiguration();
    }


}
