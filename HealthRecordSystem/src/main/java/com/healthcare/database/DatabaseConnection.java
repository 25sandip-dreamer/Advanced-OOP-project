package com.healthcare.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class handles the connection to our MySQL database
 * Think of it as a bridge between Java and MySQL
 */
public class DatabaseConnection {

    // Database connection details - CHANGE THESE to match YOUR MySQL setup!
    private static final String URL = "jdbc:mysql://localhost:3306/health_record_db";
    private static final String USERNAME = "root";  // Change if your username is different
    private static final String PASSWORD = "";  // PUT YOUR MYSQL PASSWORD HERE!

    // This will store our single database connection
    private static Connection connection = null;

    /**
     * Get a connection to the database
     * This method creates a connection if it doesn't exist, or returns the existing one
     *
     * @return Connection object to interact with database
     */
    public static Connection getConnection() {
        try {
            // If connection doesn't exist or is closed, create a new one
            if (connection == null || connection.isClosed()) {
                System.out.println("📡 Attempting to connect to database...");

                // This is the magic line that connects to MySQL!
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

                System.out.println("✅ Successfully connected to database!");
                System.out.println("🏥 Healthcare System is ready to use!");
            }

            return connection;

        } catch (SQLException e) {
            System.err.println("❌ ERROR: Could not connect to database!");
            System.err.println("📋 Error details: " + e.getMessage());
            System.err.println("\n🔍 Common fixes:");
            System.err.println("1. Check if MySQL is running");
            System.err.println("2. Verify your password in DatabaseConnection.java");
            System.err.println("3. Make sure database 'health_record_db' exists");

            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close the database connection
     * Always good practice to close connections when done!
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Database connection closed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Test if database connection is working
     * Returns true if connected, false otherwise
     */
    public static boolean testConnection() {
        Connection testConn = getConnection();
        return testConn != null;
    }
}
