package com.github.cacotopia.testcontainers.nacos;

import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.utility.MountableFile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

public abstract class ExtendableNacosContainer<SELF extends ExtendableNacosContainer<SELF>> extends GenericContainer<SELF> {
    // Nacos 相关常量
    private static final String NACOS_IMAGE = "nacos/nacos-server";
    private static final String NACOS_VERSION = "2.2.3";

    private static final int NACOS_PORT_HTTP = 8848;
    private static final int NACOS_PORT_GRPC = 9848;
    private static final int NACOS_PORT_GRPC_MGMT = 9849;

    // 配置属性
    private String username = "nacos";
    private String password = "nacos";
    private String databaseType = "embedded"; // 默认为嵌入式数据库
    private boolean clusterMode = false;
    private String[] customCommandParts;

    // 构造方法
    public ExtendableNacosContainer() {
        this(NACOS_IMAGE + ":" + NACOS_VERSION);
    }

    public ExtendableNacosContainer(String dockerImageName) {
        super(dockerImageName);
        withExposedPorts(NACOS_PORT_HTTP, NACOS_PORT_GRPC, NACOS_PORT_GRPC_MGMT);
        withLogConsumer(new Slf4jLogConsumer(logger()));
    }

    // 配置方法
    @Override
    protected void configure() {
        List<String> commandParts = new ArrayList<>();

        // 设置环境变量
        withEnv("NACOS_AUTH_ENABLE", "true");
        withEnv("NACOS_AUTH_USERNAME", username);
        withEnv("NACOS_AUTH_PASSWORD", password);
        withEnv("NACOS_AUTH_TOKEN", "SecretKey012345678901234567890123456789012345678901234567890123456789");

        if (databaseType.equals("embedded")) {
            withEnv("NACOS_AUTH_CACHE_ENABLE", "true");
        } else {
            // 可以添加其他数据库配置
        }

        if (clusterMode) {
            withEnv("NACOS_MODE", "cluster");
        } else {
            withEnv("NACOS_MODE", "standalone");
        }

        // 设置等待策略
        setWaitStrategy(Wait.forHttp("/nacos").forPort(NACOS_PORT_HTTP)
            .forStatusCode(200)
            .withStartupTimeout(Duration.ofMinutes(2)));

        // 添加自定义命令
        if (customCommandParts != null) {
            commandParts.addAll(Arrays.asList(customCommandParts));
        }

        setCommand(commandParts.toArray(new String[0]));
    }

    // 公共方法
    public SELF withUsername(String username) {
        this.username = username;
        return self();
    }

    public SELF withPassword(String password) {
        this.password = password;
        return self();
    }

    public SELF withDatabaseType(String databaseType) {
        this.databaseType = databaseType;
        return self();
    }

    public SELF withClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
        return self();
    }

    public SELF withCustomCommand(String... commands) {
        this.customCommandParts = commands;
        return self();
    }

    public String getServiceUrl() {
        return String.format("http://%s:%s/nacos", getHost(), getMappedPort(NACOS_PORT_HTTP));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
