How to use the authentication sub-system configuration?
-------------------------------------------------------
Then configuration files for all the authentication and synchronization 
examples in the chapter can be found under the 
3340_04_Code/bestmoney/alf_extensions/trunk/_alfresco/config/alfresco/extension/subsystems
directory. 

An example alfresco-global.properties file can be found in the 
3340_04_Code/bestmoney/alf_extensions/trunk/_alfresco/config/alfresco/extension directory.

To use this configuration or any one of the sub-system configurations,
copy it to your Alfresco installation and the 
tomcat/shared/classes/alfresco/extension/subsystems directory.
Then update the property file with relevant settings for your environment.

To use the custom authenticator under ldap-samba-account/bestmoneySambaLDAP
you need to run the package-alfresco-jar ant target to compile and package 
the authenticator class. Then copy the JAR file from the 
3340_04_Code/bestmoney/alf_extensions/trunk/build/lib
to the Alfresco installation /tomcat/webapps/alfresco/WEB-INF/lib.
Also, copy the sprinf ldap library from 3340_04_Code/bestmoney/alf_extensions/trunk/_alfresco/lib
to tomcat/webapps/alfresco/WEB-INF/lib.
Restart.

