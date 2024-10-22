<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:loadBundle basename="alfresco.module.com_bestmoney_module_cms.ui.webclient" var="bestmoney_msg" />

<h:outputText value="#{msg.no_tasks}" rendered="#{empty WorkflowBean.tasksCompleted}"/>

<a:richList id="tasks-completed-list" viewMode="details" value="#{WorkflowBean.tasksCompleted}" var="r"
            styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
            altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
            initialSortColumn="bpm:completionDate" initialSortDescending="true"
            rendered="#{not empty WorkflowBean.tasksCompleted}" refreshOnBind="true">

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
            <a:actionLink id="col1-act1" value="#{r['bpm:description']}"
                          image="/images/icons/completed_workflow_task.gif" showLink="false"
                          actionListener="#{WorkflowBean.setupTaskDialog}" action="dialog:viewCompletedTask">
                <f:param name="id" value="#{r.id}"/>
                <f:param name="type" value="#{r.type}"/>
            </a:actionLink>
        </f:facet>
        <a:actionLink id="col1-act2" value="#{r['bpm:description']}" actionListener="#{WorkflowBean.setupTaskDialog}"
                      action="dialog:viewCompletedTask">
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

    <%-- Completed date column --%>
    <a:column id="col5" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col5-sort" label="#{msg.completed_on}" value="bpm:completionDate" styleClass="header"/>
        </f:facet>
        <h:outputText id="col5-txt" value="#{r['bpm:completionDate']}">
            <a:convertXMLDate type="both" pattern="#{msg.date_pattern}"/>
        </h:outputText>
    </a:column>

    <%-- Outcome column --%>
    <a:column id="col6" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col6-sort" label="#{msg.outcome}" value="outcome" styleClass="header"/>
        </f:facet>
        <h:outputText id="col6-txt" value="#{r.outcome}"/>
    </a:column>

    <%-- Actions column --%>
    <a:column id="col7" actions="true" style="padding:2px;text-align:left">
        <f:facet name="header">
            <h:outputText id="col7-txt" value="#{msg.actions}"/>
        </f:facet>
        <r:actions id="col7-actions" value="dashlet_completed_actions" context="#{r}" showLink="false"
                   styleClass="inlineAction"/>
    </a:column>

    <a:dataPager styleClass="pager"/>
</a:richList>
