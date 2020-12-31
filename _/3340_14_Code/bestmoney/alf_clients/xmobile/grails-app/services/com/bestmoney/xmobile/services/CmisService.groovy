package com.bestmoney.xmobile.services

import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.HttpClient
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.beans.factory.InitializingBean


class CmisService implements InitializingBean {

  /**
   * Change to false as we are not updating any data in a database with this service
   */
  static transactional = false

  /**
   * One CMIS Service bean per session
   */
  static scope = "session"

  /**
   * Load Groovy configuration from grails-app/conf/Config.groovy
   */
  def config = ConfigurationHolder.config

  /**
   * Apache Commons HTTP Client 
   */
  def httpClient = new HttpClient()

  /**
   * The Alfresco node reference for the /Company Home folder as
   * read from the CMIS Service Document
   */
  def rootFolderNodeRef

  /** ========  Spring ==============================================================================================  */

  /**
   * Spring Init to load stuff from CMIS Service doc
   */
  void afterPropertiesSet() {
    log.debug "Getting config from CMIS service document located at ${config.mobilex.cmisUrl}"

    def usercreds = new UsernamePasswordCredentials(config.mobilex.adminUsername, config.mobilex.adminPwd)
    httpClient.getState().setCredentials(AuthScope.ANY, usercreds)

    def xmlString = executeGetRequest(config.mobilex.cmisUrl)
    def cmisDocXml = new XmlSlurper().parseText(xmlString).declareNamespace(config.mobilex.cmisNamespaces)

    def rootFolderChildrenURL
    cmisDocXml.workspace.collection.each {
      if (it.collectionType.text() == "root") {
        rootFolderChildrenURL = it.@href as String
      }
    }

    log.debug "CMIS Service document contained root folder children URL: ${rootFolderChildrenURL}"

    def rootFolderNodeId = rootFolderChildrenURL.substring(rootFolderChildrenURL.indexOf('/i/') + 3, rootFolderChildrenURL.lastIndexOf('/'))
    rootFolderNodeRef = "workspace://SpacesStore/${rootFolderNodeId}"

    log.debug "CMIS Service document contained root folder node ref: ${rootFolderNodeRef}"

    httpClient.getState().clear()
  }

  /**
   * Method used by controllers to get to the root folder node reference
   *
   * @return a node reference looking something like workspace://SpacesStore/26cef395-d9c5-40f7-b1f4-6e4954462adb
   */
  def getRootFolderNodeRef() {
    return rootFolderNodeRef
  }

  /** ========  LOGIN and LOGOUT ====================================================================================  */

  /**
   * Authenticates passed in username and password with the Alfresco server and if successful
   * a ticket is returned that can be used instead of further authentication.
   *
   * @param username the username that should be authenticated
   * @param password the password for the user
   * @return a ticket that can be used in subsequent calls, or empty string if could not authenticate
   */
  def login(username, password) {
    log.debug "About to try and login user ${username}"

    // Setup the username and password in the HTTP Client object so we have it for future requests,
    // it is not needed for this GET Request
    def usercreds = new UsernamePasswordCredentials(username, password)
    httpClient.getState().setCredentials(AuthScope.ANY, usercreds)

    // Login to verify username and password
    def url = "${config.mobilex.alfrescoApiUrl}/login?u=${username}&pw=${password}"
    def xmlString = executeGetRequest(url)
    def ticket

    try {
      ticket = new XmlSlurper().parseText(xmlString)
    } catch (org.xml.sax.SAXParseException spe) {
      log.error "Login error: ${spe}"
      httpClient.getState().clear()
      ticket = ""
      
    }

    return ticket as String
  }

  /**
   * Logs out of current session
   */
  def logout() {
    httpClient.getState().clear()
  }

  /** ========  FOLDER NAVIGATION ====================================================================================  */

  /**
   * Fetches all child folders and documents under passed in Folder node reference
   *
   * TODO: pagination (from session?)
   *
   * @param nodeRef a folder node reference
   * @return a list of map entries (property name -> property value)
   */
  def fetchChildrenForFolder(String nodeRef) {
    def nodeGuid = extractNodeGuid(nodeRef)
    def url = "${config.mobilex.childrenPath}/${nodeGuid}/children"
    def xmlString = executeGetRequest(url)
    //log.debug "ATOMPub Feed response: ${xmlString}"
    return parseAtomFeed(xmlString)
  }

  /** ========  SEARCH ===============================================================================================  */

  /**
   * Executes a keyword search on document text and name
   *
   * @param keyword the keyword to search on
   * @return a list of map entries (property name -> property value)
   */
  def keywordSearch(String keyword) {
    log.debug "Searching for documents containing keyword: $keyword"

    def queryStatement = "SELECT cmis:ObjectTypeId, cmis:ObjectId, cmis:Name, cmis:ContentStreamLength, cmis:CreatedBy, cmis:LastModificationDate FROM cmis:Document WHERE CONTAINS('$keyword') OR cmis:name LIKE '%$keyword%'"
    def headers = ['Content-type': 'application/cmisquery+xml',
            'Accept': 'application/atom+xml;type=feed']

    def queryWriter = new StringWriter()
    def queryXMLBuilder = new MarkupBuilder(queryWriter)
    queryXMLBuilder.'cmis:query'('xmlns:cmis': config.mobilex.cmisNamespaces.cmis) {
      'cmis:statement'(queryStatement)
      'cmis:searchAllVersions'(false)
      'cmis:includeAllowableActions'(false)
      'cmis:includeRelationships'('none')
      'cmis:renditionFilter'('*')
      'cmis:maxItems'(-1)
      'cmis:pageSize'(-1)
      'cmis:skipCount'(0)
    }

    def postData = queryWriter.toString()
    log.debug "CMIS Query: ${postData}"
    def xmlString = executePostRequest(config.mobilex.cmisQueryUrl, queryWriter.toString(), headers)
    def list = []
    if (!xmlString) {
      log.warn "No documents were found with keyword $keyword"
    } else {
      list = parseAtomFeed(xmlString)
    }

    return list
  }

  /** ========  HELPERS ==============================================================================================  */

  /**
   * Executes a GET HTTP Request with the help of Apache HttpClient
   *
   * @param url the URL to call
   * @return the response body returned by the server as a String
   */
  def executeGetRequest(url) {
    def method = new GetMethod(url)
    method.setDoAuthentication(true)
    def statusCode = httpClient.executeMethod(method)
    log.debug "Response status ${statusCode} from ${url}"
    def responseBody = method.getResponseBodyAsString()
    method.releaseConnection()
    return responseBody
  }

  /**
   * Executes a POST HTTP Request with the help of Apache HttpClient
   *
   * @param url the URL to post data to
   * @param xmlData the data we want to post to the server
   * @param headers any HTTP headers that should go along
   * @return the response body returned by the server as a String
   */
  def executePostRequest(url, xmlData, headers) {
    def method = new PostMethod(url)
    method.setDoAuthentication(true)
    headers.each { k, v -> method.setRequestHeader(k, v) }
    method.setRequestBody(new StringBufferInputStream(xmlData))

    def responseBody
    try {
      def statusCode = httpClient.executeMethod(method)
      log.debug "Response status ${statusCode} from ${url}"
      responseBody = method.getResponseBodyAsString()
      log.debug responseBody
    } catch (IOException ioe) {
      responseBody = ""
      throw new Exception("Error accessing CMIS", ioe)
    } finally {
      try {
        method.releaseConnection()
      } catch (Exception ex) {
        log.warn "Failed to release connection (possibly due to an earlier error): ${ex}"
      }
    }
    return responseBody
  }

  /**
   * Parse the ATOM Feed that was a response to a CMIS Query
   *
   * @param xmlString ATOM Feed XML
   * @return a list of map entries (property name -> property value)
   */
  def parseAtomFeed(xmlString) {
    def feed

    try {
      feed = new XmlSlurper().parseText(xmlString).declareNamespace(config.mobilex.cmisNamespaces)
    } catch (org.xml.sax.SAXParseException spe) {
      def xmlString4 = xmlString.replaceAll("&(?!amp;)", "&amp;")
      log.debug xmlString4
      feed = new XmlSlurper().parseText(xmlString4).declareNamespace(config.mobilex.cmisNamespaces)
    }

    //def feedParentLink = feed.link.find { it['@rel'] == 'parents' }
    //log.debug "Feed parent: ${feedParentLink}"

    def list = feed.entry.collect {
      def objectId = getCmisProperty(it, 'cmis:objectId') as String
      def docName = getCmisProperty(it, 'cmis:name') as String
      def fileSize = getCmisProperty(it, 'cmis:contentStreamLength') as String
      if (fileSize) {
        fileSize = Integer.valueOf(fileSize)
      }
      def author = getCmisProperty(it, 'cmis:createdBy') as String
      def updatedDate = parseDate(getCmisProperty(it, 'cmis:lastModificationDate') as String)
      def nsPrefixedType = getCmisProperty(it, 'cmis:objectTypeId') as String
      def type = ''
      if (nsPrefixedType) {
        // remove the xmlns prefix (e.g. cmis:)
        type = nsPrefixedType.substring(nsPrefixedType.lastIndexOf(':') + 1)
      }

      // Get the URL for downloading content
      // <entry>
      //    <content type="application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      //          src="http://localhost:8080/alfresco/service/cmis/s/workspace:SpacesStore/i/bbb487b2-3d70-4f8c-be67-e5d9300561fe/content.docx"/>
      def documentUrl = it.content.@src

      // create a map and add it to a list
      return [name: docName,
              type: type,
              contentId: objectId,
              iconUrl: it.'alf:icon' as String,
              documentUrl: documentUrl,
              fileSize: fileSize,
              modifiedDate: updatedDate,
              author: author]
    }

    return list
  }
  
  /**
   * Get CMIS Properties by name
   *
   * @param xmlRootObj
   * @param propName
   * @return
   */
  def getCmisProperty(xmlRootObj, propName) {
    return xmlRootObj.'cmisra:object'.'cmis:properties'.'**'.grep { it.@'cmis:propertyDefinitionId' == propName}.'cmis:value'[0]
  }

  /**
   * Extract just the GUID from a node reference, basically remove store and protocol
   *
   * @param nodeRef
   * @return
   */
  def extractNodeGuid(nodeRef) {
    return nodeRef?.substring((nodeRef?.lastIndexOf('/') ?: 0) + 1)
  }

  def parseDate(strDate) {
    if (strDate) {
      // dates are in format: 2010-04-26T18:13:10.245+01:00
      // also get some buggy GMT dates: 2009-11-18T11:48:15.956Z
      return new Date().parse("yyyy-MM-dd'T'HHmmss.SSSZ", strDate.replace(':', '').replace('Z', '+0000'))
    }
  }
}
