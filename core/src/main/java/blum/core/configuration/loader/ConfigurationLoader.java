package blum.core.configuration.loader;

import com.typesafe.config.Config;

import java.io.File;
import java.util.Map;

public interface ConfigurationLoader {
    Map<String, Config> loadConfigurations(File rootDirectory) throws Exception;
}
