package com.github.cacotopia.testcontainers.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;

public abstract class ExtendableNacosContainer<SELF extends ExtendableNacosContainer<SELF>> extends GenericContainer<SELF> {
    // Nacos 相关常量
    private static final String NACOS_IMAGE = "nacos/nacos-server";
    private static final String NACOS_VERSION = "2.2.3";

    private static final int NACOS_PORT_HTTP = 8848;
    private static final int NACOS_PORT_GRPC = 9848;
    private static final int NACOS_PORT_GRPC_MGMT = 9849;

    // 版本相关
    private NacosVersion nacosVersion;
    private String dockerImageName;

    // 配置属性
    private String username = "nacos";
    private String password = "nacos";
    private NacosDatabaseConfig databaseConfig = NacosDatabaseConfig.embedded();
    private boolean clusterMode = false;
    private List<NacosClusterNode> clusterNodes = new ArrayList<>();
    private String[] customCommandParts;

    // 集群相关
    private String clusterNodeId;
    private Network clusterNetwork;

    // 配置管理
    private String namespace = "";
    private String defaultGroup = "DEFAULT_GROUP";
    private List<NacosConfig> initialConfigs = new ArrayList<>();

    // 服务注册
    private List<NacosServiceInstance> initialServices = new ArrayList<>();

    // 功能开关
    private boolean authEnabled = true;
    private boolean consoleEnabled = true;
    private boolean metricsEnabled = false;
    private int tokenExpiration = 18000; // 默认 5 小时

    // 客户端缓存
    private NacosClientFactory clientFactory;
    private ConfigService configService;
    private NamingService namingService;

    // 构造方法
    public ExtendableNacosContainer() {
        this(NACOS_IMAGE + ":" + NACOS_VERSION);
    }

    public ExtendableNacosContainer(String dockerImageName) {
        super(DockerImageName.parse(dockerImageName));
        this.dockerImageName = dockerImageName;
        this.nacosVersion = NacosVersion.fromImageName(dockerImageName);
        withExposedPorts(NACOS_PORT_HTTP, NACOS_PORT_GRPC, NACOS_PORT_GRPC_MGMT);
        withLogConsumer(new Slf4jLogConsumer(logger()));
        logger().info("Using Nacos version: {}", nacosVersion.getDisplayName());
    }

    // 配置方法
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

        // 初始化客户端工厂
        this.clientFactory = new NacosClientFactory(getServiceUrl(), username, password, namespace);
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);

        // 容器启动后，导入初始配置和注册服务
        try {
            importInitialConfigs();
            registerInitialServices();
        } catch (Exception e) {
            logger().warn("Failed to initialize Nacos configs or services: {}", e.getMessage());
        }
    }

    /**
     * 导入初始配置
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
     * 注册初始服务
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
     * 获取 Nacos 版本
     */
    public NacosVersion getNacosVersion() {
        return nacosVersion;
    }

    /**
     * 判断是否为 Nacos 3.x
     */
    public boolean isV3() {
        return nacosVersion != null && nacosVersion.isV3();
    }

    /**
     * 判断是否为 Nacos 2.x
     */
    public boolean isV2() {
        return nacosVersion != null && nacosVersion.isV2();
    }

    /**
     * 获取 Docker 镜像名称
     */
    public String getDockerImageName() {
        return dockerImageName;
    }

    // 公共方法
    public SELF withUsername(String username) {
        this.username = username;
        return self();
    }

    public SELF withPassword(String password) {
        this.password = password;
        return self();
    }

    /**
     * @deprecated 使用 {@link #withDatabaseConfig(NacosDatabaseConfig)} 替代
     */
    @Deprecated
    public SELF withDatabaseType(String databaseType) {
        if ("embedded".equalsIgnoreCase(databaseType)) {
            this.databaseConfig = NacosDatabaseConfig.embedded();
        }
        return self();
    }

    /**
     * 配置数据库
     */
    public SELF withDatabaseConfig(NacosDatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        return self();
    }

    /**
     * 使用外部 MySQL 数据库
     */
    public SELF withExternalMySQL(String host, int port, String database, String username, String password) {
        this.databaseConfig = NacosDatabaseConfig.externalMySQL(host, port, database, username, password);
        return self();
    }

    /**
     * 使用 Testcontainers MySQL 容器
     */
    public SELF withMySQLContainer(MySQLContainer mysqlContainer) {
        this.databaseConfig = NacosDatabaseConfig.mysqlContainer(mysqlContainer);
        return self();
    }

    public SELF withClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
        return self();
    }

    /**
     * 配置集群节点列表
     */
    public SELF withClusterNodes(List<NacosClusterNode> clusterNodes) {
        this.clusterNodes = new ArrayList<>(clusterNodes);
        this.clusterMode = true;
        return self();
    }

    /**
     * 配置集群节点列表（可变参数）
     */
    public SELF withClusterNodes(NacosClusterNode... clusterNodes) {
        this.clusterNodes = new ArrayList<>(Arrays.asList(clusterNodes));
        this.clusterMode = true;
        return self();
    }

    /**
     * 设置集群网络（用于多容器集群通信）
     */
    public SELF withClusterNetwork(Network network) {
        this.clusterNetwork = network;
        if (network != null) {
            withNetwork(network);
        }
        return self();
    }

    /**
     * 设置集群节点标识
     */
    public SELF withClusterNodeId(String nodeId) {
        this.clusterNodeId = nodeId;
        return self();
    }

    public SELF withCustomCommand(String... commands) {
        this.customCommandParts = commands;
        return self();
    }

    // ==================== 配置管理方法 ====================

    /**
     * 设置命名空间
     */
    public SELF withNamespace(String namespace) {
        this.namespace = namespace;
        return self();
    }

    /**
     * 设置默认分组
     */
    public SELF withDefaultGroup(String group) {
        this.defaultGroup = group;
        return self();
    }

    /**
     * 添加初始配置（容器启动后自动导入）
     */
    public SELF withInitialConfig(NacosConfig config) {
        this.initialConfigs.add(config);
        return self();
    }

    /**
     * 添加初始配置（简化版）
     */
    public SELF withInitialConfig(String dataId, String content) {
        return withInitialConfig(new NacosConfig(dataId, defaultGroup, content));
    }

    /**
     * 添加初始配置（带分组）
     */
    public SELF withInitialConfig(String dataId, String group, String content) {
        return withInitialConfig(new NacosConfig(dataId, group, content));
    }

    /**
     * 批量添加初始配置
     */
    public SELF withInitialConfigs(List<NacosConfig> configs) {
        this.initialConfigs.addAll(configs);
        return self();
    }

    /**
     * 从文件导入配置（YAML 格式）
     */
    public SELF withConfigImportFromYaml(String yamlContent) {
        // TODO: 解析 YAML 并转换为 NacosConfig
        return self();
    }

    // ==================== 服务注册方法 ====================

    /**
     * 添加初始服务实例（容器启动后自动注册）
     */
    public SELF withInitialService(NacosServiceInstance instance) {
        this.initialServices.add(instance);
        return self();
    }

    /**
     * 添加初始服务实例（简化版）
     */
    public SELF withInitialService(String serviceName, String ip, int port) {
        return withInitialService(new NacosServiceInstance(serviceName, ip, port));
    }

    /**
     * 批量添加初始服务实例
     */
    public SELF withInitialServices(List<NacosServiceInstance> instances) {
        this.initialServices.addAll(instances);
        return self();
    }

    // ==================== 功能开关方法 ====================

    /**
     * 启用/禁用认证
     */
    public SELF withAuthEnabled(boolean enabled) {
        this.authEnabled = enabled;
        return self();
    }

    /**
     * 启用/禁用控制台
     */
    public SELF withConsoleEnabled(boolean enabled) {
        this.consoleEnabled = enabled;
        return self();
    }

    /**
     * 启用指标监控
     */
    public SELF withMetricsEnabled() {
        this.metricsEnabled = true;
        return self();
    }

    /**
     * 设置 Token 过期时间（秒）
     */
    public SELF withTokenExpiration(int seconds) {
        this.tokenExpiration = seconds;
        return self();
    }

    // ==================== 客户端访问方法 ====================

    public String getServiceUrl() {
        return String.format("http://%s:%s/nacos", getHost(), getMappedPort(NACOS_PORT_HTTP));
    }

    public String getGrpcAddress() {
        return String.format("%s:%s", getHost(), getMappedPort(NACOS_PORT_GRPC));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public NacosDatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public boolean isClusterMode() {
        return clusterMode;
    }

    public List<NacosClusterNode> getClusterNodes() {
        return new ArrayList<>(clusterNodes);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public List<NacosConfig> getInitialConfigs() {
        return new ArrayList<>(initialConfigs);
    }

    public List<NacosServiceInstance> getInitialServices() {
        return new ArrayList<>(initialServices);
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    // ==================== Nacos 客户端方法 ====================

    /**
     * 获取客户端工厂
     */
    public NacosClientFactory getClientFactory() {
        if (clientFactory == null) {
            clientFactory = new NacosClientFactory(getServiceUrl(), username, password, namespace);
        }
        return clientFactory;
    }

    /**
     * 获取配置服务客户端
     */
    public ConfigService getConfigService() throws NacosException {
        if (configService == null) {
            configService = getClientFactory().createConfigService();
        }
        return configService;
    }

    /**
     * 获取命名服务客户端
     */
    public NamingService getNamingService() throws NacosException {
        if (namingService == null) {
            namingService = getClientFactory().createNamingService();
        }
        return namingService;
    }

    /**
     * 等待集群健康
     */
    public void waitForClusterHealthy(Duration timeout) {
        // TODO: 实现健康检查等待逻辑
    }

    /**
     * 导出所有配置到指定路径
     */
    public void exportConfigs(String targetPath) throws NacosException {
        // TODO: 实现配置导出逻辑
    }

    /**
     * 创建配置快照
     */
    public Map<String, String> createSnapshot() throws NacosException {
        Map<String, String> snapshot = new HashMap<>();
        // TODO: 实现快照逻辑
        return snapshot;
    }
}
