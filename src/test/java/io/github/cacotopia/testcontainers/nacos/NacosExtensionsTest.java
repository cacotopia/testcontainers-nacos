package io.github.cacotopia.testcontainers.nacos;

import io.github.cacotopia.testcontainers.nacos.extensions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Nacos extensions.
 * Demonstrates the usage of the new extension features.
 */
@ExtendWith(NacosTestExtension.class)
public class NacosExtensionsTest {

    @Test
    public void testFaultSimulation(NacosContainer container) throws Exception {
        // Create fault simulator
        NacosFaultSimulator faultSimulator = new NacosFaultSimulator();

        // Verify container is running
        assertTrue(container.isRunning());

        // Simulate network latency
        faultSimulator.simulateNetworkLatency(container, 500);
        System.out.println("Simulated network latency of 500ms");

        // Remove network latency
        faultSimulator.removeNetworkLatency(container);
        System.out.println("Removed network latency");

        // Container should still be running
        assertTrue(container.isRunning());
    }

    @Test
    public void testConfigManagement(NacosContainer container) throws Exception {
        // Create config manager
        NacosConfigManager configManager = new NacosConfigManager(container);

        // Generate test data
        NacosTestDataGenerator generator = new NacosTestDataGenerator();
        List<NacosConfig> configs = generator.generateRandomConfigs(3);

        // Publish multiple configs
        configManager.publishConfigs(configs);
        System.out.println("Published " + configs.size() + " configurations");

        // Get configs
        List<String> dataIds = configs.stream().map(NacosConfig::getDataId).toList();
        Map<String, String> retrievedConfigs = configManager.getConfigs(dataIds, "DEFAULT_GROUP");

        // Verify all configs were retrieved
        assertEquals(configs.size(), retrievedConfigs.size());
        System.out.println("Retrieved " + retrievedConfigs.size() + " configurations");
    }

    @Test
    public void testServiceManagement(NacosContainer container) throws Exception {
        // Create service manager
        NacosServiceManager serviceManager = new NacosServiceManager(container);

        // Generate test data
        NacosTestDataGenerator generator = new NacosTestDataGenerator();
        List<NacosServiceInstance> instances = generator.generateRandomServiceInstances(2);

        // Register multiple instances
        serviceManager.registerInstances(instances);
        System.out.println("Registered " + instances.size() + " service instances");

        // Get all instances
        String serviceName = instances.get(0).getServiceName();
        int instanceCount = serviceManager.getAllInstances(serviceName).size();
        System.out.println("Found " + instanceCount + " instances for service: " + serviceName);

        // Verify instances were registered
        assertTrue(instanceCount > 0);
    }

    @Test
    public void testTestDataGenerator() {
        // Create test data generator
        NacosTestDataGenerator generator = new NacosTestDataGenerator();

        // Generate random config
        NacosConfig config = generator.generateRandomConfig();
        assertNotNull(config.getDataId());
        assertNotNull(config.getContent());
        System.out.println("Generated random config: " + config.getDataId());

        // Generate random service instance
        NacosServiceInstance instance = generator.generateRandomServiceInstance();
        assertNotNull(instance.getServiceName());
        assertNotNull(instance.getIp());
        System.out.println("Generated random service instance: " + instance.getServiceName());
    }
}
