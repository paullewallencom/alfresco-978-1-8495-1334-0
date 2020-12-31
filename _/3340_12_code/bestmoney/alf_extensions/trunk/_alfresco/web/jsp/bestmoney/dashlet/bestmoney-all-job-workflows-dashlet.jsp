<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:loadBundle basename="alfresco.module.com_bestmoney_module_cms.ui.webclient" var="bestmoney_msg" />

<h:outputText value="#{bestmoney_msg.not_admin_user}" rendered="#{CustomWorkflowBean.adminUser == false}"/>

<h:outputText value="#{bestmoney_msg.no_workflow_instances}"
              rendered="#{empty CustomWorkflowBean.allJobWorkflows && CustomWorkflowBean.adminUser}"/>

<h:panelGrid columns="2" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rendered="#{CustomWorkflowBean.deletingView && CustomWorkflowBean.adminUser}">
    <h:outputText id="delete_txt_heading" value="*** Delete Workflow ***"/><h:outputText value=""/>
    <h:outputText id="delete_1_txt" value="#{bestmoney_msg.procInstanceId}: "/><h:outputText id="delete_1b_txt" value="#{CustomWorkflowBean.currentWorkflowId}"/>
    <h:outputText id="delete_2_txt" value="#{bestmoney_msg.campaignId}: "/><h:outputText id="delete_2b_txt" value="#{CustomWorkflowBean.currentCampaignId}"/>
    <h:outputText id="delete_3_txt" value="#{bestmoney_msg.workflowDefinitionVersion}: "/><h:outputText id="delete_3b_txt" value="#{CustomWorkflowBean.currentWorkflowDefinitionVersion}"/>
    <h:outputText id="section_devider" value="******************************************************"/><h:outputText value=""/>
    <h:commandButton id="delete_action_button1"value="<= Go Back To List" actionListener="#{CustomWorkflowBean.goBackToList}" />
    <h:commandButton id="delete_action_button2"value="Delete" actionListener="#{CustomWorkflowBean.deleteWorkflow}" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rendered="#{CustomWorkflowBean.changingOwnerView && CustomWorkflowBean.adminUser}">
    <h:outputText id="changeOwner_txt_heading" value="*** Change Workflow Owner (Initiator) ***"/><h:outputText value=""/>
    <h:outputText id="changeOwner_1_txt" value="#{bestmoney_msg.procInstanceId}: "/><h:outputText id="changeOwner_1b_txt" value="#{CustomWorkflowBean.currentWorkflowId}"/>
    <h:outputText id="changeOwner_2_txt" value="#{bestmoney_msg.campaignId}: "/><h:outputText id="changeOwner_2b_txt" value="#{CustomWorkflowBean.currentCampaignId}"/>
    <h:outputText id="changeOwner_3_txt" value="#{bestmoney_msg.workflowDefinitionVersion}: "/><h:outputText id="changeOwner_3b_txt" value="#{CustomWorkflowBean.currentWorkflowDefinitionVersion}"/>
    <h:outputText id="changeOwner_4_txt" value="Current Owner (initiator):"/><h:outputText id="current_owner_input_txt" value="#{CustomWorkflowBean.currentOwner}" />
    <h:outputText id="changeOwner_5_txt" value="New Owner (initiator):"/><h:inputText id="new_owner_input_txt" value="#{CustomWorkflowBean.newOwner}" style="width:150px"/>
    <h:outputText id="section_devider2" value="******************************************************"/><h:outputText value=""/>
    <h:commandButton id="changeOwner_action_button1"value="<= Go Back To List" actionListener="#{CustomWorkflowBean.goBackToList}" />
    <h:commandButton id="changeOwner_action_button2"value="Change Owner" actionListener="#{CustomWorkflowBean.changeWorkflowOwner}" />
</h:panelGrid>

<a:richList id="all-job-workflows-list"
            viewMode="details"
            value="#{CustomWorkflowBean.allJobWorkflows}"
            var="workflowInstance"
            styleClass="recordSet"
            headerStyleClass="recordSetHeader"
            rowStyleClass="recordSetRow"
            altRowStyleClass="recordSetRowAlt"
            width="100%"
            pageSize="25"
            initialSortColumn="startDate"
            initialSortDescending="true"
            rendered="#{not empty CustomWorkflowBean.allJobWorkflows && CustomWorkflowBean.listingView && CustomWorkflowBean.adminUser}"
            refreshOnBind="true">

    <%-- Workflow Instance ID --%>
    <a:column id="col_1" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col_1_sort" label="#{bestmoney_msg.procInstanceId}" value="id" mode="case-insensitive"
                        styleClass="header"/>
        </f:facet>
        <h:outputText id="col_1_txt" value="#{workflowInstance.id}"/>
    </a:column>

    <%-- Workflow Instance - Workflow Definition Version --%>
    <a:column id="col_2" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col_2_sort" label="#{bestmoney_msg.workflowDefinitionVersion}"
                        value="workflowDefinitionVersion" mode="case-insensitive" styleClass="header"/>
        </f:facet>
        <h:outputText id="col_2_txt" value="#{workflowInstance.workflowDefinitionVersion}"/>
    </a:column>

    <%-- Workflow Instance - Campaign ID --%>
    <a:column id="col_3" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col_3_sort" label="#{bestmoney_msg.campaignId}" value="campaignId" mode="case-insensitive"
                        styleClass="header"/>
        </f:facet>
        <h:outputText id="col_3_txt" value="#{workflowInstance.campaignId}"/>
    </a:column>

    <%-- Workflow Instance - Start Date --%>
    <a:column id="col_4" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col_4_sort" label="#{bestmoney_msg.startDate}" value="startDate" styleClass="header"/>
        </f:facet>
        <h:outputText id="col_4_txt" value="#{workflowInstance.startDate}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}"/>
        </h:outputText>
    </a:column>

    <%-- Workflow Instance - End Date --%>
    <a:column id="col_5" style="padding:2px;text-align:left">
        <f:facet name="header">
            <a:sortLink id="col_5_sort" label="#{bestmoney_msg.endDate}" value="endDate" styleClass="header"/>
        </f:facet>
        <h:outputText id="col_5_txt" value="#{workflowInstance.endDate}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}"/>
        </h:outputText>
    </a:column>

    <%-- Actions column --%>
    <a:column id="col_6" actions="true" style="padding:2px;text-align:left">
        <f:facet name="header">
            <h:outputText id="col_6_txt" value="#{msg.actions}"/>
        </f:facet>
        <a:actionLink value="delete" actionListener="#{CustomWorkflowBean.selectDeleteOperation}">
            <f:param name="id" value="#{workflowInstance.id}"/>
            <f:param name="campaignId" value="#{workflowInstance.campaignId}"/>
            <f:param name="workflowDefinitionVersion" value="#{workflowInstance.workflowDefinitionVersion}"/>
            <f:param name="startDate" value="#{workflowInstance.startDate}"/>
            <f:param name="endDate" value="#{workflowInstance.endDate}"/>
        </a:actionLink>
    </a:column>
    <a:column id="col_7" actions="true" style="padding:2px;text-align:left">
        <a:actionLink value="change_owner" actionListener="#{CustomWorkflowBean.selectChangeOwnerOperation}">
            <f:param name="id" value="#{workflowInstance.id}"/>
            <f:param name="campaignId" value="#{workflowInstance.campaignId}"/>
            <f:param name="workflowDefinitionVersion" value="#{workflowInstance.workflowDefinitionVersion}"/>
            <f:param name="startDate" value="#{workflowInstance.startDate}"/>
            <f:param name="endDate" value="#{workflowInstance.endDate}"/>
        </a:actionLink>
    </a:column>

    <a:dataPager styleClass="pager"/>
</a:richList>
