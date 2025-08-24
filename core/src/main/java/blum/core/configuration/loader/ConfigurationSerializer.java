package blum.core.configuration.loader;

import blum.api.configuration.ConfigurationPart;
import blum.api.configuration.ConfigurationRoot;
import blum.api.configuration.annotation.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConfigurationSerializer {

    private final ReflectionHelper reflectionHelper;

    public ConfigurationSerializer(ReflectionHelper reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
    }

    /**
     * Transforme une instance de ConfigurationRoot en fichier .conf
     * @param configurationRoot L'instance à sérialiser
     * @param outputDirectory Le répertoire de sortie où créer le fichier
     * @throws IOException En cas d'erreur d'écriture
     * @throws IllegalAccessException En cas d'erreur d'accès aux champs
     */
    public void saveConfigurationToFile(ConfigurationRoot configurationRoot, File outputDirectory)
            throws IOException, IllegalAccessException {

        if (configurationRoot == null) {
            throw new IllegalArgumentException("Configuration root cannot be null");
        }

        if (outputDirectory == null || !outputDirectory.exists() || !outputDirectory.isDirectory()) {
            throw new IllegalArgumentException("Output directory must exist and be a directory");
        }

        // Obtenir le nom du fichier depuis le champ 'file' ou utiliser le nom de la classe
        String fileName = configurationRoot.file;
        if (!fileName.endsWith(".conf")) {
            fileName += ".conf";
        }

        File outputFile = new File(outputDirectory, fileName);

        try {
            // Convertir l'objet en Map pour créer le Config
            Map<String, Object> configMap = serializeConfigurationToMap(configurationRoot);

            // Créer un objet Config depuis la Map
            Config config = ConfigFactory.parseMap(configMap);

            // Écrire le fichier .conf
            writeConfigToFile(config, outputFile);

            log.info("Configuration successfully saved to: {}", outputFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Failed to save configuration to file: {}", outputFile.getAbsolutePath(), e);
            throw new IOException("Failed to save configuration to file", e);
        }
    }

    /**
     * Sérialise récursivement un objet ConfigurationRoot en Map
     */
    private Map<String, Object> serializeConfigurationToMap(Object configObject) throws IllegalAccessException {
        Map<String, Object> result = new HashMap<>();

        List<Field> fields = reflectionHelper.getAllFields(configObject.getClass());

        for (Field field : fields) {
            // Ignorer le champ 'file' dans la sérialisation
            if ("file".equals(field.getName()) ||
                    (field.isAnnotationPresent(Named.class) && "file".equals(field.getAnnotation(Named.class).value()))) {
                continue;
            }

            String configKey = getConfigurationKey(field);
            field.setAccessible(true);
            Object value = field.get(configObject);

            if (value != null) {
                // Si c'est un objet complexe (ConfigurationPart), le sérialiser récursivement
                if (isConfigurationPart(value)) {
                    result.put(configKey, serializeConfigurationToMap(value));
                } else {
                    result.put(configKey, value);
                }
            }
        }

        return result;
    }

    /**
     * Détermine si un objet est une partie de configuration (objet complexe à sérialiser)
     */
    private boolean isConfigurationPart(Object value) {
        // Vérifie si c'est un objet personnalisé (pas un type primitif ou String/Collection)
        Class<?> clazz = value.getClass();
        return clazz.isAssignableFrom(ConfigurationPart.class);
    }

    /**
     * Récupère la clé de configuration pour un champ (même logique que le code original)
     */
    private String getConfigurationKey(Field field) {
        if (field.isAnnotationPresent(Named.class)) {
            return field.getAnnotation(Named.class).value();
        }
        return field.getName();
    }

    /**
     * Écrit un objet Config dans un fichier .conf
     */
    private void writeConfigToFile(Config config, File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            // Utilise le format HOCON pour l'écriture
            String configString = config.root().render(
                    com.typesafe.config.ConfigRenderOptions.defaults()
                            .setFormatted(true)
                            .setJson(false)
                            .setComments(false)
            );
            writer.write(configString);
        }
    }

    /**
     * Méthode utilitaire pour sauvegarder directement avec un nom de fichier spécifique
     */
    public void saveConfigurationToFile(ConfigurationRoot configurationRoot, File outputDirectory, String fileName)
            throws IOException, IllegalAccessException {

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        // Temporairement modifier le nom de fichier pour la sérialisation
        String originalFileName = configurationRoot.file;

        try {
            // Créer une copie temporaire avec le nom de fichier souhaité
            setFileNameIfPossible(configurationRoot, fileName);
            saveConfigurationToFile(configurationRoot, outputDirectory);
        } finally {
            // Restaurer le nom original
            setFileNameIfPossible(configurationRoot, originalFileName);
        }
    }

    /**
     * Tente de définir le nom de fichier dans l'objet configuration
     */
    private void setFileNameIfPossible(ConfigurationRoot configurationRoot, String fileName) {
        try {
            Field fileField = null;
            List<Field> fields = reflectionHelper.getAllFields(configurationRoot.getClass());

            for (Field field : fields) {
                if ("file".equals(field.getName()) ||
                        (field.isAnnotationPresent(Named.class) && "file".equals(field.getAnnotation(Named.class).value()))) {
                    fileField = field;
                    break;
                }
            }

            if (fileField != null && fileField.getType().equals(String.class)) {
                fileField.setAccessible(true);
                fileField.set(configurationRoot, fileName);
            }
        } catch (Exception e) {
            log.debug("Could not set file name field, ignoring", e);
        }
    }
}
