package io.github.cacotopia.testcontainers.nacos;

import org.testcontainers.containers.GenericContainer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Nacos environment variable configurer.
 * Automatically adapts environment variables based on Nacos version (2.x or 3.x).
 */
public class NacosEnvironmentConfigurer {

    /**
     * Nacos version
     */
    private final NacosVersion version;

    /**
     * Container instance to configure
     */
    private final GenericContainer<?> container;

    /**
     * Creates a new NacosEnvironmentConfigurer with the specified version and container.
     *
     * @param version   The Nacos version
     * @param container The container to configure
     */
    public NacosEnvironmentConfigurer(NacosVersion version, GenericContainer<?> container) {
        this.version = version != null ? version : NacosVersion.getDefault();
        this.container = container;
    }

    /**
     * Configures basic environment variables.
     *
     * @param consoleUiEnabled Whether the console UI is enabled
     * @param namespace        The Nacos namespace
     * @param debug            Whether debug mode is enabled
     */
    public void configureBasicSettings(boolean debug,
                                       boolean consoleUiEnabled, String namespace) {
        // 认证配置（2.x 和 3.x 相同）
        withEnv(NacosConstant.NACOS_DEBUG, String.valueOf(debug));

        // 控制台配置
        if (version.isV3()) {
            // Nacos 3.x 使用新的控制台配置
            withEnv(NacosConstant.NACOS_CONSOLE_UI_ENABLED, String.valueOf(consoleUiEnabled));
        }
        // 命名空间配置
        if (namespace != null && !namespace.isEmpty()) {
            withEnv("NACOS_NAMESPACE", namespace);
        }
    }

    /**
     * Configures authentication environment variables.
     *
     * @param authEnabled        Whether authentication is enabled
     * @param tokenExpiration    Token expiration time in seconds
     * @param consoleAuthEnabled Whether the console authentication is enabled
     * @param adminAuthEnabled   Whether the admin authentication is enabled
     * @param authToken          The authentication token (if auth is enabled)
     * @param authIdentityKey    The authentication identity key (if auth is enabled)
     * @param authIdentityValue  The authentication identity value (if auth is enabled)
     */
    public void configureAuthSettings(boolean authEnabled, String authToken,
                                      boolean consoleAuthEnabled, boolean adminAuthEnabled,
                                      String authIdentityKey, String authIdentityValue,
                                      int tokenExpiration) {
        // 认证配置（2.x 和 3.x 相同）
        withEnv(NacosConstant.NACOS_AUTH_ENABLE, String.valueOf(authEnabled));
        withEnv(NacosConstant.NACOS_AUTH_TOKEN, authToken);
        withEnv(NacosConstant.NACOS_AUTH_TOKEN_EXPIRE_SECONDS, String.valueOf(tokenExpiration));

        // Nacos 2.x 服务端身份验证配置（2.2.0+ 必需）
        if (authEnabled) {
            withEnv(NacosConstant.NACOS_AUTH_IDENTITY_KEY, authIdentityKey);
            withEnv(NacosConstant.NACOS_AUTH_IDENTITY_VALUE, authIdentityValue);
        }
        // 控制台配置
        if (version.isV3()) {
            // Nacos 3.x 使用新的控制台配置
            withEnv(NacosConstant.NACOS_AUTH_CONSOLE_ENABLE, String.valueOf(consoleAuthEnabled));
            withEnv(NacosConstant.NACOS_AUTH_ADMIN_ENABLE, String.valueOf(adminAuthEnabled));
        }
    }

    /**
     * Configures database settings.
     *
     * @param databaseConfig The database configuration
     */
    public void configureDatabase(NacosDatabaseConfig databaseConfig) {
        if (databaseConfig == null || databaseConfig.isEmbedded()) {
            configureEmbeddedDatabase();
        } else if (databaseConfig.isMySQL()) {
            configureMySQL(databaseConfig);
        } else if (databaseConfig.isPostgreSQL()) {
            configurePostgreSQL(databaseConfig);
        }
    }

    /**
     * Configures embedded database settings.
     */
    private void configureEmbeddedDatabase() {
        if (version.isV3()) {
            // Nacos 3.x 使用 Derby 或 空配置表示嵌入式
            withEnv("spring.sql.init.platform", "");
        } else {
            // Nacos 2.x 使用 SPRING_DATASOURCE_PLATFORM
            withEnv(NacosConstant.SPRING_DATASOURCE_PLATFORM, "");
            withEnv(NacosConstant.EMBEDDED_STORAGE, "embedded");
            withEnv(NacosConstant.NACOS_AUTH_CACHE_ENABLE, "true");
        }
    }

    /**
     * Configures MySQL database settings.
     *
     * @param databaseConfig The database configuration
     */
    private void configureMySQL(NacosDatabaseConfig databaseConfig) {
        if (version.isV3()) {
            // Nacos 3.x 使用新的数据库配置方式
            withEnv("spring.sql.init.platform", "mysql");
            withEnv("db.num", "1");
            withEnv("db.url.0", databaseConfig.getUrl());
            withEnv("db.user.0", databaseConfig.getUsername());
            withEnv("db.password.0", databaseConfig.getPassword());
        } else {
            // Nacos 2.x 使用旧的数据库配置方式
            // TODO 添加对 MySQL 的支持
            withEnv(NacosConstant.SPRING_DATASOURCE_PLATFORM, "mysql");
            withEnv(NacosConstant.MYSQL_SERVICE_HOST, databaseConfig.getHost());
            withEnv(NacosConstant.MYSQL_SERVICE_PORT, String.valueOf(databaseConfig.getPort()));
            withEnv(NacosConstant.MYSQL_SERVICE_DB_NAME, databaseConfig.getDatabase());
            withEnv(NacosConstant.MYSQL_SERVICE_USER, databaseConfig.getUsername());
            withEnv(NacosConstant.MYSQL_SERVICE_PASSWORD, databaseConfig.getPassword());
            withEnv(NacosConstant.MYSQL_SERVICE_DB_PARAM, databaseConfig.getUrlParams());

            String url = databaseConfig.getUrl();
            if (url != null) {
                withEnv("SPRING_DATASOURCE_URL", url);
            }
        }
    }

    /**
     * Configures PostgreSQL database settings.
     *
     * @param databaseConfig The database configuration
     */
    private void configurePostgreSQL(NacosDatabaseConfig databaseConfig) {
        if (version.isV3()) {
            // Nacos 3.x 使用新的数据库配置方式
            withEnv("spring.sql.init.platform", "postgresql");
            withEnv("db.num", "1");
            withEnv("db.url.0", databaseConfig.getUrl());
            withEnv("db.user.0", databaseConfig.getUsername());
            withEnv("db.password.0", databaseConfig.getPassword());
        } else {
            // Nacos 2.x 使用旧的数据库配置方式
            withEnv(NacosConstant.SPRING_DATASOURCE_PLATFORM, "postgresql");
            withEnv(NacosConstant.MYSQL_SERVICE_HOST, databaseConfig.getHost());
            withEnv(NacosConstant.MYSQL_SERVICE_PORT, String.valueOf(databaseConfig.getPort()));
            withEnv(NacosConstant.MYSQL_SERVICE_DB_NAME, databaseConfig.getDatabase());
            withEnv(NacosConstant.MYSQL_SERVICE_USER, databaseConfig.getUsername());
            withEnv(NacosConstant.MYSQL_SERVICE_PASSWORD, databaseConfig.getPassword());
            withEnv(NacosConstant.MYSQL_SERVICE_DB_PARAM, databaseConfig.getUrlParams());
            String url = databaseConfig.getUrl();
            if (url != null) {
                withEnv("SPRING_DATASOURCE_URL", url);
            }
        }
    }

    /**
     * Configures cluster mode settings.
     *
     * @param clusterMode   Whether cluster mode is enabled
     * @param clusterNodes  List of cluster nodes
     * @param clusterNodeId Cluster node ID
     */
    public void configureClusterMode(boolean clusterMode, List<NacosClusterNode> clusterNodes, String clusterNodeId) {
        if (clusterMode) {
            if (version.isV3()) {
                // Nacos 3.x 使用 nacos.standalone=false 表示集群模式
                withEnv("nacos.standalone", "false");
            } else {
                // Nacos 2.x 使用 NACOS_MODE=cluster
                withEnv(NacosConstant.MODE, NacosConstant.CLUSTER_MODE);
            }

            // 配置集群节点列表
            if (!clusterNodes.isEmpty()) {
                String clusterConf = clusterNodes.stream()
                    .map(NacosClusterNode::getClusterAddress)
                    .collect(Collectors.joining(","));

                if (version.isV3()) {
                    // Nacos 3.x 使用 nacos.member.list
                    withEnv("nacos.member.list", clusterConf);
                } else {
                    // Nacos 2.x 使用 NACOS_SERVERS
                    withEnv(NacosConstant.NACOS_SERVERS, clusterConf.replace(",", "\n"));
                }
            }

            // 设置节点标识
            if (clusterNodeId != null) {
                if (version.isV3()) {
                    withEnv("nacos.server.ip", clusterNodeId);
                } else {
                    withEnv(NacosConstant.NACOS_SERVER_IP, clusterNodeId);
                }
            }
        } else {
            // 单机模式
            if (version.isV3()) {
                withEnv("nacos.standalone", "true");
            } else {
                withEnv(NacosConstant.MODE, NacosConstant.STANDALONE_MODE);
            }
        }
    }

    /**
     * Configures metrics monitoring settings.
     *
     * @param metricsEnabled Whether metrics are enabled
     */
    public void configureMetrics(boolean metricsEnabled) {
        if (metricsEnabled) {
            withEnv("MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE", "*");
            withEnv("MANAGEMENT_ENDPOINTS_HEALTH_SHOW_DETAILS", "always");

            if (version.isV3()) {
                // Nacos 3.x 可能有额外的指标配置
                withEnv("nacos.core.monitor.topn.enabled", "true");
            }
        }
    }

    /**
     * Configures port settings (Nacos 3.x uses new configuration items).
     *
     * @param httpPort The HTTP port
     * @param grpcPort The gRPC port
     */
    public void configurePorts(int httpPort, int grpcPort) {
        if (version.isV3()) {
            // Nacos 3.x 使用 nacos.server.main.port
            withEnv("nacos.server.main.port", String.valueOf(httpPort));
            // gRPC 端口通常是 httpPort + 1000
            // Nacos 3.x 自动处理 gRPC 端口
        }
        // Nacos 2.x 使用默认端口配置，不需要额外设置
    }

    /**
     * Gets the wait strategy path for health checking.
     *
     * @return The wait strategy path
     */
    public String getWaitStrategyPath() {
        if (version.isV3()) {
            // Nacos 3.x 可能使用不同的上下文路径
            return "/nacos";
        }
        return "/nacos";
    }

    /**
     * Helper method to set environment variables.
     *
     * @param key   The environment variable key
     * @param value The environment variable value
     */
    private void withEnv(String key, String value) {
        if (container != null && value != null) {
            container.addEnv(key, value);
        }
    }

    /**
     * Gets the current Nacos version.
     *
     * @return The Nacos version
     */
    public NacosVersion getVersion() {
        return version;
    }

    /**
     * Checks if the Nacos version is 3.x.
     *
     * @return true if Nacos version is 3.x, false otherwise
     */
    public boolean isV3() {
        return version.isV3();
    }

    /**
     * Checks if the Nacos version is 2.x.
     *
     * @return true if Nacos version is 2.x, false otherwise
     */
    public boolean isV2() {
        return version.isV2();
    }
}
