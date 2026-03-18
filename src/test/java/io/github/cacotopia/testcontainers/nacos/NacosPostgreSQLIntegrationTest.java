package io.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nacos PostgreSQL 数据库集成测试
 */
@Testcontainers
class NacosPostgreSQLIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15")
        .withDatabaseName("nacos")
        .withUsername("nacos")
        .withPassword("nacos");

    @Container
    static NacosContainer nacos = new NacosContainer()
        .withPostgreSQLContainer(postgres);

    @Test
    @DisplayName("应该使用 PostgreSQL 数据库配置")
    void shouldUsePostgreSQLDatabase() {
        NacosDatabaseConfig dbConfig = nacos.getDatabaseConfig();

        assertThat(dbConfig).isNotNull();
        assertThat(dbConfig.isPostgreSQL()).isTrue();
        assertThat(dbConfig.isEmbedded()).isFalse();
        assertThat(dbConfig.isMySQL()).isFalse();
    }

    @Test
    @DisplayName("应该返回正确的数据库连接信息")
    void shouldReturnCorrectDatabaseConnectionInfo() {
        NacosDatabaseConfig dbConfig = nacos.getDatabaseConfig();

        assertThat(dbConfig.getHost()).isNotNull();
        assertThat(dbConfig.getPort()).isEqualTo(postgres.getMappedPort(5432));
        assertThat(dbConfig.getDatabase()).isEqualTo("nacos");
        assertThat(dbConfig.getUsername()).isEqualTo("nacos");
        assertThat(dbConfig.getPassword()).isEqualTo("nacos");
    }

    @Test
    @DisplayName("应该生成正确的 PostgreSQL JDBC URL")
    void shouldGenerateCorrectJdbcUrl() {
        NacosDatabaseConfig dbConfig = nacos.getDatabaseConfig();
        String url = dbConfig.getUrl();

        assertThat(url)
            .isNotNull()
            .startsWith("jdbc:postgresql://")
            .contains("nacos")
            .contains("currentSchema=nacos");
    }

    @Test
    @DisplayName("容器应该成功启动并返回服务URL")
    void shouldStartWithPostgreSQLAndReturnServiceUrl() {
        String serviceUrl = nacos.getServiceUrl();

        assertThat(serviceUrl)
            .isNotNull()
            .startsWith("http://");
    }

    @Test
    @DisplayName("应该能获取 PostgreSQL 容器实例")
    void shouldGetPostgreSQLContainer() {
        NacosDatabaseConfig dbConfig = nacos.getDatabaseConfig();

        assertThat(dbConfig.getPostgresqlContainer()).isNotNull();
        assertThat(dbConfig.getPostgresqlContainer()).isEqualTo(postgres);
    }
}
