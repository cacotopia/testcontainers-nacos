package io.github.cacotopia.testcontainers.nacos;

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
