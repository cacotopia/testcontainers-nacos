package io.github.cacotopia.testcontainers.nacos;

/**
 * Nacos cluster node configuration class.
 * Represents a node in a Nacos cluster with its identifying information and network details.
 */
public class NacosClusterNode {

    /**
     * Node identifier
     */
    private String nodeId;

    /**
     * Host name
     */
    private String host;

    /**
     * Port number
     */
    private int port;

    /**
     * IP address
     */
    private String ip;

    /**
     * Creates a cluster node with the specified node ID, host, and port.
     *
     * @param nodeId The node identifier
     * @param host The host name
     * @param port The port number
     */
    public NacosClusterNode(String nodeId, String host, int port) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.ip = host;
    }

    /**
     * Creates a cluster node with the specified node ID, IP address, and port.
     *
     * @param nodeId The node identifier
     * @param ip The IP address
     * @param port The port number
     * @return A new NacosClusterNode instance
     */
    public static NacosClusterNode withIp(String nodeId, String ip, int port) {
        NacosClusterNode node = new NacosClusterNode(nodeId, ip, port);
        node.ip = ip;
        return node;
    }

    /**
     * Gets the node identifier.
     *
     * @return The node ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Gets the host name.
     *
     * @return The host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port number.
     *
     * @return The port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the IP address.
     *
     * @return The IP address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Gets the cluster address in the format ip:port.
     *
     * @return The cluster address
     */
    public String getClusterAddress() {
        return ip + ":" + port;
    }

    /**
     * Returns a string representation of the NacosClusterNode.
     *
     * @return A string representation of the object
     */
    @Override
    public String toString() {
        return "NacosClusterNode{" +
            "nodeId='" + nodeId + '\'' +
            ", host='" + host + '\'' +
            ", port=" + port +
            ", ip='" + ip + '\'' +
            '}';
    }
}
