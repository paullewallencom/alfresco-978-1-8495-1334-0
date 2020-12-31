package com.bestmoney.cms.workflow.bean;

import com.bestmoney.cms.model.BmContentModel;
import com.bestmoney.cms.util.AppConstants;
import com.bestmoney.cms.util.AppUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.TransientMapNode;
import org.alfresco.web.bean.workflow.WorkflowBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.event.ActionEvent;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.*;
import java.util.*;

/**
 * Bean to get all assigned tasks for all workflows in progress
 * and bean to get all workflow instances.
 * It uses JDBC and a SQL Join to do this, so it is much faster
 * then using code like in <code>CustomWorkflowBean</code>.
 * <p/>
 * This bean is used by the "All Assigned Tasks Dashlet",
 * and "All Job Workflows Dashlet".
 *
 * @author martin.bergljung@opsera.com
 */
public class CustomWorkflowBean extends WorkflowBean {
    private static final Log logger = LogFactory.getLog(CustomWorkflowBean.class);

    public static final String GET_ALL_ACTIVE_TASKS_SQL =
            "SELECT  (SELECT DISTINCT STRINGVALUE_ FROM JBPM_VARIABLEINSTANCE WHERE TI.PROCINST_ = PROCESSINSTANCE_ AND NAME_='bmc_campaignId') AS CAMPAIGN_ID," +
                    "(SELECT DISTINCT STRINGVALUE_ FROM JBPM_VARIABLEINSTANCE WHERE TI.PROCINST_ = PROCESSINSTANCE_ AND NAME_='bmc_jobType') AS JOB_TYPE," +
                    "PI.ID_ AS PROC_ID," +
                    "TI.ID_ AS TASK_ID," +
                    "TI.NAME_ AS TASK_NAME," +
                    "TI.DESCRIPTION_ AS TASK_DESCRIPTION," +
                    "TI.ACTORID_ AS OWNER," +
                    "TI.CREATE_ AS CREATED_DATE," +
                    "TI.DUEDATE_ AS DUE_DATE " +
                    "FROM JBPM_TASKINSTANCE TI, JBPM_PROCESSINSTANCE PI, JBPM_PROCESSDEFINITION PD " +
                    "WHERE TI.ISOPEN_ = 1 " +
                    "AND TI.PROCINST_ = PI.ID_ " +
                    "AND PI.PROCESSDEFINITION_ = PD.ID_ " +
                    "AND PD.NAME_ IN ('bmw:signoffProcess', 'bmw:workProcess', 'bmw:studioProcess','bmw:jobProcess') " +
                    "ORDER BY CREATED_DATE DESC";

    public static final String GET_ALL_JOB_WORKFLOWS_SQL =
            "SELECT DISTINCT PI.ID_ AS PROCINST_ID, VI.STRINGVALUE_ AS CAMPAIGN_ID, PI.VERSION_ AS WORKFLOW_DEF_VER, PI.START_ AS CREATED,PI.END_ AS ENDED " +
                    "FROM JBPM_PROCESSINSTANCE PI, JBPM_PROCESSDEFINITION PD, JBPM_VARIABLEINSTANCE VI " +
                    "WHERE PI.PROCESSDEFINITION_=PD.ID_ AND PD.NAME_='bmw:jobProcess' AND PI.ID_=VI.PROCESSINSTANCE_ " +
                    "AND VI.NAME_='bmc_campaignId' " +
                    "ORDER BY PI.START_, VI.STRINGVALUE_,PI.VERSION_";

    public static final String GET_CURRENT_SUBPROCESS_ID_SQL =
            "SELECT SUBPROCESSINSTANCE_ FROM JBPM_TOKEN WHERE PROCESSINSTANCE_=$1 AND SUBPROCESSINSTANCE_ IS NOT NULL";
    public static final String UPDATE_CURRENT_SUBPROCESS_ID_SQL =
            "UPDATE JBPM_TOKEN SET SUBPROCESSINSTANCE_=NULL WHERE PROCESSINSTANCE_=$1";
    public static final String GET_PROCESS_VARIABLE_SQL =
            "SELECT LONGVALUE_,STRINGVALUE_ FROM JBPM_VARIABLEINSTANCE WHERE PROCESSINSTANCE_=$1 AND NAME_='$2'";
    public static final String UPDATE_STRING_PROCESS_VARIABLE_SQL =
            "UPDATE JBPM_VARIABLEINSTANCE SET STRINGVALUE_='$3' WHERE PROCESSINSTANCE_=$1 AND NAME_='$2'";

    public static final String PROCESS_OWNER_VAR_NAME = "initiator";
    public static final String SUB_PROCESS_OWNER_VAR_NAME = "jobOwner";
    public static final String WORKFLOW_ID_POST_PARAM_NAME = "id";
    public static final String CAMPAIGN_ID_POST_PARAM_NAME = "campaignId";
    public static final String WORKFLOW_DEF_VER_POST_PARAM_NAME = "workflowDefinitionVersion";

    private AuthenticationService m_authenticationService;
    private AuthorityService m_authorityService;
    private SearchService m_searchService;
    private DataSource m_datasource;

    private enum DASHLET_VIEW {
        LISTING, DELETING, CHANGING_OWNER
    }

    private DASHLET_VIEW m_activeDashletView = DASHLET_VIEW.LISTING;
    private String m_currentlySpecifiedNewOwnerUserName;
    private JobWorkflowInstance m_currentlySelectedWorkflowInstance;

    /**
     * Spring DI ------------------------------------------------------------------------------------------------
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        m_authenticationService = authenticationService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        m_authorityService = authorityService;
    }

    public void setSearchService(SearchService searchService) {
        m_searchService = searchService;
    }

    public void setDataSource(DataSource ds) {
        m_datasource = ds;
    }

    /**
     * UI Handlers ----------------------------------------------------------------------------------------------
     */

    /**
     * Used by "bestmoney-all-assigned-tasks-dashlet.jsp" and
     * "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public boolean isAdminUser() {
        // Get authorities for current user
        Set<String> authorities = getAuthoritiesForCurrentUser();

        // Check if one of user's authorities has admin rights for Dashlet
        boolean marketingAdminUser = false;
        for (String authority : authorities) {
            if (StringUtils.equalsIgnoreCase(authority, AppConstants.SYSTEM_ADMIN_GROUP_NAME) ||
                    StringUtils.equalsIgnoreCase(authority, AppConstants.SYSTEM_ADMIN_ROLE_NAME) ||
                    StringUtils.equalsIgnoreCase(authority, AppConstants.MARKETING_ADMIN_GROUP_NAME)) {
                marketingAdminUser = true;
                break;
            }
        }

        return marketingAdminUser;
    }

    /**
     * Used by "bestmoney-all-assigned-tasks-dashlet.jsp"
     */
    public boolean isMarketingUser() {
        // Get authorities for current user
        Set<String> authorities = getAuthoritiesForCurrentUser();

        // Check if one of user's authorities has marketing rights for Dashlet
        boolean marketingUser = false;
        for (String authority : authorities) {
            if (StringUtils.equalsIgnoreCase(authority, AppConstants.SYSTEM_ADMIN_GROUP_NAME) ||
                    StringUtils.equalsIgnoreCase(authority, AppConstants.SYSTEM_ADMIN_ROLE_NAME) ||
                    StringUtils.equalsIgnoreCase(authority, AppConstants.MARKETING_ADMIN_GROUP_NAME) ||
                    StringUtils.equalsIgnoreCase(authority, AppConstants.MARKETING_USERS_GROUP_NAME)) {
                marketingUser = true;
                break;
            }
        }

        return marketingUser;
    }

    /**
     * Used by "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public boolean isListingView() {
        return m_activeDashletView == DASHLET_VIEW.LISTING;
    }

    /**
     * Used by "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public boolean isDeletingView() {
        return m_activeDashletView == DASHLET_VIEW.DELETING;
    }

    /**
     * Used by "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public boolean isChangingOwnerView() {
        return m_activeDashletView == DASHLET_VIEW.CHANGING_OWNER;
    }

    /**
     * Called by the a:richList in "bestmoney-all-assigned-tasks-dashlet.jsp"
     */
    public List<Node> getAllAssignedTasks() {
        return selectTasks(GET_ALL_ACTIVE_TASKS_SQL);
    }

    /**
     * Called by the a:richList in "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public List<JobWorkflowInstance> getAllJobWorkflows() {
        return selectWorkflows(GET_ALL_JOB_WORKFLOWS_SQL);
    }

    /**
     * Action handler called when the "delete" link is clicked in "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public void selectDeleteOperation(ActionEvent event) {
        m_activeDashletView = DASHLET_VIEW.DELETING;
        setCurrentWorkflowInstanceData(event);
    }

    /**
     * Action handler called when the "change_owner" link is clicked in "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public void selectChangeOwnerOperation(ActionEvent event) {
        m_activeDashletView = DASHLET_VIEW.CHANGING_OWNER;
        setCurrentWorkflowInstanceData(event);
    }

    /**
     * Action handler called when the "Delete" button is clicked in "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public void deleteWorkflow(ActionEvent event) {
        deleteJobWorkflowIncludingSubWorkflows();
        m_activeDashletView = DASHLET_VIEW.LISTING;
    }

    /**
     * Action handler called when the "Change Owner" button is clicked in "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public void changeWorkflowOwner(ActionEvent event) {
        changeOwnerForJobWorkflowIncludingSubWorkflows();
        m_activeDashletView = DASHLET_VIEW.LISTING;
    }

    /**
     * Used by "Go Back to list" link in "bestmoney-all-job-workflows-dashlet.jsp"
     */
    public void goBackToList(ActionEvent event) {
        m_activeDashletView = DASHLET_VIEW.LISTING;
    }

    public void setNewOwner(String newOwnerUserName) {
        if (newOwnerUserName != null) {
            newOwnerUserName = newOwnerUserName.trim();
        }

        m_currentlySpecifiedNewOwnerUserName = newOwnerUserName;
    }

    public String getNewOwner() {
        return m_currentlySpecifiedNewOwnerUserName;
    }

    public void setCurrentOwner(String s) {
        // Not used
    }

    public String getCurrentOwner() {
        String currentOwner = "not available";

        if (m_currentlySelectedWorkflowInstance != null) {
            currentOwner = getOwnerUserName(
                    getStringProcVariable(m_currentlySelectedWorkflowInstance.id, PROCESS_OWNER_VAR_NAME));
        }

        return currentOwner;
    }

    public void setCurrentWorkflowId(String newOwnerUserName) {
        // Not used, set when you click links in the list
    }

    public String getCurrentWorkflowId() {
        return m_currentlySelectedWorkflowInstance.id;
    }

    public void setCurrentCampaignId(String newOwnerUserName) {
        // Not used, set when you click links in the list
    }

    public String getCurrentCampaignId() {
        return m_currentlySelectedWorkflowInstance.campaignId;
    }

    public void setCurrentWorkflowDefinitionVersion(String newOwnerUserName) {
        // Not used, set when you click links in the list
    }

    public String getCurrentWorkflowDefinitionVersion() {
        return m_currentlySelectedWorkflowInstance.workflowDefinitionVersion;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// Private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setCurrentWorkflowInstanceData(ActionEvent event) {
        m_currentlySelectedWorkflowInstance = new JobWorkflowInstance();
        m_currentlySelectedWorkflowInstance.id = AppUtils.getParamValue(event, WORKFLOW_ID_POST_PARAM_NAME);
        m_currentlySelectedWorkflowInstance.campaignId = AppUtils.getParamValue(event, CAMPAIGN_ID_POST_PARAM_NAME);
        m_currentlySelectedWorkflowInstance.workflowDefinitionVersion = AppUtils.getParamValue(event, WORKFLOW_DEF_VER_POST_PARAM_NAME);
    }

    /**
     * Get the authorities for current user (i.e. what groups do the user belong to)
     *
     * @return a set of authorities
     */
    private Set<String> getAuthoritiesForCurrentUser() {
        final String currentUserName = m_authenticationService.getCurrentUserName();
        AuthenticationUtil.RunAsWork<Set<String>> getAuthoritiesForUser =
                new AuthenticationUtil.RunAsWork<Set<String>>() {
                    public Set<String> doWork() throws Exception {
                        return m_authorityService.getAuthoritiesForUser(currentUserName);
                    }
                };

        Set<String> authorities = AuthenticationUtil.runAs(
                getAuthoritiesForUser, AuthenticationUtil.SYSTEM_USER_NAME);

        return authorities;
    }

    private List<Node> selectTasks(String sql) {
        List<Node> result = new ArrayList<Node>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = m_datasource.getConnection();
            ps = conn.prepareStatement(sql.toString());

            // Execute the SELECT Statement
            rs = ps.executeQuery();

            // Loop through all data rows that matched passed in criteria and create Node objects ...
            while (rs.next()) {
                // Create a new Workflow Task instance and populate with ResultSet row
                WorkflowTask task = new WorkflowTask();
                task.id = AppConstants.JBPM_ID_PREFIX + rs.getString("TASK_ID");
                task.name = rs.getString("TASK_NAME");
                task.description = rs.getString("TASK_DESCRIPTION");

                HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, rs.getLong("PROC_ID"));
                properties.put(BmContentModel.PROP_CAMPAIGN_ID, rs.getString("CAMPAIGN_ID"));
                properties.put(BmContentModel.PROP_JOB_TYPE, rs.getString("JOB_TYPE"));
                properties.put(WorkflowModel.PROP_TASK_ID, task.id);
                properties.put(WorkflowModel.PROP_DESCRIPTION, task.description);
                properties.put(WorkflowModel.PROP_DUE_DATE, AppUtils.getJavaUtilDate(rs.getDate("DUE_DATE")));
                java.util.Date createdDate = new java.util.Date(rs.getTimestamp("CREATED_DATE").getTime());
                properties.put(ContentModel.PROP_CREATED, createdDate);
                properties.put(ContentModel.PROP_OWNER, rs.getString("OWNER"));
                task.properties = properties;

                // Add new transfer object to list
                result.add(createNode(task));
            }
        } catch (SQLException e) {
            logger.error("Error executing:" + sql, e);
        } finally {
            AppUtils.close(rs);
            AppUtils.close(ps);
            AppUtils.close(conn);
        }

        return result;
    }

    private TransientMapNode createNode(WorkflowTask task) {
        // Set the type of the task
        // Get the task name after namespace
        // in a name such as bmw:BD01_CreateMaterialBriefTask
        QName taskTypeName = QName.createQName(BmContentModel.WORKFLOW_NAMESPACE_URI, task.name.substring(4));

        // Create the basic transient node
        TransientMapNode node = new TransientMapNode(taskTypeName, task.title, task.properties);

        // Add properties for the other useful metadata
        node.getProperties().put(ContentModel.PROP_NAME.toString(), task.title);
        node.getProperties().put("type", node.getType().toString());
        node.getProperties().put("id", task.id);

        return node;
    }

    private List<JobWorkflowInstance> selectWorkflows(String sql) {
        List<JobWorkflowInstance> result = new ArrayList<JobWorkflowInstance>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = m_datasource.getConnection();
            ps = conn.prepareStatement(sql.toString());

            // Execute the SELECT Statement
            rs = ps.executeQuery();

            // Loop through all data rows that matched passed in criteria and create Job Workflow objects ...
            while (rs.next()) {
                // Create a new Job Workflow instance and populate with ResultSet row
                JobWorkflowInstance workflowInstance = new JobWorkflowInstance();
                workflowInstance.id = rs.getString("PROCINST_ID");
                workflowInstance.campaignId = rs.getString("CAMPAIGN_ID");
                workflowInstance.workflowDefinitionVersion = rs.getString("WORKFLOW_DEF_VER");
                workflowInstance.startDate = AppUtils.getJavaUtilDate(rs.getTimestamp("CREATED"));
                workflowInstance.endDate = AppUtils.getJavaUtilDate(rs.getTimestamp("ENDED"));

                // Add new job workflow object to list
                result.add(workflowInstance);
            }
        } catch (SQLException e) {
            logger.error("Error executing:" + sql, e);
        } finally {
            AppUtils.close(rs);
            AppUtils.close(ps);
            AppUtils.close(conn);
        }

        return result;
    }

    private void deleteJobWorkflowIncludingSubWorkflows() {
        if (m_currentlySelectedWorkflowInstance == null) {
            logger.warn("Cannot delete workflow, no Job workflow instance has been selected");
            return;
        }

        logger.info("User " + m_authenticationService.getCurrentUserName() +
                " is deleting Job workflow with campaign id: " +
                m_currentlySelectedWorkflowInstance.campaignId + " (" +
                m_currentlySelectedWorkflowInstance.id + "), including all related sub-workflows");


        // The order is important to not get violation of foreign key constraint
        // Then Studio Work sub-processes
        deleteSubWorkflow(AppConstants.STUDIO_CONCEPT_PROC_ID_PROCESS_VARIABLE_NAME);
        deleteSubWorkflow(AppConstants.STUDIO_COPY_PROC_ID_PROCESS_VARIABLE_NAME);
        deleteSubWorkflow(AppConstants.STUDIO_DESIGN_PROC_ID_PROCESS_VARIABLE_NAME);
        // Then Studio sub-process
        deleteSubWorkflow(AppConstants.STUDIO_PROC_ID_PROCESS_VARIABLE_NAME);
        // Then the rest of the Job sub-processes
        deleteSubWorkflow(AppConstants.SIGN_OFF_BRIEF_PROC_ID_PROCESS_VARIABLE_NAME);
        deleteSubWorkflow(AppConstants.SIGN_OFF_PRODUCTION_PROC_ID_PROCESS_VARIABLE_NAME);

        Long currentSubProcId = getCurrentSubWorkflow(m_currentlySelectedWorkflowInstance.id);
        if (currentSubProcId != null) {
            Long currentSubSubProcId = getCurrentSubWorkflow(currentSubProcId.toString());
            if (currentSubSubProcId != null) {
                // For example a Work Process
                logger.info("Deleting currently executing sub-sub-workflow id (" + currentSubSubProcId +
                        ") for sub-workflow instance id (" + currentSubProcId + ")");
                removeSubProcIdFK(currentSubProcId);
                getWorkflowService().deleteWorkflow(AppConstants.JBPM_ID_PREFIX + currentSubSubProcId);
            }
            // For example a Studio Process
            logger.info("Deleting currently executing sub-workflow id (" + currentSubProcId +
                    ") for Job workflow instance id (" + m_currentlySelectedWorkflowInstance.id + ")");
            removeSubProcIdFK(Long.parseLong(m_currentlySelectedWorkflowInstance.id));
            getWorkflowService().deleteWorkflow(AppConstants.JBPM_ID_PREFIX + currentSubProcId);
        }
        logger.info("Deleting Job workflow instance id (" + m_currentlySelectedWorkflowInstance.id + ") " +
                "with campaign id (" + m_currentlySelectedWorkflowInstance.campaignId + ")");
        getWorkflowService().deleteWorkflow(AppConstants.JBPM_ID_PREFIX + m_currentlySelectedWorkflowInstance.id);
    }

    private void deleteSubWorkflow(String subProcvarName) {
        if (m_currentlySelectedWorkflowInstance == null) {
            logger.warn("Cannot delete sub workflow instance " + subProcvarName +
                    " no parent Job workflow instance has been selected");
            return;
        }

        Long subProcId = getLongProcVariable(m_currentlySelectedWorkflowInstance.id, subProcvarName);
        if (subProcId > 0) {
            Long currentSubProcId = getCurrentSubWorkflow(subProcId.toString());
            if (currentSubProcId != null) {
                logger.info("Deleting currently executing sub-workflow id (" + currentSubProcId +
                        ") for " + subProcvarName + " with workflow id (" + subProcId + ")");
                removeSubProcIdFK(subProcId);
                getWorkflowService().deleteWorkflow(AppConstants.JBPM_ID_PREFIX + currentSubProcId);
            }

            WorkflowInstance subProcInstance = null;
            try {
                subProcInstance = getWorkflowService().getWorkflowById(AppConstants.JBPM_ID_PREFIX + subProcId);
            } catch (WorkflowException we) {
                subProcInstance = null;
            }
            if (subProcInstance != null) {
                getWorkflowService().deleteWorkflow(AppConstants.JBPM_ID_PREFIX + subProcId);
                logger.info("Deleted sub workflow instance " + subProcvarName + " with id: " + subProcId);
            } else {
                logger.warn("Could not delete sub workflow " + subProcvarName + " with id: " +
                        subProcId + ", instance was not found");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Did not delete, Job workflow id (" + m_currentlySelectedWorkflowInstance.id +
                        ") is not using sub process type: " + subProcvarName);
            }
        }
    }

    private void changeOwnerForJobWorkflowIncludingSubWorkflows() {
        if (m_currentlySelectedWorkflowInstance == null) {
            logger.warn("Cannot change owner for workflow, no Job workflow instance has been selected");
            return;
        }
        if (StringUtils.isBlank(m_currentlySpecifiedNewOwnerUserName)) {
            logger.warn("Cannot change owner for workflow, no new owner username has been specified");
            return;
        }

        // Lookup current job owner
        String currentOwner = getOwnerUserName(
                getStringProcVariable(m_currentlySelectedWorkflowInstance.id, PROCESS_OWNER_VAR_NAME));

        // Lookup person node reference for new job owner
        NodeRef newOwnerNodeRef = getPersonNodeRef(m_currentlySpecifiedNewOwnerUserName);

        // Update owner
        Long currentSubProcId = getCurrentSubWorkflow(m_currentlySelectedWorkflowInstance.id);
        if (currentSubProcId != null) {
            Long currentSubSubProcId = getCurrentSubWorkflow(currentSubProcId.toString());
            if (currentSubSubProcId != null) {
                // For example a Work Process
                logger.info("Updating owner for currently executing sub-sub-workflow id (" + currentSubSubProcId +
                        ") for sub-workflow instance id (" + currentSubProcId + ")");
                updateStringProcVariable(currentSubSubProcId, SUB_PROCESS_OWNER_VAR_NAME, newOwnerNodeRef.toString());
            }
            // For example Studio process
            logger.info("Updating owner for currently executing sub-workflow id (" + currentSubProcId +
                    ")for Job workflow id (" + m_currentlySelectedWorkflowInstance.id + ")");
            updateStringProcVariable(currentSubProcId, SUB_PROCESS_OWNER_VAR_NAME, newOwnerNodeRef.toString());
        }

        updateStringProcVariable(m_currentlySelectedWorkflowInstance.getIdAsLong(),
                PROCESS_OWNER_VAR_NAME, newOwnerNodeRef.toString());
        logger.info("User " + m_authenticationService.getCurrentUserName() +
                " changed owner for Job workflow id " + m_currentlySelectedWorkflowInstance.id + " from " +
                currentOwner + " to " + m_currentlySpecifiedNewOwnerUserName);

        // Change owner for all the sub-processes
        changeOwnerSubWorkflow(AppConstants.STUDIO_CONCEPT_PROC_ID_PROCESS_VARIABLE_NAME, newOwnerNodeRef);
        changeOwnerSubWorkflow(AppConstants.STUDIO_COPY_PROC_ID_PROCESS_VARIABLE_NAME, newOwnerNodeRef);
        changeOwnerSubWorkflow(AppConstants.STUDIO_DESIGN_PROC_ID_PROCESS_VARIABLE_NAME, newOwnerNodeRef);
        changeOwnerSubWorkflow(AppConstants.STUDIO_PROC_ID_PROCESS_VARIABLE_NAME, newOwnerNodeRef);
        changeOwnerSubWorkflow(AppConstants.SIGN_OFF_BRIEF_PROC_ID_PROCESS_VARIABLE_NAME, newOwnerNodeRef);
        changeOwnerSubWorkflow(AppConstants.SIGN_OFF_PRODUCTION_PROC_ID_PROCESS_VARIABLE_NAME, newOwnerNodeRef);
    }

    private void changeOwnerSubWorkflow(String subProcvarName, NodeRef newOwnerNodeRef) {
        if (m_currentlySelectedWorkflowInstance == null) {
            logger.warn("Cannot change owner for sub-workflow, no parent Job workflow instance has been selected");
            return;
        }
        if (StringUtils.isBlank(m_currentlySpecifiedNewOwnerUserName)) {
            logger.warn("Cannot change owner for sub-workflow, no new owner username has been specified");
            return;
        }

        Long subProcId = getLongProcVariable(m_currentlySelectedWorkflowInstance.id, subProcvarName);
        if (subProcId > 0) {
            Long currentSubProcId = getCurrentSubWorkflow(subProcId.toString());
            if (currentSubProcId != null) {
                logger.info("Changed owner for currently executing sub-workflow id (" + currentSubProcId + ") for " +
                        subProcvarName + " with workflow id (" + subProcId + ")");
                updateStringProcVariable(currentSubProcId, SUB_PROCESS_OWNER_VAR_NAME, newOwnerNodeRef.toString());
            }

            updateStringProcVariable(subProcId, SUB_PROCESS_OWNER_VAR_NAME, newOwnerNodeRef.toString());
            logger.info("Changed Owner to " + m_currentlySpecifiedNewOwnerUserName + " for sub workflow instance " +
                    subProcvarName + " with id: " + subProcId);
        } else {
            logger.warn("Could not change owner to " + m_currentlySpecifiedNewOwnerUserName + " for sub workflow " +
                    subProcvarName + ", instance was not found for this job");
        }
    }

    private String getStringProcVariable(String procId, String varName) {
        Object stringVarValue = getProcVariable(procId, varName, true);
        if (stringVarValue != null && StringUtils.isNotBlank(stringVarValue.toString())) {
            return stringVarValue.toString();
        } else {
            return "";
        }
    }

    private Long getLongProcVariable(String procId, String varName) {
        Object longVarValue = getProcVariable(procId, varName, false);
        if (longVarValue != null) {
            return (Long) longVarValue;
        } else {
            return 0L;
        }
    }

    /**
     * Get the value of passed in process instance variable
     *
     * @param procId  the process instance id
     * @param varName the process variable name
     * @return the value as either String or Long
     */
    private Object getProcVariable(String procId, String varName, boolean fetchString) {
        String stringResult = "";
        Long longResult = 0L;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = GET_PROCESS_VARIABLE_SQL.replace("$1", procId);
        sql = sql.replace("$2", varName);

        try {
            conn = m_datasource.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            // Just get the first row
            while (rs.next()) {
                if (fetchString) {
                    stringResult = rs.getString("STRINGVALUE_");
                } else {
                    longResult = rs.getLong("LONGVALUE_");
                }
                break;
            }
        } catch (SQLException e) {
            logger.error("Error executing:" + sql, e);
        } finally {
            AppUtils.close(rs);
            AppUtils.close(ps);
            AppUtils.close(conn);
        }

        if (fetchString) {
            return stringResult;
        } else {
            return longResult;
        }
    }

    /**
     * Get current sub-process id if process with passed in ID is currently executing in a sub-process.
     * This information is contained in the SUBPROCESSINSTANCE_ column of the JBPM_TOKEN table.
     * <p/>
     * A foreign key exist to enforce this: FK_TOKEN_SUBPI
     *
     * @param procId process id to check for sub-process execution
     * @return ID of currently executing sub-process,
     *         or null if there is no current sub-process executing for passed in process
     */
    private Long getCurrentSubWorkflow(String procId) {
        Long longResult = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = GET_CURRENT_SUBPROCESS_ID_SQL.replace("$1", procId);

        try {
            conn = m_datasource.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            // Just get the first row
            while (rs.next()) {
                longResult = rs.getLong("SUBPROCESSINSTANCE_");
                break;
            }
        } catch (SQLException e) {
            logger.error("Error executing:" + sql, e);
        } finally {
            AppUtils.close(rs);
            AppUtils.close(ps);
            AppUtils.close(conn);
        }

        if (longResult == null || longResult == 0) {
            return null;
        } else {
            return longResult;
        }
    }

    /**
     * Update String process instance variable
     *
     * @param procId   the process instance id
     * @param varName  the process variable name
     * @param varValue the process variable value
     */
    private void updateStringProcVariable(Long procId, String varName, String varValue) {
        Connection conn = null;
        Statement stmt = null;
        String sql = UPDATE_STRING_PROCESS_VARIABLE_SQL.replace("$1", procId.toString());
        sql = sql.replace("$2", varName);
        sql = sql.replace("$3", varValue);

        try {
            conn = m_datasource.getConnection();
            stmt = conn.createStatement();
            int rowCount = stmt.executeUpdate(sql);
            if (rowCount < 1) {
                logger.warn("Did not update 1 row (" + rowCount + ") when executing: " + sql);
            }
            if (rowCount > 1) {
                logger.warn("Updated more than 1 row (" + rowCount + ") when executing: " + sql);
            }

            // Commit update transaction as auto-commit seems to be set to false
            conn.commit();
        } catch (SQLException e) {
            logger.error("Error when executing: " + sql, e);
        } finally {
            AppUtils.close(stmt);
            AppUtils.close(conn);
        }
    }

    /**
     * Remove current sub process id foreign key from JBPM_TOKEN for passed in process id
     *
     * @param procId the process instance id
     */
    private void removeSubProcIdFK(Long procId) {
        Connection conn = null;
        Statement stmt = null;
        String sql = UPDATE_CURRENT_SUBPROCESS_ID_SQL.replace("$1", procId.toString());

        try {
            conn = m_datasource.getConnection();
            stmt = conn.createStatement();
            int rowCount = stmt.executeUpdate(sql);
            if (rowCount < 1) {
                logger.warn("Did not update 1 row (" + rowCount + ") when executing: " + sql);
            }
            if (rowCount > 1) {
                logger.warn("Updated more than 1 row (" + rowCount + ") when executing: " + sql);
            }

            // Commit update transaction as auto-commit seems to be set to false
            conn.commit();
        } catch (SQLException e) {
            logger.error("Error when executing: " + sql, e);
        } finally {
            AppUtils.close(stmt);
            AppUtils.close(conn);
        }
    }

    /**
     * Returns the username for a person node reference
     *
     * @param personNodeRef the Person node reference
     * @return a username, or null if passed in noderef is null, empty or blank
     */
    private String getOwnerUserName(String personNodeRef) {
        if (StringUtils.isBlank(personNodeRef)) {
            return null;
        }
        return (String) getNodeService().getProperty(new NodeRef(personNodeRef), ContentModel.PROP_USERNAME);
    }

    /**
     * Get the Person node for passed in username
     *
     * @param username the username to use when looking for Person node
     * @return a person node reference
     */
    private NodeRef getPersonNodeRef(String username) {
        StringBuilder luceneQuery = new StringBuilder(100);
        luceneQuery.append("+PATH:\"/sys:system/sys:people/*\" ");
        luceneQuery.append("+TYPE:\"");
        luceneQuery.append(ContentModel.TYPE_PERSON.toString());
        luceneQuery.append("\" ");
        luceneQuery.append(AppUtils.getPropertyQuery(ContentModel.PROP_USERNAME, username));

        // Search for the job folder
        List<NodeRef> nodes = AppUtils.search(
                m_searchService, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, luceneQuery);

        if (nodes.isEmpty()) {
            String msg = "Could not find Person node for username: " + username;
            logger.error(msg);
            return null;
        }

        if (nodes.size() > 1) {
            logger.warn("Found more than one Person node for username: " + username);
        }

        return nodes.get(0);
    }
}

