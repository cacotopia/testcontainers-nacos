package com.github.cacotopia.testcontainers.nacos;

/**
 * Nacos Testcontainer
 * 支持 Nacos 2.x 和 Nacos 3.x 版本
 *
 * @author cacotopia
 */
public class NacosContainer extends ExtendableNacosContainer<NacosContainer> {

    /**
     * 使用默认 Nacos 版本 (2.2.3)
     */
    public NacosContainer() {
        super();
    }

    /**
     * 使用指定的 Docker 镜像
     * 支持自动检测 Nacos 版本并应用相应的配置
     *
     * @param dockerImageName 镜像名称，如 "nacos/nacos-server:2.2.3" 或 "nacos/nacos-server:3.0.0"
     */
    public NacosContainer(String dockerImageName) {
        super(dockerImageName);
    }

    /**
     * 使用指定的 Nacos 版本
     *
     * @param version Nacos 版本
     */
    public static NacosContainer withVersion(NacosVersion version) {
        String image = "nacos/nacos-server:" + version.getVersionPrefix();
        return new NacosContainer(image);
    }

    /**
     * 使用 Nacos 3.x 版本
     */
    public static NacosContainer v3() {
        return new NacosContainer("nacos/nacos-server:3.0.0");
    }

    /**
     * 使用 Nacos 3.x 指定版本
     */
    public static NacosContainer v3(String version) {
        return new NacosContainer("nacos/nacos-server:" + version);
    }

    /**
     * 使用 Nacos 2.x 最新版本
     */
    public static NacosContainer v2() {
        return new NacosContainer("nacos/nacos-server:2.5.0");
    }

    /**
     * 使用 Nacos 2.x 指定版本
     */
    public static NacosContainer v2(String version) {
        return new NacosContainer("nacos/nacos-server:" + version);
    }

}
