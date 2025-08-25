package blum.api.configuration;

import blum.api.exception.ServiceStartException;

import java.io.IOException;

public interface ConfigurationService {

    public void reload() throws ServiceStartException;

    public <T extends ConfigurationRoot> T loadConfiguration(String name, Class<T> type) throws Exception;
    public <T extends ConfigurationRoot> T createConfiguration(String name, Class<T> config) throws Exception;

    public <T extends ConfigurationRoot> void saveConfiguration(T configuration) throws IOException, IllegalAccessException;

    public boolean hasConfiguration(String name);
    public int getConfigurationCount();
}
