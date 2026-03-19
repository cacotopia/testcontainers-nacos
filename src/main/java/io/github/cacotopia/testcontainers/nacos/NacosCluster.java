package io.github.cacotopia.testcontainers.nacos;

import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nacos cluster management class.
 * Used to simplify the creation and management of multi-node Nacos clusters.
 */
public class NacosCluster implements Startable {

    /**
     * Number of nodes in the cluster
     */
    private final int nodeCount;

    /**
     * List of Nacos containers in the cluster
     */
    private final List<NacosContainer> nodes;

    /**
     * Network for inter-container communication
     */
    private final Network network;

    /**
     * Database configuration for the cluster
     */
    private final NacosDatabaseConfig databaseConfig;

    /**
     * Nacos Docker image to use
     */
    private final String nacosImage;

    /**
     * Creates a new NacosCluster using the provided Builder.
     *
     * @param builder The builder to use
     */
    private NacosCluster(Builder builder) {
        this.nodeCount = builder.nodeCount;
        this.network = builder.network != null ? builder.network : Network.newNetwork();
        this.databaseConfig = builder.databaseConfig != null ? builder.databaseConfig : NacosDatabaseConfig.embedded();
        this.nacosImage = builder.nacosImage != null ? builder.nacosImage : "nacos/nacos-server:2.2.3";
        this.nodes = new ArrayList<>();
    }

    /**
     * Starts the Nacos cluster.
     * First starts the MySQL container if needed, then creates and starts all Nacos nodes.
     */
    @Override
    public void start() {

        if (databaseConfig.getType() == NacosDatabaseConfig.DatabaseType.MYSQL_CONTAINER) {
            // 如果使用 MySQL 容器，先启动 MySQL
            MySQLContainer mysql = databaseConfig.getMysqlContainer();
            if (mysql != null && !mysql.isRunning()) {
                mysql.start();
            }
        } else if (databaseConfig.getType() == NacosDatabaseConfig.DatabaseType.POSTGRESQL_CONTAINER) {
            // 如果使用 PostgreSQL 容器，先启动 PostgreSQL
            PostgreSQLContainer postgresql = databaseConfig.getPostgresqlContainer();
            if (postgresql != null && !postgresql.isRunning()) {
                postgresql.start();
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

    /**
     * Stops the Nacos cluster.
     * Stops all Nacos nodes, then stops the MySQL container if used, and closes the network.
     */
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

    /**
     * Closes the Nacos cluster by stopping all resources.
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Creates a single Nacos node for the cluster.
     *
     * @param nodeName  The name of the node
     * @param nodeIndex The index of the node
     * @param allNodes  List of all nodes in the cluster
     * @return A NacosContainer instance
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
     * Gets all nodes in the cluster.
     *
     * @return List of NacosContainer instances
     */
    public List<NacosContainer> getNodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * Gets the primary node (first node) in the cluster.
     *
     * @return The primary NacosContainer instance, or null if no nodes
     */
    public NacosContainer getPrimaryNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    /**
     * Gets the node at the specified index.
     *
     * @param index The index of the node
     * @return The NacosContainer instance at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public NacosContainer getNode(int index) {
        if (index < 0 || index >= nodes.size()) {
            throw new IndexOutOfBoundsException("Node index out of range: " + index);
        }
        return nodes.get(index);
    }

    /**
     * Gets the number of nodes in the cluster.
     *
     * @return The number of nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * Gets service URLs for all nodes in the cluster.
     *
     * @return List of service URLs
     */
    public List<String> getServiceUrls() {
        return nodes.stream()
            .map(NacosContainer::getServiceUrl)
            .collect(Collectors.toList());
    }

    /**
     * Gets the service URL of the primary node.
     *
     * @return The service URL of the primary node, or null if no primary node
     */
    public String getPrimaryServiceUrl() {
        NacosContainer primary = getPrimaryNode();
        return primary != null ? primary.getServiceUrl() : null;
    }

    /**
     * Waits for all nodes in the cluster to become ready.
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
     * Creates a new Builder for NacosCluster.
     *
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for NacosCluster.
     */
    public static class Builder {
        /**
         * Number of nodes in the cluster (default: 3)
         */
        private int nodeCount = 3;

        /**
         * Network for inter-container communication
         */
        private Network network;

        /**
         * Database configuration for the cluster
         */
        private NacosDatabaseConfig databaseConfig;

        /**
         * Nacos Docker image to use
         */
        private String nacosImage;

        /**
         * Sets the number of nodes in the cluster.
         *
         * @param nodeCount The number of nodes
         * @return This Builder instance
         */
        public Builder withNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        /**
         * Sets the network for inter-container communication.
         *
         * @param network The network to use
         * @return This Builder instance
         */
        public Builder withNetwork(Network network) {
            this.network = network;
            return this;
        }

        /**
         * Sets the database configuration for the cluster.
         *
         * @param databaseConfig The database configuration
         * @return This Builder instance
         */
        public Builder withDatabaseConfig(NacosDatabaseConfig databaseConfig) {
            this.databaseConfig = databaseConfig;
            return this;
        }

        /**
         * Configures the cluster to use a MySQL container for storage.
         *
         * @param mysqlContainer The MySQL container to use
         * @return This Builder instance
         */
        public Builder withMySQLContainer(MySQLContainer mysqlContainer) {
            this.databaseConfig = NacosDatabaseConfig.mysqlContainer(mysqlContainer);
            return this;
        }

        /**
         * Sets the Nacos Docker image to use.
         *
         * @param nacosImage The Docker image name
         * @return This Builder instance
         */
        public Builder withNacosImage(String nacosImage) {
            this.nacosImage = nacosImage;
            return this;
        }

        /**
         * Builds the NacosCluster instance.
         *
         * @return A new NacosCluster instance
         * @throws IllegalArgumentException if node count is less than 1
         */
        public NacosCluster build() {
            if (nodeCount < 1) {
                throw new IllegalArgumentException("Node count must be at least 1");
            }
            return new NacosCluster(this);
        }
    }
}
