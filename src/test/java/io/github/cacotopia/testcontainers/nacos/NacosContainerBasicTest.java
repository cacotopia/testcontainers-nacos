package io.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NacosContainer 基础功能测试
 */
@Testcontainers
class NacosContainerBasicTest {

    @Container
    static NacosContainer nacos = new NacosContainer();

    @Test
    @DisplayName("容器应该成功启动并返回服务URL")
    void shouldStartAndReturnServiceUrl() {
        String serviceUrl = nacos.getServiceUrl();

        assertThat(serviceUrl)
            .isNotNull()
            .startsWith("http://")
            .contains("/nacos");
    }

    @Test
    @DisplayName("应该返回正确的默认凭证")
    void shouldReturnDefaultCredentials() {
        assertThat(nacos.getUsername()).isEqualTo("nacos");
        assertThat(nacos.getPassword()).isEqualTo("nacos");
    }

    @Test
    @DisplayName("应该返回gRPC地址")
    void shouldReturnGrpcAddress() {
        String grpcAddress = nacos.getGrpcAddress();

        assertThat(grpcAddress)
            .isNotNull()
            .contains(":");
    }

    @Test
    @DisplayName("应该检测到默认版本为2.x")
    void shouldDetectDefaultVersionAsV2() {
        NacosVersion version = nacos.getNacosVersion();

        assertThat(version).isNotNull();
        assertThat(nacos.isV2()).isTrue();
        assertThat(nacos.isV3()).isFalse();
    }

    @Test
    @DisplayName("应该返回Docker镜像名称")
    void shouldReturnDockerImageName() {
        String imageName = nacos.getDockerImageName();

        assertThat(imageName)
            .isNotNull()
            .contains("nacos/nacos-server");
    }

    @Test
    @DisplayName("应该处于非集群模式")
    void shouldNotBeInClusterMode() {
        assertThat(nacos.isClusterMode()).isFalse();
    }

    @Test
    @DisplayName("应该返回默认命名空间")
    void shouldReturnDefaultNamespace() {
        assertThat(nacos.getNamespace()).isEmpty();
    }

    @Test
    @DisplayName("应该返回默认分组")
    void shouldReturnDefaultGroup() {
        assertThat(nacos.getDefaultGroup()).isEqualTo("DEFAULT_GROUP");
    }
}
