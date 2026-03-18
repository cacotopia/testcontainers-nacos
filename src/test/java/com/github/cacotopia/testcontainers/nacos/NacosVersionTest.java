package com.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NacosVersion 版本检测测试
 */
class NacosVersionTest {

    @Test
    @DisplayName("应该从镜像名称解析 Nacos 2.2 版本")
    void shouldParseVersion22FromImageName() {
        NacosVersion version = NacosVersion.fromImageName("nacos/nacos-server:2.2.3");
        
        assertThat(version).isEqualTo(NacosVersion.V2_2);
        assertThat(version.isV2()).isTrue();
        assertThat(version.isV3()).isFalse();
    }

    @Test
    @DisplayName("应该从镜像名称解析 Nacos 3.0 版本")
    void shouldParseVersion30FromImageName() {
        NacosVersion version = NacosVersion.fromImageName("nacos/nacos-server:3.0.0");
        
        assertThat(version).isEqualTo(NacosVersion.V3_0);
        assertThat(version.isV2()).isFalse();
        assertThat(version.isV3()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "nacos/nacos-server:2.0.0, V2_0",
        "nacos/nacos-server:2.1.0, V2_1",
        "nacos/nacos-server:2.2.0, V2_2",
        "nacos/nacos-server:2.3.0, V2_3",
        "nacos/nacos-server:2.4.0, V2_4",
        "nacos/nacos-server:2.5.0, V2_5",
        "nacos/nacos-server:3.0.0, V3_0",
        "nacos/nacos-server:3.1.0, V3_1"
    })
    @DisplayName("应该正确解析各种版本")
    void shouldParseVariousVersions(String imageName, String expectedVersion) {
        NacosVersion version = NacosVersion.fromImageName(imageName);
        
        assertThat(version).isEqualTo(NacosVersion.valueOf(expectedVersion));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "nacos/nacos-server:2.2.3-slim",
        "nacos/nacos-server:2.2.3-alpine",
        "registry.cn-hangzhou.aliyuncs.com/nacos/nacos-server:2.2.3"
    })
    @DisplayName("应该处理带后缀的镜像名称")
    void shouldHandleImageNamesWithSuffixes(String imageName) {
        NacosVersion version = NacosVersion.fromImageName(imageName);
        
        assertThat(version).isEqualTo(NacosVersion.V2_2);
    }

    @Test
    @DisplayName("空镜像名称应该返回默认版本")
    void shouldReturnDefaultVersionForEmptyImageName() {
        NacosVersion version = NacosVersion.fromImageName("");
        
        assertThat(version).isEqualTo(NacosVersion.getDefault());
    }

    @Test
    @DisplayName("null 镜像名称应该返回默认版本")
    void shouldReturnDefaultVersionForNullImageName() {
        NacosVersion version = NacosVersion.fromImageName(null);
        
        assertThat(version).isEqualTo(NacosVersion.getDefault());
    }

    @Test
    @DisplayName("未知版本应该返回 UNKNOWN")
    void shouldReturnUnknownForUnrecognizedVersion() {
        NacosVersion version = NacosVersion.fromImageName("nacos/nacos-server:1.4.2");
        
        assertThat(version).isEqualTo(NacosVersion.UNKNOWN);
    }

    @Test
    @DisplayName("应该返回正确的版本前缀")
    void shouldReturnCorrectVersionPrefix() {
        assertThat(NacosVersion.V2_2.getVersionPrefix()).isEqualTo("2.2");
        assertThat(NacosVersion.V3_0.getVersionPrefix()).isEqualTo("3.0");
    }

    @Test
    @DisplayName("应该返回正确的显示名称")
    void shouldReturnCorrectDisplayName() {
        assertThat(NacosVersion.V2_2.getDisplayName()).isEqualTo("Nacos 2.2");
        assertThat(NacosVersion.V3_0.getDisplayName()).isEqualTo("Nacos 3.0");
    }

    @Test
    @DisplayName("应该返回默认镜像")
    void shouldReturnDefaultImage() {
        String defaultImage = NacosVersion.getDefaultImage();
        
        assertThat(defaultImage)
            .isNotNull()
            .contains("nacos/nacos-server");
    }

    @Test
    @DisplayName("所有2.x版本应该返回isV2为true")
    void allV2VersionsShouldReturnIsV2True() {
        assertThat(NacosVersion.V2_0.isV2()).isTrue();
        assertThat(NacosVersion.V2_1.isV2()).isTrue();
        assertThat(NacosVersion.V2_2.isV2()).isTrue();
        assertThat(NacosVersion.V2_3.isV2()).isTrue();
        assertThat(NacosVersion.V2_4.isV2()).isTrue();
        assertThat(NacosVersion.V2_5.isV2()).isTrue();
    }

    @Test
    @DisplayName("所有3.x版本应该返回isV3为true")
    void allV3VersionsShouldReturnIsV3True() {
        assertThat(NacosVersion.V3_0.isV3()).isTrue();
        assertThat(NacosVersion.V3_1.isV3()).isTrue();
    }
}
