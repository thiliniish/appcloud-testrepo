package org.wso2.carbon.cloud.deployment.monitor.utils.dto;

import java.util.Date;

/**
 * FailureSummary bean
 */
public class FailureSummary {

    private String server;
    private String taskName;
    private int startID;
    private int endID;
    private Date date;
    private long startTime;
    private long endTime;
    private long downTime;

    public FailureSummary(String server, String taskName, int startID, Date date, long startTime) {
        this.server = server;
        this.taskName = taskName;
        this.startID = startID;
        this.date = new Date(date.getTime());
        this.startTime = startTime;
    }

    public String getServer() {
        return server;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getStartID() {
        return startID;
    }

    public int getEndID() {
        return endID;
    }

    public void setEndID(int endID) {
        this.endID = endID;
    }

    public Date getDate() {
        return new Date(this.date.getTime());
    }

    public long getDownTime() {
        return downTime;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
