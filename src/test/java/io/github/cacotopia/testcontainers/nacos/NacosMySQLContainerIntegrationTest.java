package io.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nacos MySQL 数据库集成测试
 */
@Testcontainers
class NacosMySQLContainerIntegrationTest {

    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0.45")
        .withDatabaseName("nacos")
        .withUsername("root")
        .withPassword("123456");

    @Container
    static NacosContainer nacos = new NacosContainer()
        .withDebugEnabled(true)
        .withUsername("admin")
        .withPassword("admin123")
        .withMySQLContainer(mysql);

    @Test
    @DisplayName("应该使用 MySQL 数据库配置")
    void shouldUseMySQLDatabase() {
        NacosDatabaseConfig dbConfig = nacos.getDatabaseConfig();

        assertThat(dbConfig).isNotNull();
        assertThat(dbConfig.isMySQL()).isTrue();
        assertThat(dbConfig.isEmbedded()).isFalse();
        assertThat(dbConfig.isPostgreSQL()).isFalse();
    }

    @Test
    @DisplayName("应该返回正确的数据库连接信息")
    void shouldReturnCorrectDatabaseConnectionInfo() {
        NacosDatabaseConfig dbConfig = nacos.getDatabaseConfig();

        assertThat(dbConfig.getHost()).isNotNull();
        assertThat(dbConfig.getPort()).isGreaterThan(0);
        assertThat(dbConfig.getDatabase()).isEqualTo("nacos");
        assertThat(dbConfig.getUsername()).isEqualTo("root");
        assertThat(dbConfig.getPassword()).isEqualTo("123456");
    }

    @Test
    @DisplayName("应该生成正确的 JDBC URL")
    void shouldGenerateCorrectJdbcUrl() {
        NacosDatabaseConfig dbConfig = nacos.getDatabaseConfig();
        String url = dbConfig.getUrl();

        assertThat(url)
            .isNotNull()
            .startsWith("jdbc:mysql://")
            .contains("nacos")
            .contains("useSSL=false");
    }

    @Test
    @DisplayName("容器应该成功启动并返回服务URL")
    void shouldReturnCorrectNacosUserInfo() {
        assertThat(nacos.getUsername()).isEqualTo("admin");
        assertThat(nacos.getPassword()).isEqualTo("admin123");
    }

    @Test
    @DisplayName("容器应该成功启动并返回服务URL")
    void shouldStartWithMySQLAndReturnServiceUrl() {
        String serviceUrl = nacos.getServiceUrl();

        assertThat(serviceUrl)
            .isNotNull()
            .startsWith("http://");
    }
}
