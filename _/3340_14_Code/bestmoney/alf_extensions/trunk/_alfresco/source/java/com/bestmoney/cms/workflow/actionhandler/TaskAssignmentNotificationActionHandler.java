package com.bestmoney.cms.workflow.actionhandler;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

import java.util.Date;
import java.util.Set;

/**
 * Action handler to send Task Assignment Notification Email
 *
 * @author martin.bergljung@opsera.com
 */
public class TaskAssignmentNotificationActionHandler extends JBPMSpringActionHandler {
    private static final Logger logger = Logger.getLogger(TaskAssignmentNotificationActionHandler.class);

    private static final String FROM_ADDRESS = "alfresco@bestmoney.com";
    private static final String SUBJECT = "Task Assignment: ";
    private static final String RECIPIENT_EMAIL_PROCESS_VARIABLE = "taskNotificationRecipientEmail";
    private static final String RECIPIENT_GROUP_PROCESS_VARIABLE = "taskNotificationRecipientGroup";

    private ActionService m_actionService;
    private AuthorityService m_authorityService;
    private PersonService m_personService;
    private NodeService m_nodeService;

    @Override
    protected void initialiseHandler(BeanFactory factory) {
        m_actionService = (ActionService) factory.getBean("actionService");
        m_authorityService = (AuthorityService) factory.getBean("authorityService");
        m_personService = (PersonService) factory.getBean("personService");
        m_nodeService = (NodeService) factory.getBean("nodeService");
    }

    public void execute(ExecutionContext context) throws Exception {
        String recipientEmail = (String) context.getVariable(RECIPIENT_EMAIL_PROCESS_VARIABLE);
        String recipientGroup = (String) context.getVariable(RECIPIENT_GROUP_PROCESS_VARIABLE);

        String task = context.getToken().getNode().getName();

        if (logger.isDebugEnabled()) {
            logger.debug("Sending notification email for task " + task + " (address = " + recipientEmail + ", group = "
                + recipientGroup + ")");
        }

        if (StringUtils.isBlank(recipientEmail) && StringUtils.isBlank(recipientGroup)) {
            logger.fatal("Variable " + RECIPIENT_EMAIL_PROCESS_VARIABLE + " and variable " +
                    RECIPIENT_GROUP_PROCESS_VARIABLE + " are both null, cannot send " +
                    "task assignment notification email for task node: " + task);
            return;
        }

        Object var = context.getContextInstance().getVariable("bmc_campaignId");
        String campaignId = "Undefined";
        if (var == null) {
            logger.warn("Variable bmc_campaignId is null, ok for createJob task (" + task + ")");
        } else {
            campaignId = (String) var;
        }

        var = context.getContextInstance().getVariable("bmc_jobStatus");
        String jobStatus = "";
        if (var == null) {
            logger.warn("Variable bmc_jobStatus is null (" + task + ")");
        } else {
            jobStatus = (String) var;
        }

        var = context.getContextInstance().getVariable("bpm_dueDate");
        Date dueDate = null;
        if (var == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Variable bpm_dueDate is null (" + task + ")");
            }
        } else {
            dueDate = (Date) var;
        }

        var = context.getContextInstance().getVariable("bpm_description");
        String taskDescription = null;
        if (var == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Variable bpm_description is null (" + task + ")");
            }
            // Fall back on task name
            taskDescription = task;
        } else {
            taskDescription = (String) var;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("You have been assigned:");
        sb.append("\r\n\r\n");
        sb.append("Campaign: " + campaignId);
        sb.append("\r\n");
        //sb.append("Task    : " + context.getToken().getNode().getName());
        sb.append("Task    : " + taskDescription);
        sb.append("\r\n");
        sb.append("Phase   : " + jobStatus);
        sb.append("\r\n");
        sb.append("Due Date: " + dueDate);
        sb.append("\r\n\r\n");
        sb.append("Best Money Marketing - Alfresco Workflows");

        String subject = SUBJECT + taskDescription + " (" + campaignId + ")";
        if (StringUtils.isNotBlank(recipientEmail)) {
            sendEmail(subject, recipientEmail, sb.toString());
        }

        if (StringUtils.isNotBlank(recipientGroup)) {
            // We got a group, so get all users in this group.
            // Searching deep in sub-groups also.
            Set<String> usernames = m_authorityService.getContainedAuthorities(
                    AuthorityType.USER, recipientGroup, false);
            for (String username : usernames) {
                NodeRef personRef = m_personService.getPerson(username);
                String email = (String) m_nodeService.getProperty(personRef, ContentModel.PROP_EMAIL);
                sendEmail(subject, email, sb.toString());
            }
        }
    }

    private void sendEmail(String subject, String toAddress, String bodyText) {
        if (StringUtils.isBlank(toAddress)) {
            logger.warn("Not sending task assignment email " + subject + " as email address was empty for user.");
            return;
        }

        Action mailAction = m_actionService.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subject);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, toAddress);
        mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, FROM_ADDRESS);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, bodyText);
        m_actionService.executeAction(mailAction, null);
    }
}

