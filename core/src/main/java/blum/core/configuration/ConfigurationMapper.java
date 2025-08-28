package blum.core.configuration;

import blum.api.configuration.ConfigurationPart;
import blum.api.configuration.ConfigurationRoot;
import blum.api.annotation.Named;
import blum.core.configuration.loader.ReflectionHelper;
import com.typesafe.config.*;

import java.lang.reflect.Field;
import java.util.List;

public class ConfigurationMapper {

    private ReflectionHelper reflectionHelper;

    public ConfigurationMapper(ReflectionHelper reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
    }

    public ConfigObject toConfig(ConfigurationPart obj) throws IllegalAccessException {
        if (obj == null) {
            throw new IllegalArgumentException("Configuration object cannot be null");
        }

        Config conf = ConfigFactory.empty();

        List<Field> fields = reflectionHelper.getAllFields(obj.getClass());

        for (Field field : fields) {
            if (field.getName().equals("file")) continue;

            String key = getConfigurationKey(field); // ex: "custom.dotted.name"
            field.setAccessible(true);
            Object value = field.get(obj);

            if (value != null) {
                if (value instanceof ConfigurationPart) {
                    // récursif
                    conf = conf.withValue(key, toConfig((ConfigurationPart) value));
                } else {
                    conf = conf.withValue(key, ConfigValueFactory.fromAnyRef(value));
                }
            }
        }

        return conf.root();
    }



    public <T extends ConfigurationPart> T parseConfigurationPart(ConfigObject conf, Class<T> type, String name) throws Exception {

        ConfigurationPart obj;

        if(ConfigurationRoot.class.isAssignableFrom(type)) {
            obj = reflectionHelper.createInstance(type.asSubclass(ConfigurationRoot.class), name);
        }else {
            obj = reflectionHelper.createInstance(type);
        }

        List<Field> fields = reflectionHelper.getAllFields(type);

        for (Field field : fields) {
            String configKey = getConfigurationKey(field);
            if (conf.toConfig().hasPath(configKey)) {
                setFieldValue(field, obj, conf.toConfig().getValue(configKey));
            }
        }

        return (T) obj;
    }

    private String getConfigurationKey(Field field) {
        if (field.isAnnotationPresent(Named.class)) {
            return field.getAnnotation(Named.class).value();
        }
        return field.getName();
    }

    private void setFieldValue(Field field, Object target, ConfigValue configValue) throws IllegalAccessException {
        field.setAccessible(true);
        Object value = configValue.unwrapped();
        field.set(target, value);
    }

}
