package org.wso2.carbon.cloud.deployment.monitor.utils.dto;

/**
 * SuccessRecord bean
 */
public class SuccessRecord {

    private String taskName;
    private String server;
    private long timestamp;

    public SuccessRecord(String taskName, String server, long timestamp) {
        this.taskName = taskName;
        this.server = server;
        this.timestamp = timestamp;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getServer() {
        return server;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
