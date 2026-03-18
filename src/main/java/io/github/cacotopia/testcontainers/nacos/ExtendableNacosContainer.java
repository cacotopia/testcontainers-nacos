package io.github.cacotopia.testcontainers.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

/**
 * A Testcontainers implementation for Nacos service discovery and configuration server.
 * This abstract class provides a flexible and extensible way to create Nacos containers
 * with various configurations and features.
 *
 * @param <SELF> The type of the subclass extending this container
 */
public abstract class ExtendableNacosContainer<SELF extends ExtendableNacosContainer<SELF>> extends GenericContainer<SELF> {
    /**
     * Default Nacos Docker image name
     */
    private static final String NACOS_IMAGE = "nacos/nacos-server";

    /**
     * Default Nacos version
     */
    private static final String NACOS_VERSION = "v2.5.2";

    /**
     * Nacos HTTP port
     */
    private static final int NACOS_PORT_HTTP = 8848;

    /**
     * Nacos gRPC port
     */
    private static final int NACOS_PORT_GRPC = 9848;

    /**
     * Nacos gRPC management port
     */
    private static final int NACOS_PORT_GRPC_MGMT = 9849;

    /**
     * Nacos version information
     */
    private NacosVersion nacosVersion;

    /**
     * Docker image name
     */
    private String dockerImageName;

    /**
     * Nacos username
     */
    private String username = "nacos";

    /**
     * Nacos password
     */
    private String password = "nacos";

    /**
     * Database configuration
     */
    private NacosDatabaseConfig databaseConfig = NacosDatabaseConfig.embedded();

    /**
     * Whether cluster mode is enabled
     */
    private boolean clusterMode = false;

    /**
     * List of cluster nodes
     */
    private List<NacosClusterNode> clusterNodes = new ArrayList<>();

    /**
     * Custom command parts
     */
    private String[] customCommandParts;

    /**
     * Cluster node ID
     */
    private String clusterNodeId;

    /**
     * Cluster network for inter-container communication
     */
    private Network clusterNetwork;

    /**
     * Nacos namespace
     */
    private String namespace = "";

    /**
     * Default config group
     */
    private String defaultGroup = "DEFAULT_GROUP";

    /**
     * Initial configs to import on container start
     */
    private List<NacosConfig> initialConfigs = new ArrayList<>();

    /**
     * Initial service instances to register on container start
     */
    private List<NacosServiceInstance> initialServices = new ArrayList<>();

    /**
     * Whether authentication is enabled
     */
    private boolean authEnabled = true;

    /**
     * Whether console is enabled
     */
    private boolean consoleEnabled = true;

    /**
     * Whether metrics are enabled
     */
    private boolean metricsEnabled = false;

    /**
     * Token expiration time in seconds (default: 5 hours)
     */
    private int tokenExpiration = 18000; // 默认 5 小时

    /**
     * Nacos client factory
     */
    private NacosClientFactory clientFactory;

    /**
     * Config service client
     */
    private ConfigService configService;

    /**
     * Naming service client
     */
    private NamingService namingService;

    /**
     * Creates a new ExtendableNacosContainer with the default Nacos image and version.
     */
    public ExtendableNacosContainer() {
        this(NACOS_IMAGE + ":" + NACOS_VERSION);
    }

    /**
     * Creates a new ExtendableNacosContainer with the specified Docker image name.
     *
     * @param dockerImageName The Docker image name to use
     */
    public ExtendableNacosContainer(String dockerImageName) {
        super(DockerImageName.parse(dockerImageName));
        this.dockerImageName = dockerImageName;
        this.nacosVersion = NacosVersion.fromImageName(dockerImageName);
        withExposedPorts(NACOS_PORT_HTTP, NACOS_PORT_GRPC, NACOS_PORT_GRPC_MGMT);
        withLogConsumer(new Slf4jLogConsumer(logger()));
        logger().info("Using Nacos version: {}", nacosVersion.getDisplayName());
    }

    /**
     * Configures the Nacos container with the specified settings.
     */}

    /**
     * Starts the container.
     *
     * @throws IllegalStateException if the container cannot be started
     */
    @Override
    public void start() {
        if (databaseConfig != null && !databaseConfig.isEmbedded()) {
            try {
                // Initialize database schema
                NacosDatabaseInitializer.initialize(databaseConfig);
            } catch (SQLException | IOException e) {
                throw new IllegalStateException("Failed to initialize database", e);
            }
            if (databaseConfig.getType() == NacosDatabaseConfig.DatabaseType.MYSQL_CONTAINER && databaseConfig.getMysqlContainer() != null) {
                databaseConfig.getMysqlContainer().start();
            }
            if (databaseConfig.getType() == NacosDatabaseConfig.DatabaseType.POSTGRESQL_CONTAINER && databaseConfig.getPostgresqlContainer() != null) {
                databaseConfig.getPostgresqlContainer().start();
            }
        }
        super.start();
    }
}

    /**
     * Starts the container.
     *
     * @throws IllegalStateException if the container cannot be started
     */
    @Override
    public void start() {
        if (databaseConfig != null && !databaseConfig.isEmbedded()) {
            try {
                // Initialize database schema
                NacosDatabaseInitializer.initialize(databaseConfig);
            } catch (SQLException | IOException e) {
                throw new IllegalStateException("Failed to initialize database", e);
            }
            if (databaseConfig.getType() == NacosDatabaseConfig.DatabaseType.MYSQL_CONTAINER && databaseConfig.getMysqlContainer() != null) {
                databaseConfig.getMysqlContainer().start();
            }
            if (databaseConfig.getType() == NacosDatabaseConfig.DatabaseType.POSTGRESQL_CONTAINER && databaseConfig.getPostgresqlContainer() != null) {
                databaseConfig.getPostgresqlContainer().start();
            }
        }
        super.start();
    }

    @Override
    protected void configure() {
        List<String> commandParts = new ArrayList<>();

        // 使用版本适配器配置环境变量
        NacosEnvironmentConfigurer configurer = new NacosEnvironmentConfigurer(nacosVersion, this);

        // 配置基础设置
        configurer.configureBasicSettings(username, password, authEnabled, tokenExpiration, consoleEnabled, namespace);

        // 配置数据库
        configurer.configureDatabase(databaseConfig);

        // 配置集群模式
        configurer.configureClusterMode(clusterMode, clusterNodes, clusterNodeId);

        // 配置指标监控
        configurer.configureMetrics(metricsEnabled);

        // 配置端口（Nacos 3.x）
        configurer.configurePorts(NACOS_PORT_HTTP, NACOS_PORT_GRPC);

        // 设置等待策略
        String waitPath = configurer.getWaitStrategyPath();
        setWaitStrategy(Wait.forHttp(waitPath).forPort(NACOS_PORT_HTTP)
            .forStatusCode(200)
            .withStartupTimeout(Duration.ofMinutes(2)));

        // 添加自定义命令
        if (customCommandParts != null) {
            commandParts.addAll(Arrays.asList(customCommandParts));
        }

        if (!commandParts.isEmpty()) {
            setCommand(commandParts.toArray(new String[0]));
        }
    }

    /**
     * Called when the container is started.
     * Imports initial configurations and registers initial services.
     *
     * @param containerInfo The container information
     */
    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);

        // 容器启动后，初始化客户端工厂
        this.clientFactory = new NacosClientFactory(getServiceUrl(), username, password, namespace);

        // 容器启动后，导入初始配置和注册服务
        try {
            importInitialConfigs();
            registerInitialServices();
        } catch (Exception e) {
            logger().warn("Failed to initialize Nacos configs or services: {}", e.getMessage());
        }
    }

    /**
     * Imports initial configurations into Nacos.
     *
     * @throws NacosException If an error occurs while importing configs
     */
    private void importInitialConfigs() throws NacosException {
        if (initialConfigs.isEmpty()) {
            return;
        }

        ConfigService configService = getConfigService();
        for (NacosConfig config : initialConfigs) {
            configService.publishConfig(config.getDataId(), config.getGroup(), config.getContent());
            logger().info("Imported Nacos config: {}:{}", config.getGroup(), config.getDataId());
        }
    }

    /**
     * Registers initial service instances with Nacos.
     *
     * @throws NacosException If an error occurs while registering services
     */
    private void registerInitialServices() throws NacosException {
        if (initialServices.isEmpty()) {
            return;
        }

        NamingService namingService = getNamingService();
        for (NacosServiceInstance instance : initialServices) {
            com.alibaba.nacos.api.naming.pojo.Instance nacosInstance = new com.alibaba.nacos.api.naming.pojo.Instance();
            nacosInstance.setIp(instance.getIp());
            nacosInstance.setPort(instance.getPort());
            nacosInstance.setClusterName(instance.getClusterName());
            nacosInstance.setWeight(instance.getWeight());
            nacosInstance.setHealthy(instance.isHealthy());
            nacosInstance.setEnabled(instance.isEnabled());
            nacosInstance.setEphemeral(instance.isEphemeral());
            nacosInstance.setMetadata(instance.getMetadata());
            // instance.getGroup(),
            namingService.registerInstance(instance.getServiceName(), nacosInstance);
            logger().info("Registered Nacos service: {} - {}:{}",
                instance.getServiceName(), instance.getIp(), instance.getPort());
        }
    }

    /**
     * Gets the Nacos version.
     *
     * @return The Nacos version
     */
    public NacosVersion getNacosVersion() {
        return nacosVersion;
    }

    /**
     * Checks if the Nacos version is 3.x.
     *
     * @return true if Nacos version is 3.x, false otherwise
     */
    public boolean isV3() {
        return nacosVersion != null && nacosVersion.isV3();
    }

    /**
     * Checks if the Nacos version is 2.x.
     *
     * @return true if Nacos version is 2.x, false otherwise
     */
    public boolean isV2() {
        return nacosVersion != null && nacosVersion.isV2();
    }

    /**
     * Gets the Docker image name.
     *
     * @return The Docker image name
     */
    public String getDockerImageName() {
        return dockerImageName;
    }

    /**
     * Sets the username for Nacos authentication.
     *
     * @param username The username to use
     * @return This container instance
     */
    public SELF withUsername(String username) {
        this.username = username;
        return self();
    }

    /**
     * Sets the password for Nacos authentication.
     *
     * @param password The password to use
     * @return This container instance
     */
    public SELF withPassword(String password) {
        this.password = password;
        return self();
    }

    /**
     * @param databaseType The databaseType to use
     * @return This container instance
     * @deprecated Use {@link #withDatabaseConfig(NacosDatabaseConfig)} instead
     */
    @Deprecated
    public SELF withDatabaseType(String databaseType) {
        if ("embedded".equalsIgnoreCase(databaseType)) {
            this.databaseConfig = NacosDatabaseConfig.embedded();
        }
        return self();
    }

    /**
     * Configures the database for Nacos.
     *
     * @param databaseConfig The database configuration
     * @return This container instance
     */
    public SELF withDatabaseConfig(NacosDatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        return self();
    }

    /**
     * Configures Nacos to use an external MySQL database.
     *
     * @param host     The MySQL host
     * @param port     The MySQL port
     * @param database The database name
     * @param username The MySQL username
     * @param password The MySQL password
     * @return This container instance
     */
    public SELF withExternalMySQL(String host, int port, String database, String username, String password) {
        this.databaseConfig = NacosDatabaseConfig.externalMySQL(host, port, database, username, password);
        return self();
    }

    /**
     * Configures Nacos to use a Testcontainers MySQL container.
     *
     * @param mysqlContainer The MySQL container to use
     * @return This container instance
     */
    public SELF withMySQLContainer(MySQLContainer mysqlContainer) {
        this.databaseConfig = NacosDatabaseConfig.mysqlContainer(mysqlContainer);
        return self();
    }

    /**
     * Configures Nacos to use an external PostgreSQL database.
     *
     * @param host     The PostgreSQL host
     * @param port     The PostgreSQL port
     * @param database The database name
     * @param username The PostgreSQL username
     * @param password The PostgreSQL password
     * @return This container instance
     */
    public SELF withExternalPostgreSQL(String host, int port, String database, String username, String password) {
        this.databaseConfig = NacosDatabaseConfig.externalPostgreSQL(host, port, database, username, password);
        return self();
    }

    /**
     * Configures Nacos to use a Testcontainers PostgreSQL container.
     *
     * @param postgresqlContainer The PostgreSQL container to use
     * @return This container instance
     */
    public SELF withPostgreSQLContainer(PostgreSQLContainer postgresqlContainer) {
        this.databaseConfig = NacosDatabaseConfig.postgresqlContainer(postgresqlContainer);
        return self();
    }

    /**
     * Enables or disables cluster mode.
     *
     * @param clusterMode true to enable cluster mode, false otherwise
     * @return This container instance
     */
    public SELF withClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
        return self();
    }

    /**
     * Configures the list of cluster nodes.
     *
     * @param clusterNodes The list of cluster nodes
     * @return This container instance
     */
    public SELF withClusterNodes(List<NacosClusterNode> clusterNodes) {
        this.clusterNodes = new ArrayList<>(clusterNodes);
        this.clusterMode = true;
        return self();
    }

    /**
     * Configures the list of cluster nodes (varargs).
     *
     * @param clusterNodes The cluster nodes
     * @return This container instance
     */
    public SELF withClusterNodes(NacosClusterNode... clusterNodes) {
        this.clusterNodes = new ArrayList<>(Arrays.asList(clusterNodes));
        this.clusterMode = true;
        return self();
    }

    /**
     * Sets the cluster network for inter-container communication.
     *
     * @param network The network to use
     * @return This container instance
     */
    public SELF withClusterNetwork(Network network) {
        this.clusterNetwork = network;
        if (network != null) {
            withNetwork(network);
        }
        return self();
    }

    /**
     * Sets the cluster node ID.
     *
     * @param nodeId The node ID
     * @return This container instance
     */
    public SELF withClusterNodeId(String nodeId) {
        this.clusterNodeId = nodeId;
        return self();
    }

    /**
     * Sets custom commands to run in the container.
     *
     * @param commands The custom commands
     * @return This container instance
     */
    public SELF withCustomCommand(String... commands) {
        this.customCommandParts = commands;
        return self();
    }

    /**
     * Sets the namespace for Nacos.
     *
     * @param namespace The namespace to use
     * @return This container instance
     */
    public SELF withNamespace(String namespace) {
        this.namespace = namespace;
        return self();
    }

    /**
     * Sets the default config group.
     *
     * @param group The default group name
     * @return This container instance
     */
    public SELF withDefaultGroup(String group) {
        this.defaultGroup = group;
        return self();
    }

    /**
     * Adds an initial configuration to import when the container starts.
     *
     * @param config The configuration to add
     * @return This container instance
     */
    public SELF withInitialConfig(NacosConfig config) {
        this.initialConfigs.add(config);
        return self();
    }

    /**
     * Adds an initial configuration (simplified version).
     *
     * @param dataId  The data ID
     * @param content The configuration content
     * @return This container instance
     */
    public SELF withInitialConfig(String dataId, String content) {
        return withInitialConfig(new NacosConfig(dataId, defaultGroup, content));
    }

    /**
     * Adds an initial configuration with a specific group.
     *
     * @param dataId  The data ID
     * @param group   The group name
     * @param content The configuration content
     * @return This container instance
     */
    public SELF withInitialConfig(String dataId, String group, String content) {
        return withInitialConfig(new NacosConfig(dataId, group, content));
    }

    /**
     * Adds multiple initial configurations.
     *
     * @param configs The configurations to add
     * @return This container instance
     */
    public SELF withInitialConfigs(List<NacosConfig> configs) {
        this.initialConfigs.addAll(configs);
        return self();
    }

    /**
     * Imports configurations from YAML content.
     *
     * @param yamlContent The YAML content
     * @return This container instance
     */
    public SELF withConfigImportFromYaml(String yamlContent) {
        // TODO: 解析 YAML 并转换为 NacosConfig
        return self();
    }

    /**
     * Adds an initial service instance to register when the container starts.
     *
     * @param instance The service instance to add
     * @return This container instance
     */
    public SELF withInitialService(NacosServiceInstance instance) {
        this.initialServices.add(instance);
        return self();
    }

    /**
     * Adds an initial service instance (simplified version).
     *
     * @param serviceName The service name
     * @param ip          The instance IP
     * @param port        The instance port
     * @return This container instance
     */
    public SELF withInitialService(String serviceName, String ip, int port) {
        return withInitialService(new NacosServiceInstance(serviceName, ip, port));
    }

    /**
     * Adds multiple initial service instances.
     *
     * @param instances The service instances to add
     * @return This container instance
     */
    public SELF withInitialServices(List<NacosServiceInstance> instances) {
        this.initialServices.addAll(instances);
        return self();
    }

    /**
     * Enables or disables authentication.
     *
     * @param enabled true to enable authentication, false otherwise
     * @return This container instance
     */
    public SELF withAuthEnabled(boolean enabled) {
        this.authEnabled = enabled;
        return self();
    }

    /**
     * Enables or disables the console.
     *
     * @param enabled true to enable the console, false otherwise
     * @return This container instance
     */
    public SELF withConsoleEnabled(boolean enabled) {
        this.consoleEnabled = enabled;
        return self();
    }

    /**
     * Enables metrics monitoring.
     *
     * @return This container instance
     */
    public SELF withMetricsEnabled() {
        this.metricsEnabled = true;
        return self();
    }

    /**
     * Sets the token expiration time in seconds.
     *
     * @param seconds The token expiration time in seconds
     * @return This container instance
     */
    public SELF withTokenExpiration(int seconds) {
        this.tokenExpiration = seconds;
        return self();
    }

    /**
     * Gets the Nacos service URL.
     *
     * @return The service URL
     */
    public String getServiceUrl() {
        return String.format("http://%s:%s/nacos", getHost(), getMappedPort(NACOS_PORT_HTTP));
    }

    /**
     * Gets the gRPC address for Nacos.
     *
     * @return The gRPC address
     */
    public String getGrpcAddress() {
        return String.format("%s:%s", getHost(), getMappedPort(NACOS_PORT_GRPC));
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
     * Gets the database configuration.
     *
     * @return The database configuration
     */
    public NacosDatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    /**
     * Checks if cluster mode is enabled.
     *
     * @return true if cluster mode is enabled, false otherwise
     */
    public boolean isClusterMode() {
        return clusterMode;
    }

    /**
     * Gets the list of cluster nodes.
     *
     * @return The list of cluster nodes
     */
    public List<NacosClusterNode> getClusterNodes() {
        return new ArrayList<>(clusterNodes);
    }

    /**
     * Gets the namespace.
     *
     * @return The namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the default group.
     *
     * @return The default group
     */
    public String getDefaultGroup() {
        return defaultGroup;
    }

    /**
     * Gets the initial configurations.
     *
     * @return The initial configurations
     */
    public List<NacosConfig> getInitialConfigs() {
        return new ArrayList<>(initialConfigs);
    }

    /**
     * Gets the initial service instances.
     *
     * @return The initial service instances
     */
    public List<NacosServiceInstance> getInitialServices() {
        return new ArrayList<>(initialServices);
    }

    /**
     * Checks if authentication is enabled.
     *
     * @return true if authentication is enabled, false otherwise
     */
    public boolean isAuthEnabled() {
        return authEnabled;
    }

    /**
     * Checks if the console is enabled.
     *
     * @return true if the console is enabled, false otherwise
     */
    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    /**
     * Checks if metrics are enabled.
     *
     * @return true if metrics are enabled, false otherwise
     */
    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    /**
     * Gets the Nacos client factory.
     *
     * @return The client factory
     */
    public NacosClientFactory getClientFactory() {
        if (clientFactory == null) {
            clientFactory = new NacosClientFactory(getServiceUrl(), username, password, namespace);
        }
        return clientFactory;
    }

    /**
     * Gets the config service client.
     *
     * @return The config service client
     * @throws NacosException If an error occurs while creating the client
     */
    public ConfigService getConfigService() throws NacosException {
        if (configService == null) {
            configService = getClientFactory().createConfigService();
        }
        return configService;
    }

    /**
     * Gets the naming service client.
     *
     * @return The naming service client
     * @throws NacosException If an error occurs while creating the client
     */
    public NamingService getNamingService() throws NacosException {
        if (namingService == null) {
            namingService = getClientFactory().createNamingService();
        }
        return namingService;
    }

    /**
     * Waits for the cluster to become healthy.
     *
     * @param timeout The maximum time to wait
     */
    public void waitForClusterHealthy(Duration timeout) {
        // TODO: 实现健康检查等待逻辑
    }

    /**
     * Exports all configurations to the specified path.
     *
     * @param targetPath The target path
     * @throws NacosException If an error occurs while exporting configs
     */
    public void exportConfigs(String targetPath) throws NacosException {
        // TODO: 实现配置导出逻辑
    }

    /**
     * Creates a snapshot of all configurations.
     *
     * @return A map of configurations
     * @throws NacosException If an error occurs while creating the snapshot
     */
    public Map<String, String> createSnapshot() throws NacosException {
        Map<String, String> snapshot = new HashMap<>();
        // TODO: 实现快照逻辑
        return snapshot;
    }
}
