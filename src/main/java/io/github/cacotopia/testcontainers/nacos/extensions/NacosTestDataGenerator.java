package io.github.cacotopia.testcontainers.nacos.extensions;

import io.github.cacotopia.testcontainers.nacos.NacosConfig;
import io.github.cacotopia.testcontainers.nacos.NacosServiceInstance;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Nacos test data generator for creating random test data.
 * Simplifies test data preparation for Nacos configuration and service discovery tests.
 */
public class NacosTestDataGenerator {

    private final Faker faker;

    public NacosTestDataGenerator() {
        this.faker = new Faker();
    }

    /**
     * Generate a random Nacos configuration.
     *
     * @return A random NacosConfig
     */
    public NacosConfig generateRandomConfig() {
        String dataId = faker.lorem().word() + "-" + System.currentTimeMillis();
        String group = faker.lorem().word();
        String content = generateRandomConfigContent();
        String format = getRandomFormat();

        return new NacosConfig(dataId, group, content, format);
    }

    /**
     * Generate multiple random Nacos configurations.
     *
     * @param count The number of configurations to generate
     * @return List of random NacosConfig objects
     */
    public List<NacosConfig> generateRandomConfigs(int count) {
        List<NacosConfig> configs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            configs.add(generateRandomConfig());
        }
        return configs;
    }

    /**
     * Generate a random service instance.
     *
     * @return A random NacosServiceInstance
     */
    public NacosServiceInstance generateRandomServiceInstance() {
        String serviceName = faker.lorem().word() + "-service";
        String ip = generateRandomIp();
        int port = ThreadLocalRandom.current().nextInt(10000, 65535);
        String clusterName = faker.lorem().word();

        NacosServiceInstance instance = new NacosServiceInstance(serviceName, ip, port, clusterName);

        // Add random metadata
        Map<String, String> metadata = generateRandomMetadata();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            instance.withMetadata(entry.getKey(), entry.getValue());
        }

        return instance;
    }

    /**
     * Generate multiple random service instances.
     *
     * @param count The number of instances to generate
     * @return List of random NacosServiceInstance objects
     */
    public List<NacosServiceInstance> generateRandomServiceInstances(int count) {
        List<NacosServiceInstance> instances = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            instances.add(generateRandomServiceInstance());
        }
        return instances;
    }

    /**
     * Generate random configuration content based on format.
     *
     * @return Random configuration content
     */
    private String generateRandomConfigContent() {
        String format = getRandomFormat();
        switch (format) {
            case "yaml":
                return generateYamlContent();
            case "json":
                return generateJsonContent();
            case "properties":
            default:
                return generatePropertiesContent();
        }
    }

    /**
     * Generate random YAML content.
     *
     * @return Random YAML content
     */
    private String generateYamlContent() {
        StringBuilder yaml = new StringBuilder();
        yaml.append("server:\n");
        yaml.append("  port: ").append(ThreadLocalRandom.current().nextInt(8000, 9000)).append("\n");
        yaml.append("\n");
        yaml.append("spring:\n");
        yaml.append("  application:\n");
        yaml.append("    name: " + faker.lorem().word()).append("\n");
        yaml.append("  \n");
        yaml.append("  datasource:\n");
        yaml.append("    url: jdbc:mysql://localhost:3306/" + faker.lorem().word()).append("\n");
        yaml.append("    username: " + faker.internet().username()).append("\n");
        yaml.append("    password: " + faker.internet().password()).append("\n");
        return yaml.toString();
    }

    /**
     * Generate random JSON content.
     *
     * @return Random JSON content
     */
    private String generateJsonContent() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"server\": {");
        json.append("\"port\": " + ThreadLocalRandom.current().nextInt(8000, 9000));
        json.append("},");
        json.append("\"spring\": {");
        json.append("\"application\": {");
        json.append("\"name\": \"" + faker.lorem().word() + "\"");
        json.append("}");
        json.append("}");
        json.append("}");
        return json.toString();
    }

    /**
     * Generate random properties content.
     *
     * @return Random properties content
     */
    private String generatePropertiesContent() {
        StringBuilder properties = new StringBuilder();
        properties.append("server.port=").append(ThreadLocalRandom.current().nextInt(8000, 9000)).append("\n");
        properties.append("spring.application.name=").append(faker.lorem().word()).append("\n");
        properties.append("spring.datasource.url=jdbc:mysql://localhost:3306/").append(faker.lorem().word()).append("\n");
        properties.append("spring.datasource.username=").append(faker.internet().username()).append("\n");
        properties.append("spring.datasource.password=").append(faker.internet().password()).append("\n");
        return properties.toString();
    }

    /**
     * Generate random metadata.
     *
     * @return Random metadata map
     */
    private Map<String, String> generateRandomMetadata() {
        Map<String, String> metadata = new HashMap<>();
        int metadataCount = ThreadLocalRandom.current().nextInt(1, 5);

        for (int i = 0; i < metadataCount; i++) {
            String key = faker.lorem().word();
            String value = faker.lorem().word();
            metadata.put(key, value);
        }

        return metadata;
    }

    /**
     * Generate a random IP address.
     *
     * @return Random IP address
     */
    private String generateRandomIp() {
        return ThreadLocalRandom.current().nextInt(1, 255) + "." +
               ThreadLocalRandom.current().nextInt(0, 255) + "." +
               ThreadLocalRandom.current().nextInt(0, 255) + "." +
               ThreadLocalRandom.current().nextInt(1, 255);
    }

    /**
     * Get a random configuration format.
     *
     * @return Random format
     */
    private String getRandomFormat() {
        String[] formats = {"properties", "yaml", "json"};
        return formats[ThreadLocalRandom.current().nextInt(formats.length)];
    }
}
