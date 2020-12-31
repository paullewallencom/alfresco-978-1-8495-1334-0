package com.bestmoney.test.foundationservice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class MyCmsServiceImpl implements MyCmsService {
    private static Log logger = LogFactory.getLog(MyCmsServiceImpl.class);

    public static final String FILENAME = "helloworld.txt";
    public static final String FILENAME2 = "helloworld2.txt";
    public static final String FILENAME2RENAMED = "helloworld2renamed.txt";
    public static final String FOLDERNAME = "MyFolder";
    public static final String FOLDERNAME2 = "MyFolder2";
    public static final String FILE_CONTENT = "Hello World!";

    private NodeService m_nodeService;
    private ContentService m_contentService;
    private FileFolderService m_fileFolderService;
    private SearchService m_searchService;
    private NamespaceService m_namespaceService;
    private PermissionService m_permissionService;
    private DictionaryService m_dictionaryService;

    /**
     * Parameter set in the spring context, will resolve to app:company_home
     */
    private String m_companyHomeChildname;

    public void setNodeService(NodeService nodeService) {
        m_nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        m_contentService = contentService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        m_fileFolderService = fileFolderService;
    }

    public void setSearchService(SearchService searchService) {
        m_searchService = searchService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        m_namespaceService = namespaceService;
    }

    public void setPermissionService(PermissionService permissionService) {
        m_permissionService = permissionService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        m_dictionaryService = dictionaryService;
    }

    public void setCompanyHomeChildname(String companyHomeChildname) {
        m_companyHomeChildname = companyHomeChildname;
    }

    /**
     * Calls that only read data go here and they are executed inside a read-only transaction.
     */
    public void readSomething() {
        testReadNodeAndContentService();
        testReadFileFolderService();
        testPermissionService();
        testDictionaryService();
    }

    /**
     * Here we can use methods that require a read/write transaction.
     */
    public void writeSomething() {
        testWriteNodeAndContentService();
        testWriteFileFolderService();
    }

    public void cleanUp() {
        // Clean up and delete the file and the folder

        // If you only have display path do this
        List<String> displayPathFolderList = new ArrayList<String>();
        displayPathFolderList.add(FILENAME);
        m_nodeService.deleteNode(search("+PATH:\"" + getXPathFromDisplayPath(displayPathFolderList) + "\""));

        // If you got the XPATH do this
        m_nodeService.deleteNode(search("+PATH:\"/app:company_home/cm:" + FILENAME2RENAMED + "\""));
        m_nodeService.deleteNode(search("+PATH:\"/app:company_home/cm:" + FOLDERNAME + "\""));
        m_nodeService.deleteNode(search("+PATH:\"/app:company_home/cm:" + FOLDERNAME2 + "\""));

        logger.info("NodeService: Deleted file nodes (" + FILENAME + ", " + FILENAME2RENAMED + ") and folder nodes (" +
                FOLDERNAME + "," + FOLDERNAME2 + ")");
    }

    private void testReadNodeAndContentService() {
        // List all file and sub-folder nodes for the /Company Home node
        NodeRef parentFolderNodeRef = getCompanyHomeNodeRef();
        List<ChildAssociationRef> childAssocRefs = m_nodeService.getChildAssocs(parentFolderNodeRef);

        if (childAssocRefs.isEmpty()) {
            return;
        }

        NodeRef fileNodeRef = null;
        NodeRef fileOrFolderNodeRef = null;

        logger.info("NodeService: Starting listing of all nodes in /Company Home");
        for (ChildAssociationRef childAssocRef : childAssocRefs) {
            NodeRef childNodeRef = childAssocRef.getChildRef();
            QName nodeTypeQName = m_nodeService.getType(childNodeRef);
            Map<QName, Serializable> nodeProperties = m_nodeService.getProperties(childNodeRef);

            if (nodeTypeQName.equals(ContentModel.TYPE_CONTENT)) {
                // Do something with the file metadata or content
                String filename = (String) nodeProperties.get(ContentModel.PROP_NAME);
                logger.info("NodeService: Found file node (" + filename + ") [fileNodeRef= " + childNodeRef + "]");
                fileNodeRef = childNodeRef;
            } else if (nodeTypeQName.equals(ContentModel.TYPE_FOLDER)) {
                // Do something with folder metadata
                String foldername = (String) nodeProperties.get(ContentModel.PROP_NAME);
                Date createdDate = (Date) nodeProperties.get(ContentModel.PROP_CREATED);
                logger.info("NodeService: Found folder node (" + foldername + ") [createdDate=" + createdDate +
                        "][fileNodeRef= " + childNodeRef + "]");
                fileOrFolderNodeRef = childNodeRef;
            }
        }
        logger.info("NodeService: Done listing all nodes in /Company Home");


        // Get parent folder for a node
        parentFolderNodeRef = m_nodeService.getPrimaryParent(fileOrFolderNodeRef).getParentRef();
        logger.info("NodeService: Got parent folder node reference (" + parentFolderNodeRef + ") for node ref (" + fileOrFolderNodeRef + ")");

        // Check if node is versioned
        Boolean isVersioned = m_nodeService.hasAspect(fileOrFolderNodeRef, ContentModel.ASPECT_VERSIONABLE);

        logger.info("NodeService: Checked if node ref (" + fileOrFolderNodeRef + ") is versioned = " + isVersioned);

        // Read content from a file node
        if (fileNodeRef != null) {
            ContentReader reader = m_contentService.getReader(fileNodeRef, ContentModel.PROP_CONTENT);
            if (reader == null) {
                // Maybe it was a folder after all
                return;
            }

            InputStream is = reader.getContentInputStream();

            try {
                // Read from the input stream
                String contentText = IOUtils.toString(is, "UTF-8");
                logger.info("ContentService: Read content from node ref (" + fileNodeRef + "): " + contentText);
            } catch (IOException ioe) {
                String errorMsg = "ContentService: Error reading content from node ref (" + fileNodeRef + ") :" + ioe.getMessage();
                logger.error(errorMsg, ioe);
                throw new RuntimeException(errorMsg);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Throwable e) {
                        logger.error(e);
                    }
                }
            }
        }
    }

    private void testReadFileFolderService() {
        // List all file and sub-folder nodes for the /Company Home node
        NodeRef parentFolderNodeRef = getCompanyHomeNodeRef();
        List<FileInfo> filesAndFolders = m_fileFolderService.list(parentFolderNodeRef);

        if (filesAndFolders.isEmpty()) {
            return;
        }

        NodeRef fileNodeRef = null;

        logger.info("FileFolderService: Starting listing of all nodes in /Company Home");
        for (FileInfo fileOrFolder : filesAndFolders) {
            NodeRef nodeRef = fileOrFolder.getNodeRef();
            String name = fileOrFolder.getName();
            Date createdDate = fileOrFolder.getCreatedDate();
            Date modifiedDate = fileOrFolder.getModifiedDate();
            if (fileOrFolder.isFolder()) {
                // Do something with the folder
                logger.info("FileFolderService: Found folder node (" + name + ") [folderNodeRef= " + nodeRef + "]");
            } else if (fileOrFolder.isLink()) {
                // Do something with a link to a folder or file
                logger.info("FileFolderService: Found link node (" + name + ") [linkNodeRef= " + nodeRef + "]");
            } else {
                // Do something with a file
                fileNodeRef = nodeRef;
                ContentData contentInfo = fileOrFolder.getContentData();
                String mimetype = contentInfo.getMimetype();
                long size = contentInfo.getSize();
                String encoding = contentInfo.getEncoding();
                logger.info("FileFolderService: Found file node (" + name + ") [mimetype=" + mimetype + "][size=" + size + "]" +
                        "[encoding=" + encoding + "][fileNodeRef= " + nodeRef + "]");
            }
        }
        logger.info("FileFolderService: Done listing all nodes in /Company Home");

        // Read content from a file node
        if (fileNodeRef != null) {
            ContentReader reader = m_fileFolderService.getReader(fileNodeRef);
            if (reader == null) {
                // Maybe it was a folder after all
                return;
            }

            InputStream is = reader.getContentInputStream();

            try {
                // Read from the input stream
                String contentText = IOUtils.toString(is, "UTF-8");
                logger.info("ContentService: Read content from node ref (" + fileNodeRef + "): " + contentText);
            } catch (IOException ioe) {
                String errorMsg = "ContentService: Error reading content from node ref (" + fileNodeRef + ") :" + ioe.getMessage();
                logger.error(errorMsg, ioe);
                throw new RuntimeException(errorMsg);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Throwable e) {
                        logger.error(e);
                    }
                }
            }
        }
    }

    private void testPermissionService() {
        NodeRef parentFolderNodeRef = getCompanyHomeNodeRef();

        boolean hasCreatePermission = m_permissionService.hasPermission(parentFolderNodeRef,
                PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED;

        if (hasCreatePermission) {
            logger.info("PermissionService: Current user has ADD_CHILDREN permission to /Company Home");
        }


        boolean hasReadPermission = m_permissionService.hasPermission(parentFolderNodeRef,
                PermissionService.READ) == AccessStatus.ALLOWED;

        if (hasReadPermission) {
            logger.info("PermissionService: Current user has READ permission to /Company Home");
        }

        boolean hasUpdatePermission = m_permissionService.hasPermission(parentFolderNodeRef,
                PermissionService.WRITE) == AccessStatus.ALLOWED;

        if (hasUpdatePermission) {
            logger.info("PermissionService: Current user has WRITE permission to /Company Home");
        }

        boolean hasDeletePermission = m_permissionService.hasPermission(parentFolderNodeRef,
                PermissionService.DELETE) == AccessStatus.ALLOWED;

        if (hasDeletePermission) {
            logger.info("PermissionService: Current user has DELETE permission to /Company Home");
        }
    }

    private void testDictionaryService() {
        Collection<QName> allDeployedModels = m_dictionaryService.getAllModels();
        for (QName model : allDeployedModels) {
            ModelDefinition modelDef = m_dictionaryService.getModel(model);
            QName modelName = modelDef.getName();
            logger.info("DictionaryService: Found content model: " + modelName);
            Collection<QName> allTypes = m_dictionaryService.getTypes(modelName);
            for (QName type : allTypes) {
                logger.info("DictionaryService:    type: " + type);
            }
            Collection<QName> allAspects = m_dictionaryService.getAspects(modelName);
            for (QName aspect : allAspects) {
                logger.info("DictionaryService:    aspect: " + aspect);
            }
        }

        NodeRef someNodeRef = getCompanyHomeNodeRef();
        QName nodeTypeQName = m_nodeService.getType(someNodeRef);
        Boolean isFolder = m_dictionaryService.isSubClass(nodeTypeQName, ContentModel.TYPE_FOLDER);
        logger.info("DictionaryService: /Company Home is folder check = " + isFolder);
    }

    private void testWriteNodeAndContentService() {
        NodeRef parentFolderNodeRef = getCompanyHomeNodeRef();
        ChildAssociationRef parentChildAssocRef = null;
        QName associationType = ContentModel.ASSOC_CONTAINS;
        QName associationQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(FILENAME));
        QName nodeType = ContentModel.TYPE_CONTENT;

        Map<QName, Serializable> nodeProperties = new HashMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, FILENAME);

        // Create a new file Node without any content
        parentChildAssocRef = m_nodeService.createNode(
                parentFolderNodeRef, associationType, associationQName, nodeType, nodeProperties);

        NodeRef newFileNodeRef = parentChildAssocRef.getChildRef();

        // Add some text content to the new file node
        Boolean updateContentPropertyAutomatically = true;
        ContentWriter writer = m_contentService.getWriter(newFileNodeRef, ContentModel.PROP_CONTENT,
                updateContentPropertyAutomatically);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(FILE_CONTENT);

        logger.info("NodeService: Created new file node (" + FILENAME + ") with content (" + FILE_CONTENT +
                ") [fileNodeRef= " + newFileNodeRef + "][parentFolderRef=" + parentChildAssocRef.getParentRef() + "]");

        // Add the Titled aspect to the new file node
        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
        aspectProperties.put(ContentModel.PROP_TITLE, "Hello World!");
        aspectProperties.put(ContentModel.PROP_DESCRIPTION, "This is the traditional Hello World example.");
        m_nodeService.addAspect(newFileNodeRef, ContentModel.ASPECT_TITLED, aspectProperties);

        logger.info("NodeService: Added the Titled aspect to the new file node (" + FILENAME + ")");

        // Create a new folder
        associationType = ContentModel.ASSOC_CONTAINS;
        associationQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(FOLDERNAME));
        nodeType = ContentModel.TYPE_FOLDER;

        nodeProperties = new HashMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, FOLDERNAME);

        ChildAssociationRef childAssocRef = m_nodeService.createNode(
                parentFolderNodeRef, associationType, associationQName, nodeType, nodeProperties);
        NodeRef newFolderNodeRef = childAssocRef.getChildRef();
        logger.info("NodeService: Created new folder node (" + FOLDERNAME + ") [folderNodeRef= " + newFolderNodeRef
                + "][parentFolderRef=" + parentChildAssocRef.getParentRef() + "]");
    }

    private void testWriteFileFolderService() {
        NodeRef parentFolderNodeRef = getCompanyHomeNodeRef();
        QName nodeType = ContentModel.TYPE_CONTENT;

        // Create a new file Node without any content
        FileInfo file = m_fileFolderService.create(
                parentFolderNodeRef, FILENAME2, nodeType);

        NodeRef newFileNodeRef = file.getNodeRef();

        // Add some text content to the new file node
        ContentWriter writer = m_fileFolderService.getWriter(newFileNodeRef);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(FILE_CONTENT);

        logger.info("FileFolderService: Created new file node (" + FILENAME2 + ") with content (" + FILE_CONTENT +
                ") [fileNodeRef= " + newFileNodeRef + "][parentFolderRef=" + parentFolderNodeRef + "]");


        try {
            m_fileFolderService.rename(newFileNodeRef, FILENAME2RENAMED);

            logger.info("FileFolderService: Renamed (" + FILENAME2 + ") to (" + FILENAME2RENAMED +
                    ") [fileNodeRef= " + newFileNodeRef + "][parentFolderRef=" + "]");
        } catch (FileNotFoundException fnfe) {
            logger.error("FileFolderService: Could not rename (" + FILENAME2 + ") to (" + FILENAME2RENAMED +
                    "), file was not found [fileNodeRef= " + newFileNodeRef + "][parentFolderRef=" +
                    parentFolderNodeRef + "]");
        }

        // Create a new folder
        nodeType = ContentModel.TYPE_FOLDER;
        FileInfo folder = m_fileFolderService.create(parentFolderNodeRef, FOLDERNAME2, nodeType);
        NodeRef newFolderNodeRef = folder.getNodeRef();

        logger.info("FileFolderService: Created new folder node (" + FOLDERNAME2 + ") [folderNodeRef= " + newFolderNodeRef
                + "][parentFolderRef=" + parentFolderNodeRef + "]");
    }

    private NodeRef getCompanyHomeNodeRef() {
        return search("+PATH:\"/app:company_home\"");
    }

    private NodeRef search(String query) {
        StoreRef workspaceStore = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        ResultSet results = null;
        List<NodeRef> matchingNodes = null;

        try {
            results = m_searchService.query(workspaceStore,
                    SearchService.LANGUAGE_LUCENE, query);
        } finally {
            if (results != null) {
                matchingNodes = results.getNodeRefs();
            } else {
                matchingNodes = new ArrayList<NodeRef>();
            }

            // Underlying search engine, such as Lucene,
            // might have IO resources open so close them
            results.close();
        }

        if (matchingNodes.isEmpty()) {
            throw new RuntimeException("Could not find any nodes with query: " + query);
        }

        return matchingNodes.get(0);
    }

    /**
     * Converts a Display Path to an XPATH
     * <p/>
     * (shows another way to get to the Company Home node reference then just searching for it)
     *
     * @param displayPathFolderList a list of folders in a display path excluding /Company Home
     * @return an XPATH such as /app:company_home/cm:MyFolder
     */
    private String getXPathFromDisplayPath(List<String> displayPathFolderList) {
        StoreRef workspaceStore = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        NodeRef storeRootNodeRef = m_nodeService.getRootNode(workspaceStore);

        // Setup Company Home path (i.e. app:company_home)
        QName companyHomePath = QName.createQName(m_companyHomeChildname, m_namespaceService);

        // Get node reference for Company Home
        List<ChildAssociationRef> assocRefs = m_nodeService.getChildAssocs(storeRootNodeRef,
                ContentModel.ASSOC_CHILDREN, companyHomePath);
        NodeRef companyHomeNodeRef = assocRefs.get(0).getChildRef();

        // Get the file information for the leaf folder
        FileInfo folderInfo = null;
        try {
            folderInfo = m_fileFolderService.resolveNamePath(companyHomeNodeRef, displayPathFolderList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Get the XPath formatted path
        String xpath = m_nodeService.getPath(folderInfo.getNodeRef()).toPrefixString(m_namespaceService);

        return xpath;
    }
}