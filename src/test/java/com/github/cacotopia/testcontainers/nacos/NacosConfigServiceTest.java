package com.github.cacotopia.testcontainers.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nacos 配置服务测试
 */
@Testcontainers
class NacosConfigServiceTest {

    @Container
    static NacosContainer nacos;

    static {
        nacos = new NacosContainer();
        nacos.withInitialConfig("test.properties", "key1=value1\nkey2=value2");
        nacos.withInitialConfig("app.yaml", "TEST_GROUP", "server:\n  port: 8080");
    }

    @Test
    @DisplayName("应该获取 ConfigService 客户端")
    void shouldGetConfigService() throws NacosException {
        ConfigService configService = nacos.getConfigService();

        assertThat(configService).isNotNull();
    }

    @Test
    @DisplayName("应该能够发布和获取配置")
    void shouldPublishAndGetConfig() throws NacosException {
        ConfigService configService = nacos.getConfigService();
        String dataId = "dynamic-config";
        String group = "DEFAULT_GROUP";
        String content = "dynamic.value=test123";

        // 发布配置
        boolean success = configService.publishConfig(dataId, group, content);
        assertThat(success).isTrue();

        // 获取配置
        String retrievedContent = configService.getConfig(dataId, group, 5000);
        assertThat(retrievedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("应该能够删除配置")
    void shouldRemoveConfig() throws NacosException {
        ConfigService configService = nacos.getConfigService();
        String dataId = "temp-config";
        String group = "DEFAULT_GROUP";

        // 先发布配置
        configService.publishConfig(dataId, group, "temp=value");

        // 删除配置
        boolean removed = configService.removeConfig(dataId, group);
        assertThat(removed).isTrue();

        // 验证配置已删除
        String content = configService.getConfig(dataId, group, 5000);
        assertThat(content).isNull();
    }

    @Test
    @DisplayName("应该返回初始配置列表")
    void shouldReturnInitialConfigs() {
        var configs = nacos.getInitialConfigs();

        assertThat(configs).hasSize(2);
        assertThat(configs.get(0).getDataId()).isEqualTo("test.properties");
        assertThat(configs.get(1).getDataId()).isEqualTo("app.yaml");
        assertThat(configs.get(1).getGroup()).isEqualTo("TEST_GROUP");
    }

    @Test
    @DisplayName("应该获取客户端工厂")
    void shouldGetClientFactory() {
        NacosClientFactory factory = nacos.getClientFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getServerAddr()).isEqualTo(nacos.getServiceUrl());
        assertThat(factory.getUsername()).isEqualTo(nacos.getUsername());
        assertThat(factory.getPassword()).isEqualTo(nacos.getPassword());
    }
}
