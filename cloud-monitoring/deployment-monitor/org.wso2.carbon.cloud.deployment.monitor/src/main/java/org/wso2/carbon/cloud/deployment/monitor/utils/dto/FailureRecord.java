package org.wso2.carbon.cloud.deployment.monitor.utils.dto;

/**
 * FailureRecord bean
 */
public class FailureRecord {

    private String taskName;
    private String server;
    private String error;
    private long timestamp;

    public FailureRecord(String taskName, String server, String error, long timestamp) {
        this.taskName = taskName;
        this.server = server;
        this.error = error;
        this.timestamp = timestamp;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getServer() {
        return server;
    }


    public String getError() {
        return error;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
