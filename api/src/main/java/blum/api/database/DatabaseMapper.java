package blum.api.database;

import blum.api.annotation.Identifier;
import blum.api.annotation.Ignored;
import blum.api.annotation.Named;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DatabaseMapper {

    /**
     * Maps a ResultSet row to an object instance of the specified type
     */
    public static <T> T map(ResultSet set, Class<T> type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {
        T instance = type.getDeclaredConstructor().newInstance();

        for (Field f : type.getDeclaredFields()) {
            if (f.isAnnotationPresent(Ignored.class)) continue;

            f.setAccessible(true);

            String name = getColumnName(f);
            Object value = extractValue(set, f, name);

            if (value != null) {
                f.set(instance, value);
            }
        }

        return instance;
    }

    /**
     * Extracts the appropriate value from ResultSet based on field type
     */
    private static Object extractValue(ResultSet set, Field field, String columnName) {
        try {
            Class<?> fieldType = field.getType();

            if (fieldType.equals(String.class)) {
                return set.getString(columnName);
            } else if (fieldType.equals(Integer.class)) {
                return (Integer) set.getObject(columnName);
            } else if (fieldType.equals(int.class)) {
                int tmp = set.getInt(columnName);
                return set.wasNull() ? 0 : tmp;
            } else if (fieldType.equals(Long.class)) {
                return (Long) set.getObject(columnName);
            } else if (fieldType.equals(long.class)) {
                long tmp = set.getLong(columnName);
                return set.wasNull() ? 0L : tmp;
            } else if (fieldType.equals(Double.class)) {
                return (Double) set.getObject(columnName);
            } else if (fieldType.equals(double.class)) {
                double tmp = set.getDouble(columnName);
                return set.wasNull() ? 0.0 : tmp;
            } else if (fieldType.equals(Float.class)) {
                return (Float) set.getObject(columnName);
            } else if (fieldType.equals(float.class)) {
                float tmp = set.getFloat(columnName);
                return set.wasNull() ? 0f : tmp;
            } else if (fieldType.equals(Boolean.class)) {
                return (Boolean) set.getObject(columnName);
            } else if (fieldType.equals(boolean.class)) {
                boolean tmp = set.getBoolean(columnName);
                return set.wasNull() ? false : tmp;
            } else if (fieldType.equals(Short.class)) {
                return (Short) set.getObject(columnName);
            } else if (fieldType.equals(short.class)) {
                short tmp = set.getShort(columnName);
                return set.wasNull() ? 0 : tmp;
            } else if (fieldType.equals(Byte.class)) {
                return (Byte) set.getObject(columnName);
            } else if (fieldType.equals(byte.class)) {
                byte tmp = set.getByte(columnName);
                return set.wasNull() ? 0 : tmp;
            } else if (fieldType.equals(java.sql.Date.class)) {
                return set.getDate(columnName);
            } else if (fieldType.equals(java.sql.Time.class)) {
                return set.getTime(columnName);
            } else if (fieldType.equals(java.sql.Timestamp.class)) {
                return set.getTimestamp(columnName);
            } else if (fieldType.equals(java.time.LocalDate.class)) {
                java.sql.Date d = set.getDate(columnName);
                return d != null ? d.toLocalDate() : null;
            } else if (fieldType.equals(java.time.LocalDateTime.class)) {
                java.sql.Timestamp ts = set.getTimestamp(columnName);
                return ts != null ? ts.toLocalDateTime() : null;
            } else if (fieldType.equals(java.math.BigDecimal.class)) {
                return set.getBigDecimal(columnName);
            } else if (fieldType.equals(byte[].class)) {
                return set.getBytes(columnName);
            } else {
                return set.getObject(columnName);
            }

        } catch (SQLException exception) {
            log.debug("Mapping error for column '{}': {}", columnName, exception.getMessage());
            return null;
        }
    }

    /**
     * Builds an INSERT SQL statement from an object
     */
    public static <T> String buildInsertSQL(T object, String tableName) throws IllegalAccessException {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        List<Field> fieldsToInsert = getInsertableFields(object);

        if (fieldsToInsert.isEmpty()) {
            throw new IllegalArgumentException("No non-null fields found to insert");
        }

        for (int i = 0; i < fieldsToInsert.size(); i++) {
            Field field = fieldsToInsert.get(i);
            field.setAccessible(true);
            Object value = field.get(object);

            String columnName = getColumnName(field);

            if (i > 0) {
                columns.append(", ");
                values.append(", ");
            }

            columns.append(columnName);
            values.append(formatValue(value));
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName, columns.toString(), values.toString());
    }

    /**
     * Gets the list of fields that should be included in an INSERT statement
     */
    private static <T> List<Field> getInsertableFields(T object) throws IllegalAccessException {
        List<Field> insertableFields = new ArrayList<>();

        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Ignored.class)) continue;
            if (field.isAnnotationPresent(Identifier.class)) continue;

            field.setAccessible(true);
            Object value = field.get(object);

            // Skip null values
            if (value == null) continue;

            insertableFields.add(field);
        }

        return insertableFields;
    }

    /**
     * Gets the column name for a field, considering @Named annotation
     */
    public static String getColumnName(Field field) {
        if (field.isAnnotationPresent(Named.class)) {
            return field.getAnnotation(Named.class).value();
        }
        return field.getName();
    }

    /**
     * Formats a value for SQL insertion
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + ((String) value).replace("'", "''") + "'";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            // For other types, convert to string and quote
            return "'" + value.toString().replace("'", "''") + "'";
        }
    }

    /**
     * Formats SQL with parameter substitution
     */
    public static String formatSQL(String sql, String... values) {
        if (values == null || values.length == 0) return sql;

        StringBuilder formatted = new StringBuilder();
        int valueIndex = 0;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '?' && valueIndex < values.length) {
                formatted.append("'").append(values[valueIndex].replace("'", "''")).append("'");
                valueIndex++;
            } else {
                formatted.append(c);
            }
        }
        return formatted.toString();
    }
}