package com.hospital.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseIntegrationTest {

    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String USERNAME = "root";

    private static final String PASSWORD = "12348765@";


    @Test
    @DisplayName("Test MySQL Database Connection")
    public void testDatabaseConnection() {
        assertDoesNotThrow(() -> {
            try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
                assertTrue(connection.isValid(5), "Connection should be valid");
                assertFalse(connection.isClosed(), "Connection should not be closed");
            }
        }, "Database connection should not throw exceptions");
    }

    @Test
    @DisplayName("Test Database Query Execution")
    public void testDatabaseQuery() {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1 as test");

            assertTrue(resultSet.next(), "Query should return at least one row");
            assertEquals(1, resultSet.getInt("test"), "Query should return correct value");

        } catch (SQLException e) {
            fail("Database query test failed: " + e.getMessage());
        }
    }
}