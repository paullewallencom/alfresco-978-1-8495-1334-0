package com.bestmoney.cms.model;

import org.alfresco.service.namespace.QName;

/**
 * Content types for the Best Money Marketing content and workflow model.
 *
 * @author martin.bergljung@opsera.com
 */
public class BmContentModel {
    // ==== Model ===============================================================================

    public static final String CONTENT_NAMESPACE_URI = "http://www.bestmoney.com/model/content/1.0";
    public static final String WORKFLOW_NAMESPACE_URI = "http://www.bestmoney.com/model/workflow/1.0";

    // ==== Types ===============================================================================



    // ==== Aspects =============================================================================

    // ==== Constraints =======================================================================

    // ==== Properties for aspects and types ===================================================

    /**
     * Process properties
     */
    public static final QName PROP_CAMPAIGN_ID = QName.createQName(CONTENT_NAMESPACE_URI, "campaignId");
    public static final QName PROP_PRODUCT = QName.createQName(CONTENT_NAMESPACE_URI, "product");
    public static final QName PROP_JOB_TYPE = QName.createQName(CONTENT_NAMESPACE_URI, "jobType");
    public static final QName PROP_JOB_STATUS = QName.createQName(WORKFLOW_NAMESPACE_URI, "jobStatus");

    /**
     * Workflow definitions
     */
    public static final QName JOB_PROCESS_DEFINITION = QName.createQName(WORKFLOW_NAMESPACE_URI, "jobProcess");
    public static final QName SIGNOFF_SUBPROCESS_DEFINITION = QName.createQName(WORKFLOW_NAMESPACE_URI, "signoffProcess");
    public static final QName WORK_SUBPROCESS_DEFINITION = QName.createQName(WORKFLOW_NAMESPACE_URI, "workProcess");
    public static final QName STUDIO_SUBPROCESS_DEFINITION = QName.createQName(WORKFLOW_NAMESPACE_URI, "studioProcess");
}
