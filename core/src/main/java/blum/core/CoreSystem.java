package blum.core;

import blum.api.services.ServicesManager;
import blum.core.configuration.CoreConfigurationService;
import blum.core.endpoints.CoreEndpoint;
import blum.core.library.CoreLibraryService;
import blum.core.network.CoreGatewayService;
import blum.core.services.CoreServicesManager;
import blum.core.util.VersionUtil;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Slf4j
public class CoreSystem {

    public static final String VERSION = VersionUtil.getVersion();

    public static final File CORE_DIR = new File(System.getProperty("user.home"), "blumcore");
    public static final File PLUGINS_DIR = new File(CORE_DIR, "plugins");
    public static final File CONFIGURATION_DIR = new File(CORE_DIR, "configuration");
    public static final File DATA_DIR = new File(CORE_DIR, "data");

    @Getter
    private static ServicesManager servicesManager;

    @Getter
    private static BlumConfiguration configuration;

    public static void main(String[] args) throws Exception {
        final OptionParser parser = new OptionParser();
        parser.accepts("debug", "Show extra debugging output");
        parser.accepts("safe-mode", "Disables external plugins");

        OptionSpec<String> serviceThreadsCountOption = parser.accepts("service-threads", "Number of threads for services scheduler").withRequiredArg().defaultsTo("2");
        parser.accepts("help", "Show this text").forHelp();

        OptionSet options = parser.parse(args);

        if (options.has("help"))
        {
            System.out.println("Blum Core Launch Arguments");
            parser.printHelpOn(System.out);
            System.exit(0);
        }else {
            log.info("Starting BlumCore... ({})", VERSION);
            log.info("Core directory: {}", CORE_DIR.getAbsolutePath());
        }

        if (options.has("debug")) {
            log.info("Debug mode enabled");
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
        }

        int serviceThreadsCount = Integer.parseInt(serviceThreadsCountOption.value(options));

        if (options.has("safe-mode")) {
            log.info("Safe mode enabled - external plugins disabled");
        }

        log.info("Service threads: {}", serviceThreadsCount);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
        {
            log.error("Uncaught exception:", throwable);
        });

        servicesManager = new CoreServicesManager(serviceThreadsCount);

        servicesManager.registerService(CoreConfigurationService.class);

        CoreConfigurationService confService = servicesManager.getService("configuration", CoreConfigurationService.class);

        if(confService.hasConfiguration("core")) {
            configuration = confService.loadConfiguration("core", BlumConfiguration.class);
        }else {
            configuration = confService.createConfiguration("core", BlumConfiguration.class);
        }

        servicesManager.registerService(CoreGatewayService.class);
        servicesManager.registerService(CoreLibraryService.class);
        servicesManager.getService("gateway", CoreGatewayService.class).registerEndpoints(new CoreEndpoint());

    }

}
