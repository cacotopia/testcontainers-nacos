package io.github.cacotopia.testcontainers.nacos;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import io.github.cacotopia.testcontainers.nacos.NacosContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JUnit 5 extension for Nacos containers.
 * Provides automatic lifecycle management for NacosContainer instances.
 */
public class NacosTestExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Map<String, NacosContainer> containerMap = new HashMap<>();

    @Override
    public void beforeAll(ExtensionContext context) {
        // Initialize container if needed
        getContainer(context);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Stop and remove container
        String key = getContainerKey(context);
        NacosContainer container = containerMap.remove(key);
        if (container != null && container.isRunning()) {
            container.stop();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        // Ensure container is running
        NacosContainer container = getContainer(context);
        if (!container.isRunning()) {
            container.start();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // Optional: reset container state
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == NacosContainer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return getContainer(extensionContext);
    }

    private NacosContainer getContainer(ExtensionContext context) {
        String key = getContainerKey(context);
        return containerMap.computeIfAbsent(key, k -> {
            NacosContainer container = new NacosContainer();
            container.start();
            return container;
        });
    }

    private String getContainerKey(ExtensionContext context) {
        Optional<Class<?>> testClass = context.getTestClass();
        return testClass.map(Class::getName).orElse("default");
    }
}
