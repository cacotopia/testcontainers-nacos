package com.github.cacotopia.testcontainers.nacos;

/**
 * Nacos 版本枚举
 * 支持 Nacos 2.x 和 Nacos 3.x 版本检测和适配
 */
public enum NacosVersion {
    
    V2_0("2.0", "Nacos 2.0"),
    V2_1("2.1", "Nacos 2.1"),
    V2_2("2.2", "Nacos 2.2"),
    V2_3("2.3", "Nacos 2.3"),
    V2_4("2.4", "Nacos 2.4"),
    V2_5("2.5", "Nacos 2.5"),
    V3_0("3.0", "Nacos 3.0"),
    V3_1("3.1", "Nacos 3.1"),
    UNKNOWN("unknown", "Unknown Version");
    
    private final String versionPrefix;
    private final String displayName;
    
    NacosVersion(String versionPrefix, String displayName) {
        this.versionPrefix = versionPrefix;
        this.displayName = displayName;
    }
    
    public String getVersionPrefix() {
        return versionPrefix;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 判断是否为 Nacos 3.x 版本
     */
    public boolean isV3() {
        return this == V3_0 || this == V3_1 || versionPrefix.startsWith("3.");
    }
    
    /**
     * 判断是否为 Nacos 2.x 版本
     */
    public boolean isV2() {
        return versionPrefix.startsWith("2.");
    }
    
    /**
     * 从镜像名称或版本字符串解析版本
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
     * 从版本字符串提取版本号
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
     * 获取默认镜像版本
     */
    public static String getDefaultImage() {
        return "nacos/nacos-server:2.2.3";
    }
    
    /**
     * 获取默认版本
     */
    public static NacosVersion getDefault() {
        return V2_2;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
