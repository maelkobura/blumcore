package blum.core.configuration.loader;

import blum.api.configuration.ConfigurationPart;
import blum.api.configuration.ConfigurationRoot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class ReflectionHelper {

    public <T extends ConfigurationRoot> T createInstance(Class<T> type, String name) throws Exception {
        try {
            return type.getDeclaredConstructor(String.class).newInstance(name);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new Exception("Failed to create instance of " + type.getSimpleName(), e);
        }
    }

    public <T extends ConfigurationPart> T createInstance(Class<T> type) throws Exception {
        try {
            return type.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new Exception("Failed to create instance of " + type.getSimpleName(), e);
        }
    }

    public List<Field> getAllFields(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredFields());
    }
}