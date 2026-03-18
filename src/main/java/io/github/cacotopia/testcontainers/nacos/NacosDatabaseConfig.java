package io.github.cacotopia.testcontainers.nacos;


import org.testcontainers.mysql.MySQLContainer;

/**
 * Nacos 数据库配置类
 * 支持嵌入式数据库、外置 MySQL 容器或外部 MySQL 实例
 */
public class NacosDatabaseConfig {

    public enum DatabaseType {
        EMBEDDED,      // 嵌入式数据库（默认）
        MYSQL_CONTAINER, // Testcontainers MySQL 容器
        EXTERNAL_MYSQL  // 外部 MySQL 实例
    }

    private DatabaseType type = DatabaseType.EMBEDDED;

    // MySQL 连接配置
    private String host;
    private Integer port;
    private String database = "nacos";
    private String username = "nacos";
    private String password = "nacos";
    private String url;

    // Testcontainers MySQL 容器引用
    private MySQLContainer mysqlContainer;

    // 私有构造方法，使用工厂方法创建
    private NacosDatabaseConfig() {
    }

    /**
     * 创建嵌入式数据库配置（默认）
     */
    public static NacosDatabaseConfig embedded() {
        NacosDatabaseConfig config = new NacosDatabaseConfig();
        config.type = DatabaseType.EMBEDDED;
        return config;
    }

    /**
     * 创建外部 MySQL 配置
     */
    public static NacosDatabaseConfig externalMySQL(String host, int port, String database, String username, String password) {
        NacosDatabaseConfig config = new NacosDatabaseConfig();
        config.type = DatabaseType.EXTERNAL_MYSQL;
        config.host = host;
        config.port = port;
        config.database = database;
        config.username = username;
        config.password = password;
        return config;
    }

    /**
     * 使用 Testcontainers MySQL 容器
     */
    public static NacosDatabaseConfig mysqlContainer(MySQLContainer mysqlContainer) {
        NacosDatabaseConfig config = new NacosDatabaseConfig();
        config.type = DatabaseType.MYSQL_CONTAINER;
        config.mysqlContainer = mysqlContainer;
        config.host = mysqlContainer.getHost();
        config.port = mysqlContainer.getMappedPort(3306);
        config.database = mysqlContainer.getDatabaseName();
        config.username = mysqlContainer.getUsername();
        config.password = mysqlContainer.getPassword();
        return config;
    }

    /**
     * 使用 Testcontainers MySQL 容器（简化版）
     */
    public static NacosDatabaseConfig mysqlContainer(String mysqlImage, String database, String username, String password) {
        MySQLContainer mysql = new MySQLContainer(mysqlImage)
            .withDatabaseName(database)
            .withUsername(username)
            .withPassword(password);
        return mysqlContainer(mysql);
    }

    public DatabaseType getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        if (url != null) {
            return url;
        }
        if (type == DatabaseType.EMBEDDED) {
            return null;
        }
        return String.format("jdbc:mysql://%s:%d/%s?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai",
            host, port, database);
    }

    public MySQLContainer getMysqlContainer() {
        return mysqlContainer;
    }

    public boolean isEmbedded() {
        return type == DatabaseType.EMBEDDED;
    }

    public boolean isMySQL() {
        return type == DatabaseType.MYSQL_CONTAINER || type == DatabaseType.EXTERNAL_MYSQL;
    }

    @Override
    public String toString() {
        return "NacosDatabaseConfig{" +
            "type=" + type +
            ", host='" + host + '\'' +
            ", port=" + port +
            ", database='" + database + '\'' +
            ", username='" + username + '\'' +
            '}';
    }
}
