package com.bestmoney.test.patch;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MyPatch extends AbstractPatch {
    private static Log logger = LogFactory.getLog(MyPatch.class);

    private ImporterBootstrap importerBootstrap;

    public void setImporterBootstrap(ImporterBootstrap importerBootstrap) {
        this.importerBootstrap = importerBootstrap;
    }

    @Override
    protected String applyInternal() throws Exception {
        StoreRef storeRef = importerBootstrap.getStoreRef();
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);

		 // Do some patching of the system...
        logger.info("Patching the system...");

        return "Success";
    }
}

