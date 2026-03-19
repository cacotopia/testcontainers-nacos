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
     * Nacos Console port
     */
    private static final int NACOS_PORT_CONSOLE = 8080;

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
     * Whether Debug Mode is enabled
     */
    private boolean debugEnabled = false;

    /**
     * Whether authentication is enabled
     */
    private boolean authEnabled = false;

    /**
     * Whether authentication Cache is enabled
     */
    private boolean authCacheEnabled = true;

    /**
     * Authentication token when  authentication is enabled
     * Must be set since Nacos  2.2.1 when  authentication is enabled
     */
    private String authToken = "";

    /**
     * Authentication Identity Key when  authentication is enabled
     * Must be set since Nacos  2.2.1 when  authentication is enabled
     */
    private String authIdentityKey = "";

    /**
     * Authentication Identity Value when  authentication is enabled
     * Must be set since Nacos  2.2.1 when  authentication is enabled
     */
    private String authIdentityValue = "";

    /**
     * Token expiration time in seconds (default: 5 hours)
     */
    private int tokenExpiration = 18000;

    /**
     * Whether console is enabled
     */
    private boolean consoleUiEnabled = true;

    /**
     * Whether Console API authentication is enabled
     */
    private boolean consoleAuthEnabled = true;

    /**
     * Whether Admin API authentication is enabled
     */
    private boolean adminAuthEnabled = true;

    /**
     * Whether metrics are enabled
     */
    private boolean metricsEnabled = false;

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
        withExposedPorts(NACOS_PORT_CONSOLE, NACOS_PORT_HTTP, NACOS_PORT_GRPC, NACOS_PORT_GRPC_MGMT);
        withLogConsumer(new Slf4jLogConsumer(logger()));
        logger().info("Using Nacos version: {}", nacosVersion.getDisplayName());
    }

    /**
     * Starts the container.
     *
     * @throws IllegalStateException if the container cannot be started
     */
    @Override
    public void start() {
        if (databaseConfig != null && !databaseConfig.isEmbedded()) {
            // Setup shared network for container databases
            Network sharedNetwork = setupSharedNetwork();

            // Start database containers
            if (databaseConfig.getType() == DatabaseType.MYSQL_CONTAINER && databaseConfig.getMysqlContainer() != null) {
                MySQLContainer mysqlContainer = databaseConfig.getMysqlContainer();
                // Apply shared network if DB container doesn't have one
                if (sharedNetwork != null && mysqlContainer.getNetwork() == null) {
                    mysqlContainer.withNetwork(sharedNetwork);
                    mysqlContainer.withNetworkAliases("mysql");
                    logger().info("Configured MySQL container with network alias 'mysql' on network: {}", sharedNetwork.getId());
                }
                mysqlContainer.start();
            }
            if (databaseConfig.getType() == DatabaseType.POSTGRESQL_CONTAINER && databaseConfig.getPostgresqlContainer() != null) {
                PostgreSQLContainer postgresqlContainer = databaseConfig.getPostgresqlContainer();
                // Apply shared network if DB container doesn't have one
                if (sharedNetwork != null && postgresqlContainer.getNetwork() == null) {
                    postgresqlContainer.withNetwork(sharedNetwork);
                    postgresqlContainer.withNetworkAliases("postgresql");
                    logger().info("Configured PostgreSQL container with network alias 'postgresql' on network: {}", sharedNetwork.getId());
                }
                postgresqlContainer.start();
            }
            try {
                // Initialize database schema
                NacosDatabaseInitializer.initialize(databaseConfig);
            } catch (SQLException | IOException e) {
                throw new IllegalStateException("Failed to initialize database", e);
            }
        }
        super.start();
    }

    /**
     * Creates or returns a shared network for inter-container communication.
     * Must be called before starting any containers.
     */
    private Network setupSharedNetwork() {

        if (!isContainerDb()) {
            return null;
        }

        // If Nacos already has a network, use it
        Network network = this.getNetwork();
        if (network != null) {
            logger().info("Using existing Nacos network: {}", network.getId());
            return network;
        }

        // Check if DB container already has a network
        if (databaseConfig.getType() == DatabaseType.MYSQL_CONTAINER && databaseConfig.getMysqlContainer() != null) {
            Network dbNetwork = databaseConfig.getMysqlContainer().getNetwork();
            if (dbNetwork != null) {
                this.withNetwork(dbNetwork);
                logger().info("Using MySQL container's network: {}", dbNetwork.getId());
                return dbNetwork;
            }
        }
        if (databaseConfig.getType() == DatabaseType.POSTGRESQL_CONTAINER && databaseConfig.getPostgresqlContainer() != null) {
            Network dbNetwork = databaseConfig.getPostgresqlContainer().getNetwork();
            if (dbNetwork != null) {
                this.withNetwork(dbNetwork);
                logger().info("Using PostgreSQL container's network: {}", dbNetwork.getId());
                return dbNetwork;
            }
        }

        // Create a new shared network
        Network newNetwork = Network.newNetwork();
        this.withNetwork(newNetwork);
        logger().info("Created new shared network: {}", newNetwork.getId());
        return newNetwork;
    }

    /**
     * Sets up shared network between Nacos and database container.
     * This must be called at configuration time (before start()).
     */
    private void setupNetworkForDatabaseContainer(GenericContainer<?> dbContainer, String alias) {
        if (dbContainer == null) {
            return;
        }

        // If Nacos already has a network, use it for the DB container
        Network nacosNetwork = this.getNetwork();
        if (nacosNetwork != null) {
            if (dbContainer.getNetwork() == null) {
                dbContainer.withNetwork(nacosNetwork);
                dbContainer.withNetworkAliases(alias);
                logger().info("Configured {} container with Nacos network and alias '{}'", alias, alias);
            }
            return;
        }

        // If DB container already has a network, use it for Nacos
        Network dbNetwork = dbContainer.getNetwork();
        if (dbNetwork != null) {
            this.withNetwork(dbNetwork);
            dbContainer.withNetworkAliases(alias);
            logger().info("Using existing {} container network for Nacos with alias '{}'", alias, alias);
            return;
        }

        // Create a new shared network for both
        Network newNetwork = Network.newNetwork();
        this.withNetwork(newNetwork);
        dbContainer.withNetwork(newNetwork);
        dbContainer.withNetworkAliases(alias);
        logger().info("Created shared network for Nacos and {} container with alias '{}'", alias, alias);
    }

    /**
     * Configures the Nacos container with the specified settings.
     */
    @Override
    protected void configure() {
        List<String> commandParts = new ArrayList<>();

        // Setup shared network for container databases before any container starts
        setupDatabaseContainerNetwork();

        // 使用版本适配器配置环境变量
        NacosEnvironmentConfigurer configurer = new NacosEnvironmentConfigurer(nacosVersion, this);

        // 配置基础设置
        configurer.configureBasicSettings(debugEnabled, consoleUiEnabled, namespace);
        configurer.configureAuthSettings(authEnabled, authToken, consoleAuthEnabled, adminAuthEnabled, authIdentityKey, authIdentityValue, tokenExpiration);
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
     * Sets up shared Docker network for inter-container communication.
     * Must be called in configure() before containers start.
     */
    private void setupDatabaseContainerNetwork() {
        if (databaseConfig == null || databaseConfig.isEmbedded()) {
            return;
        }
        if (!isContainerDb()) {
            return;
        }

        // Create a shared network if Nacos doesn't have one
        Network sharedNetwork = this.getNetwork();
        if (sharedNetwork == null) {
            sharedNetwork = Network.newNetwork();
            this.withNetwork(sharedNetwork);
            logger().info("Created shared Docker network for Nacos and database containers");
        }

        // Configure MySQL container with the same network and alias
        if (databaseConfig.getType() == DatabaseType.MYSQL_CONTAINER && databaseConfig.getMysqlContainer() != null) {
            MySQLContainer mysqlContainer = databaseConfig.getMysqlContainer();
            // Only configure network if not already set
            if (mysqlContainer.getNetwork() == null) {
                mysqlContainer.withNetwork(sharedNetwork);
                mysqlContainer.withNetworkAliases("mysql");
                logger().info("Configured MySQL container with network alias 'mysql'");
            }
        }

        // Configure PostgreSQL container with the same network and alias
        if (databaseConfig.getType() == DatabaseType.POSTGRESQL_CONTAINER && databaseConfig.getPostgresqlContainer() != null) {
            PostgreSQLContainer postgresqlContainer = databaseConfig.getPostgresqlContainer();
            // Only configure network if not already set
            if (postgresqlContainer.getNetwork() == null) {
                postgresqlContainer.withNetwork(sharedNetwork);
                postgresqlContainer.withNetworkAliases("postgresql");
                logger().info("Configured PostgreSQL container with network alias 'postgresql'");
            }
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
     * Determines if the database is a container database.
     *
     * @return true if the database is a container database, false otherwise
     */
    private boolean isContainerDb() {
        return databaseConfig.getType() == DatabaseType.MYSQL_CONTAINER ||
            databaseConfig.getType() == DatabaseType.POSTGRESQL_CONTAINER;
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
     * Enables or disables Nacos debug mode.
     *
     * @param enabled true to enable debug mode, false otherwise
     * @return This container instance
     */
    public SELF withDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
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
     * Enables or disables authentication cache.
     *
     * @param enabled true to enable authentication cache, false otherwise
     * @return This container instance
     */
    public SELF withAuthCacheEnabled(boolean enabled) {
        this.authCacheEnabled = enabled;
        return self();
    }

    /**
     * Sets the authentication token when authentication is enabled.
     *
     * @param authToken The authentication token
     * @return This container instance
     */
    public SELF withAuthToken(String authToken) {
        this.authToken = authToken;
        return self();
    }

    /**
     * Enables or disables the Console API authentication.
     *
     * @param enabled true to enable the console, false otherwise
     * @return This container instance
     */
    public SELF withAdminAuthEnabled(boolean enabled) {
        this.adminAuthEnabled = enabled;
        return self();
    }

    /**
     * Enables or disables the Console API authentication.
     *
     * @param enabled true to enable the console, false otherwise
     * @return This container instance
     */
    public SELF withConsoleAuthEnabled(boolean enabled) {
        this.consoleAuthEnabled = enabled;
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
     * Sets the authentication identity key when authentication is enabled.
     *
     * @param authIdentityKey The authentication identity key
     * @return This container instance
     */
    public SELF withAuthIdentityKey(String authIdentityKey) {
        this.authIdentityKey = authIdentityKey;
        return self();
    }

    /**
     * Sets the authentication identity value when authentication is enabled.
     *
     * @param authIdentityValue The authentication identity value
     * @return This container instance
     */
    public SELF withAuthIdentityValue(String authIdentityValue) {
        this.authIdentityValue = authIdentityValue;
        return self();
    }

    /**
     * Enables or disables the console UI.
     *
     * @param enabled true to enable the console, false otherwise
     * @return This container instance
     */
    public SELF withConsoleUiEnabled(boolean enabled) {
        this.consoleUiEnabled = enabled;
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
     * Configures the embedded derby database for Nacos.
     *
     * @return This container instance
     */
    public SELF withEmbeddedDatabaseType() {
        this.databaseConfig = NacosDatabaseConfig.embedded();
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
    public SELF withExternalMySQL(String host, int port, String database, String username, String password, String urlParams) {
        this.databaseConfig = NacosDatabaseConfig.externalMySQL(host, port, database, username, password, urlParams);
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
        // Setup shared network for inter-container communication
        setupNetworkForDatabaseContainer(mysqlContainer, "mysql");
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
    public SELF withExternalPostgreSQL(String host, int port, String database, String username, String password, String urlParams) {
        this.databaseConfig = NacosDatabaseConfig.externalPostgreSQL(host, port, database, username, password, urlParams);
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
        // Setup shared network for inter-container communication
        setupNetworkForDatabaseContainer(postgresqlContainer, "postgresql");
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

    public boolean isAuthCacheEnabled() {
        return authCacheEnabled;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getAuthIdentityKey() {
        return authIdentityKey;
    }

    public String getAuthIdentityValue() {
        return authIdentityValue;
    }

    /**
     * Checks if the console is enabled.
     *
     * @return true if the console is enabled, false otherwise
     */
    public boolean isConsoleUiEnabled() {
        return consoleUiEnabled;
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
