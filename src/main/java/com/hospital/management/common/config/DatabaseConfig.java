package com.hospital.management.common.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Configuration for Hospital Management System
 * Local Development Setup with Migration Support
 */
public class DatabaseConfig {

    // Local MySQL Configuration
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "hospital_management_system";

    // Connection URLs
    private static final String ROOT_URL = String.format("jdbc:mysql://%s:%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            HOST, PORT);
    private static final String DATABASE_URL = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            HOST, PORT, DATABASE);
//private static final String DATABASE_URL = String.format(
//        "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useAffectedRows=true",
//        HOST, PORT, DATABASE
//);


    // Local development credentials - UPDATE THESE WITH YOUR LOCAL MYSQL CREDENTIALS
    private static final String USERNAME = "root";  // Your local MySQL username

    private static final String PASSWORD = "12348765@";  // Your local MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("❌ MySQL JDBC driver not found", e);
        }
    }

    /**
     * Get connection to MySQL server (without specific database)
     * Used for creating the database
     */
    public static Connection getRootConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(ROOT_URL, USERNAME, PASSWORD);
            System.out.println("✅ Connected to MySQL server");
            return connection;
        } catch (SQLException e) {
            System.err.println("❌ MySQL server connection failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get connection to the hospital management database
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
            System.out.println("✅ Connected to Hospital Management System database");
            return connection;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Create the database if it doesn't exist
     */
    public static void createDatabaseIfNotExists() {
        System.out.println("🔧 Checking if database exists...");

        try (Connection conn = getRootConnection();
             Statement stmt = conn.createStatement()) {

            // Create database
            String createDbSql = String.format(
                    "CREATE DATABASE IF NOT EXISTS %s CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                    DATABASE
            );
            stmt.execute(createDbSql);
            System.out.println("✅ Database '" + DATABASE + "' is ready");

        } catch (SQLException e) {
            System.err.println("❌ Failed to create database: " + e.getMessage());
            throw new RuntimeException("Database creation failed", e);
        }
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn.isValid(5);
            if (isValid) {
                System.out.println("🎉 Database Connection Test: SUCCESS");
                System.out.println("   Host: " + HOST);
                System.out.println("   Database: " + DATABASE);
                System.out.println("   User: " + USERNAME);
            }
            return isValid;
        } catch (SQLException e) {
            System.err.println("❌ Database Connection Test: FAILED");
            System.err.println("   Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test MySQL server connection (without database)
     */
    public static boolean testRootConnection() {
        try (Connection conn = getRootConnection()) {
            boolean isValid = conn.isValid(5);
            if (isValid) {
                System.out.println("🎉 MySQL Server Connection Test: SUCCESS");
                System.out.println("   Host: " + HOST);
                System.out.println("   User: " + USERNAME);
            }
            return isValid;
        } catch (SQLException e) {
            System.err.println("❌ MySQL Server Connection Test: FAILED");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("💡 Please check your local MySQL credentials in DatabaseConfig.java");
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("🔧 Testing Local MySQL Connection...");

        if (testRootConnection()) {
            createDatabaseIfNotExists();
            if (testConnection()) {
                System.out.println("🚀 Local database ready for development!");
            }
        } else {
            System.out.println("💡 Update USERNAME and PASSWORD in DatabaseConfig.java with your local MySQL credentials");
        }
    }
}