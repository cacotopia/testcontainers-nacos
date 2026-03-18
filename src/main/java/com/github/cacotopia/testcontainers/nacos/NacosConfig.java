package com.github.cacotopia.testcontainers.nacos;

/**
 * Nacos 配置项
 */
public class NacosConfig {
    
    private String dataId;
    private String group = "DEFAULT_GROUP";
    private String content;
    private String format = "properties"; // properties, yaml, json, xml
    
    public NacosConfig(String dataId, String content) {
        this.dataId = dataId;
        this.content = content;
    }
    
    public NacosConfig(String dataId, String group, String content) {
        this.dataId = dataId;
        this.group = group;
        this.content = content;
    }
    
    public NacosConfig(String dataId, String group, String content, String format) {
        this.dataId = dataId;
        this.group = group;
        this.content = content;
        this.format = format;
    }
    
    public String getDataId() {
        return dataId;
    }
    
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    /**
     * 获取完整的 DataId（包含格式后缀）
     */
    public String getFullDataId() {
        if (dataId.endsWith("." + format)) {
            return dataId;
        }
        return dataId + "." + format;
    }
    
    @Override
    public String toString() {
        return "NacosConfig{" +
            "dataId='" + dataId + '\'' +
            ", group='" + group + '\'' +
            ", format='" + format + '\'' +
            ", contentLength=" + (content != null ? content.length() : 0) +
            '}';
    }
}
