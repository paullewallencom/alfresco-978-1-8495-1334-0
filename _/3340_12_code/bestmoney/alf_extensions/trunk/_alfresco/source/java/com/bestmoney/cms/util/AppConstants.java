package com.bestmoney.cms.util;

/**
 * Application constants
 *
 * @author martin.bergljung@opsera.com
 */
public interface AppConstants {
    public static final String JBPM_ID_PREFIX = "jbpm$";

    /**
     * Group and Role names
     */
    public static final String SYSTEM_ADMIN_GROUP_NAME = "GROUP_ALFRESCO_ADMINISTRATORS";
    public static final String SYSTEM_ADMIN_ROLE_NAME = "ROLE_ADMINISTRATOR";
    public static final String MARKETING_ADMIN_GROUP_NAME = "GROUP_Marketing-Admins";
    public static final String MARKETING_USERS_GROUP_NAME = "GROUP_Marketing-All Users";

    /**
     * Workflow variable names
     */
    public static final String WORKFLOW_INSTANCE_ID_PROCESS_VARIABLE_NAME = "workflowinstanceid";

    public static final String STUDIO_PROC_ID_PROCESS_VARIABLE_NAME = "studioProcId";
    public static final String STUDIO_CONCEPT_PROC_ID_PROCESS_VARIABLE_NAME = "studioConceptWorkProcId";
    public static final String STUDIO_COPY_PROC_ID_PROCESS_VARIABLE_NAME = "studioCopyWorkProcId";
    public static final String STUDIO_DESIGN_PROC_ID_PROCESS_VARIABLE_NAME = "studioDesignWorkProcId";
    public static final String SIGN_OFF_BRIEF_PROC_ID_PROCESS_VARIABLE_NAME = "briefSignOffProcId";
    public static final String SIGN_OFF_PRODUCTION_PROC_ID_PROCESS_VARIABLE_NAME = "productionSignOffProcId";
}
