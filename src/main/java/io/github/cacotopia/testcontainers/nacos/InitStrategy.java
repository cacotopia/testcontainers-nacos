package io.github.cacotopia.testcontainers.nacos;

/**
 * Database initialization strategy enumeration
 */
public enum InitStrategy {
    /**
     * Create database if it doesn't exist
     */
    CREATE_IF_NOT_EXISTS,
    /**
     * Always create a new database, dropping existing one if it exists
     */
    ALWAYS_RECREATE,
    /**
     * Do not create database, assume it already exists
     */
    SKIP_INITIALIZATION
}
