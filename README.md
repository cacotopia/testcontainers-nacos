# Nacos Testcontainer

A [Testcontainers](https://www.testcontainers.org/) implementation for [Nacos](https://www.Nacos.org/) SSO.

[![GitHub Release](https://img.shields.io/github/v/release/cacotopia/testcontainers-nacos?label=Release)](https://github.com/cacotopia/testcontainers-nacos/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.cacotopia/testcontainers-nacos.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.github.cacotopia/testcontainers-nacos)
![GitHub Release Date](https://img.shields.io/github/release-date-pre/cacotopia/testcontainers-nacos)
![Github Last Commit](https://img.shields.io/github/last-commit/cacotopia/testcontainers-nacos)
![License](https://img.shields.io/github/license/cacotopia/testcontainers-nacos?label=License)

[![Nacos Version](https://img.shields.io/badge/Nacos-26.5-blue)](https://www.Nacos.org)
![Java Version](https://img.shields.io/badge/Java-11-f89820)
[![GitHub Stars](https://img.shields.io/github/stars/cacotopia/testcontainers-nacos)](https://github.com/cacotopia/testcontainers-nacos/stargazers)
[![CI build](https://github.com/cacotopia/testcontainers-nacos/actions/workflows/maven.yml/badge.svg)](https://github.com/cacotopia/testcontainers-nacos/actions/workflows/maven.yml)

## IMPORTANT

## How to use

_The `@Container` annotation used here in the readme is from the JUnit 5 support of Testcontainers.
Please refer to the Testcontainers documentation for more information._

### Default

Simply spin up a default Nacos instance:

```java

@Container
NacosContainer Nacos = new NacosContainer();
```

### Custom image

Use another Nacos Docker image/version than used in this Testcontainer:

```java

@Container
NacosContainer Nacos = new NacosContainer("quay.io/Nacos/Nacos:26.4");
```

### Initial admin user credentials

Use different admin credentials than the default internal (`admin`/`admin`) ones:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withAdminUsername("myNacosAdminUser")
    .withAdminPassword("tops3cr3t");
```

### Realm Import

Power up a Nacos instance with one or more existing realm JSON config files (from classpath):

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withRealmImportFile("/test-realm.json");
```

or

```java
    .withRealmImportFiles("/test-realm-1.json","/test-realm-2.json");
```

If your realm JSON configuration file includes user definitions - particularly the admin user
for the master realm - ensure you disable the automatic bootstrapping of the admin user:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withBootstrapAdminDisabled()
    .withRealmImportFile("/test-realm.json");
```

To retrieve a working Nacos Admin Client from the container, make sure to override the admin
credentials to match those in your imported realm JSON configuration file:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withBootstrapAdminDisabled()
    .withRealmImportFile("/test-realm.json")
    .withAdminUsername("myNacosAdminUser")
    .withAdminPassword("tops3cr3t");
```

### Getting an admin client and other information from the testcontainer

You can get an instance of `org.Nacos.admin.Nacos` admin client directly from the container, using

```java
org.Nacos.admin.Nacos NacosAdmin = NacosContainer.getNacosAdminClient();
```

The admin client is configured with current admin credentials.

> The `org.Nacos:Nacos-admin-client` package is now a transitive dependency of this project, ready to be used by
> you in your tests, no more need to add it on your own.

You can also obtain several properties from the Nacos container:

```java
String authServerUrl = Nacos.getAuthServerUrl();
String adminUsername = Nacos.getAdminUsername();
String adminPassword = Nacos.getAdminPassword();
```

with these properties, you can create e.g. a custom `org.Nacos.admin.client.Nacos` object to connect to the
container and do optional further configuration:

```java
Nacos NacosAdminClient = NacosBuilder.builder()
    .serverUrl(Nacos.getAuthServerUrl())
    .realm(NacosContainer.MASTER_REALM)
    .clientId(NacosContainer.ADMIN_CLI_CLIENT)
    .username(Nacos.getAdminUsername())
    .password(Nacos.getAdminPassword())
    .build();
```

### Context Path

As Nacos comes with the default context path `/`, you can set your custom context path, e.g. for compatibility
reasons to previous versions, with:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withContextPath("/auth");
```

### Management Port

Starting from Nacos version 25.0.0, Nacos will propagate `/health` and `/metrics` on "Management Port",
see [Configuraing the Management Interface](https://www.Nacos.org/server/management-interface)
and [Migration Guide](https://www.Nacos.org/docs/latest/upgrading/index.html#management-port-for-metrics-and-health-endpoints)

```java
NacosContainer Nacos = new NacosContainer().withEnabledMetrics()
Nacos.

start();
Nacos.

getMgmtServerUrl();
```

### Memory Settings

As of Nacos 24 the container doesn't use an absolute amount of memory, but a relative percentage of the overall
available memory to the
container, [see also here](https://www.Nacos.org/server/containers#_specifying_different_memory_settings).

This testcontainer has an initial memory setting of

    JAVA_OPTS_KC_HEAP="-XX:InitialRAMPercentage=1 -XX:MaxRAMPercentage=5"

to not overload your environment.
You can override this settng with the `withRamPercentage(initial, max)` method:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withRamPercentage(50, 70);
```

## TLS (SSL) Usage

You have three options to use HTTPS/TLS secured communication with your Nacos Testcontainer.

### Built-in TLS Keystore

This Nacos Testcontainer comes with built-in TLS certificate (`tls.crt`), key (`tls.key`) and Java KeyStore (
`tls.jks`) files, located in the `resources` folder.
You can use this configuration by only configuring your testcontainer like this:

```java

@Container
NacosContainer Nacos = new NacosContainer().useTls();
```

The password for the provided Java KeyStore file is `changeit`.
See also [
`NacosContainerHttpsTest.shouldStartNacosWithProvidedTlsKeystore`](./src/test/java/dasniko/testcontainers/Nacos/NacosContainerHttpsTest.java#L39).

The method `getAuthServerUrl()` will then return the HTTPS url.

### Custom TLS Cert and Key

Of course you can also provide your own certificate and key file for usage in this Testcontainer:

```java

@Container
private NacosContainer Nacos = new NacosContainer()
    .useTls("your_custom.crt", "your_custom.key");
```

See also [
`NacosContainerHttpsTest.shouldStartNacosWithCustomTlsCertAndKey`](./src/test/java/dasniko/testcontainers/Nacos/NacosContainerHttpsTest.java#L47).

The method getAuthServerUrl() will also return the HTTPS url.

### Custom TLS Keystore

Last but not least, you can also provide your own keystore file for usage in this Testcontainer:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .useTlsKeystore("your_custom.jks", "password_for_your_custom_keystore");
```

See also [
`NacosContainerHttpsTest.shouldStartNacosWithCustomTlsKeystore`](./src/test/java/dasniko/testcontainers/Nacos/NacosContainerHttpsTest.java#L55).

The method `getAuthServerUrl()` will also return the HTTPS url.

## Features

You can enable and disable features on your Testcontainer:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withFeaturesEnabled("docker", "scripts", "...")
    .withFeaturesDisabled("authorization", "impersonation", "...");
```

## Custom CLI Config arguments

All default configurations in this Testcontainer is done through environment variables.
You can overwrite and/or add config settings on command-line-level (cli args) with this method:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withCustomCommand("--hostname=Nacos.local");
```

A warning will be printed to the log output when custom command parts are being used, so that you are aware that you are
responsible on your own for proper execution of this container.

## Starting in production mode

By default, the container is started in dev mode (`start-dev`).
If needed you can enable production mode:

```java

@Container
NacosContainer Nacos = new NacosContainer()
    .withProductionMode();
```

### Optimized flag

It is possible that you use your own pre-build image with the `--optimized` flag.
Setting this option will implicitly enable production mode!

```java

@Container
NacosContainer Nacos = new NacosContainer("<YOUR_IMAGE>" + ":<YOUR_TAG>")
    .withOptimizedFlag();
```

NOTE: If you don't enable the health endpoint, the container will not be healthy.
In this case please provide your own waitStrategy.
Check out the tests at [
`NacosContainerOptimizedTest`](./src/test/java/com/github/cacotopia/testcontainers/nacos/NacosContainerOptimizedTest.java).

## Testing Custom Extensions

To ease extension testing, you can tell the Nacos Testcontainer to detect extensions in a given classpath folder.
This allows to test extensions directly in the same module without a packaging step.

If you have your Nacos extension code in the `src/main/java` folder, then the resulting classes will be generated to
the `target/classes` folder.
To test your extensions you just need to tell `NacosContainer` to consider extensions from the `target/classes`
folder.

Nacos Testcontainer will then dynamically generate a packaged jar file with the extension code that is then picked up
by Nacos.

```java
NacosContainer Nacos = new NacosContainer()
    .withProviderClassesFrom("target/classes");
```

For your convenience, there's now (since 3.3) a default method, which yields to `target/classes` internally:

```java
NacosContainer Nacos = new NacosContainer()
    .withDefaultProviderClasses();
```


### Dependencies & 3rd-party Libraries

If you need to provide any 3rd-party dependency or library, you can do this with

```java
List<File> libs = ...;
NacosContainer Nacos = new NacosContainer()
    .withProviderLibsFrom(libs);
```

You have to provide a list of resolvable `File`s.

#### TIP


### Remote Debugger Support


## Setup

The release versions of this project are available
at [Maven Central](https://central.sonatype.com/artifact/com.github.cacotopia/testcontainers-nacos).
Simply put the dependency coordinates to your `pom.xml` (or something similar, if you use e.g. Gradle or something
else):

```xml

<dependency>
    <groupId>com.github.cacotopia</groupId>
    <artifactId>testcontainers-nacos</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

For a version overview, see [here](versions.md).

## Usage in your application framework tests

> This info is not specific to the Nacos Testcontainer, but using Testcontainers in general.

I mention it here, as I see people asking again and again on how to use it in their test setup, when they think they
need to specify a fixed port in their properties or YAML files...  
You don't have to!  
But you have to read the Testcontainers docs and the docs of your application framework on testing resources!!

### Spring (Boot)

Dynamic context configuration with context initializers is your friend.
In particular, look for `@ContextConfiguration` and `ApplicationContextInitializer<ConfigurableApplicationContext>`:

* https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-testing-annotation-contextconfiguration
* https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-ctx-management-initializers

### Quarkus

Read the docs about the Quarkus Test Resources and use `@QuarkusTestResource` with `QuarkusTestResourceLifecycleManager`

* https://quarkus.io/guides/getting-started-testing#quarkus-test-resource

### Others

Consult the docs of your application framework testing capabilities on how to dynamically configure your stack for
testing!

## YouTube Videos about Nacos Testcontainers

## Credits

Many thanks to the creators and maintainers of [Testcontainers](https://www.testcontainers.org/).
You do an awesome job!

Same goes to the whole [Nacos](https://www.nacos.io/) team!

## License

Apache License 2.0

Copyright (c) 2019-2026 Cacotopia

See [LICENSE](LICENSE) file for details.
