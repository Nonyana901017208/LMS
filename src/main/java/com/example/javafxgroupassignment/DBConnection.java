package com.example.javafxgroupassignment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:postgresql://localhost:6767/management";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "123456";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgresSQL JDBC Driver not found!", e);
        }
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("❌ Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}