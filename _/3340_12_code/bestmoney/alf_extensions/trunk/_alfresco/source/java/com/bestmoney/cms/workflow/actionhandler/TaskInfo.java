package com.bestmoney.cms.workflow.actionhandler;

import java.util.Date;

/**
 * Holds task information
 *
 * @author martin.bergljung@opsera.com
 */
public class TaskInfo implements Comparable<TaskInfo> {
    String campaignId;
    String product;
    String jobType;
    String phase;   // e.g. Brief Definition
    String name;    // e.g  SO03-Sign-off Phase (Material Brief)
    String outcome; // e.g. approved or rejected
    String owner;   // e.g. approver3
    Date createdDate;
    Date dueDate;
    Integer priority;

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof TaskInfo) {
            TaskInfo ti = (TaskInfo) other;
            if (name.compareTo(ti.name) == 0)
                return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(TaskInfo other) {
        return createdDate.compareTo(other.createdDate);
    }
}


