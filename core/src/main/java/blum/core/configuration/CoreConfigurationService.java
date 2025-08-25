package blum.core.configuration;

import blum.api.configuration.ConfigurationPart;
import blum.api.configuration.ConfigurationRoot;
import blum.api.configuration.ConfigurationService;
import blum.api.configuration.annotation.Named;
import blum.api.exception.ServiceInitializationException;
import blum.api.exception.ServiceStartException;
import blum.api.services.Service;
import blum.api.services.annotation.ServiceDescriptor;
import blum.core.CoreSystem;
import blum.core.configuration.loader.ConfigurationLoader;
import blum.core.configuration.loader.ConfigurationSerializer;
import blum.core.configuration.loader.FileSystemConfigurationLoader;
import blum.core.configuration.loader.ReflectionHelper;
import com.typesafe.config.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServiceDescriptor(
        name = "configuration",
        displayName = "Core Configuration",
        description = "Configuration for Core Services and plugins",
        version = "1.0.0"
)
public class CoreConfigurationService implements Service, ConfigurationService {

    private final Map<String, Config> configMap = new ConcurrentHashMap<>();
    private final ConfigurationLoader configurationLoader;
    private final ReflectionHelper reflectionHelper;
    private final ConfigurationMapper mapper;

    @Getter
    @Setter
    private File rootDirectory;

    public CoreConfigurationService() {
        this(new FileSystemConfigurationLoader(), new ReflectionHelper());
        this.rootDirectory = CoreSystem.CONFIGURATION_DIR;
        this.rootDirectory.mkdir();
    }

    // Constructeur pour les tests avec injection de dépendances
    public CoreConfigurationService(ConfigurationLoader configurationLoader, ReflectionHelper reflectionHelper) {
        this.configurationLoader = configurationLoader;
        this.reflectionHelper = reflectionHelper;
        this.mapper = new ConfigurationMapper(reflectionHelper);
    }

    @Override
    public void start() throws ServiceStartException {
        log.info("Loading configurations...");
        loadConfigurations();
    }

    private void loadConfigurations() throws ServiceStartException {
        try {
            Map<String, Config> loadedConfigs = configurationLoader.loadConfigurations(rootDirectory);
            configMap.clear();
            configMap.putAll(loadedConfigs);
            log.info("Loaded {} configuration files", configMap.size());
        } catch (Exception e) {
            throw new ServiceStartException("Failed to load configurations", e);
        }
    }

    @Override
    public void reload() throws ServiceStartException {
        log.info("Reloading configurations");
        loadConfigurations();
    }

    @Override
    public <T extends ConfigurationRoot> T loadConfiguration(String name, Class<T> type) throws Exception {
        try {
            Config config = getConfiguration(name);
            return mapper.parseConfigurationPart(config.root(), type, name);
        } catch (Exception e) {
            log.error("Failed to load configuration '{}' of type {}", name, type.getSimpleName(), e);
            throw new RuntimeException("Failed to load configuration: " + name, e);
        }
    }

    @Override
    public <T extends ConfigurationRoot> T createConfiguration(String name, Class<T> config) throws Exception {
        T obj = reflectionHelper.createInstance(config, name);
        saveConfiguration(obj);
        return obj;
    }

    private Config getConfiguration(String name) {
        Config config = configMap.get(name);
        if (config == null) {
            throw new IllegalArgumentException("Configuration not found: " + name);
        }
        return config;
    }

    @Override
    public <T extends ConfigurationRoot> void saveConfiguration(T configuration) throws IOException, IllegalAccessException {
        ConfigObject obj = mapper.toConfig(configuration);
        String name = configuration.file;

        String str = obj.render(ConfigRenderOptions.defaults().setComments(false).setFormatted(true));

        File file = new File(rootDirectory, name + ".conf");

        if(!file.exists()) file.createNewFile();

        FileWriter writer = new FileWriter(file);
        writer.write(str);
        writer.flush();
        writer.close();
        log.info("Saved configuration: {}", file.getAbsolutePath());
    }


    private void setFieldValue(Field field, Object target, ConfigValue configValue) throws IllegalAccessException {
        field.setAccessible(true);
        Object value = configValue.unwrapped();
        field.set(target, value);
    }

    @Override
    public boolean hasConfiguration(String name) {
        return configMap.containsKey(name);
    }

    @Override
    public int getConfigurationCount() {
        return configMap.size();
    }

    @Override
    public void stop() throws ServiceStartException {
        log.info("Stopping configuration service");
        configMap.clear();
    }
    

}