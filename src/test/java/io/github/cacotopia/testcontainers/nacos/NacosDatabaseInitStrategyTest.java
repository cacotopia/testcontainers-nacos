package io.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.Test;
import org.testcontainers.mysql.MySQLContainer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for Nacos database initialization strategies.
 * Verifies that different initialization strategies work correctly.
 */
public class NacosDatabaseInitStrategyTest {

    @Test
    public void testCreateIfNotExistsStrategy() {
        // Create MySQL container
        MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0")
            .withDatabaseName("nacos")
            .withUsername("root")
            .withPassword("root");

        // Start MySQL container
        mysqlContainer.start();

        try {
            // Create Nacos container with CREATE_IF_NOT_EXISTS strategy
            NacosContainer nacosContainer = new NacosContainer()
                .withUsername("testUser")
                .withPassword("testPassword123")
                .withMySQLContainer(mysqlContainer);

            // Start Nacos container
            nacosContainer.start();

            // Verify container is running
            assertTrue(nacosContainer.isRunning());

            // Get service URL
            String serviceUrl = nacosContainer.getServiceUrl();
            assertNotNull(serviceUrl);

            // Stop and restart container to verify database persists
            nacosContainer.stop();
            nacosContainer.start();

            // Verify container is running again
            assertTrue(nacosContainer.isRunning());

        } finally {
            // Stop containers
            mysqlContainer.stop();
        }
    }

    @Test
    public void testAlwaysRecreateStrategy() {
        // Create MySQL container
        MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0")
            .withDatabaseName("nacos")
            .withUsername("root")
            .withPassword("root");

        // Start MySQL container
        mysqlContainer.start();

        try {
            // Create Nacos container with ALWAYS_RECREATE strategy
            NacosContainer nacosContainer = new NacosContainer()
                .withUsername("testUser")
                .withPassword("testPassword123")
                .withMySQLContainer(mysqlContainer)
                .withDatabaseInitStrategy(InitStrategy.ALWAYS_RECREATE);

            // Start Nacos container
            nacosContainer.start();

            // Verify container is running
            assertTrue(nacosContainer.isRunning());

        } finally {
            // Stop containers
            mysqlContainer.stop();
        }
    }

    @Test
    public void testSkipInitializationStrategy() {
        // Create MySQL container
        MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0")
            .withDatabaseName("nacos")
            .withUsername("root")
            .withPassword("root");

        // Start MySQL container
        mysqlContainer.start();

        try {
            // First, initialize database with CREATE_IF_NOT_EXISTS
            NacosContainer nacosContainer1 = new NacosContainer()
                .withUsername("testUser")
                .withPassword("testPassword123")
                .withMySQLContainer(mysqlContainer);

            // Start and stop container to initialize database
            nacosContainer1.start();
            nacosContainer1.stop();

            // Now use SKIP_INITIALIZATION strategy
            NacosContainer nacosContainer2 = new NacosContainer()
                .withUsername("testUser")
                .withPassword("testPassword123")
                .withMySQLContainer(mysqlContainer)
                .withDatabaseInitStrategy(InitStrategy.SKIP_INITIALIZATION);

            // Start Nacos container
            nacosContainer2.start();

            // Verify container is running
            assertTrue(nacosContainer2.isRunning());

        } finally {
            // Stop containers
            mysqlContainer.stop();
        }
    }
}
