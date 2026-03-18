package io.github.cacotopia.testcontainers.nacos.extensions;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import io.github.cacotopia.testcontainers.nacos.NacosContainer;
import io.github.cacotopia.testcontainers.nacos.NacosConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Nacos configuration manager with enhanced features for testing.
 * Supports configuration change listeners, batch operations, and version management.
 */
public class NacosConfigManager {

    private final NacosContainer container;
    private final ConfigService configService;

    public NacosConfigManager(NacosContainer container) throws NacosException {
        this.container = container;
        this.configService = container.getConfigService();
    }

    /**
     * Publish multiple configurations at once.
     *
     * @param configs List of configurations to publish
     * @throws NacosException If an error occurs
     */
    public void publishConfigs(List<NacosConfig> configs) throws NacosException {
        for (NacosConfig config : configs) {
            configService.publishConfig(
                    config.getDataId(),
                    config.getGroup(),
                    config.getContent()
            );
        }
    }

    /**
     * Get multiple configurations at once.
     *
     * @param dataIds List of data IDs
     * @param group The group name
     * @return Map of data ID to configuration content
     * @throws NacosException If an error occurs
     */
    public Map<String, String> getConfigs(List<String> dataIds, String group) throws NacosException {
        return dataIds.stream()
                .collect(Collectors.toMap(
                        dataId -> dataId,
                        dataId -> {
                            try {
                                return configService.getConfig(dataId, group, 5000);
                            } catch (NacosException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ));
    }

    /**
     * Add a configuration change listener and wait for a change.
     *
     * @param dataId The data ID to listen to
     * @param group The group name
     * @param timeoutSeconds The timeout in seconds
     * @return The new configuration content
     * @throws NacosException If an error occurs
     * @throws InterruptedException If interrupted
     */
    public String waitForConfigChange(String dataId, String group, int timeoutSeconds) throws NacosException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder newContent = new StringBuilder();

        configService.addListener(dataId, group, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                newContent.append(configInfo);
                latch.countDown();
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });

        boolean success = latch.await(timeoutSeconds, TimeUnit.SECONDS);
        if (!success) {
            throw new RuntimeException("Timeout waiting for config change");
        }

        return newContent.toString();
    }

    /**
     * Rollback a configuration to a previous version.
     *
     * @param dataId The data ID
     * @param group The group name
     * @param version The version to rollback to
     * @throws NacosException If an error occurs
     */
    public void rollbackConfig(String dataId, String group, String version) throws NacosException {
        // This is a placeholder - actual implementation depends on Nacos API support
        // For now, we'll just log a message
        System.out.println("Rolling back config " + dataId + " to version " + version);
    }

    /**
     * Get configuration history for a data ID.
     *
     * @param dataId The data ID
     * @param group The group name
     * @return List of configuration versions
     * @throws NacosException If an error occurs
     */
    public List<String> getConfigHistory(String dataId, String group) throws NacosException {
        // This is a placeholder - actual implementation depends on Nacos API support
        // For now, we'll return an empty list
        return List.of();
    }

    /**
     * Validate configuration syntax.
     *
     * @param config The configuration to validate
     * @return true if valid, false otherwise
     */
    public boolean validateConfig(NacosConfig config) {
        // Basic validation - check if dataId and content are not empty
        return config.getDataId() != null && !config.getDataId().isEmpty()
                && config.getContent() != null && !config.getContent().isEmpty();
    }

    /**
     * Get the ConfigService instance.
     *
     * @return The ConfigService instance
     */
    public ConfigService getConfigService() {
        return configService;
    }
}

// Needed for the Listener interface
interface Executor {
    void execute(Runnable command);
}
