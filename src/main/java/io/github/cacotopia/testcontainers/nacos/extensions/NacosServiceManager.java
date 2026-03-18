package io.github.cacotopia.testcontainers.nacos.extensions;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.exception.NacosException;
import io.github.cacotopia.testcontainers.nacos.NacosContainer;
import io.github.cacotopia.testcontainers.nacos.NacosServiceInstance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Nacos service manager with enhanced features for testing.
 * Supports service health status simulation, weight adjustment, and metadata management.
 */
public class NacosServiceManager {

    private final NacosContainer container;
    private final NamingService namingService;

    public NacosServiceManager(NacosContainer container) throws NacosException {
        this.container = container;
        this.namingService = container.getNamingService();
    }

    /**
     * Register multiple service instances at once.
     *
     * @param instances List of service instances to register
     * @throws NacosException If an error occurs
     */
    public void registerInstances(List<NacosServiceInstance> instances) throws NacosException {
        for (NacosServiceInstance instance : instances) {
            Instance nacosInstance = createNacosInstance(instance);
            namingService.registerInstance(instance.getServiceName(), nacosInstance);
        }
    }

    /**
     * Deregister multiple service instances at once.
     *
     * @param instances List of service instances to deregister
     * @throws NacosException If an error occurs
     */
    public void deregisterInstances(List<NacosServiceInstance> instances) throws NacosException {
        for (NacosServiceInstance instance : instances) {
            namingService.deregisterInstance(
                    instance.getServiceName(),
                    instance.getIp(),
                    instance.getPort()
            );
        }
    }

    /**
     * Update the health status of a service instance.
     *
     * @param serviceName The service name
     * @param ip The instance IP
     * @param port The instance port
     * @param healthy Whether the instance is healthy
     * @throws NacosException If an error occurs
     */
    public void updateInstanceHealth(String serviceName, String ip, int port, boolean healthy) throws NacosException {
        // Note: Nacos client doesn't directly support updating health status
        // This is a workaround - we'll deregister and re-register with updated status
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setHealthy(healthy);

        namingService.deregisterInstance(serviceName, ip, port);
        namingService.registerInstance(serviceName, instance);
    }

    /**
     * Update the weight of a service instance.
     *
     * @param serviceName The service name
     * @param ip The instance IP
     * @param port The instance port
     * @param weight The new weight
     * @throws NacosException If an error occurs
     */
    public void updateInstanceWeight(String serviceName, String ip, int port, double weight) throws NacosException {
        // Note: Nacos client doesn't directly support updating weight
        // This is a workaround - we'll deregister and re-register with updated weight
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setWeight(weight);

        namingService.deregisterInstance(serviceName, ip, port);
        namingService.registerInstance(serviceName, instance);
    }

    /**
     * Update the metadata of a service instance.
     *
     * @param serviceName The service name
     * @param ip The instance IP
     * @param port The instance port
     * @param metadata The new metadata
     * @throws NacosException If an error occurs
     */
    public void updateInstanceMetadata(String serviceName, String ip, int port, Map<String, String> metadata) throws NacosException {
        // Note: Nacos client doesn't directly support updating metadata
        // This is a workaround - we'll deregister and re-register with updated metadata
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setMetadata(metadata);

        namingService.deregisterInstance(serviceName, ip, port);
        namingService.registerInstance(serviceName, instance);
    }

    /**
     * Wait for a service to be registered.
     *
     * @param serviceName The service name
     * @param timeoutSeconds The timeout in seconds
     * @return The service info
     * @throws NacosException If an error occurs
     * @throws InterruptedException If interrupted
     */
    public ServiceInfo waitForServiceRegistration(String serviceName, int timeoutSeconds) throws NacosException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ServiceInfo[] serviceInfoHolder = new ServiceInfo[1];

        namingService.subscribe(serviceName, event -> {
            serviceInfoHolder[0] = event.getServiceInfo();
            latch.countDown();
        });

        boolean success = latch.await(timeoutSeconds, TimeUnit.SECONDS);
        if (!success) {
            throw new RuntimeException("Timeout waiting for service registration");
        }

        return serviceInfoHolder[0];
    }

    /**
     * Get all instances for a service across all clusters.
     *
     * @param serviceName The service name
     * @return List of instances
     * @throws NacosException If an error occurs
     */
    public List<Instance> getAllInstances(String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }

    /**
     * Get only healthy instances for a service.
     *
     * @param serviceName The service name
     * @return List of healthy instances
     * @throws NacosException If an error occurs
     */
    public List<Instance> getHealthyInstances(String serviceName) throws NacosException {
        return namingService.selectInstances(serviceName, true);
    }

    /**
     * Get all services in a namespace.
     *
     * @param namespaceId The namespace ID
     * @return List of service names
     * @throws NacosException If an error occurs
     */
    public List<String> getServices(String namespaceId) throws NacosException {
        ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, namespaceId);
        return services.getData();
    }

    /**
     * Create a Nacos Instance from a NacosServiceInstance.
     *
     * @param serviceInstance The service instance
     * @return The Nacos Instance
     */
    private Instance createNacosInstance(NacosServiceInstance serviceInstance) {
        Instance instance = new Instance();
        instance.setIp(serviceInstance.getIp());
        instance.setPort(serviceInstance.getPort());
        instance.setClusterName(serviceInstance.getClusterName());
        instance.setWeight(serviceInstance.getWeight());
        instance.setHealthy(serviceInstance.isHealthy());
        instance.setEnabled(serviceInstance.isEnabled());
        instance.setEphemeral(serviceInstance.isEphemeral());
        instance.setMetadata(serviceInstance.getMetadata());
        return instance;
    }

    /**
     * Get the NamingService instance.
     *
     * @return The NamingService instance
     */
    public NamingService getNamingService() {
        return namingService;
    }
}
