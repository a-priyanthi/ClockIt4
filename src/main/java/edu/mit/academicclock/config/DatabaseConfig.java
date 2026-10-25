package edu.mit.academicclock.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Unit IV: Databases with JDBC
 * Thread-safe Database Configuration & Connection Provider.
 */
public class DatabaseConfig {
    private static final String DB_DIR = "data";
    private static final String DB_NAME = "academic_clock";
    private static final String JDBC_URL = "jdbc:h2:file:./" + DB_DIR + "/" + DB_NAME + ";DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    static {
        try {
            // Ensure data directory exists
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Register H2 JDBC Driver
            Class.forName("org.h2.Driver");
            System.out.println("✅ H2 JDBC Driver successfully registered.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Failed to load H2 JDBC Driver: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static String getJdbcUrl() {
        return JDBC_URL;
    }
}
