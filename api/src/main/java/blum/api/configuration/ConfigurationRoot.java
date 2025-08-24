package blum.api.configuration;


public class ConfigurationRoot implements ConfigurationPart{

    public final String file;

    public ConfigurationRoot(String file) {
        this.file = file;
    }
}
