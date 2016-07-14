package org.wso2.carbon.cloud.deployment.monitor.utils.dto;

/**
 * LiveStatus bean
 */
public class LiveStatus {

    private String server;
    private String taskName;
    private Status status;

    public enum Status {UP, DOWN, MAINTENANCE, NA}

    public LiveStatus(String server, String taskName, Status status) {
        this.server = server;
        this.taskName = taskName;
        this.status = status;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStatus() {
        return this.status.name();
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
