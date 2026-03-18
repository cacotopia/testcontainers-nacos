package io.github.cacotopia.testcontainers.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import com.alibaba.nacos.api.naming.NamingService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nacos 命名服务测试
 */
@Testcontainers
class NacosNamingServiceTest {

    @Container
    static NacosContainer nacos;

    static {
        NacosServiceInstance orderService = new NacosServiceInstance("order-service", "192.168.1.101", 8081);
        orderService.setMetadata(java.util.Map.of("version", "v1.0", "region", "beijing"));

        nacos = new NacosContainer();
        nacos.withInitialService("test-service", "192.168.1.100", 8080)
            .withInitialService(orderService);
    }

    @Test
    @DisplayName("应该获取 NamingService 客户端")
    void shouldGetNamingService() throws NacosException {
        com.alibaba.nacos.api.naming.NamingService namingService = nacos.getNamingService();

        assertThat(namingService).isNotNull();
    }

    @Test
    @DisplayName("应该能够注册和发现服务实例")
    void shouldRegisterAndDiscoverInstance() throws NacosException {
        NamingService namingService = nacos.getNamingService();
        String serviceName = "user-service";

        // 创建实例
        Instance instance = new Instance();
        instance.setIp("192.168.1.200");
        instance.setPort(8082);
        instance.setHealthy(true);
        instance.setMetadata(java.util.Map.of("version", "v2.0"));

        // 注册实例
        namingService.registerInstance(serviceName, instance);

        // 发现实例
        List<Instance> instances = namingService.getAllInstances(serviceName);
        assertThat(instances).hasSize(1);
        assertThat(instances.get(0).getIp()).isEqualTo("192.168.1.200");
        assertThat(instances.get(0).getPort()).isEqualTo(8082);
    }

    @Test
    @DisplayName("应该能够注销服务实例")
    void shouldDeregisterInstance() throws NacosException, InterruptedException {
        com.alibaba.nacos.api.naming.NamingService namingService = nacos.getNamingService();
        String serviceName = "temp-service";

        // 注册实例
        Instance instance = new Instance();
        instance.setIp("192.168.1.201");
        instance.setPort(8083);
        namingService.registerInstance(serviceName, instance);

        // 验证实例存在
        List<Instance> instances = namingService.getAllInstances(serviceName);
        assertThat(instances).hasSize(1);

        // 注销实例
        namingService.deregisterInstance(serviceName, "192.168.1.201", 8083);

        // 验证实例已注销（可能需要等待）
        Thread.sleep(1000);
        instances = namingService.getAllInstances(serviceName);
        assertThat(instances).isEmpty();
    }

    @Test
    @DisplayName("应该返回初始服务列表")
    void shouldReturnInitialServices() {
        var services = nacos.getInitialServices();

        assertThat(services).hasSize(2);
        assertThat(services.get(0).getServiceName()).isEqualTo("test-service");
        assertThat(services.get(1).getServiceName()).isEqualTo("order-service");
        assertThat(services.get(1).getMetadata()).containsEntry("version", "v1.0");
        assertThat(services.get(1).getMetadata()).containsEntry("region", "beijing");
    }

    @Test
    @DisplayName("应该获取所有健康实例")
    void shouldGetHealthyInstances() throws NacosException {
        com.alibaba.nacos.api.naming.NamingService namingService = nacos.getNamingService();
        String serviceName = "health-test-service";

        // 注册健康实例
        Instance healthyInstance = new Instance();
        healthyInstance.setIp("192.168.1.210");
        healthyInstance.setPort(8084);
        healthyInstance.setHealthy(true);
        namingService.registerInstance(serviceName, healthyInstance);

        // 获取健康实例
        List<Instance> healthyInstances = namingService.selectInstances(serviceName, true);
        assertThat(healthyInstances).isNotEmpty();
        assertThat(healthyInstances.get(0).isHealthy()).isTrue();
    }
}
