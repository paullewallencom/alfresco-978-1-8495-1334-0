How to build and deploy AMP
---------------------------
1) Set variables in build.properties to point to your Alfresco installation and your Alfresco SDK
2) Make a copy of the original alfresco.war called alfresco.war.bak in the same webapps directory (the build script uses it)
3) Run the update-war ant target, which will build and install this amp into the war.

How to run Java tests
---------------------------
The Foundation Service Examples can be run by executing the following Web Script:

	http://localhost:8080/alfresco/service/3340_02/testfs

This will kick-off all the NodeService, ContentService, FileFolderService, PermissionService, and DataDictionaryService tests.

It will also automatically kick off the Event manager that has been installed to listen to when any content is added, updated, or deleted.

A patch will automatically run first time Alfresco is started after this module has been installed.

To test the metadata extractor upload an XML file to any folder. It will have its title and descriptor fields set from extracted metadata.

How to run JavaScript tests
---------------------------
The JavaScript file can be found under trunk/source/javascript.
Upload them to the /Data Dictionary/Scripts folder.
They can then be run by executing an action from the UI (i.e. run action -> execute script -> select one of the scripts)



