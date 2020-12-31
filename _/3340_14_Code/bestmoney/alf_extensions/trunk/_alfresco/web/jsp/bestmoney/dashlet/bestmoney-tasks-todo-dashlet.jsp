<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:loadBundle basename="alfresco.module.com_bestmoney_module_cms.ui.webclient" var="bestmoney_msg" />

<h:outputText value="#{msg.no_tasks}" rendered="#{empty WorkflowBean.tasksToDo}"/>

<a:richList id="bestmoney-tasks-todo-list" viewMode="details" value="#{WorkflowBean.tasksToDo}" var="r"
            styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
            altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
            initialSortColumn="created" initialSortDescending="true"
            rendered="#{not empty WorkflowBean.tasksToDo}" refreshOnBind="true">

    <%-- Best Money Campaign id --%>
    <a:column id="col00" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col00-sort" label="#{bestmoney_msg.campaignId}" value="bmc:campaignId"
                        mode="case-insensitive" styleClass="header"/>
        </f:facet>
        <h:outputText id="col00-txt" value="#{r['bmc:campaignId']}"/>
    </a:column>

    <%-- Best Money Job Type --%>
    <a:column id="col0" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col0-sort" label="#{bestmoney_msg.jobType}" value="bmc:jobType"
                        mode="case-insensitive" styleClass="header"/>
        </f:facet>
        <h:outputText id="col0-txt" value="#{r['bmc:jobType']}"/>
    </a:column>

    <%-- Primary column for details view mode --%>
    <a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col1-sort" label="#{msg.description}" value="bpm:description" mode="case-insensitive"
                        styleClass="header"/>
        </f:facet>
        <f:facet name="small-icon">
            <a:actionLink id="col1-act1" value="#{r['bpm:description']}" image="/images/icons/workflow_task.gif"
                          showLink="false"
                          actionListener="#{WorkflowBean.setupTaskDialog}" action="dialog:manageTask">
                <f:param name="id" value="#{r.id}"/>
                <f:param name="type" value="#{r.type}"/>
            </a:actionLink>
        </f:facet>
        <a:actionLink id="col1-act2" value="#{r['bpm:description']}" actionListener="#{WorkflowBean.setupTaskDialog}"
                      action="dialog:manageTask">
            <f:param name="id" value="#{r.id}"/>
            <f:param name="type" value="#{r.type}"/>
        </a:actionLink>
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

    <%-- Status column --%>
    <a:column id="col6" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col6-sort" label="#{msg.status}" value="bpm:status" styleClass="header"/>
        </f:facet>
        <h:outputText id="col6-txt" value="#{r['bpm:status']}"/>
    </a:column>

    <%-- Priority column --%>
    <a:column id="col7" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col7-sort" label="#{msg.priority}" value="bpm:priority" styleClass="header"/>
        </f:facet>
        <h:outputText id="col7-txt" value="#{r['bpm:priority']}"/>
    </a:column>

    <%-- Actions column --%>
    <a:column id="col8" actions="true" style="padding:2px;text-align:left">
        <f:facet name="header">
            <h:outputText id="col8-txt" value="#{msg.actions}"/>
        </f:facet>
        <r:actions id="col8-actions" value="dashlet_todo_actions" context="#{r}" showLink="false"
                   styleClass="inlineAction"/>
    </a:column>

    <a:dataPager styleClass="pager"/>
</a:richList>
