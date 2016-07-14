package org.wso2.carbon.cloud.deployment.monitor.utils.dto;

/**
 * LiveStatus bean
 */
public class LiveStatus {

    private String server;
    private String taskName;
    private boolean isUp;

    public LiveStatus(String server, String taskName, boolean isUp) {
        this.server = server;

        this.taskName = taskName;
        this.isUp = isUp;
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

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean up) {
        isUp = up;
    }

}
