package com.github.cacotopia.testcontainers.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;

import java.util.Properties;

/**
 * Nacos 客户端工厂
 * 用于创建 ConfigService 和 NamingService 客户端
 */
public class NacosClientFactory {
    
    private final String serverAddr;
    private final String username;
    private final String password;
    private final String namespace;
    
    public NacosClientFactory(String serverAddr, String username, String password) {
        this(serverAddr, username, password, "");
    }
    
    public NacosClientFactory(String serverAddr, String username, String password, String namespace) {
        this.serverAddr = serverAddr;
        this.username = username;
        this.password = password;
        this.namespace = namespace;
    }
    
    /**
     * 从 NacosContainer 创建工厂
     */
    public static NacosClientFactory fromContainer(NacosContainer container) {
        return new NacosClientFactory(
            container.getServiceUrl(),
            container.getUsername(),
            container.getPassword()
        );
    }
    
    /**
     * 创建配置服务客户端
     */
    public ConfigService createConfigService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("username", username);
        properties.put("password", password);
        if (namespace != null && !namespace.isEmpty()) {
            properties.put("namespace", namespace);
        }
        return NacosFactory.createConfigService(properties);
    }
    
    /**
     * 创建命名服务客户端
     */
    public NamingService createNamingService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("username", username);
        properties.put("password", password);
        if (namespace != null && !namespace.isEmpty()) {
            properties.put("namespace", namespace);
        }
        return NacosFactory.createNamingService(properties);
    }
    
    /**
     * 创建客户端属性
     */
    public Properties createProperties() {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("username", username);
        properties.put("password", password);
        if (namespace != null && !namespace.isEmpty()) {
            properties.put("namespace", namespace);
        }
        return properties;
    }
    
    public String getServerAddr() {
        return serverAddr;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getNamespace() {
        return namespace;
    }
}
