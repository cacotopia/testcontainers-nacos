package io.github.cacotopia.testcontainers.nacos;

/**
 * Nacos Testcontainer implementation.
 * Supports both Nacos 2.x and Nacos 3.x versions.
 *
 * @author cacotopia
 */
public class NacosContainer extends ExtendableNacosContainer<NacosContainer> {

    /**
     * Creates a new NacosContainer with the default Nacos version (2.2.3).
     */
    public NacosContainer() {
        super();
    }

    /**
     * Creates a new NacosContainer with the specified Docker image.
     * Automatically detects the Nacos version and applies the appropriate configuration.
     *
     * @param dockerImageName The Docker image name, e.g., "nacos/nacos-server:2.2.3" or "nacos/nacos-server:3.0.0"
     */
    public NacosContainer(String dockerImageName) {
        super(dockerImageName);
    }

    /**
     * Creates a NacosContainer with the specified Nacos version.
     *
     * @param version The Nacos version to use
     * @return A new NacosContainer instance
     */
    public static NacosContainer withVersion(NacosVersion version) {
        String image = "nacos/nacos-server:" + version.getVersionPrefix();
        return new NacosContainer(image);
    }

    /**
     * Creates a NacosContainer with Nacos 3.x version.
     *
     * @return A new NacosContainer instance with Nacos 3.0.0
     */
    public static NacosContainer v3() {
        return new NacosContainer("nacos/nacos-server:3.0.0");
    }

    /**
     * Creates a NacosContainer with the specified Nacos 3.x version.
     *
     * @param version The Nacos 3.x version to use
     * @return A new NacosContainer instance
     */
    public static NacosContainer v3(String version) {
        return new NacosContainer("nacos/nacos-server:" + version);
    }

    /**
     * Creates a NacosContainer with the latest Nacos 2.x version.
     *
     * @return A new NacosContainer instance with Nacos 2.5.0
     */
    public static NacosContainer v2() {
        return new NacosContainer("nacos/nacos-server:2.5.0");
    }

    /**
     * Creates a NacosContainer with the specified Nacos 2.x version.
     *
     * @param version The Nacos 2.x version to use
     * @return A new NacosContainer instance
     */
    public static NacosContainer v2(String version) {
        return new NacosContainer("nacos/nacos-server:" + version);
    }

}
