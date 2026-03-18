package io.github.cacotopia.testcontainers.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;

import java.util.Properties;

/**
 * Factory class for creating Nacos client instances.
 * Used to create ConfigService and NamingService clients with proper configuration.
 */
public class NacosClientFactory {

    /**
     * Nacos server address
     */
    private final String serverAddr;

    /**
     * Nacos username
     */
    private final String username;

    /**
     * Nacos password
     */
    private final String password;

    /**
     * Nacos namespace
     */
    private final String namespace;

    /**
     * Creates a new NacosClientFactory with the specified server address, username, and password.
     *
     * @param serverAddr The Nacos server address
     * @param username The Nacos username
     * @param password The Nacos password
     */
    public NacosClientFactory(String serverAddr, String username, String password) {
        this(serverAddr, username, password, "");
    }

    /**
     * Creates a new NacosClientFactory with the specified server address, username, password, and namespace.
     *
     * @param serverAddr The Nacos server address
     * @param username The Nacos username
     * @param password The Nacos password
     * @param namespace The Nacos namespace
     */
    public NacosClientFactory(String serverAddr, String username, String password, String namespace) {
        this.serverAddr = serverAddr;
        this.username = username;
        this.password = password;
        this.namespace = namespace;
    }

    /**
     * Creates a NacosClientFactory from a NacosContainer instance.
     *
     * @param container The NacosContainer instance
     * @return A new NacosClientFactory
     */
    public static NacosClientFactory fromContainer(NacosContainer container) {
        return new NacosClientFactory(
            container.getServiceUrl(),
            container.getUsername(),
            container.getPassword()
        );
    }

    /**
     * Creates a ConfigService client.
     *
     * @return A ConfigService instance
     * @throws NacosException If an error occurs while creating the client
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
     * Creates a NamingService client.
     *
     * @return A NamingService instance
     * @throws NacosException If an error occurs while creating the client
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
     * Creates client properties for Nacos connections.
     *
     * @return Properties for Nacos client configuration
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

    /**
     * Gets the server address.
     *
     * @return The server address
     */
    public String getServerAddr() {
        return serverAddr;
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the namespace.
     *
     * @return The namespace
     */
    public String getNamespace() {
        return namespace;
    }
}
