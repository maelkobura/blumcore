package blum.core.system;

import blum.api.core.BlumConfiguration;
import blum.api.services.ServicesManager;
import blum.core.util.VersionUtil;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.File;

@Slf4j
public class BlumBoot {

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

        if (options.has("safe-mode")) {
            log.info("Safe mode enabled - external plugins disabled");
        }

        BlumSystem.init(options, Integer.parseInt(options.valueOf(serviceThreadsCountOption)));

    }

}
