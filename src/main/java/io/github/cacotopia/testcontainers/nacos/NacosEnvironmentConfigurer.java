package io.github.cacotopia.testcontainers.nacos;

import org.testcontainers.containers.GenericContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nacos 环境变量配置器
 * 根据 Nacos 版本（2.x 或 3.x）自动适配环境变量
 */
public class NacosEnvironmentConfigurer {

    private final NacosVersion version;
    private final GenericContainer<?> container;

    public NacosEnvironmentConfigurer(NacosVersion version, GenericContainer<?> container) {
        this.version = version != null ? version : NacosVersion.getDefault();
        this.container = container;
    }

    /**
     * 配置基础环境变量
     */
    public void configureBasicSettings(String username, String password,
                                       boolean authEnabled, int tokenExpiration,
                                       boolean consoleEnabled, String namespace) {
        // 认证配置（2.x 和 3.x 相同）
        withEnv("NACOS_AUTH_ENABLE", String.valueOf(authEnabled));
        withEnv("NACOS_AUTH_USERNAME", username);
        withEnv("NACOS_AUTH_PASSWORD", password);
        withEnv("NACOS_AUTH_TOKEN", "SecretKey012345678901234567890123456789012345678901234567890123456789");
        withEnv("NACOS_AUTH_TOKEN_EXPIRE_SECONDS", String.valueOf(tokenExpiration));

        // 控制台配置
        if (version.isV3()) {
            // Nacos 3.x 使用新的控制台配置
            withEnv("NACOS_CONSOLE_ENABLED", String.valueOf(consoleEnabled));
        } else {
            // Nacos 2.x 控制台配置
            withEnv("NACOS_CONSOLE_ENABLED", String.valueOf(consoleEnabled));
        }

        // 命名空间配置
        if (namespace != null && !namespace.isEmpty()) {
            withEnv("NACOS_NAMESPACE", namespace);
        }
    }

    /**
     * 配置数据库
     */
    public void configureDatabase(NacosDatabaseConfig databaseConfig) {
        if (databaseConfig == null || databaseConfig.isEmbedded()) {
            configureEmbeddedDatabase();
        } else if (databaseConfig.isMySQL()) {
            configureMySQL(databaseConfig);
        }
    }

    /**
     * 配置嵌入式数据库
     */
    private void configureEmbeddedDatabase() {
        if (version.isV3()) {
            // Nacos 3.x 使用 Derby 或 空配置表示嵌入式
            withEnv("spring.sql.init.platform", "");
        } else {
            // Nacos 2.x 使用 SPRING_DATASOURCE_PLATFORM
            withEnv("SPRING_DATASOURCE_PLATFORM", "embedded");
            withEnv("NACOS_AUTH_CACHE_ENABLE", "true");
        }
    }

    /**
     * 配置 MySQL 数据库
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
            withEnv("SPRING_DATASOURCE_PLATFORM", "mysql");
            withEnv("MYSQL_SERVICE_HOST", databaseConfig.getHost());
            withEnv("MYSQL_SERVICE_PORT", String.valueOf(databaseConfig.getPort()));
            withEnv("MYSQL_SERVICE_DB_NAME", databaseConfig.getDatabase());
            withEnv("MYSQL_SERVICE_USER", databaseConfig.getUsername());
            withEnv("MYSQL_SERVICE_PASSWORD", databaseConfig.getPassword());

            String url = databaseConfig.getUrl();
            if (url != null) {
                withEnv("SPRING_DATASOURCE_URL", url);
            }
        }
    }

    /**
     * 配置集群模式
     */
    public void configureClusterMode(boolean clusterMode, List<NacosClusterNode> clusterNodes, String clusterNodeId) {
        if (clusterMode) {
            if (version.isV3()) {
                // Nacos 3.x 使用 nacos.standalone=false 表示集群模式
                withEnv("nacos.standalone", "false");
            } else {
                // Nacos 2.x 使用 NACOS_MODE=cluster
                withEnv("NACOS_MODE", "cluster");
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
                    withEnv("NACOS_SERVERS", clusterConf.replace(",", "\n"));
                }
            }

            // 设置节点标识
            if (clusterNodeId != null) {
                if (version.isV3()) {
                    withEnv("nacos.server.ip", clusterNodeId);
                } else {
                    withEnv("NACOS_SERVER_IP", clusterNodeId);
                }
            }
        } else {
            // 单机模式
            if (version.isV3()) {
                withEnv("nacos.standalone", "true");
            } else {
                withEnv("NACOS_MODE", "standalone");
            }
        }
    }

    /**
     * 配置指标监控
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
     * 配置端口（Nacos 3.x 使用新的配置项）
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
     * 获取等待策略的检测路径
     */
    public String getWaitStrategyPath() {
        if (version.isV3()) {
            // Nacos 3.x 可能使用不同的上下文路径
            return "/nacos";
        }
        return "/nacos";
    }

    /**
     * 辅助方法：设置环境变量
     */
    private void withEnv(String key, String value) {
        if (container != null && value != null) {
            container.addEnv(key, value);
        }
    }

    /**
     * 获取当前版本
     */
    public NacosVersion getVersion() {
        return version;
    }

    /**
     * 判断是否为 Nacos 3.x
     */
    public boolean isV3() {
        return version.isV3();
    }

    /**
     * 判断是否为 Nacos 2.x
     */
    public boolean isV2() {
        return version.isV2();
    }
}
