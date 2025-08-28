package blum.api.database;

import blum.api.services.Service;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseService extends Service {

    public Connection createOrGet(String name) throws SQLException;
    public <E, T extends BlumRepository<E>> T createOrGet(String name, Class<E> entityType, Class<T> repo) throws Exception;
}
