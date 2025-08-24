package blum.core.configuration.loader;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileSystemConfigurationLoader implements ConfigurationLoader {

    @Override
    public Map<String, Config> loadConfigurations(File rootDirectory) throws Exception {
        Map<String, Config> configurations = new ConcurrentHashMap<>();

        if (rootDirectory == null || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid root directory: " + rootDirectory);
        }

        File[] configFiles = rootDirectory.listFiles(this::isValidConfigFile);
        if (configFiles == null) {
            return configurations;
        }

        for (File configFile : configFiles) {
            try {
                Config config = ConfigFactory.parseFile(configFile);
                String name = getConfigurationName(configFile);
                configurations.put(name, config);
            } catch (Exception e) {
                throw new Exception("Error loading config " + configFile.getName() + ": " + e.getMessage(), e);
            }
        }

        return configurations;
    }

    private boolean isValidConfigFile(File file) {
        return file.isFile() &&
                file.canRead() &&
                file.getName().endsWith(".conf");
    }

    private String getConfigurationName(File configFile) {
        String name = configFile.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
