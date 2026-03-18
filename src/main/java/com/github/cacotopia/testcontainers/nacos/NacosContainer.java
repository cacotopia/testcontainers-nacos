package com.github.cacotopia.testcontainers.nacos;

/**
 * @author cacotopia
 */
public class NacosContainer extends ExtendableNacosContainer<NacosContainer> {

    public NacosContainer() {
        super();
    }

    public NacosContainer(String dockerImageName) {
        super(dockerImageName);
    }

}
