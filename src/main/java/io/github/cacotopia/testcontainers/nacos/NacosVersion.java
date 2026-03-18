package io.github.cacotopia.testcontainers.nacos;

/**
 * Nacos version enumeration.
 * Supports Nacos 2.x and Nacos 3.x version detection and adaptation.
 */
public enum NacosVersion {

    /**
     * Nacos 2.0 version
     */
    V2_0("2.0", "Nacos 2.0"),

    /**
     * Nacos 2.1 version
     */
    V2_1("2.1", "Nacos 2.1"),

    /**
     * Nacos 2.2 version
     */
    V2_2("2.2", "Nacos 2.2"),

    /**
     * Nacos 2.3 version
     */
    V2_3("2.3", "Nacos 2.3"),

    /**
     * Nacos 2.4 version
     */
    V2_4("2.4", "Nacos 2.4"),

    /**
     * Nacos 2.5 version
     */
    V2_5("2.5", "Nacos 2.5"),

    /**
     * Nacos 3.0 version
     */
    V3_0("3.0", "Nacos 3.0"),

    /**
     * Nacos 3.1 version
     */
    V3_1("3.1", "Nacos 3.1"),

    /**
     * Unknown version
     */
    UNKNOWN("unknown", "Unknown Version");

    /**
     * Version prefix
     */
    private final String versionPrefix;

    /**
     * Display name
     */
    private final String displayName;

    /**
     * Creates a new NacosVersion with the specified version prefix and display name.
     *
     * @param versionPrefix The version prefix
     * @param displayName The display name
     */
    NacosVersion(String versionPrefix, String displayName) {
        this.versionPrefix = versionPrefix;
        this.displayName = displayName;
    }

    /**
     * Gets the version prefix.
     *
     * @return The version prefix
     */
    public String getVersionPrefix() {
        return versionPrefix;
    }

    /**
     * Gets the display name.
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this is a Nacos 3.x version.
     *
     * @return true if this is a Nacos 3.x version, false otherwise
     */
    public boolean isV3() {
        return this == V3_0 || this == V3_1 || versionPrefix.startsWith("3.");
    }

    /**
     * Checks if this is a Nacos 2.x version.
     *
     * @return true if this is a Nacos 2.x version, false otherwise
     */
    public boolean isV2() {
        return versionPrefix.startsWith("2.");
    }

    /**
     * Parses the Nacos version from an image name or version string.
     *
     * @param imageName The image name or version string
     * @return The corresponding NacosVersion enum value
     */
    public static NacosVersion fromImageName(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return V2_2; // 默认版本
        }

        // 提取版本号部分
        String version = extractVersion(imageName);

        // 匹配版本前缀
        for (NacosVersion v : values()) {
            if (v == UNKNOWN) continue;
            if (version.startsWith(v.versionPrefix)) {
                return v;
            }
        }

        // 尝试匹配主版本号
        if (version.startsWith("3.")) {
            return V3_0;
        } else if (version.startsWith("2.")) {
            return V2_2;
        }

        return UNKNOWN;
    }

    /**
     * Extracts the version number from a version string.
     *
     * @param imageName The image name or version string
     * @return The extracted version number
     */
    private static String extractVersion(String imageName) {
        // 处理形如 nacos/nacos-server:2.2.3 或 2.2.3 的输入
        String version = imageName;

        // 移除 registry 和 repository 前缀
        if (version.contains(":")) {
            version = version.substring(version.lastIndexOf(":") + 1);
        }

        // 移除标签后缀（如 -slim, -alpine）
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf("-"));
        }

        return version;
    }

    /**
     * Gets the default Nacos image.
     *
     * @return The default Nacos image
     */
    public static String getDefaultImage() {
        return "nacos/nacos-server:2.2.3";
    }

    /**
     * Gets the default Nacos version.
     *
     * @return The default NacosVersion
     */
    public static NacosVersion getDefault() {
        return V2_2;
    }

    /**
     * Returns the display name of the Nacos version.
     *
     * @return The display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
