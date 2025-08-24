package blum.core.library;

import blum.api.exception.DatabaseInitializationException;
import blum.api.exception.ServiceStartException;
import blum.core.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.Scanner;

@Slf4j
public class DatabaseHandler {

    private Connection connection;
    private File databaseFile;
    private String table;

    private DatabaseHandler() {}

    public static DatabaseHandler create(File dbFile, String table) throws DatabaseInitializationException {
        DatabaseHandler hand = new DatabaseHandler();
        hand.databaseFile = dbFile;
        String url = "jdbc:sqlite:" + hand.databaseFile.getAbsolutePath();

        try  {
            hand.connection = DriverManager.getConnection(url);
            hand.table = table;

            hand.setup();

            log.info("Initialized database at {}", hand.databaseFile.getAbsolutePath());
        } catch (Exception e) {
            throw new DatabaseInitializationException("Database initialization failed", e);
        }

        return hand;
    }

    private void setup() throws Exception {

        // Lire le fichier SQL entièrement
        String sql = FileUtil.getFileContentFromJar("database/" + table + ".sql");

        // Séparer les requêtes par le point-virgule
        String[] statements = sql.split(";");

        try (Statement stmt = connection.createStatement()) {
            for (String s : statements) {
                s = s.trim();
                if (!s.isEmpty()) {
                    stmt.execute(s);
                }
            }
        }
    }

    public void close() throws SQLException {
        connection.close();
    }

}
