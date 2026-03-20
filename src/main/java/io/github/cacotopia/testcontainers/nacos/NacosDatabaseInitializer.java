package io.github.cacotopia.testcontainers.nacos;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
     * @param config    The database configuration
     * @param container The Nacos container instance
     * @throws SQLException If an SQL error occurs
     * @throws IOException  If an I/O error occurs
     */
    public static void initialize(NacosDatabaseConfig config, ExtendableNacosContainer<?> container) throws SQLException, IOException {
        if (config.isEmbedded()) {
            // Embedded database doesn't need initialization
            return;
        }

        if (config.isMySQL()) {
            initializeMySQL(config, container);
        } else if (config.isPostgreSQL()) {
            initializePostgreSQL(config, container);
        }
    }

    /**
     * Initializes the database schema for Nacos (backward compatibility).
     *
     * @param config The database configuration
     * @throws SQLException If an SQL error occurs
     * @throws IOException  If an I/O error occurs
     */
    public static void initialize(NacosDatabaseConfig config) throws SQLException, IOException {
        initialize(config, null);
    }

    /**
     * Initializes MySQL database schema for Nacos.
     *
     * @param config    The database configuration
     * @param container The Nacos container instance
     * @throws SQLException If an SQL error occurs
     * @throws IOException  If an I/O error occurs
     */
    private static void initializeMySQL(NacosDatabaseConfig config, ExtendableNacosContainer<?> container) throws SQLException, IOException {
        // Start MySQL container if it's not running
        if (config.getType() == DatabaseType.MYSQL_CONTAINER) {
            MySQLContainer mysqlContainer = config.getMysqlContainer();
            if (mysqlContainer != null && !mysqlContainer.isRunning()) {
                mysqlContainer.start();
            }
        }

        // Get Nacos username and password from container if provided
        String nacosUsername = NacosConstant.DEFAULT_NACOS_USERNAME;
        String nacosPassword = NacosConstant.DEFAULT_NACOS_PASSWORD;
        if (container != null) {
            nacosUsername = container.getUsername();
            nacosPassword = container.getPassword();
        }

        // Create database based on initialization strategy
        String databaseName = config.getDatabase();
        String rootUrl = config.getUrl().replace("/" + databaseName, "");
        String rootUsername = config.getUsername();
        String rootPassword = config.getPassword();

        // Handle database initialization based on strategy
        InitStrategy initStrategy = config.getInitStrategy();
        if (initStrategy != InitStrategy.SKIP_INITIALIZATION) {
            try (Connection connection = DriverManager.getConnection(rootUrl, rootUsername, rootPassword);
                 Statement statement = connection.createStatement()) {
                if (initStrategy == InitStrategy.ALWAYS_RECREATE) {
                    // Drop database if it exists
                    statement.execute("DROP DATABASE IF EXISTS `" + databaseName + "`;");
                }
                // Create database
                statement.execute("CREATE DATABASE IF NOT EXISTS `" + databaseName + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
            }
        }

        // Read MySQL schema
        String schema = readResource(MYSQL_SCHEMA_RESOURCE);

        // Replace database name in schema
        schema = schema.replace("USE `nacos`;", "USE `" + databaseName + "`;");

        // Encrypt password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encryptedPassword = encoder.encode(nacosPassword);

        // Replace default username and password in schema
        schema = schema.replace("INSERT INTO `users` VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', TRUE);",
            "INSERT INTO `users` VALUES ('" + nacosUsername + "', '" + encryptedPassword + "', TRUE);");
        schema = schema.replace("INSERT INTO `roles` VALUES ('nacos', 'ROLE_ADMIN');",
            "INSERT INTO `roles` VALUES ('" + nacosUsername + "', 'ROLE_ADMIN');");

        // Execute schema
        executeSql(rootUrl, rootUsername, rootPassword, schema);
    }

    /**
     * Initializes PostgreSQL database schema for Nacos.
     *
     * @param config    The database configuration
     * @param container The Nacos container instance
     * @throws SQLException If an SQL error occurs
     * @throws IOException  If an I/O error occurs
     */
    private static void initializePostgreSQL(NacosDatabaseConfig config, ExtendableNacosContainer<?> container) throws SQLException, IOException {
        // Start PostgreSQL container if it's not running
        if (config.getType() == DatabaseType.POSTGRESQL_CONTAINER) {
            PostgreSQLContainer postgresqlContainer = config.getPostgresqlContainer();
            if (postgresqlContainer != null && !postgresqlContainer.isRunning()) {
                postgresqlContainer.start();
            }
        }

        // Get Nacos username and password from container if provided
        String nacosUsername = NacosConstant.DEFAULT_NACOS_USERNAME;
        String nacosPassword = NacosConstant.DEFAULT_NACOS_PASSWORD;
        if (container != null) {
            nacosUsername = container.getUsername();
            nacosPassword = container.getPassword();
        }

        // Create database based on initialization strategy
        String databaseName = config.getDatabase();
        String rootUrl = config.getUrl().replace("/" + databaseName, "") + "/postgres";
        String rootUsername = config.getUsername();
        String rootPassword = config.getPassword();

        // Handle database initialization based on strategy
        InitStrategy initStrategy = config.getInitStrategy();
        if (initStrategy != InitStrategy.SKIP_INITIALIZATION) {
            try (Connection connection = DriverManager.getConnection(rootUrl, rootUsername, rootPassword);
                 Statement statement = connection.createStatement()) {
                if (initStrategy == InitStrategy.ALWAYS_RECREATE) {
                    // Drop database if it exists
                    statement.execute("DROP DATABASE IF EXISTS " + databaseName + ";");
                }
                // Create database
                statement.execute("CREATE DATABASE " + databaseName + " WITH ENCODING 'UTF8';");
            } catch (SQLException e) {
                // Ignore if database already exists (only for CREATE_IF_NOT_EXISTS strategy)
                if (initStrategy != InitStrategy.CREATE_IF_NOT_EXISTS || !e.getMessage().contains("already exists")) {
                    throw e;
                }
            }
        }

        // Read PostgreSQL schema
        String schema = readResource(POSTGRESQL_SCHEMA_RESOURCE);

        // Encrypt password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encryptedPassword = encoder.encode(nacosPassword);

        // Replace default username and password in schema
        schema = schema.replace("INSERT INTO users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', TRUE);",
            "INSERT INTO users (username, password, enabled) VALUES ('" + nacosUsername + "', '" + encryptedPassword + "', TRUE);");
        schema = schema.replace("INSERT INTO roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');",
            "INSERT INTO roles (username, role) VALUES ('" + nacosUsername + "', 'ROLE_ADMIN');");

        // Execute schema
        executeSql(config.getUrl(), rootUsername, rootPassword, schema);
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
