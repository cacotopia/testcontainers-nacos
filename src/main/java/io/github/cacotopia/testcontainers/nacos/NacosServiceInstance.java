package io.github.cacotopia.testcontainers.nacos;

import java.util.HashMap;
import java.util.Map;

/**
 * Nacos service instance class.
 * Represents a service instance registered with Nacos service discovery.
 */
public class NacosServiceInstance {

    /**
     * Service name
     */
    private String serviceName;

    /**
     * Instance IP address
     */
    private String ip;

    /**
     * Instance port
     */
    private int port;

    /**
     * Cluster name (default: DEFAULT)
     */
    private String clusterName = "DEFAULT";

    /**
     * Instance weight (default: 1.0)
     */
    private double weight = 1.0;

    /**
     * Whether the instance is healthy (default: true)
     */
    private boolean healthy = true;

    /**
     * Whether the instance is enabled (default: true)
     */
    private boolean enabled = true;

    /**
     * Whether the instance is ephemeral (default: true)
     */
    private boolean ephemeral = true;

    /**
     * Instance metadata
     */
    private Map<String, String> metadata = new HashMap<>();

    /**
     * Creates a new NacosServiceInstance with the specified service name, IP, and port.
     * Uses default values for other properties.
     *
     * @param serviceName The service name
     * @param ip The instance IP address
     * @param port The instance port
     */
    public NacosServiceInstance(String serviceName, String ip, int port) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
    }

    /**
     * Creates a new NacosServiceInstance with the specified service name, IP, port, and cluster name.
     * Uses default values for other properties.
     *
     * @param serviceName The service name
     * @param ip The instance IP address
     * @param port The instance port
     * @param clusterName The cluster name
     */
    public NacosServiceInstance(String serviceName, String ip, int port, String clusterName) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
        this.clusterName = clusterName;
    }

    /**
     * Gets the service name.
     *
     * @return The service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the service name.
     *
     * @param serviceName The service name to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Gets the instance IP address.
     *
     * @return The IP address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Sets the instance IP address.
     *
     * @param ip The IP address to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Gets the instance port.
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the instance port.
     *
     * @param port The port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the cluster name.
     *
     * @return The cluster name
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * Sets the cluster name.
     *
     * @param clusterName The cluster name to set
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * Gets the instance weight.
     *
     * @return The weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets the instance weight.
     *
     * @param weight The weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Checks if the instance is healthy.
     *
     * @return true if healthy, false otherwise
     */
    public boolean isHealthy() {
        return healthy;
    }

    /**
     * Sets whether the instance is healthy.
     *
     * @param healthy true if healthy, false otherwise
     */
    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    /**
     * Checks if the instance is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the instance is enabled.
     *
     * @param enabled true if enabled, false otherwise
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if the instance is ephemeral.
     *
     * @return true if ephemeral, false otherwise
     */
    public boolean isEphemeral() {
        return ephemeral;
    }

    /**
     * Sets whether the instance is ephemeral.
     *
     * @param ephemeral true if ephemeral, false otherwise
     */
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    /**
     * Gets the instance metadata.
     *
     * @return The metadata map
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the instance metadata.
     *
     * @param metadata The metadata map to set
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Adds a metadata key-value pair.
     *
     * @param key The metadata key
     * @param value The metadata value
     * @return This NacosServiceInstance instance
     */
    public NacosServiceInstance withMetadata(String key, String value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Returns a string representation of the NacosServiceInstance.
     *
     * @return A string representation of the object
     */
    @Override
    public String toString() {
        return "NacosServiceInstance{" +
            "serviceName='" + serviceName + '\'' +
            ", ip='" + ip + '\'' +
            ", port=" + port +
            ", clusterName='" + clusterName + '\'' +
            ", weight=" + weight +
            ", healthy=" + healthy +
            ", enabled=" + enabled +
            ", ephemeral=" + ephemeral +
            '}';
    }
}
