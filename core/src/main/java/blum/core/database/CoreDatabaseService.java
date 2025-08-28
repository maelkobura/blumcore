package blum.core.database;

import blum.api.database.BlumRepository;
import blum.api.database.DatabaseService;
import blum.api.exception.ServiceStartException;
import blum.api.services.Service;
import blum.api.services.annotation.ServiceDescriptor;
import blum.core.system.BlumBoot;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ServiceDescriptor(
        name = "database",
        displayName = "Blum Database Service",
        description = "Global database access for Blum Core, Plugins and others apps",
        version = "1.0.0"
)
@Slf4j
public class CoreDatabaseService implements Service, DatabaseService {

    private Map<String, Connection> databases = new HashMap<>();
    private File dataFolder;

    @Override
    public void start() throws ServiceStartException {
        dataFolder = BlumBoot.DATA_DIR;

        for(String nm : dataFolder.list((dir, name) -> name.endsWith(".db"))) {
            try {
                databases.put(nm.substring(0, nm.length() - 3), get(nm));
            } catch (SQLException e) {
                log.error("Unable to load database: {}", nm, e);
            }
        }

    }

    private Connection get(String name) throws SQLException {
        File file = new File(dataFolder, name);
        String url = "jdbc:sqlite:" + file.getAbsolutePath();

        return DriverManager.getConnection(url);
    }

    @Override
    public Connection createOrGet(String name) throws SQLException {
        if(databases.containsKey(name)) return databases.get(name);

        Connection conn = get(name+".db");

        if(conn == null) throw new SQLException("Unable to create database connection");

        databases.put(name, conn);

        return conn;
    }

    @Override
    public <E, T extends BlumRepository<E>> T createOrGet(String name, Class<E> entityType, Class<T> repo) throws Exception {
        Connection conn = createOrGet(name);
        T repoObj = repo.getConstructor(Connection.class, Class.class).newInstance(conn, entityType);

        if(repoObj.query("SELECT name FROM sqlite_master WHERE type='table' AND name=?", repoObj.getTableName()) == null) {
            repoObj.execute(repoObj.initTable());
        }

        return repoObj;
    }


    @Override
    public void stop() throws ServiceStartException {
        databases.values().forEach(connection -> {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Failed to close database connection",e);
            }
        });
    }
}
