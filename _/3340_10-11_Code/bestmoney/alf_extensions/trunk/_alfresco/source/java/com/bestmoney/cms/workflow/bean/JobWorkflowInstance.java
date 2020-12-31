package com.bestmoney.cms.workflow.bean;

import org.alfresco.service.cmr.workflow.WorkflowInstance;

import java.util.Date;

/**
 * JavaBean for Job Workflow instance data
 *
 * @author martin.bergljung@opsera.com
 */
public class JobWorkflowInstance extends WorkflowInstance {
    public String campaignId;
    public String workflowDefinitionVersion;
    public String creator;

    public JobWorkflowInstance() {
        super();
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getWorkflowDefinitionVersion() {
        return workflowDefinitionVersion;
    }

    public String getId() {
        return id;
    }

    public Long getIdAsLong() {
        return Long.parseLong(id);
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getCreator() {
        return creator;
    }
}