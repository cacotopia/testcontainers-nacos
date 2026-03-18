package com.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NacosCluster 多节点集群测试
 */
class NacosClusterTest {

    static NacosCluster cluster;

    @BeforeAll
    static void setUp() {
        cluster = NacosCluster.builder()
            .withNodeCount(3)
            .build();
        cluster.start();
    }

    @AfterAll
    static void tearDown() {
        if (cluster != null) {
            cluster.stop();
        }
    }

    @Test
    @DisplayName("应该创建指定数量的节点")
    void shouldCreateSpecifiedNumberOfNodes() {
        List<NacosContainer> nodes = cluster.getNodes();

        assertThat(nodes).hasSize(3);
    }

    @Test
    @DisplayName("应该获取主节点")
    void shouldGetPrimaryNode() {
        NacosContainer primaryNode = cluster.getPrimaryNode();

        assertThat(primaryNode).isNotNull();
        assertThat(primaryNode.getServiceUrl()).startsWith("http://");
    }

    @Test
    @DisplayName("应该通过索引获取节点")
    void shouldGetNodeByIndex() {
        NacosContainer node0 = cluster.getNode(0);
        NacosContainer node1 = cluster.getNode(1);
        NacosContainer node2 = cluster.getNode(2);

        assertThat(node0).isNotNull();
        assertThat(node1).isNotNull();
        assertThat(node2).isNotNull();
        assertThat(node0).isEqualTo(cluster.getPrimaryNode());
    }

    @Test
    @DisplayName("应该返回所有节点的服务URL")
    void shouldReturnAllNodeServiceUrls() {
        List<String> urls = cluster.getServiceUrls();

        assertThat(urls).hasSize(3);
        assertThat(urls.get(0)).startsWith("http://");
        assertThat(urls.get(1)).startsWith("http://");
        assertThat(urls.get(2)).startsWith("http://");
    }

    @Test
    @DisplayName("应该返回主节点服务URL")
    void shouldReturnPrimaryServiceUrl() {
        String primaryUrl = cluster.getPrimaryServiceUrl();

        assertThat(primaryUrl).isNotNull().startsWith("http://");
    }

    @Test
    @DisplayName("所有节点应该处于集群模式")
    void allNodesShouldBeInClusterMode() {
        for (NacosContainer node : cluster.getNodes()) {
            assertThat(node.isClusterMode()).isTrue();
        }
    }

    @Test
    @DisplayName("所有节点应该返回正确的节点数量")
    void shouldReturnCorrectNodeCount() {
        assertThat(cluster.getNodeCount()).isEqualTo(3);
    }
}
