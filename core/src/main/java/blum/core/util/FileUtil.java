package blum.core.util;

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

}
