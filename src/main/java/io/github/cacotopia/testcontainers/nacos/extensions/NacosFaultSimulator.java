package io.github.cacotopia.testcontainers.nacos.extensions;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Frame;
import io.github.cacotopia.testcontainers.nacos.NacosContainer;
import io.github.cacotopia.testcontainers.nacos.NacosCluster;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;

import java.io.Closeable;
import java.io.IOException;

/**
 * Nacos fault simulator for testing fault tolerance scenarios.
 * Supports simulating node failures, network partitions, and latency.
 */
public class NacosFaultSimulator {

    private final DockerClient dockerClient;

    public NacosFaultSimulator() {
        this.dockerClient = DockerClientFactory.instance().client();
    }

    /**
     * Simulate a node failure by stopping a Nacos container.
     *
     * @param container The Nacos container to stop
     */
    public void simulateNodeFailure(NacosContainer container) {
        container.stop();
    }

    /**
     * Simulate a node recovery by starting a stopped Nacos container.
     *
     * @param container The Nacos container to start
     */
    public void simulateNodeRecovery(NacosContainer container) {
        if (!container.isRunning()) {
            container.start();
        }
    }

    /**
     * Simulate network partition by disconnecting a container from the network.
     *
     * @param container The Nacos container to disconnect
     * @param networkName The network name to disconnect from
     */
    public void simulateNetworkPartition(NacosContainer container, String networkName) {
        String containerId = container.getContainerId();
        dockerClient.disconnectFromNetworkCmd()
                .withContainerId(containerId)
                .withNetworkId(networkName)
                .exec();
    }

    /**
     * Simulate network recovery by reconnecting a container to the network.
     *
     * @param container The Nacos container to reconnect
     * @param networkName The network name to reconnect to
     */
    public void simulateNetworkRecovery(NacosContainer container, String networkName) {
        String containerId = container.getContainerId();
        dockerClient.connectToNetworkCmd()
                .withContainerId(containerId)
                .withNetworkId(networkName)
                .exec();
    }

    /**
     * Simulate network latency for a container.
     *
     * @param container The Nacos container to add latency to
     * @param milliseconds The latency in milliseconds
     */
    public void simulateNetworkLatency(NacosContainer container, int milliseconds) {
        String containerId = container.getContainerId();

        // Use tc (traffic control) to add latency
        String[] cmd = {
                "sh", "-c",
                "tc qdisc add dev eth0 root netem delay " + milliseconds + "ms"
        };

        ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ResultCallback<Frame> callback = new ResultCallback<Frame>() {
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(Frame object) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {

            }
        };

        dockerClient.execStartCmd(exec.getId()).exec(callback);
    }

    /**
     * Remove network latency for a container.
     *
     * @param container The Nacos container to remove latency from
     */
    public void removeNetworkLatency(NacosContainer container) {
        String containerId = container.getContainerId();

        // Remove the latency qdisc
        String[] cmd = {
                "sh", "-c",
                "tc qdisc del dev eth0 root"
        };

        ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        ResultCallback<Frame> callback = new ResultCallback<Frame>() {
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(Frame object) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {

            }
        };
        dockerClient.execStartCmd(exec.getId()).exec(callback);
    }

    /**
     * Simulate high CPU usage for a container.
     *
     * @param container The Nacos container to stress
     * @param durationSeconds The duration in seconds
     */
    public void simulateHighCpu(NacosContainer container, int durationSeconds) {
        String containerId = container.getContainerId();

        // Use stress tool to generate CPU load
        String[] cmd = {
                "sh", "-c",
                "stress --cpu 4 --timeout " + durationSeconds + "s"
        };

        ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        ResultCallback<Frame> callback = new ResultCallback<Frame>() {
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(Frame object) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {

            }
        };
        dockerClient.execStartCmd(exec.getId()).exec(callback);
    }

    /**
     * Get the network name for a cluster.
     *
     * @param cluster The Nacos cluster
     * @return The network name
     */
    public String getClusterNetworkName(NacosCluster cluster) {
        // This is a simplified implementation
        // In practice, you would need to extract the network name from the cluster
        return "nacos-cluster-network";
    }
}
