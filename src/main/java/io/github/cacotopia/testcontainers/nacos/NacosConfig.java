package io.github.cacotopia.testcontainers.nacos;

/**
 * Nacos configuration item class.
 * Represents a configuration entry in Nacos with data ID, group, content, and format.
 */
public class NacosConfig {

    /**
     * Configuration data ID
     */
    private String dataId;

    /**
     * Configuration group (default: DEFAULT_GROUP)
     */
    private String group = "DEFAULT_GROUP";

    /**
     * Configuration content
     */
    private String content;

    /**
     * Configuration format (default: properties)
     * Supported formats: properties, yaml, json, xml
     */
    private String format = "properties";

    /**
     * Creates a new NacosConfig with the specified data ID and content.
     * Uses default group and format.
     *
     * @param dataId The configuration data ID
     * @param content The configuration content
     */
    public NacosConfig(String dataId, String content) {
        this.dataId = dataId;
        this.content = content;
    }

    /**
     * Creates a new NacosConfig with the specified data ID, group, and content.
     * Uses default format.
     *
     * @param dataId The configuration data ID
     * @param group The configuration group
     * @param content The configuration content
     */
    public NacosConfig(String dataId, String group, String content) {
        this.dataId = dataId;
        this.group = group;
        this.content = content;
    }

    /**
     * Creates a new NacosConfig with the specified data ID, group, content, and format.
     *
     * @param dataId The configuration data ID
     * @param group The configuration group
     * @param content The configuration content
     * @param format The configuration format
     */
    public NacosConfig(String dataId, String group, String content, String format) {
        this.dataId = dataId;
        this.group = group;
        this.content = content;
        this.format = format;
    }

    /**
     * Gets the data ID.
     *
     * @return The data ID
     */
    public String getDataId() {
        return dataId;
    }

    /**
     * Sets the data ID.
     *
     * @param dataId The data ID to set
     */
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    /**
     * Gets the group.
     *
     * @return The group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the group.
     *
     * @param group The group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Gets the content.
     *
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content.
     *
     * @param content The content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the format.
     *
     * @return The format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format.
     *
     * @param format The format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the full DataId including the format suffix.
     *
     * @return The full DataId with format suffix
     */
    public String getFullDataId() {
        if (dataId.endsWith("." + format)) {
            return dataId;
        }
        return dataId + "." + format;
    }

    /**
     * Returns a string representation of the NacosConfig.
     *
     * @return A string representation of the object
     */
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
