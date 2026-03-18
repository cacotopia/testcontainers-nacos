package io.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.Test;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Nacos database initialization.
 * Verifies that MySQL and PostgreSQL databases are properly initialized for Nacos.
 */
public class NacosDatabaseInitTest {

    @Test
    public void testMySQLDatabaseInitialization() throws SQLException, IOException {
        // Create MySQL container
        MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("nacos")
            .withUsername("nacos")
            .withPassword("nacos");

        // Start MySQL container
        mysqlContainer.start();

        try {
            // Create database config
            NacosDatabaseConfig config = NacosDatabaseConfig.mysqlContainer(mysqlContainer);

            // Initialize database
            NacosDatabaseInitializer.initialize(config);

            // Verify database tables are created
            verifyMySQLDatabase(config);
        } finally {
            // Stop MySQL container
            mysqlContainer.stop();
        }
    }

    @Test
    public void testPostgreSQLDatabaseInitialization() throws SQLException, IOException {
        // Create PostgreSQL container
        PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("nacos")
            .withUsername("nacos")
            .withPassword("nacos");

        // Start PostgreSQL container
        postgresqlContainer.start();

        try {
            // Create database config
            NacosDatabaseConfig config = NacosDatabaseConfig.postgresqlContainer(postgresqlContainer);

            // Initialize database
            NacosDatabaseInitializer.initialize(config);

            // Verify database tables are created
            verifyPostgreSQLDatabase(config);
        } finally {
            // Stop PostgreSQL container
            postgresqlContainer.stop();
        }
    }

    /**
     * Verify MySQL database tables are created.
     *
     * @param config The database configuration
     * @throws SQLException If an SQL error occurs
     */
    private void verifyMySQLDatabase(NacosDatabaseConfig config) throws SQLException {
        try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
             Statement statement = connection.createStatement()) {

            // Check if config_info table exists
            ResultSet rs = statement.executeQuery("SHOW TABLES LIKE 'config_info'");
            assertTrue(rs.next(), "config_info table should exist");

            // Check if users table exists
            rs = statement.executeQuery("SHOW TABLES LIKE 'users'");
            assertTrue(rs.next(), "users table should exist");

            // Check if roles table exists
            rs = statement.executeQuery("SHOW TABLES LIKE 'roles'");
            assertTrue(rs.next(), "roles table should exist");

            // Check if nacos user exists
            rs = statement.executeQuery("SELECT * FROM users WHERE username = 'nacos'");
            assertTrue(rs.next(), "nacos user should exist");

            // Check if config_info has data
            rs = statement.executeQuery("SELECT * FROM config_info");
            assertTrue(rs.next(), "config_info should have data");
        }
    }

    /**
     * Verify PostgreSQL database tables are created.
     *
     * @param config The database configuration
     * @throws SQLException If an SQL error occurs
     */
    private void verifyPostgreSQLDatabase(NacosDatabaseConfig config) throws SQLException {
        try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
             Statement statement = connection.createStatement()) {

            // Set search path to nacos schema
            statement.execute("SET search_path TO nacos");

            // Check if config_info table exists
            ResultSet rs = statement.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'nacos' AND table_name = 'config_info'");
            assertTrue(rs.next(), "config_info table should exist");

            // Check if users table exists
            rs = statement.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'nacos' AND table_name = 'users'");
            assertTrue(rs.next(), "users table should exist");

            // Check if roles table exists
            rs = statement.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'nacos' AND table_name = 'roles'");
            assertTrue(rs.next(), "roles table should exist");

            // Check if nacos user exists
            rs = statement.executeQuery("SELECT * FROM users WHERE username = 'nacos'");
            assertTrue(rs.next(), "nacos user should exist");

            // Check if config_info has data
            rs = statement.executeQuery("SELECT * FROM config_info");
            assertTrue(rs.next(), "config_info should have data");
        }
    }
}
