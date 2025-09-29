package com.hospital.management.core.factories;

import com.hospital.management.common.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {

    public static Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
}
