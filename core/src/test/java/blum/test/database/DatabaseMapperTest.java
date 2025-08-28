package blum.test.database;

import blum.api.annotation.Identifier;
import blum.api.annotation.Ignored;
import blum.api.annotation.Named;
import blum.api.database.DatabaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseMapperTest {

    @Mock
    private ResultSet mockResultSet;

    // Classes de test
    public static class SimpleUser {
        private String name;
        private int age;
        private Double salary;
        private Boolean active;

        public SimpleUser() {}

        // Getters pour les assertions
        public String getName() { return name; }
        public int getAge() { return age; }
        public Double getSalary() { return salary; }
        public Boolean getActive() { return active; }
    }

    public static class UserWithAnnotations {
        @Identifier
        private Long id;

        @Named("user_name")
        private String name;

        @Ignored
        private String internalField;

        private int age;

        public UserWithAnnotations() {}

        public UserWithAnnotations(Long id, String name, String internalField, int age) {
            this.id = id;
            this.name = name;
            this.internalField = internalField;
            this.age = age;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getInternalField() { return internalField; }
        public int getAge() { return age; }
    }

    public static class AllTypesEntity {
        private String stringField;
        private Integer integerField;
        private int intField;
        private Long longField;
        private long longPrimitiveField;
        private Double doubleField;
        private double doublePrimitiveField;
        private Float floatField;
        private float floatPrimitiveField;
        private Boolean booleanField;
        private boolean booleanPrimitiveField;
        private Short shortField;
        private short shortPrimitiveField;
        private Byte byteField;
        private byte bytePrimitiveField;
        private Date dateField;
        private Time timeField;
        private Timestamp timestampField;
        private LocalDate localDateField;
        private LocalDateTime localDateTimeField;
        private BigDecimal bigDecimalField;
        private byte[] bytesField;

        public AllTypesEntity() {}

        // Getters pour les assertions
        public String getStringField() { return stringField; }
        public Integer getIntegerField() { return integerField; }
        public int getIntField() { return intField; }
        public Long getLongField() { return longField; }
        public long getLongPrimitiveField() { return longPrimitiveField; }
        public Double getDoubleField() { return doubleField; }
        public double getDoublePrimitiveField() { return doublePrimitiveField; }
        public Float getFloatField() { return floatField; }
        public float getFloatPrimitiveField() { return floatPrimitiveField; }
        public Boolean getBooleanField() { return booleanField; }
        public boolean isBooleanPrimitiveField() { return booleanPrimitiveField; }
        public Short getShortField() { return shortField; }
        public short getShortPrimitiveField() { return shortPrimitiveField; }
        public Byte getByteField() { return byteField; }
        public byte getBytePrimitiveField() { return bytePrimitiveField; }
        public Date getDateField() { return dateField; }
        public Time getTimeField() { return timeField; }
        public Timestamp getTimestampField() { return timestampField; }
        public LocalDate getLocalDateField() { return localDateField; }
        public LocalDateTime getLocalDateTimeField() { return localDateTimeField; }
        public BigDecimal getBigDecimalField() { return bigDecimalField; }
        public byte[] getBytesField() { return bytesField; }
    }

    @BeforeEach
    void setUp() {
        reset(mockResultSet);
    }

    @Test
    void testMapSimpleObject() throws Exception {
        // Arrange
        when(mockResultSet.getString("name")).thenReturn("John Doe");
        when(mockResultSet.getInt("age")).thenReturn(30);
        when(mockResultSet.wasNull()).thenReturn(false);
        when(mockResultSet.getObject("salary")).thenReturn(50000.0);
        when(mockResultSet.getObject("active")).thenReturn(true);

        // Act
        SimpleUser user = DatabaseMapper.map(mockResultSet, SimpleUser.class);

        // Assert
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals(30, user.getAge());
        assertEquals(50000.0, user.getSalary());
        assertTrue(user.getActive());
    }

    @Test
    void testMapWithAnnotations() throws Exception {
        // Arrange
        when(mockResultSet.getObject("id")).thenReturn(1L);
        when(mockResultSet.getString("user_name")).thenReturn("Jane Doe");
        when(mockResultSet.getInt("age")).thenReturn(25);
        when(mockResultSet.wasNull()).thenReturn(false);

        // Act
        UserWithAnnotations user = DatabaseMapper.map(mockResultSet, UserWithAnnotations.class);

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("Jane Doe", user.getName());
        assertEquals(25, user.getAge());
        assertNull(user.getInternalField()); // @Ignored field should not be set
    }

    @Test
    void testMapAllTypes() throws Exception {
        // Arrange
        Date testDate = Date.valueOf("2023-01-01");
        Time testTime = Time.valueOf("10:30:00");
        Timestamp testTimestamp = Timestamp.valueOf("2023-01-01 10:30:00");
        BigDecimal testBigDecimal = new BigDecimal("123.45");
        byte[] testBytes = {1, 2, 3, 4};

        when(mockResultSet.getString("stringField")).thenReturn("test");
        when(mockResultSet.getObject("integerField")).thenReturn(100);
        when(mockResultSet.getInt("intField")).thenReturn(200);
        when(mockResultSet.getObject("longField")).thenReturn(300L);
        when(mockResultSet.getLong("longPrimitiveField")).thenReturn(400L);
        when(mockResultSet.getObject("doubleField")).thenReturn(10.5);
        when(mockResultSet.getDouble("doublePrimitiveField")).thenReturn(20.5);
        when(mockResultSet.getObject("floatField")).thenReturn(5.5f);
        when(mockResultSet.getFloat("floatPrimitiveField")).thenReturn(6.5f);
        when(mockResultSet.getObject("booleanField")).thenReturn(true);
        when(mockResultSet.getBoolean("booleanPrimitiveField")).thenReturn(false);
        when(mockResultSet.getObject("shortField")).thenReturn((short) 10);
        when(mockResultSet.getShort("shortPrimitiveField")).thenReturn((short) 20);
        when(mockResultSet.getObject("byteField")).thenReturn((byte) 1);
        when(mockResultSet.getByte("bytePrimitiveField")).thenReturn((byte) 2);
        when(mockResultSet.getDate("dateField")).thenReturn(testDate);
        when(mockResultSet.getTime("timeField")).thenReturn(testTime);
        when(mockResultSet.getTimestamp("timestampField")).thenReturn(testTimestamp);
        when(mockResultSet.getDate("localDateField")).thenReturn(testDate);
        when(mockResultSet.getTimestamp("localDateTimeField")).thenReturn(testTimestamp);
        when(mockResultSet.getBigDecimal("bigDecimalField")).thenReturn(testBigDecimal);
        when(mockResultSet.getBytes("bytesField")).thenReturn(testBytes);
        when(mockResultSet.wasNull()).thenReturn(false);

        // Act
        AllTypesEntity entity = DatabaseMapper.map(mockResultSet, AllTypesEntity.class);

        // Assert
        assertNotNull(entity);
        assertEquals("test", entity.getStringField());
        assertEquals(100, entity.getIntegerField());
        assertEquals(200, entity.getIntField());
        assertEquals(300L, entity.getLongField());
        assertEquals(400L, entity.getLongPrimitiveField());
        assertEquals(10.5, entity.getDoubleField());
        assertEquals(20.5, entity.getDoublePrimitiveField());
        assertEquals(5.5f, entity.getFloatField());
        assertEquals(6.5f, entity.getFloatPrimitiveField());
        assertTrue(entity.getBooleanField());
        assertFalse(entity.isBooleanPrimitiveField());
        assertEquals((short) 10, entity.getShortField());
        assertEquals((short) 20, entity.getShortPrimitiveField());
        assertEquals((byte) 1, entity.getByteField());
        assertEquals((byte) 2, entity.getBytePrimitiveField());
        assertEquals(testDate, entity.getDateField());
        assertEquals(testTime, entity.getTimeField());
        assertEquals(testTimestamp, entity.getTimestampField());
        assertEquals(testDate.toLocalDate(), entity.getLocalDateField());
        assertEquals(testTimestamp.toLocalDateTime(), entity.getLocalDateTimeField());
        assertEquals(testBigDecimal, entity.getBigDecimalField());
        assertArrayEquals(testBytes, entity.getBytesField());
    }

    @Test
    void testMapWithNullValues() throws Exception {
        // Arrange
        when(mockResultSet.getString("name")).thenReturn(null);
        when(mockResultSet.getInt("age")).thenReturn(0);
        when(mockResultSet.wasNull()).thenReturn(true);
        when(mockResultSet.getObject("salary")).thenReturn(null);
        when(mockResultSet.getObject("active")).thenReturn(null);

        // Act
        SimpleUser user = DatabaseMapper.map(mockResultSet, SimpleUser.class);

        // Assert
        assertNotNull(user);
        assertNull(user.getName());
        assertEquals(0, user.getAge()); // primitive int defaults to 0 when null
        assertNull(user.getSalary());
        assertNull(user.getActive());
    }

    @Test
    void testMapWithSQLException() throws Exception {
        // Arrange
        when(mockResultSet.getString("name")).thenThrow(new SQLException("Column not found"));
        when(mockResultSet.getInt("age")).thenReturn(30);
        when(mockResultSet.wasNull()).thenReturn(false);

        // Act
        SimpleUser user = DatabaseMapper.map(mockResultSet, SimpleUser.class);

        // Assert
        assertNotNull(user);
        assertNull(user.getName()); // Should be null due to SQLException
        assertEquals(30, user.getAge());
    }

    @Test
    void testBuildInsertSQL() throws Exception {
        // Arrange
        UserWithAnnotations user = new UserWithAnnotations(null, "John Doe", "ignored", 30);

        // Act
        String sql = DatabaseMapper.buildInsertSQL(user, "users");

        // Assert
        assertNotNull(sql);
        assertTrue(sql.startsWith("INSERT INTO users"));
        assertTrue(sql.contains("user_name")); // Should use @Named annotation
        assertTrue(sql.contains("'John Doe'"));
        assertTrue(sql.contains("age"));
        assertTrue(sql.contains("30"));
        assertFalse(sql.contains("id")); // Should not include @Identifier field
        assertFalse(sql.contains("internalField")); // Should not include @Ignored field
    }

    @Test
    void testFormatValue() {
        // Test via buildInsertSQL since formatValue is private
        UserWithAnnotations user = new UserWithAnnotations(null, "John's Data", null, 25);

        assertDoesNotThrow(() -> {
            String sql = DatabaseMapper.buildInsertSQL(user, "users");
            // SQL should properly escape single quotes
            assertTrue(sql.contains("'John''s Data'"));
        });
    }

    @Test
    void testFormatSQL() {
        // Arrange
        String sql = "SELECT * FROM users WHERE name = ? AND age = ?";
        String[] values = {"John Doe", "25"};

        // Act
        String formattedSQL = DatabaseMapper.formatSQL(sql, values);

        // Assert
        assertEquals("SELECT * FROM users WHERE name = 'John Doe' AND age = '25'", formattedSQL);
    }

    @Test
    void testFormatSQLWithNoParameters() {
        // Arrange
        String sql = "SELECT * FROM users";

        // Act
        String formattedSQL = DatabaseMapper.formatSQL(sql);

        // Assert
        assertEquals("SELECT * FROM users", formattedSQL);
    }

    @Test
    void testFormatSQLWithQuoteEscaping() {
        // Arrange
        String sql = "SELECT * FROM users WHERE name = ?";
        String[] values = {"John's Data"};

        // Act
        String formattedSQL = DatabaseMapper.formatSQL(sql, values);

        // Assert
        assertEquals("SELECT * FROM users WHERE name = 'John''s Data'", formattedSQL);
    }

    @Test
    void testFormatSQLWithMoreParametersThanValues() {
        // Arrange
        String sql = "SELECT * FROM users WHERE name = ? AND age = ? AND city = ?";
        String[] values = {"John Doe"};

        // Act
        String formattedSQL = DatabaseMapper.formatSQL(sql, values);

        // Assert
        assertEquals("SELECT * FROM users WHERE name = 'John Doe' AND age = ? AND city = ?", formattedSQL);
    }

    @Test
    void testGetColumnName() throws Exception {
        // Test field without annotation
        assertEquals("age", DatabaseMapper.getColumnName(UserWithAnnotations.class.getDeclaredField("age")));

        // Test field with @Named annotation
        assertEquals("user_name", DatabaseMapper.getColumnName(UserWithAnnotations.class.getDeclaredField("name")));
    }

    // Test pour une classe sans constructeur par défaut
    static class NoDefaultConstructor {
        private String name;

        public NoDefaultConstructor(String name) {
            this.name = name;
        }
    }

    @Test
    void testMapWithNoDefaultConstructor() {
        // Act & Assert
        assertThrows(NoSuchMethodException.class, () -> {
            DatabaseMapper.map(mockResultSet, NoDefaultConstructor.class);
        });
    }

    // Test pour les types primitifs avec valeurs null
    @Test
    void testMapPrimitivesWithNull() throws Exception {
        // Arrange
        when(mockResultSet.getInt("intField")).thenReturn(0);
        when(mockResultSet.getLong("longPrimitiveField")).thenReturn(0L);
        when(mockResultSet.getDouble("doublePrimitiveField")).thenReturn(0.0);
        when(mockResultSet.getFloat("floatPrimitiveField")).thenReturn(0f);
        when(mockResultSet.getBoolean("booleanPrimitiveField")).thenReturn(false);
        when(mockResultSet.getShort("shortPrimitiveField")).thenReturn((short) 0);
        when(mockResultSet.getByte("bytePrimitiveField")).thenReturn((byte) 0);
        when(mockResultSet.wasNull()).thenReturn(true); // Simule une valeur NULL en base

        // Act
        AllTypesEntity entity = DatabaseMapper.map(mockResultSet, AllTypesEntity.class);

        // Assert - les primitifs devraient avoir leurs valeurs par défaut
        assertEquals(0, entity.getIntField());
        assertEquals(0L, entity.getLongPrimitiveField());
        assertEquals(0.0, entity.getDoublePrimitiveField());
        assertEquals(0f, entity.getFloatPrimitiveField());
        assertFalse(entity.isBooleanPrimitiveField());
        assertEquals((short) 0, entity.getShortPrimitiveField());
        assertEquals((byte) 0, entity.getBytePrimitiveField());
    }
}