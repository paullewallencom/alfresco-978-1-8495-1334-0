package com.bestmoney.cms.workflow.actionhandler;

import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.model.ContentModel;
import org.springframework.beans.factory.BeanFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.def.Transition;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * Sign-off notification email for external reviewer, from which he or she can
 * sign-off produced materials by clicking on links in email.
 *
 * @author martin.bergljung@opsera.com
 */
public class ExternalSignOffNotification extends JBPMSpringActionHandler {
    private static final Logger logger = Logger.getLogger(ExternalSignOffNotification.class);

    private static final String FROM_ADDRESS = "alfresco@bestmoney.com";
    private static final String SUBJECT = "Workflow task requires action";
    private static final String RECIP_PROCESS_VARIABLE = "notificationRecipient";

    private ActionService actionService;
    private NodeService nodeService;

    @Override
    protected void initialiseHandler(BeanFactory factory) {
        actionService = (ActionService) factory.getBean("actionService");
        nodeService = (NodeService) factory.getBean("nodeService");
    }

    public void execute(ExecutionContext context) throws Exception {
        String recipient = (String) context.getVariable(RECIP_PROCESS_VARIABLE);

        logger.debug("recipient email = " + recipient);

        Object res = context.getContextInstance().getVariable("bpm_package");
        if (res == null) {
            logger.fatal("Variable bpm_package is null");
            return;
        }
        JBPMNode packageNode = (JBPMNode) res;

        NodeRef packageNodeRef = packageNode.getNodeRef();
        if (packageNodeRef == null) {
            logger.fatal("NodeRef for variable bpm_package is null");
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("You have been assigned to a task named ");
        sb.append(context.getToken().getNode().getName());
        sb.append(". To sign-off click on the link below. \r\n\r\n");

        sb.append("Documents that are waiting sign-off: \r\n\r\n");
        List<ChildAssociationRef> childs = nodeService.getChildAssocs(packageNodeRef);
        for (ChildAssociationRef child : childs) {
            String name = (String) nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME);
            logger.debug("Package document = " + name + " with nodeRef = " + child.getChildRef());
            sb.append(name + "\r\n");
        }
        sb.append("\r\n");

        // Example URL that is put in email for approve transition:
        // http://localhost:8080/alfresco/service/bestmoney/marketing/bpm/signoff?id=jbpm$34-@externalReview2&action=approve&guest=true
        List<Transition> transitionList = context.getNode().getLeavingTransitions();
        for (Transition transition : transitionList) {
            sb.append(transition.getName());
            sb.append("\r\n");
            sb.append("http://localhost:8080/alfresco/service/bestmoney/marketing/bpm/signoff?id=jbpm$");
            sb.append(context.getProcessInstance().getId());
            sb.append("-@externalReview2"); // from transition name
            sb.append("&action=");
            sb.append(transition.getName());
            sb.append("&guest=true");
            sb.append("\r\n\r\n");
        }

        logger.debug("Email text = " + sb.toString());

        Action mailAction = actionService.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, SUBJECT);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, recipient);
        mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, FROM_ADDRESS);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, sb.toString());
        actionService.executeAction(mailAction, null);
    }
}


