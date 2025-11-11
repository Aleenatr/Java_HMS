package hospital.management.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Database Connection Class for Hospital Management System
 * Java 21 Compatible - Using modern connection practices
 */
public class conn {

    Connection connection;
    Statement statement;

    // Database configuration - Update these values for your MySQL setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hospital_management_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "B0$$0fy0u";

    public conn() {
        try {
            // Explicitly load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Set connection properties
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "UTC");
            
            // Create connection
            connection = DriverManager.getConnection(DB_URL, props);
            statement = connection.createStatement(
                java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
                java.sql.ResultSet.CONCUR_READ_ONLY
            );
            System.out.println("✅ Database connected successfully!");
        } catch (Exception e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
        }
    }

    // Method to close connections
    public void close() {
        try {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        conn c = new conn();
        System.out.println("Testing database connection...");
        c.close();
    }
}
