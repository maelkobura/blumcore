package blum.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    public static String getFileContentFromJar(String resourcePath) throws IOException {
        try (InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Ressource introuvable dans le JAR : " + resourcePath);
            }


            return new String(inputStream.readAllBytes());
        }
    }

    public static String stripExtension(File file) {
        if (file == null) {
            return null;
        }

        String filename = file.getName();

        int lastDot = filename.lastIndexOf('.');
        int lastSeparator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));

        // Vérifie que le '.' est bien après un séparateur (pour ne pas couper sur un chemin genre ".hiddenfile")
        if (lastDot > lastSeparator) {
            return filename.substring(0, lastDot);
        }
        return filename;
    }

}
