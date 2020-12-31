To build the Portal WAR file do as follows
------------------------------------------------------------------------
Setup properties in build.properties

Build the Portlets by running the ant target "war" in the build.xml file, this runs the
GWT compiler, copies GXT resources, copies WEB-INF files, and creates a
RecentlyAddedDocumentsPortlet.war file that can be deployed into Liferay.


