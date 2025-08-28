package blum.core.system;

import blum.api.configuration.ConfigurationService;
import blum.api.core.Blum;
import blum.api.core.BlumCore;
import blum.api.services.ServicesManager;
import blum.api.core.BlumConfiguration;
import blum.core.configuration.CoreConfigurationService;
import blum.core.database.CoreDatabaseService;
import blum.core.endpoints.CoreEndpoint;
import blum.core.library.CoreLibraryService;
import blum.core.network.CoreGatewayService;
import blum.core.services.CoreServicesManager;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlumSystem implements BlumCore {

    private ServicesManager servicesManager;
    private CoreConfigurationService configurationService;

    private BlumConfiguration configuration;

    public static void init(OptionSet options, int serviceThreadsCount) {

        if(Blum.getCore() != null)return;

        BlumSystem core = new BlumSystem();
        Blum.init(core);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
        {
            log.error("Uncaught exception:", throwable);
        });

        core.servicesManager = new CoreServicesManager(serviceThreadsCount);
        log.info("Service threads: {}", serviceThreadsCount);

        core.servicesManager.registerService(CoreConfigurationService.class);

        core.configurationService = core.servicesManager.getService("configuration", CoreConfigurationService.class);

        try {
            if(core.configurationService.hasConfiguration("core")) {
                core.configuration = core.configurationService.loadConfiguration("core", BlumConfiguration.class);
            }else {
                core.configuration = core.configurationService.createConfiguration("core", BlumConfiguration.class);
            }
        }catch (Exception e) {
            log.error("Failed to load core configuration", e);
            log.info("Loading default configuration...");

            core.configuration = new BlumConfiguration("core");
        }

        core.loadCoreServices();
        core.registerCoreEndpoints();
        log.info("Blum Core online.");
    }

    private void loadCoreServices() {
        servicesManager.registerService(CoreDatabaseService.class);
        servicesManager.registerService(CoreGatewayService.class);
        servicesManager.registerService(CoreLibraryService.class);
    }

    private void registerCoreEndpoints() {
        if(servicesManager.hasService("gateway")) {
            servicesManager.getService("gateway", CoreGatewayService.class).registerEndpoints(new CoreEndpoint());
        }else {
            log.warn("Gateway service is offline, network interface unavailible. Please restart Blum Core");
        }
    }

    private BlumSystem(){}

    @Override
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }


    @Override
    public ServicesManager getServiceManager() {
        return servicesManager;
    }

    @Override
    public BlumConfiguration getCoreConfiguration() {
        return configuration;
    }
}
