package blum.core.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class VersionUtil {

    public static String getVersion() {
        // Cherche le fichier "version.txt" dans le classpath (donc dans le JAR)
        try (InputStream is = VersionUtil.class.getResourceAsStream("/version.txt")) {
            if (is == null) {
                throw new IllegalStateException("Failed to access version file");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n")).trim();
            }
        } catch (Exception e) {
            return "Unknown version: " +e.getMessage();
        }
    }

}
