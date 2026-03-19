# Testcontainers Nacos

A [Testcontainers](https://www.testcontainers.org/) implementation for [Nacos](https://nacos.io/) - an easy-to-use
dynamic service discovery, configuration and service management platform.

[![GitHub Release](https://img.shields.io/github/v/release/cacotopia/testcontainers-nacos?label=Release)](https://github.com/cacotopia/testcontainers-nacos/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.cacotopia/testcontainers-nacos.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.cacotopia/testcontainers-nacos)
![GitHub Release Date](https://img.shields.io/github/release-date-pre/cacotopia/testcontainers-nacos)
![Github Last Commit](https://img.shields.io/github/last-commit/cacotopia/testcontainers-nacos)
![License](https://img.shields.io/github/license/cacotopia/testcontainers-nacos?label=License)

[![Nacos Version](https://img.shields.io/badge/Nacos-2.x%20%7C%203.x-blue)](https://nacos.io)
![Java Version](https://img.shields.io/badge/Java-11-f89820)
[![GitHub Stars](https://img.shields.io/github/stars/cacotopia/testcontainers-nacos)](https://github.com/cacotopia/testcontainers-nacos/stargazers)
[![CI build](https://github.com/cacotopia/testcontainers-nacos/actions/workflows/maven.yml/badge.svg)](https://github.com/cacotopia/testcontainers-nacos/actions/workflows/maven.yml)

## Features

- **Support for Nacos 2.x and 3.x** (automatic version detection and configuration adaptation)
- Standalone and cluster mode
- **External MySQL database support** (Testcontainers MySQL or external instance)
- **External PostgreSQL database support** (Testcontainers PostgreSQL or external instance)
- **Automatic database initialization** for MySQL and PostgreSQL
- **Multi-node cluster management** with `NacosCluster`
- Configurable authentication credentials
- Exposed ports: HTTP (8848), gRPC (9848), gRPC management (9849)
- Built-in wait strategy for container readiness
- Compatible with Spring Cloud Nacos
- **JUnit 5 extension** for automatic container management
- **Fault simulation** for testing fault tolerance scenarios
- **Enhanced configuration management** with batch operations and change listening
- **Enhanced service discovery** with health status management and metadata updates
- **Test data generation** for simplified test data preparation

## Requirements

- Java 11 or higher
- Docker environment

## Installation

The release versions of this project are available
at [Maven Central](https://central.sonatype.com/artifact/io.github.cacotopia/testcontainers-nacos).

### Maven

```xml

<dependency>
    <groupId>io.github.cacotopia</groupId>
    <artifactId>testcontainers-nacos</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

### Gradle

```groovy
testImplementation 'io.github.cacotopia:testcontainers-nacos:VERSION'
```

For a version overview, see [versions.md](versions.md).

## Usage

### Basic Usage

Simply spin up a default Nacos instance:

```java
@Container
NacosContainer nacos = new NacosContainer();
```

### Using JUnit 5 Extension

Leverage the JUnit 5 extension for automatic container management:

```java
@ExtendWith(NacosTestExtension.class)
public class NacosTest {
    
    @Test
    public void testNacosFunctionality(NacosContainer container) {
        // Container is automatically started and injected
        String serviceUrl = container.getServiceUrl();
        // Test code...
    }
}
```

### Nacos Version Selection

#### Using Nacos 2.x (Default)

```java
// Use default version (2.2.3)
@Container
NacosContainer nacos = new NacosContainer();

// Use specific 2.x version
@Container
NacosContainer nacos = new NacosContainer("nacos/nacos-server:2.5.0");

// Use factory method
@Container
NacosContainer nacos = NacosContainer.v2();  // Latest 2.x
@Container
NacosContainer nacos = NacosContainer.v2("2.3.0");  // Specific version
```

#### Using Nacos 3.x

```java
// Use Nacos 3.0.0
@Container
NacosContainer nacos = NacosContainer.v3();

// Use specific 3.x version
@Container
NacosContainer nacos = NacosContainer.v3("3.0.1");

// Or use image name directly
@Container
NacosContainer nacos = new NacosContainer("nacos/nacos-server:3.0.0");
```

The container automatically detects the Nacos version and applies the appropriate configuration (environment variables,
database settings, cluster mode, etc.).

### Configuration Options

#### Custom Credentials

Override the default credentials (`nacos`/`nacos`):

```java

@Container
NacosContainer nacos = new NacosContainer()
    .withUsername("admin")
    .withPassword("secret");
```

#### External MySQL Database

Use an external MySQL database instead of the embedded one:

```java
@Container
NacosContainer nacos = new NacosContainer()
    .withExternalMySQL("mysql-host", 3306, "nacos_db", "nacos", "nacos_password");
```

Or use a Testcontainers MySQL container:

```java
@Container
MySQLContainer mysql = new MySQLContainer<>("mysql:8.0.45")
    .withDatabaseName("nacos")
    .withUsername("nacos")
    .withPassword("nacos");

@Container
NacosContainer nacos = new NacosContainer()
    .withMySQLContainer(mysql);
```

**Note:** The database will be automatically initialized with the required Nacos schema.

#### External PostgreSQL Database

Use an external PostgreSQL database instead of the embedded one:

```java
@Container
NacosContainer nacos = new NacosContainer()
    .withExternalPostgreSQL("postgres-host", 5432, "nacos_db", "nacos", "nacos_password");
```

Or use a Testcontainers PostgreSQL container:

```java
@Container
PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:15")
    .withDatabaseName("nacos")
    .withUsername("nacos")
    .withPassword("nacos");

@Container
NacosContainer nacos = new NacosContainer()
    .withPostgreSQLContainer(postgres);
```

**Note:** The database will be automatically initialized with the required Nacos schema.

#### Cluster Mode (Single Node)

Enable cluster mode on a single node (default: standalone):

```java

@Container
NacosContainer nacos = new NacosContainer()
    .withClusterMode(true);
```

#### Custom Command Arguments

Add custom JVM or Nacos command line arguments:

```java

@Container
NacosContainer nacos = new NacosContainer()
    .withCustomCommand("-Dnacos.core.auth.enabled=true");
```

### Accessing Container Information

Retrieve the service URL and credentials after the container starts:

```java

@Container
NacosContainer nacos = new NacosContainer();

@BeforeEach
void setUp() {
    String serviceUrl = nacos.getServiceUrl();  // e.g., http://localhost:12345/nacos
    String username = nacos.getUsername();      // nacos
    String password = nacos.getPassword();      // nacos
}
```

### Complete JUnit 5 Example

```java

@Testcontainers
class NacosIntegrationTest {

    @Container
    static NacosContainer nacos = new NacosContainer()
        .withUsername("test")
        .withPassword("test123");

    @Test
    void testNacosConnection() {
        String serviceUrl = nacos.getServiceUrl();
        // Use the URL to connect to Nacos
        assertThat(serviceUrl).startsWith("http://");
    }
}
```

### Multi-Node Cluster Example

Create a 3-node Nacos cluster with embedded database:

```java

@Testcontainers
class NacosClusterTest {

    static NacosCluster cluster = NacosCluster.builder()
        .withNodeCount(3)
        .build();

    @BeforeAll
    static void startCluster() {
        cluster.start();
    }

    @AfterAll
    static void stopCluster() {
        cluster.stop();
    }

    @Test
    void testClusterConnection() {
        // Get the primary node URL
        String primaryUrl = cluster.getPrimaryServiceUrl();
        assertThat(primaryUrl).startsWith("http://");

        // Get all node URLs
        List<String> urls = cluster.getServiceUrls();
        assertThat(urls).hasSize(3);
    }
}
```

### Cluster with MySQL Example

Create a cluster with MySQL as the shared database:

```java

@Testcontainers
class NacosClusterWithMySQLTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("nacos")
        .withUsername("nacos")
        .withPassword("nacos");

    static NacosCluster cluster;

    @BeforeAll
    static void startCluster() {
        cluster = NacosCluster.builder()
            .withNodeCount(3)
            .withMySQLContainer(mysql)
            .build();
        cluster.start();
    }

    @AfterAll
    static void stopCluster() {
        if (cluster != null) {
            cluster.stop();
        }
    }

    @Test
    void testClusterWithMySQL() {
        String primaryUrl = cluster.getPrimaryServiceUrl();
        // Test cluster functionality
    }
}
```

### Configuration Import Example

Pre-load configurations when the container starts:

```java

@Container
NacosContainer nacos = new NacosContainer()
    .withInitialConfig("application.properties", "server.port=8080")
    .withInitialConfig("app-config", "TEST_GROUP", "key=value")
    .withNamespace("test-namespace");
```

### Service Registration Example

Pre-register services when the container starts:

```java

@Container
NacosContainer nacos = new NacosContainer()
    .withInitialService("order-service", "192.168.1.10", 8080)
    .withInitialService(new NacosServiceInstance("user-service", "192.168.1.11", 8081)
        .withMetadata("version", "v1.0")
        .withMetadata("region", "beijing"));
```

### Using Nacos Client Example

Access Nacos Java SDK directly from the container:

```java
@Container
NacosContainer nacos = new NacosContainer();

@Test
void testWithNacosClient() throws NacosException {
    // Get ConfigService
    ConfigService configService = nacos.getConfigService();
    configService.publishConfig("test", "DEFAULT_GROUP", "content");

    // Get NamingService
    NamingService namingService = nacos.getNamingService();
    List<Instance> instances = namingService.getAllInstances("my-service");

    // Or use the client factory
    NacosClientFactory factory = nacos.getClientFactory();
    Properties props = factory.createProperties();
}
```

### Using Extensions

#### Fault Simulation

Test fault tolerance scenarios with the fault simulator:

```java
@Container
NacosContainer nacos = new NacosContainer();

@Test
void testFaultTolerance() {
    NacosFaultSimulator simulator = new NacosFaultSimulator();
    
    // Simulate network latency
    simulator.simulateNetworkLatency(nacos, 500);
    // Test fault scenario...
    
    // Remove latency
    simulator.removeNetworkLatency(nacos);
    
    // Simulate node failure
    simulator.simulateNodeFailure(nacos);
    // Test recovery...
    simulator.simulateNodeRecovery(nacos);
}
```

#### Enhanced Configuration Management

Use the configuration manager for advanced config operations:

```java
@Container
NacosContainer nacos = new NacosContainer();

@Test
void testConfigManagement() throws Exception {
    NacosConfigManager configManager = new NacosConfigManager(nacos);
    
    // Batch publish configurations
    List<NacosConfig> configs = List.of(
        new NacosConfig("app.config", "DEFAULT_GROUP", "key=value"),
        new NacosConfig("db.config", "DEFAULT_GROUP", "url=jdbc:mysql://localhost:3306/db")
    );
    configManager.publishConfigs(configs);
    
    // Wait for config changes
    String newContent = configManager.waitForConfigChange("app.config", "DEFAULT_GROUP", 10);
}
```

#### Enhanced Service Management

Use the service manager for advanced service operations:

```java
@Container
NacosContainer nacos = new NacosContainer();

@Test
void testServiceManagement() throws Exception {
    NacosServiceManager serviceManager = new NacosServiceManager(nacos);
    
    // Register multiple service instances
    List<NacosServiceInstance> instances = List.of(
        new NacosServiceInstance("order-service", "192.168.1.10", 8080),
        new NacosServiceInstance("user-service", "192.168.1.11", 8081)
    );
    serviceManager.registerInstances(instances);
    
    // Update instance health status
    serviceManager.updateInstanceHealth("order-service", "192.168.1.10", 8080, false);
}
```

#### Test Data Generation

Generate test data easily with the test data generator:

```java
@Test
void testWithGeneratedData() throws Exception {
    NacosTestDataGenerator generator = new NacosTestDataGenerator();
    
    // Generate random configurations
    List<NacosConfig> configs = generator.generateRandomConfigs(5);
    
    // Generate random service instances
    List<NacosServiceInstance> instances = generator.generateRandomServiceInstances(3);
    
    // Use generated data in tests...
}
```

## Framework Integration

### Spring Boot

Use `@DynamicPropertySource` to inject Nacos configuration into your Spring tests:

```java

@SpringBootTest
@Testcontainers
class SpringBootNacosTest {

    @Container
    static NacosContainer nacos = new NacosContainer()
        .withUsername("nacos")
        .withPassword("nacos");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.nacos.discovery.server-addr", nacos::getServiceUrl);
        registry.add("spring.cloud.nacos.config.server-addr", nacos::getServiceUrl);
        registry.add("spring.cloud.nacos.config.import-check.enabled", () -> "false");
        registry.add("spring.cloud.nacos.username", nacos::getUsername);
        registry.add("spring.cloud.nacos.password", nacos::getPassword);
    }

    @Test
    void contextLoads() {
        // Your test code
    }
}
```

### Other Frameworks

For other frameworks, use the container's `getServiceUrl()`, `getUsername()`, and `getPassword()` methods to configure
your application dynamically. Refer to your framework's testing documentation for dynamic configuration options.

## API Reference

### NacosContainer Constructor Methods

| Method                                   | Description                                           |
|------------------------------------------|-------------------------------------------------------|
| `NacosContainer()`                       | Create with default image `nacos/nacos-server:v2.5.2` |
| `NacosContainer(String dockerImageName)` | Create with custom Docker image                       |

### NacosContainer Configuration Methods

| Method                                                                              | Description                      | Default         |
|-------------------------------------------------------------------------------------|----------------------------------|-----------------|
| `withUsername(String username)`                                                     | Set Nacos username               | `nacos`         |
| `withPassword(String password)`                                                     | Set Nacos password               | `nacos`         |
| `withDatabaseType(String type)`                                                     | Set database type (deprecated)   | `embedded`      |
| `withDatabaseConfig(NacosDatabaseConfig config)`                                    | Set database configuration       | embedded        |
| `withExternalMySQL(String host, int port, String db, String user, String pwd)`      | Use external MySQL               | -               |
| `withMySQLContainer(MySQLContainer<?> mysql)`                                       | Use Testcontainers MySQL         | -               |
| `withExternalPostgreSQL(String host, int port, String db, String user, String pwd)` | Use external PostgreSQL          | -               |
| `withPostgreSQLContainer(PostgreSQLContainer<?> postgres)`                          | Use Testcontainers PostgreSQL    | -               |
| `withClusterMode(boolean enabled)`                                                  | Enable cluster mode              | `false`         |
| `withClusterNodes(NacosClusterNode... nodes)`                                       | Configure cluster nodes          | -               |
| `withClusterNetwork(Network network)`                                               | Set Docker network for cluster   | -               |
| `withCustomCommand(String... commands)`                                             | Add custom command arguments     | -               |
| `withNamespace(String namespace)`                                                   | Set Nacos namespace              | `""`            |
| `withDefaultGroup(String group)`                                                    | Set default config group         | `DEFAULT_GROUP` |
| `withInitialConfig(NacosConfig config)`                                             | Add initial configuration        | -               |
| `withInitialConfig(String dataId, String content)`                                  | Add initial config (simplified)  | -               |
| `withInitialService(NacosServiceInstance instance)`                                 | Add initial service              | -               |
| `withInitialService(String name, String ip, int port)`                              | Add initial service (simplified) | -               |
| `withAuthEnabled(boolean enabled)`                                                  | Enable/disable authentication    | `true`          |
| `withConsoleEnabled(boolean enabled)`                                               | Enable/disable console           | `true`          |
| `withMetricsEnabled()`                                                              | Enable Prometheus metrics        | `false`         |
| `withTokenExpiration(int seconds)`                                                  | Set auth token expiration        | `18000`         |

### NacosContainer Accessor Methods

| Method                | Description                        |
|-----------------------|------------------------------------|
| `getServiceUrl()`     | Get the Nacos service URL          |
| `getGrpcAddress()`    | Get the gRPC address               |
| `getUsername()`       | Get the configured username        |
| `getPassword()`       | Get the configured password        |
| `getNamespace()`      | Get the configured namespace       |
| `getDefaultGroup()`   | Get the default group              |
| `getDatabaseConfig()` | Get the database configuration     |
| `isClusterMode()`     | Check if cluster mode is enabled   |
| `getClusterNodes()`   | Get the list of cluster nodes      |
| `isAuthEnabled()`     | Check if authentication is enabled |
| `isConsoleEnabled()`  | Check if console is enabled        |
| `isMetricsEnabled()`  | Check if metrics are enabled       |

### NacosClient Methods

| Method                                    | Description                  |
|-------------------------------------------|------------------------------|
| `getClientFactory()`                      | Get the NacosClientFactory   |
| `getConfigService()`                      | Get ConfigService (Java SDK) |
| `getNamingService()`                      | Get NamingService (Java SDK) |
| `exportConfigs(String path)`              | Export all configs to file   |
| `createSnapshot()`                        | Create config snapshot       |
| `waitForClusterHealthy(Duration timeout)` | Wait for cluster health      |

### Version Detection Methods

| Method                 | Description                    |
|------------------------|--------------------------------|
| `getNacosVersion()`    | Get the detected Nacos version |
| `isV2()`               | Check if using Nacos 2.x       |
| `isV3()`               | Check if using Nacos 3.x       |
| `getDockerImageName()` | Get the Docker image name      |

### NacosCluster (Multi-Node Cluster Management)

| Method                                       | Description                      |
|----------------------------------------------|----------------------------------|
| `NacosCluster.builder()`                     | Create a cluster builder         |
| `builder.withNodeCount(int count)`           | Set number of nodes (default: 3) |
| `builder.withMySQLContainer(MySQLContainer)` | Use MySQL for shared storage     |
| `builder.withNacosImage(String image)`       | Set custom Nacos image           |
| `cluster.start()`                            | Start all cluster nodes          |
| `cluster.stop()`                             | Stop all cluster nodes           |
| `cluster.getNodes()`                         | Get all node containers          |
| `cluster.getPrimaryNode()`                   | Get the first node               |
| `cluster.getNode(int index)`                 | Get node by index                |
| `cluster.getPrimaryServiceUrl()`             | Get primary node URL             |
| `cluster.getServiceUrls()`                   | Get all node URLs                |

### Extensions API

#### NacosTestExtension (JUnit 5 Extension)

| Method | Description |
|--------|-------------|
| `@ExtendWith(NacosTestExtension.class)` | Apply the extension to a test class |
| `resolveParameter(ParameterContext, ExtensionContext)` | Inject NacosContainer instance |

#### NacosFaultSimulator

| Method | Description |
|--------|-------------|
| `simulateNodeFailure(NacosContainer)` | Simulate node failure |
| `simulateNodeRecovery(NacosContainer)` | Simulate node recovery |
| `simulateNetworkLatency(NacosContainer, int)` | Simulate network latency |
| `removeNetworkLatency(NacosContainer)` | Remove network latency |
| `simulateHighCpu(NacosContainer, int)` | Simulate high CPU usage |

#### NacosConfigManager

| Method | Description |
|--------|-------------|
| `publishConfigs(List<NacosConfig>)` | Publish multiple configurations |
| `getConfigs(List<String>, String)` | Get multiple configurations |
| `waitForConfigChange(String, String, int)` | Wait for configuration change |
| `rollbackConfig(String, String, String)` | Rollback configuration |
| `getConfigHistory(String, String)` | Get configuration history |

#### NacosServiceManager

| Method | Description |
|--------|-------------|
| `registerInstances(List<NacosServiceInstance>)` | Register multiple service instances |
| `deregisterInstances(List<NacosServiceInstance>)` | Deregister multiple service instances |
| `updateInstanceHealth(String, String, int, boolean)` | Update instance health status |
| `updateInstanceWeight(String, String, int, double)` | Update instance weight |
| `waitForServiceRegistration(String, int)` | Wait for service registration |

#### NacosTestDataGenerator

| Method | Description |
|--------|-------------|
| `generateRandomConfig()` | Generate a random configuration |
| `generateRandomConfigs(int)` | Generate multiple random configurations |
| `generateRandomServiceInstance()` | Generate a random service instance |
| `generateRandomServiceInstances(int)` | Generate multiple random service instances |

## Exposed Ports

| Port | Description                        |
|------|------------------------------------|
| 8848 | HTTP API and Console               |
| 9848 | gRPC client communication          |
| 9849 | gRPC server internal communication |

## Architecture

### Standalone Mode with Embedded Database

```
┌─────────────────┐
│  NacosContainer │
│  (Standalone)   │
│  - Embedded DB  │
└─────────────────┘
```

### Standalone Mode with External MySQL

```
┌─────────────────┐      ┌─────────────┐
│  NacosContainer │──────│   MySQL     │
│  (Standalone)   │      │  (External) │
│  - MySQL Config │      └─────────────┘
└─────────────────┘
```

### Cluster Mode with NacosCluster

```
┌─────────────────┐
│  NacosCluster   │
│  (3 nodes)      │
├─────────────────┤
│ ┌─────────────┐ │
│ │  Nacos-1    │ │◄── Primary Node
│ │  (Leader)   │ │
│ └─────────────┘ │
│ ┌─────────────┐ │
│ │  Nacos-2    │ │
│ │  (Follower) │ │
│ └─────────────┘ │
│ ┌─────────────┐ │
│ │  Nacos-3    │ │
│ │  (Follower) │ │
│ └─────────────┘ │
└─────────────────┘
```

### Cluster Mode with Shared MySQL

```
         ┌─────────────┐
         │    MySQL    │
         │   (Shared)  │
         └──────┬──────┘
                │
    ┌───────────┼───────────┐
    │           │           │
┌───┴───┐   ┌───┴───┐   ┌───┴───┐
│Nacos-1│   │Nacos-2│   │Nacos-3│
│(Node) │   │(Node) │   │(Node) │
└───────┘   └───────┘   └───────┘
```

## Version Compatibility

### Nacos 2.x vs 3.x Configuration Differences

The container automatically handles the following version differences:

| Feature         | Nacos 2.x                                             | Nacos 3.x                                           |
|-----------------|-------------------------------------------------------|-----------------------------------------------------|
| Database Type   | `SPRING_DATASOURCE_PLATFORM`                          | `spring.sql.init.platform`                          |
| MySQL Host      | `MYSQL_SERVICE_HOST`                                  | `db.url.0`                                          |
| MySQL Port      | `MYSQL_SERVICE_PORT`                                  | `db.url.0` (in URL)                                 |
| PostgreSQL      | Supported via `SPRING_DATASOURCE_PLATFORM=postgresql` | Supported via `spring.sql.init.platform=postgresql` |
| Cluster Mode    | `NACOS_MODE=cluster`                                  | `nacos.standalone=false`                            |
| Cluster Nodes   | `NACOS_SERVERS`                                       | `nacos.member.list`                                 |
| Server IP       | `NACOS_SERVER_IP`                                     | `nacos.server.ip`                                   |
| Standalone Mode | `NACOS_MODE=standalone`                               | `nacos.standalone=true`                             |

### Tested Versions

- Nacos 2.2.3, 2.3.0, 2.4.0, 2.5.0
- Nacos 3.0.0, 3.0.1

## Credits

- [Testcontainers](https://www.testcontainers.org/) - The testing framework that makes this possible
- [Nacos](https://nacos.io/) - The dynamic service discovery and configuration platform

## License

Apache License 2.0

Copyright (c) 2019-2026 Cacotopia

See [LICENSE](LICENSE) file for details.
