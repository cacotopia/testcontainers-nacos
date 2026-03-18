package com.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nacos 集群模式测试
 */
@Testcontainers
class NacosClusterModeTest {

    @Container
    static NacosContainer nacos;

    static {
        nacos = new NacosContainer();
        nacos.withClusterMode(true);
        nacos.withClusterNodes(
            new NacosClusterNode("nacos1", "192.168.1.1", 8848),
            new NacosClusterNode("nacos2", "192.168.1.2", 8848),
            new NacosClusterNode("nacos3", "192.168.1.3", 8848)
        );
    }

    @Test
    @DisplayName("应该启用集群模式")
    void shouldEnableClusterMode() {
        assertThat(nacos.isClusterMode()).isTrue();
    }

    @Test
    @DisplayName("应该返回配置的集群节点")
    void shouldReturnConfiguredClusterNodes() {
        List<NacosClusterNode> nodes = nacos.getClusterNodes();

        assertThat(nodes).hasSize(3);
        assertThat(nodes.get(0).getNodeId()).isEqualTo("nacos1");
        assertThat(nodes.get(1).getNodeId()).isEqualTo("nacos2");
        assertThat(nodes.get(2).getNodeId()).isEqualTo("nacos3");
    }

    @Test
    @DisplayName("应该返回正确的集群地址格式")
    void shouldReturnCorrectClusterAddressFormat() {
        List<NacosClusterNode> nodes = nacos.getClusterNodes();

        assertThat(nodes.get(0).getClusterAddress()).isEqualTo("192.168.1.1:8848");
        assertThat(nodes.get(1).getClusterAddress()).isEqualTo("192.168.1.2:8848");
        assertThat(nodes.get(2).getClusterAddress()).isEqualTo("192.168.1.3:8848");
    }

    @Test
    @DisplayName("容器应该成功启动")
    void shouldStartInClusterMode() {
        String serviceUrl = nacos.getServiceUrl();

        assertThat(serviceUrl)
            .isNotNull()
            .startsWith("http://");
    }
}
