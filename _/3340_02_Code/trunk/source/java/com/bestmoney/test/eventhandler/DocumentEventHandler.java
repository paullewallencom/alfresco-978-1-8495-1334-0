package com.bestmoney.test.eventhandler;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocumentEventHandler {
    private static Log logger = LogFactory.getLog(DocumentEventHandler.class);

    private PolicyComponent m_eventManager;
    private NodeService m_nodeService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        m_eventManager = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        m_nodeService = nodeService;
    }

    public void registerEventHandlers() {
        m_eventManager.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onAddDocument",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        m_eventManager.bindClassBehaviour(
                NodeServicePolicies.OnUpdateNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onUpdateDocument",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        m_eventManager.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onDeleteDocument",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    public void onAddDocument(ChildAssociationRef
            parentChildAssocRef) {
        NodeRef parentFolderRef = parentChildAssocRef.getParentRef();
        NodeRef docRef = parentChildAssocRef.getChildRef();

        // Check if node exists, might be moved, or created and deleted in same transaction.
        if (docRef == null || !m_nodeService.exists(docRef)) {
            // Does not exist, nothing to do
            logger.warn("onAddDocument: A new document was added " +
                    "but removed in same transaction");
            return;
        } else {
            logger.info("onAddDocument: A new document with ref (" +
                    docRef + ") was just created in folder (" + parentFolderRef + ")");
        }
    }

    public void onUpdateDocument(NodeRef docNodeRef) {
        // Check if node exists, might be moved, or created and deleted in same transaction.
        if (docNodeRef == null || !m_nodeService.exists(docNodeRef)) {
            // Does not exist, nothing to do
            logger.warn("onUpdateDocument: A document was updated " +
                    "but removed in same transaction");
            return;
        } else {
            NodeRef parentFolderRef = m_nodeService.getPrimaryParent(docNodeRef).getParentRef();
            logger.info("onUpdateDocument: A document with ref (" + 
                docNodeRef + ") was just updated in folder (" + parentFolderRef + ")");
        }
    }

    public void onDeleteDocument(ChildAssociationRef
            parentChildAssocRef, boolean isNodeArchived) {
        NodeRef parentFolderRef = parentChildAssocRef.getParentRef();
        NodeRef docRef = parentChildAssocRef.getChildRef();
        logger.info("onDeleteDocument: A document with ref (" +
                docRef + ") was just deleted in folder (" + parentFolderRef + ")");
    }

}
