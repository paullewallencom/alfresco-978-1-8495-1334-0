package com.bestmoney.cms.workflow.actionhandler;

import com.bestmoney.cms.model.BmContentModel;
import com.bestmoney.cms.util.AppConstants;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Create a read-only file that lists all tasks
 * for a completed or Job.
 * Called at the end of the Job Process.
 *
 * @author martin.bergljung@opsera.com
 */
public class CreateTaskListForJobActionHandler extends JBPMSpringActionHandler {
    private static final Logger logger = Logger.getLogger(CreateTaskListForJobActionHandler.class);

    private WorkflowService m_workflowService;
    private NodeService m_nodeService;
    private ContentService m_contentService;

    @Override
    protected void initialiseHandler(BeanFactory factory) {
        ServiceRegistry services = (ServiceRegistry) factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        m_workflowService = services.getWorkflowService();
        m_nodeService = services.getNodeService();
        m_contentService = services.getContentService();
    }

    public void execute(ExecutionContext context) throws Exception {
        // Get the workflow instance IDs (parent and any subprocess) that we are creating task summary file for
        String jobProcId = getProcIdVarAsString(context, AppConstants.WORKFLOW_INSTANCE_ID_PROCESS_VARIABLE_NAME);
        String studioProcId = getProcIdVarAsString(context, AppConstants.STUDIO_PROC_ID_PROCESS_VARIABLE_NAME);
        String studioConceptProcId = getProcIdVarAsString(context, AppConstants.STUDIO_CONCEPT_PROC_ID_PROCESS_VARIABLE_NAME);
        String studioCopyProcId = getProcIdVarAsString(context, AppConstants.STUDIO_COPY_PROC_ID_PROCESS_VARIABLE_NAME);
        String studioDesignProcId = getProcIdVarAsString(context, AppConstants.STUDIO_DESIGN_PROC_ID_PROCESS_VARIABLE_NAME);
        String signOffCBProcId = getProcIdVarAsString(context, AppConstants.SIGN_OFF_BRIEF_PROC_ID_PROCESS_VARIABLE_NAME);
        String signOffCPProcId = getProcIdVarAsString(context, AppConstants.SIGN_OFF_PRODUCTION_PROC_ID_PROCESS_VARIABLE_NAME);

        // Create the file by getting all WorkflowTask objects, converting them to TaskInfo objects,
        // and then write them to an Excel file, and finally write the file to a node
        List<WorkflowTask> allTasks = new ArrayList<WorkflowTask>();
        List<WorkflowTask> tasks = getTasksForProcessInstance(jobProcId, true);
        allTasks.addAll(tasks);
        tasks = getTasksForProcessInstance(studioProcId, false);
        allTasks.addAll(tasks);
        tasks = getTasksForProcessInstance(studioConceptProcId, false);
        allTasks.addAll(tasks);
        tasks = getTasksForProcessInstance(studioCopyProcId, false);
        allTasks.addAll(tasks);
        tasks = getTasksForProcessInstance(studioDesignProcId, false);
        allTasks.addAll(tasks);
        tasks = getTasksForProcessInstance(signOffCBProcId, false);
        allTasks.addAll(tasks);
        tasks = getTasksForProcessInstance(signOffCPProcId, false);
        allTasks.addAll(tasks);

        List<TaskInfo> taskInfoList = convertWorkflowTaskList2TaskInfoList(allTasks);
        Collections.sort(taskInfoList);
        if (!taskInfoList.isEmpty()) {
            HSSFWorkbook wb = createExcelFile(taskInfoList);
            String filename = getFilename(taskInfoList);
            createNodeWithContent(getCurrentFolderNodeRef(context, filename), wb, filename);
        }
    }

    /**
     * Convert a list of WorkflowTask objects to a list of TaskInfo objects
     *
     * @param tasks the list of WorkflowTask objects
     * @return a list of TaskInfo objects
     */
    private List<TaskInfo> convertWorkflowTaskList2TaskInfoList(List<WorkflowTask> tasks) {
        List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();

        // Loop thru each workflow task and create task info object to hold task data
        for (WorkflowTask task : tasks) {
            taskInfoList.add(createTaskInfo(task));
        }

        return taskInfoList;
    }

    /**
     * Extract all properties from the WorkflowTask object and create a TaskInfo object
     *
     * @param workflowTask the workflow task with all the properties
     * @return a task information object
     */
    private TaskInfo createTaskInfo(WorkflowTask workflowTask) {
        TaskInfo task = new TaskInfo();
        // The following properties are the same for all tasks in Job Process
        task.campaignId = (String) workflowTask.properties.get(BmContentModel.PROP_CAMPAIGN_ID);
        task.product = (String) workflowTask.properties.get(BmContentModel.PROP_PRODUCT);
        task.jobType = (String) workflowTask.properties.get(BmContentModel.PROP_JOB_TYPE);

        // Task specific properties
        task.phase = (String) workflowTask.properties.get(BmContentModel.PROP_JOB_STATUS);
        task.name = (String) workflowTask.properties.get(WorkflowModel.PROP_DESCRIPTION);
        task.outcome = (String) workflowTask.properties.get(WorkflowModel.PROP_OUTCOME);
        task.owner = (String) workflowTask.properties.get(ContentModel.PROP_OWNER);
        task.createdDate = (Date) workflowTask.properties.get(ContentModel.PROP_CREATED);
        task.dueDate = (Date) workflowTask.properties.get(WorkflowModel.PROP_DUE_DATE);
        task.priority = (Integer) workflowTask.properties.get(WorkflowModel.PROP_PRIORITY);

        return task;
    }

    /**
     * Get the name of the Excel file with pattern "[campaignId] - [job type].xls",
     * such as for example "TestCampaign1 - Envelope.xsl".
     *
     * @param taskInfoList the list of tasks
     * @return the new filename
     */
    private String getFilename(List<TaskInfo> taskInfoList) {
        String filename = "AllTasks.xls";

        for (TaskInfo taskInfo:taskInfoList)  {
            if (StringUtils.isNotBlank(taskInfo.campaignId) && StringUtils.isNotBlank(taskInfo.jobType)) {
                filename = taskInfo.campaignId + "-" + taskInfo.jobType + ".xls";
                break;
            }
        }

        return filename;
    }

    /**
     * Create an Excel spreadsheet with all passed in tasks.
     *
     * @param tasks the tasks to put in the Excel sheet
     * @return the Excel spreadsheet with all the tasks
     */
    public static HSSFWorkbook createExcelFile(List<TaskInfo> tasks) {
        HSSFWorkbook wb = new HSSFWorkbook();

        // Create a new sheet to hold task list
        HSSFSheet sheet = wb.createSheet("All Tasks");
        sheet.setColumnWidth((short) 0, (short) 7000);
        sheet.setColumnWidth((short) 1, (short) 12000);
        sheet.setColumnWidth((short) 2, (short) 4000);
        sheet.setColumnWidth((short) 3, (short) 4000);
        sheet.setColumnWidth((short) 4, (short) 4000);
        sheet.setColumnWidth((short) 5, (short) 4000);
        sheet.setColumnWidth((short) 6, (short) 2000);

        // Create header rows
        createHeaderRow(wb, sheet);

        // Create data rows for the tasks
        createDataRows(wb, sheet, tasks);

        return wb;
    }

    /**
     * Stores an Excel file in Alfresco.
     * Specifically stores the file in the Job's Materials folder.
     *
     * @param materialsFolderNodeRef the Materials folder where the Excel file node will be created
     * @param wb                     the Excel workbook with the spreadsheet to be stored
     * @param filename               the filename for the new Excel spreadsheet node
     */
    private void createNodeWithContent(NodeRef materialsFolderNodeRef, HSSFWorkbook wb, String filename) {
        // Create a new Alfresco node properties Map, just filename for the moment
        Map<QName, Serializable> nodeProperties = new HashMap<QName, Serializable>(9);
        nodeProperties.put(ContentModel.PROP_NAME, filename);

        // Create the new Excel spreadsheet node
        ChildAssociationRef assocRef = m_nodeService.createNode(
                materialsFolderNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, QName.createValidLocalName(filename)),
                ContentModel.TYPE_CONTENT,
                nodeProperties);
        NodeRef messageNodeRef = assocRef.getChildRef();

        // Write the Excel spreadsheet as content for this new node
        ContentWriter writer = m_contentService.getWriter(messageNodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_EXCEL);

        try {
            writeExcelSheet(writer, wb); // closes output stream
        } catch (Exception e) {
            logger.error("Could not write Excel sheet (" + filename + ") to Materials folder");
            e.printStackTrace();
        }
    }


    /**
     * Writes the content of the pased in Excel sheet to passed in content writer
     * and closes the output stream when done.
     *
     * @param writer an Alfresco repository content writer
     * @param wb     an Excel spreadsheet
     * @throws IOException
     * @throws javax.mail.MessagingException
     */
    public void writeExcelSheet(ContentWriter writer, HSSFWorkbook wb) throws IOException, MessagingException {
        OutputStream os = null;

        try {
            os = writer.getContentOutputStream();
            wb.write(os);
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }

    /**
     * Get current folder associated with current execution context.
     *
     * @param context    the execution context for the process
     * @param loggingInfo for logging
     * @return the alfresco node reference for the folder
     */
    private NodeRef getCurrentFolderNodeRef(ExecutionContext context, String loggingInfo) {
        Object res = context.getContextInstance().getVariable("bpm_package");
        if (res == null) {
            logger.fatal("Variable bpm_package is null");
            return null;
        }

        JBPMNode packageNode = (JBPMNode) res;
        NodeRef packageNodeRef = packageNode.getNodeRef();
        if (packageNodeRef == null) {
            logger.fatal("NodeRef for variable bpm_package is null");
            return null;
        }

        NodeRef currentFolderNodeRef = null;
        List<ChildAssociationRef> producedMaterialAndOtherDocs = m_nodeService.getChildAssocs(packageNodeRef);
        if (!producedMaterialAndOtherDocs.isEmpty()) {
            // Get the node ref for the first doc we can find and then get the parent folder node ref for it
            currentFolderNodeRef = m_nodeService.getPrimaryParent(
                    producedMaterialAndOtherDocs.get(0).getChildRef()).getParentRef();
            if (currentFolderNodeRef == null) {
                logger.fatal("Could not find current folder for bpm_package content for campaign " + loggingInfo);
            }
        } else {
            logger.fatal("Could not find any documents in current bpm_package, " +
                    "cannot get current folder for campaign " + loggingInfo);

        }

        return currentFolderNodeRef;
    }

    /**
     * Create the header row in the Excel file
     *
     * @param wb
     * @param sheet
     */
    public static void createHeaderRow(HSSFWorkbook wb, HSSFSheet sheet) {
        // Create a header row
        HSSFRow row = sheet.createRow((short) 0);

        // Create the header font
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        // Fonts are set into a style so create a new one to use.
        HSSFCellStyle style = wb.createCellStyle();
        style.setFont(font);

        // Create the header cells
        // TODO: fix alignment, it does not work even if specified as follows
        createCell(row, (short) 0, "Phase", style, HSSFCellStyle.ALIGN_LEFT);
        createCell(row, (short) 1, "Task", style, HSSFCellStyle.ALIGN_LEFT);
        createCell(row, (short) 2, "Decision", style, HSSFCellStyle.ALIGN_LEFT);
        createCell(row, (short) 3, "Owner", style, HSSFCellStyle.ALIGN_LEFT);
        createCell(row, (short) 4, "Created", style, HSSFCellStyle.ALIGN_LEFT);
        createCell(row, (short) 5, "Due", style, HSSFCellStyle.ALIGN_RIGHT);
        createCell(row, (short) 6, "Priority", style, HSSFCellStyle.ALIGN_CENTER);
    }

    public static void createDataRows(HSSFWorkbook wb, HSSFSheet sheet, List<TaskInfo> tasks) {
        HSSFCellStyle style = wb.createCellStyle();
        short rowNum = 1;

        for (TaskInfo task : tasks) {
            HSSFRow row = sheet.createRow(rowNum++);

            createCell(row, (short) 0, task.phase, style, HSSFCellStyle.ALIGN_FILL);
            createCell(row, (short) 1, task.name, style, HSSFCellStyle.ALIGN_LEFT);
            createCell(row, (short) 2, task.outcome, style, HSSFCellStyle.ALIGN_LEFT);
            createCell(row, (short) 3, task.owner, style, HSSFCellStyle.ALIGN_LEFT);
            createCell(row, (short) 4, task.createdDate, style, HSSFCellStyle.ALIGN_LEFT);
            createCell(row, (short) 5, task.dueDate, style, HSSFCellStyle.ALIGN_RIGHT);
            createCell(row, (short) 6, task.priority, style, HSSFCellStyle.ALIGN_CENTER);
        }
    }

    /**
     * Creates a cell and aligns it a certain way.
     *
     * @param row    the row to create the cell in
     * @param column the column number to create the cell in
     * @param align  the alignment for the cell.
     */
    public static void createCell(HSSFRow row, short column, Object value,
                                  HSSFCellStyle style, short align) {
        HSSFCell cell = row.createCell(column);
        style.setAlignment(align);
        cell.setCellStyle(style);

        if (value instanceof String) {
            cell.setCellValue(new HSSFRichTextString((String) value));
        } else if (value instanceof Integer) {
            cell.setCellValue(new HSSFRichTextString(((Integer) value).toString()));
        } else if (value instanceof Long) {
            cell.setCellValue(new HSSFRichTextString(((Long) value).toString()));
        } else if (value instanceof Date) {
            style.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
            cell.setCellValue((Date) value);
        }
    }

    /**
     * Get all tasks for completed process with passed in ID.
     *
     * @param processInstanceId the process instance identifier
     * @param processIsActive true if search for a process that is active (i.e. not completed)
     * @return a list of tasks for passed in instance
     */
    private List<WorkflowTask> getTasksForProcessInstance(String processInstanceId, boolean processIsActive) {

        if (StringUtils.isBlank(processInstanceId)) {
            // Nothing to get, process instance ID was null or empty (process was probably not executed)
            return new ArrayList<WorkflowTask>();
        }

        // Setup query for active tasks in progress
        WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setActive(processIsActive);
        query.setTaskState(WorkflowTaskState.COMPLETED);
        query.setProcessId(processInstanceId);
        query.setOrderBy(new WorkflowTaskQuery.OrderBy[]{
                WorkflowTaskQuery.OrderBy.TaskCreated_Desc,
                WorkflowTaskQuery.OrderBy.TaskActor_Asc});

        // Do the query
        List<WorkflowTask> tasks = m_workflowService.queryTasks(query);

        return tasks;
    }

    /**
     * Get the value of the passed in process ID variable as a String.
     *
     * @param context workflow instance context
     * @param varName the name of the process ID variable
     * @return the String value or null
     */
    private String getProcIdVarAsString(ExecutionContext context, String varName) {
        Object variable = context.getContextInstance().getVariable(varName);
        if (variable == null) {
            logger.error("Variable " + varName + " is null");
            return null;
        }

        String procId = variable.toString();
        if (!procId.startsWith(AppConstants.JBPM_ID_PREFIX)) {
            procId = AppConstants.JBPM_ID_PREFIX + procId;
        }

        return procId;
    }
}




