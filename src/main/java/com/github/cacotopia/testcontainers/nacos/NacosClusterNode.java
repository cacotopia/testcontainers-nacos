package com.github.cacotopia.testcontainers.nacos;

/**
 * Nacos 集群节点配置
 */
public class NacosClusterNode {
    
    private String nodeId;
    private String host;
    private int port;
    private String ip;
    
    /**
     * 创建集群节点
     * @param nodeId 节点标识
     * @param host 主机地址
     * @param port 端口
     */
    public NacosClusterNode(String nodeId, String host, int port) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.ip = host;
    }
    
    /**
     * 创建集群节点（使用 IP）
     * @param nodeId 节点标识
     * @param ip IP 地址
     * @param port 端口
     */
    public static NacosClusterNode withIp(String nodeId, String ip, int port) {
        NacosClusterNode node = new NacosClusterNode(nodeId, ip, port);
        node.ip = ip;
        return node;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getIp() {
        return ip;
    }
    
    /**
     * 获取集群地址格式 ip:port
     */
    public String getClusterAddress() {
        return ip + ":" + port;
    }
    
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
