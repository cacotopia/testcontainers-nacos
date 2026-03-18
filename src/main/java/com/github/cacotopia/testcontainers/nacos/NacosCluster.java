package com.github.cacotopia.testcontainers.nacos;

import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.mysql.MySQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nacos 集群管理类
 * 用于简化多节点 Nacos 集群的创建和管理
 */
public class NacosCluster implements Startable {

    private final int nodeCount;
    private final List<NacosContainer> nodes;
    private final Network network;
    private final NacosDatabaseConfig databaseConfig;
    private final String nacosImage;

    private NacosCluster(Builder builder) {
        this.nodeCount = builder.nodeCount;
        this.network = builder.network != null ? builder.network : Network.newNetwork();
        this.databaseConfig = builder.databaseConfig != null ? builder.databaseConfig : NacosDatabaseConfig.embedded();
        this.nacosImage = builder.nacosImage != null ? builder.nacosImage : "nacos/nacos-server:2.2.3";
        this.nodes = new ArrayList<>();
    }

    @Override
    public void start() {
        // 如果使用 MySQL 容器，先启动 MySQL
        if (databaseConfig.getType() == NacosDatabaseConfig.DatabaseType.MYSQL_CONTAINER) {
            MySQLContainer mysql = databaseConfig.getMysqlContainer();
            if (mysql != null && !mysql.isRunning()) {
                mysql.start();
            }
        }

        // 构建集群节点地址列表
        List<NacosClusterNode> clusterNodeList = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            String nodeName = "nacos" + (i + 1);
            // 在 Docker 网络中使用容器名作为主机名
            clusterNodeList.add(new NacosClusterNode(nodeName, nodeName, 8848));
        }

        // 创建并启动所有 Nacos 节点
        for (int i = 0; i < nodeCount; i++) {
            String nodeName = "nacos" + (i + 1);
            NacosContainer node = createNode(nodeName, i + 1, clusterNodeList);
            nodes.add(node);
            node.start();
        }
    }

    @Override
    public void stop() {
        // 停止所有节点
        for (NacosContainer node : nodes) {
            node.stop();
        }

        // 如果使用 MySQL 容器，停止 MySQL
        if (databaseConfig.getType() == NacosDatabaseConfig.DatabaseType.MYSQL_CONTAINER) {
            MySQLContainer mysql = databaseConfig.getMysqlContainer();
            if (mysql != null && mysql.isRunning()) {
                mysql.stop();
            }
        }

        // 关闭网络
        if (network != null) {
            network.close();
        }
    }

    @Override
    public void close() {
        stop();
    }

    /**
     * 创建单个节点
     */
    private NacosContainer createNode(String nodeName, int nodeIndex, List<NacosClusterNode> allNodes) {
        NacosContainer node = new NacosContainer(nacosImage)
            .withNetwork(network)
            .withNetworkAliases(nodeName)
            .withClusterMode(true)
            .withClusterNodeId(nodeName)
            .withClusterNodes(allNodes)
            .withDatabaseConfig(databaseConfig);

        // 设置节点特定的端口映射（避免冲突）
        // 第一个节点使用默认端口映射，其他节点使用动态端口
        if (nodeIndex > 1) {
            // 对于非第一个节点，不暴露特定端口，使用动态端口
            // 注意：Testcontainers 会自动分配可用端口
        }

        return node;
    }

    /**
     * 获取所有节点
     */
    public List<NacosContainer> getNodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * 获取主节点（第一个节点）
     */
    public NacosContainer getPrimaryNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    /**
     * 获取指定索引的节点
     */
    public NacosContainer getNode(int index) {
        if (index < 0 || index >= nodes.size()) {
            throw new IndexOutOfBoundsException("Node index out of range: " + index);
        }
        return nodes.get(index);
    }

    /**
     * 获取集群大小
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * 获取所有节点的服务 URL
     */
    public List<String> getServiceUrls() {
        return nodes.stream()
            .map(NacosContainer::getServiceUrl)
            .collect(Collectors.toList());
    }

    /**
     * 获取主节点服务 URL
     */
    public String getPrimaryServiceUrl() {
        NacosContainer primary = getPrimaryNode();
        return primary != null ? primary.getServiceUrl() : null;
    }

    /**
     * 等待集群所有节点就绪
     */
    public void waitForClusterReady() {
        for (NacosContainer node : nodes) {
            // 等待节点启动完成
            while (!node.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for cluster", e);
                }
            }
        }
    }

    /**
     * 创建 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类
     */
    public static class Builder {
        private int nodeCount = 3;
        private Network network;
        private NacosDatabaseConfig databaseConfig;
        private String nacosImage;

        public Builder withNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        public Builder withNetwork(Network network) {
            this.network = network;
            return this;
        }

        public Builder withDatabaseConfig(NacosDatabaseConfig databaseConfig) {
            this.databaseConfig = databaseConfig;
            return this;
        }

        public Builder withMySQLContainer(MySQLContainer mysqlContainer) {
            this.databaseConfig = NacosDatabaseConfig.mysqlContainer(mysqlContainer);
            return this;
        }

        public Builder withNacosImage(String nacosImage) {
            this.nacosImage = nacosImage;
            return this;
        }

        public NacosCluster build() {
            if (nodeCount < 1) {
                throw new IllegalArgumentException("Node count must be at least 1");
            }
            return new NacosCluster(this);
        }
    }
}
