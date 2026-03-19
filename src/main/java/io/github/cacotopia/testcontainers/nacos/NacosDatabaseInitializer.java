package io.github.cacotopia.testcontainers.nacos;

import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Nacos database initializer.
 * Responsible for initializing the database schema for Nacos.
 */
public class NacosDatabaseInitializer {

    private static final String MYSQL_SCHEMA_RESOURCE = "/nacos-mysql.sql";
    private static final String POSTGRESQL_SCHEMA_RESOURCE = "/nacos-postgresql.sql";

    /**
     * Initializes the database schema for Nacos.
     *
     * @param config The database configuration
     * @throws SQLException If an SQL error occurs
     * @throws IOException If an I/O error occurs
     */
    public static void initialize(NacosDatabaseConfig config) throws SQLException, IOException {
        if (config.isEmbedded()) {
            // Embedded database doesn't need initialization
            return;
        }

        if (config.isMySQL()) {
            initializeMySQL(config);
        } else if (config.isPostgreSQL()) {
            initializePostgreSQL(config);
        }
    }

    /**
     * Initializes MySQL database schema for Nacos.
     *
     * @param config The database configuration
     * @throws SQLException If an SQL error occurs
     * @throws IOException If an I/O error occurs
     */
    private static void initializeMySQL(NacosDatabaseConfig config) throws SQLException, IOException {
        // Start MySQL container if it's not running
        if (config.getType() == DatabaseType.MYSQL_CONTAINER) {
            MySQLContainer mysqlContainer = config.getMysqlContainer();
            if (mysqlContainer != null && !mysqlContainer.isRunning()) {
                mysqlContainer.start();
            }
        }

        // Read MySQL schema
        String schema = readResource(MYSQL_SCHEMA_RESOURCE);

        // Execute schema
        executeSql(config.getUrl(), config.getUsername(), config.getPassword(), schema);
    }

    /**
     * Initializes PostgreSQL database schema for Nacos.
     *
     * @param config The database configuration
     * @throws SQLException If an SQL error occurs
     * @throws IOException If an I/O error occurs
     */
    private static void initializePostgreSQL(NacosDatabaseConfig config) throws SQLException, IOException {
        // Start PostgreSQL container if it's not running
        if (config.getType() == DatabaseType.POSTGRESQL_CONTAINER) {
            PostgreSQLContainer postgresqlContainer = config.getPostgresqlContainer();
            if (postgresqlContainer != null && !postgresqlContainer.isRunning()) {
                postgresqlContainer.start();
            }
        }

        // Read PostgreSQL schema
        String schema = readResource(POSTGRESQL_SCHEMA_RESOURCE);

        // Execute schema
        executeSql(config.getUrl(), config.getUsername(), config.getPassword(), schema);
    }

    /**
     * Executes SQL statements.
     *
     * @param url      The database URL
     * @param username The database username
     * @param password The database password
     * @param sql      The SQL statements to execute
     * @throws SQLException If an SQL error occurs
     */
    private static void executeSql(String url, String username, String password, String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            // Split SQL statements by semicolon
            String[] statements = sql.split(";\s*");
            for (String stmt : statements) {
                if (!stmt.trim().isEmpty()) {
                    statement.execute(stmt);
                }
            }
        }
    }

    /**
     * Reads a resource file as string.
     *
     * @param resourcePath The resource path
     * @return The resource content as string
     * @throws IOException If an I/O error occurs
     */
    private static String readResource(String resourcePath) throws IOException {
        try (InputStream is = NacosDatabaseInitializer.class.getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
