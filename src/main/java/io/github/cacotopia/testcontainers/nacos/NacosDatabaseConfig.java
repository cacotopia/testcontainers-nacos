package io.github.cacotopia.testcontainers.nacos;


import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Nacos database configuration class.
 * Supports embedded database, external MySQL container, or external MySQL instance.
 */
public class NacosDatabaseConfig {

    /**
     * Database type enum
     */
    public enum DatabaseType {
        /**
         * Embedded database (default)
         */
        EMBEDDED,
        /**
         * Testcontainers MySQL container
         */
        MYSQL_CONTAINER,
        /**
         * External MySQL instance
         */
        EXTERNAL_MYSQL,
        /**
         * Testcontainers PostgreSQL container
         */
        POSTGRESQL_CONTAINER,
        /**
         * External PostgreSQL instance
         */
        EXTERNAL_POSTGRESQL
    }

    /**
     * Database type
     */
    private DatabaseType type = DatabaseType.EMBEDDED;

    // MySQL connection configuration
    /**
     * MySQL host
     */
    private String host;

    /**
     * MySQL port
     */
    private Integer port;

    /**
     * Database name
     */
    private String database = "nacos";

    /**
     * MySQL username
     */
    private String username = "nacos";

    /**
     * MySQL password
     */
    private String password = "nacos";

    /**
     * JDBC URL Params
     * For example: Mysql-> "characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai"
     */
    private String urlParams;

    /**
     * JDBC URL
     */
    private String url;

    /**
     * Testcontainers MySQL container reference
     * MySQL container instance
     */
    private MySQLContainer mysqlContainer;

    /**
     * Testcontainers PostgreSQL container reference
     * PostgreSQL container instance
     */
    private PostgreSQLContainer postgresqlContainer;

    /**
     * Private constructor. Use factory methods to create instances.
     */
    private NacosDatabaseConfig() {
    }

    /**
     * Creates an embedded database configuration (default).
     *
     * @return A new NacosDatabaseConfig instance with embedded database
     */
    public static NacosDatabaseConfig embedded() {
        NacosDatabaseConfig config = new NacosDatabaseConfig();
        config.type = DatabaseType.EMBEDDED;
        return config;
    }

    /**
     * Creates an external MySQL configuration.
     *
     * @param host     The MySQL host
     * @param port     The MySQL port
     * @param database The database name
     * @param username The MySQL username
     * @param password The MySQL password
     * @return A new NacosDatabaseConfig instance with external MySQL
     */
    public static NacosDatabaseConfig externalMySQL(String host, int port, String database, String username, String password, String urlParams) {
        NacosDatabaseConfig config = new NacosDatabaseConfig();
        config.type = DatabaseType.EXTERNAL_MYSQL;
        config.host = host;
        config.port = port;
        config.database = database;
        config.username = username;
        config.password = password;
        config.urlParams = urlParams;
        return config;
    }

    /**
     * Creates a configuration using Testcontainers MySQL container.
     *
     * @param mysqlContainer The MySQL container to use
     * @return A new NacosDatabaseConfig instance with MySQL container
     */
    public static NacosDatabaseConfig mysqlContainer(MySQLContainer mysqlContainer) {
        NacosDatabaseConfig config = new NacosDatabaseConfig();
        config.type = DatabaseType.MYSQL_CONTAINER;
        config.mysqlContainer = mysqlContainer;
        config.host = mysqlContainer.getHost();
        config.port = mysqlContainer.getMappedPort(3306);
        config.database = mysqlContainer.getDatabaseName();
        config.username = mysqlContainer.getUsername();
        config.password = mysqlContainer.getPassword();
        return config;
    }

    /**
     * Creates a configuration using Testcontainers MySQL container (simplified version).
     *
     * @param mysqlImage The MySQL Docker image
     * @param database   The database name
     * @param username   The MySQL username
     * @param password   The MySQL password
     * @return A new NacosDatabaseConfig instance with MySQL container
     */
    public static NacosDatabaseConfig mysqlContainer(String mysqlImage, String database, String username, String password, String urlParams) {
        MySQLContainer mysql = new MySQLContainer(mysqlImage)
            .withDatabaseName(database)
            .withUsername(username)
            .withPassword(password);
        // TODO: Set URL params
        return mysqlContainer(mysql);
    }

    /**
     * Creates an external PostgreSQL configuration.
     *
     * @param host     The PostgreSQL host
     * @param port     The PostgreSQL port
     * @param database The database name
     * @param username The PostgreSQL username
     * @param password The PostgreSQL password
     * @return A new NacosDatabaseConfig instance with external PostgreSQL
     */
    public static NacosDatabaseConfig externalPostgreSQL(String host, int port, String database, String username, String password, String urlParams) {
        NacosDatabaseConfig config = new NacosDatabaseConfig();
        config.type = DatabaseType.EXTERNAL_POSTGRESQL;
        config.host = host;
        config.port = port;
        config.database = database;
        config.username = username;
        config.password = password;
        config.urlParams = urlParams;
        return config;
    }

    /**
     * Creates a configuration using Testcontainers PostgreSQL container.
     *
     * @param postgresqlContainer The PostgreSQL container to use
     * @return A new NacosDatabaseConfig instance with PostgreSQL container
     */
    public static NacosDatabaseConfig postgresqlContainer(PostgreSQLContainer postgresqlContainer) {
        NacosDatabaseConfig config = new NacosDatabaseConfig();
        config.type = DatabaseType.POSTGRESQL_CONTAINER;
        config.postgresqlContainer = postgresqlContainer;
        config.host = postgresqlContainer.getHost();
        config.port = postgresqlContainer.getMappedPort(5432);
        config.database = postgresqlContainer.getDatabaseName();
        config.username = postgresqlContainer.getUsername();
        config.password = postgresqlContainer.getPassword();
        return config;
    }

    /**
     * Creates a configuration using Testcontainers PostgreSQL container (simplified version).
     *
     * @param postgresqlImage The PostgreSQL Docker image
     * @param database        The database name
     * @param username        The MySQL username
     * @param password        The MySQL password
     * @return A new NacosDatabaseConfig instance with MySQL container
     */
    public static NacosDatabaseConfig postgresqlContainer(String postgresqlImage, String database, String username, String password, String urlParams) {
        PostgreSQLContainer postgresql = new PostgreSQLContainer(postgresqlImage)
            .withDatabaseName(database)
            .withUsername(username)
            .withPassword(password);
        // TODO: Set URL params
        return postgresqlContainer(postgresql);
    }

    /**
     * Gets the database type.
     *
     * @return The database type
     */
    public DatabaseType getType() {
        return type;
    }

    /**
     * Gets the MySQL host.
     *
     * @return The MySQL host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the MySQL port.
     *
     * @return The MySQL port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Gets the database name.
     *
     * @return The database name
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Gets the MySQL username.
     *
     * @return The MySQL username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the MySQL password.
     *
     * @return The MySQL password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the JDBC URL params.
     *
     * @return The JDBC URL params
     */
    public String getUrlParams() {
        return urlParams;
    }

    /**
     * Gets the JDBC URL.
     *
     * @return The JDBC URL, or null for embedded database
     */
    public String getUrl() {
        if (url != null) {
            return url;
        }
        if (type == DatabaseType.EMBEDDED) {
            return null;
        }
        if (isPostgreSQL()) {
            return String.format("jdbc:postgresql://%s:%d/%s?currentSchema=nacos",
                host, port, database);
        }
        return String.format("jdbc:mysql://%s:%d/%s?",
            host, port, database);
    }

    /**
     * Gets the MySQL container instance.
     *
     * @return The MySQL container instance, or null if not using MySQL container
     */
    public MySQLContainer getMysqlContainer() {
        return mysqlContainer;
    }

    /**
     * Gets the PostgreSQL container instance.
     *
     * @return The PostgreSQL container instance, or null if not using PostgreSQL container
     */
    public PostgreSQLContainer getPostgresqlContainer() {
        return postgresqlContainer;
    }

    /**
     * Checks if using embedded database.
     *
     * @return true if using embedded database, false otherwise
     */
    public boolean isEmbedded() {
        return type == DatabaseType.EMBEDDED;
    }

    /**
     * Checks if using MySQL database (either container or external).
     *
     * @return true if using MySQL, false otherwise
     */
    public boolean isMySQL() {
        return type == DatabaseType.MYSQL_CONTAINER || type == DatabaseType.EXTERNAL_MYSQL;
    }

    /**
     * Checks if using PostgreSQL database (either container or external).
     *
     * @return true if using PostgreSQL, false otherwise
     */
    public boolean isPostgreSQL() {
        return type == DatabaseType.POSTGRESQL_CONTAINER || type == DatabaseType.EXTERNAL_POSTGRESQL;
    }

    /**
     * Returns a string representation of the NacosDatabaseConfig.
     *
     * @return A string representation of the object
     */
    @Override
    public String toString() {
        return "NacosDatabaseConfig{" +
            "type=" + type +
            ", host='" + host + '\'' +
            ", port=" + port +
            ", database='" + database + '\'' +
            ", username='" + username + '\'' +
            '}';
    }
}
