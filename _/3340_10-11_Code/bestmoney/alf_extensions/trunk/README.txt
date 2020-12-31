To deploy the document management and workflow code for chapter 10 and 11
do as follows
---------------------------------------------------------------------
1) Create the "/Company Home/Data Dictionary/Email Templates/Best Money/Marketing" folder
2) Update the paths in build.properties
3) Stop Alfresco
4) Run the deploy-alfresco-amp ant target
5) Run the deploy-share-jar ant target
6) Start Alfresco

Note. you need to also manually deploy the workflow definitions
as described in the chapters.


