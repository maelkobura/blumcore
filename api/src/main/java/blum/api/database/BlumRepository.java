package blum.api.database;

import blum.api.database.annotation.Model;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.InvalidClassException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class BlumRepository<T> {

    /**
     * -- GETTER --
     *  Gets the connection used by this repository
     */
    @Getter
    private Connection connection;
    private Class<T> type;

    public BlumRepository(Connection connection, Class<T> type) throws InvalidClassException {
        this.type = type;
        this.connection = connection;
        if(!type.isAnnotationPresent(Model.class)) {
            throw new InvalidClassException(this.getClass().getName(),
                    String.format("The class %1$s isn't @Model", type.getName()));
        }
    }

    /**
     * Executes a SELECT query and returns a list of mapped objects
     */
    public List<T> listQuery(String sql, String... values) {
        sql = DatabaseMapper.formatSQL(sql, values);
        log.debug("Query: {}", sql);

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(DatabaseMapper.map(resultSet, type));
            }
            return result;

        } catch (Exception e) {
            log.error("Failed to execute sql request on {} repository ({}): {}",
                    this.getClass().getSimpleName(), connection.toString(), sql, e);
            return List.of();
        }
    }

    /**
     * Executes a SELECT query and returns the first result or null
     */
    public T query(String sql, String... values) {
        List<T> results = listQuery(sql, values);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Creates a PreparedStatement for the given SQL
     */
    public PreparedStatement prepare(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    /**
     * Inserts an object into the database
     */
    public void create(T object) throws SQLException, IllegalAccessException {
        String sql = DatabaseMapper.buildInsertSQL(object, getTableName());
        log.debug("Create: {}", sql);

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Executes a generic SQL statement
     */
    public boolean execute(String sql, String... values) {
        sql = DatabaseMapper.formatSQL(sql, values);
        try {
            log.debug("Execute: {}", sql);
            try (Statement statement = connection.createStatement()) {
                return statement.execute(sql);
            }
        } catch (SQLException e) {
            log.error("Failed to execute sql request on {} repository ({}): {}",
                    this.getClass().getSimpleName(), connection.toString(), sql, e);
            return false;
        }
    }

    /**
     * Gets the type class managed by this repository
     */
    protected Class<T> getType() {
        return type;
    }

    /**
     * Abstract method to get the table name for this repository
     */
    public abstract String getTableName();

    /**
     * Abstract method to initialize the table structure
     */
    public abstract String initTable() throws Exception;
}