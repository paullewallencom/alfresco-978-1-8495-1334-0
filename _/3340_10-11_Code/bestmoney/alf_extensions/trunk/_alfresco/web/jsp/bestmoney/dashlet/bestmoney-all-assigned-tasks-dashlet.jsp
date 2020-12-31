<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:loadBundle basename="alfresco.module.com_bestmoney_module_cms.ui.webclient" var="bestmoney_msg" />

<h:outputText value="#{bestmoney_msg.not_marketing_user}" rendered="#{CustomWorkflowBean.marketingUser == false}"/>

<h:outputText value="#{msg.no_tasks}" rendered="#{empty CustomWorkflowBean.allAssignedTasks && CustomWorkflowBean.marketingUser}"/>

<a:richList id="all-assigned-tasks-list" viewMode="details" value="#{CustomWorkflowBean.allAssignedTasks}"
            var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
            altRowStyleClass="recordSetRowAlt" width="100%" pageSize="25"
            initialSortColumn="created" initialSortDescending="true"
            rendered="#{not empty CustomWorkflowBean.allAssignedTasks && CustomWorkflowBean.marketingUser}"
            refreshOnBind="true">

    <%-- Best Money Campaign id --%>
    <a:column id="col00" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col00-sort" label="#{bestmoney_msg.campaignId}" value="bmc:campaignId" mode="case-insensitive" styleClass="header"/>
        </f:facet>
        <h:outputText id="col00-txt" value="#{r['bmc:campaignId']}"/>
    </a:column>

    <%-- Best Money Job Type --%>
    <a:column id="col0" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col0-sort" label="#{bestmoney_msg.jobType}" value="bmc:jobType" mode="case-insensitive" styleClass="header"/>
        </f:facet>
        <h:outputText id="col0-txt" value="#{r['bmc:jobType']}"/>
    </a:column>

    <%-- Primary column for details view mode when Admin user --%>
    <a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left" rendered="#{CustomWorkflowBean.adminUser}">
        <f:facet name="header">
            <a:sortLink id="col1-sort" label="#{msg.description}" value="bpm:description" mode="case-insensitive" styleClass="header"/>
        </f:facet>
        <f:facet name="small-icon">
            <a:actionLink id="col1-act1" value="#{r['bpm:description']}"
                          image="/images/icons/workflow_task.gif"
                          showLink="false"
                          actionListener="#{CustomWorkflowBean.setupTaskDialog}"
                          action="dialog:manageTask">
                <f:param name="id" value="#{r.id}"/>
                <f:param name="type" value="#{r.type}"/>
            </a:actionLink>
        </f:facet>
        <a:actionLink id="col1-act2" value="#{r['bpm:description']}"
                      actionListener="#{CustomWorkflowBean.setupTaskDialog}"
                      action="dialog:manageTask">
            <f:param name="id" value="#{r.id}"/>
            <f:param name="type" value="#{r.type}"/>
        </a:actionLink>
    </a:column>

    <%-- Primary column for details view mode when normal marketing user --%>
    <a:column id="col1a" primary="true" width="200" style="padding:2px;text-align:left" rendered="#{CustomWorkflowBean.adminUser == false}">
        <f:facet name="header">
            <a:sortLink id="col1a-sort" label="#{msg.description}" value="bpm:description" mode="case-insensitive" styleClass="header"/>
        </f:facet>
        <h:outputText id="col1a-txt" value="#{r['bpm:description']}"/>
    </a:column>

    <%-- Created Date column --%>
    <a:column id="col4" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col4-sort" label="#{msg.created}" value="created" styleClass="header"/>
        </f:facet>
        <h:outputText id="col4-txt" value="#{r.created}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}"/>
        </h:outputText>
    </a:column>

    <%-- Due date column --%>
    <a:column id="col5" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col5-sort" label="#{msg.due_date}" value="bpm:dueDate" styleClass="header"/>
        </f:facet>
        <h:outputText id="col5-txt" value="#{r['bpm:dueDate']}">
            <a:convertXMLDate type="both" pattern="#{msg.date_pattern}"/>
        </h:outputText>
    </a:column>

    <%-- Task Owner (Assignee) column --%>
    <a:column id="col6" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col6-sort" label="#{bestmoney_msg.task_owner}" value="cm:owner" styleClass="header"/>
        </f:facet>
        <h:outputText id="col6-txt" value="#{r['cm:owner']}"/>
    </a:column>

    <%-- Actions column --%>
    <a:column id="col8" actions="true" style="padding:2px;text-align:left" rendered="#{CustomWorkflowBean.adminUser}">
        <f:facet name="header">
            <h:outputText id="col8-txt" value="#{msg.actions}"/>
        </f:facet>
        <r:actions id="col8-actions" value="dashlet_todo_actions" context="#{r}" showLink="false" styleClass="inlineAction"/>
    </a:column>

    <a:dataPager styleClass="pager"/>
</a:richList>
