package com.github.cacotopia.testcontainers.nacos;

import java.util.HashMap;
import java.util.Map;

/**
 * Nacos 服务实例
 */
public class NacosServiceInstance {

    private String serviceName;
    private String ip;
    private int port;
    private String clusterName = "DEFAULT";
    private double weight = 1.0;
    private boolean healthy = true;
    private boolean enabled = true;
    private boolean ephemeral = true;
    private Map<String, String> metadata = new HashMap<>();

    public NacosServiceInstance(String serviceName, String ip, int port) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
    }

    public NacosServiceInstance(String serviceName, String ip, int port, String clusterName) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
        this.clusterName = clusterName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public NacosServiceInstance withMetadata(String key, String value) {
        this.metadata.put(key, value);
        return this;
    }

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
