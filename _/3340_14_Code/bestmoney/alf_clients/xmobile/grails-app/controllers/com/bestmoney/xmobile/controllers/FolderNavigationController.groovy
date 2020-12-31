package com.bestmoney.xmobile.controllers

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class FolderNavigationController {
  def cmisService

  /**
   * Load Groovy configuration from grails-app/conf/Config.groovy
   */
  def config = ConfigurationHolder.config

  /**
   * Default action according to conf/UrlMappings.groovy
   */
  def index = {
    redirect(action:list)
  }

  /**
   * Main action, list folders and documents
   */
  def list = {
    if (!params.parentFolderNodeRef) {
      log.debug 'Fetching top level folders and documents'
      def topLevelContentList = []
      def folderRootNode = cmisService.getRootFolderNodeRef()

      try {
        topLevelContentList = cmisService.fetchChildrenForFolder(folderRootNode)
      } catch (Exception e) {
        log.error "Exception: ${e.message}"
        flash.message = "Could not access Alfresco via CMIS"
      }

      log.debug "Fetched ${topLevelContentList?.size()} folders and documents"

      session.currentNode = folderRootNode
      session.folderNodeRefStack = new Stack()

      [contentList: topLevelContentList, parentFolderNodeRef: null]
    } else {
      // here we have a node ref passed in as params.node
      // (e.g. "workspace://SpacesStore/83320c35-2160-42ff-840b-6cdd1ac3d0ba")
      log.debug "Fetching folders and documents in folder ${params.parentFolderNodeRef}"

      def childContentList = []

      try {
        childContentList = cmisService.fetchChildrenForFolder(params.parentFolderNodeRef)
      } catch (Exception e) {
        log.warn "Exception accessing CMIS: ${e.message}"
        flash.message = "Could not access Alfresco via CMIS"
      }

      log.debug "Fetched ${childContentList?.size()} folders and documents in folder ${params.parentFolderNodeRef}"

      def parentFolderNodeRef = null
      def topFolderNodeRef = null

      if (!session.folderNodeRefStack.empty()) {
        topFolderNodeRef = session.folderNodeRefStack.peek()
      }

      // Navigating Up
      if (topFolderNodeRef == params.parentFolderNodeRef) {
        // Take this entry off the stack
        session.folderNodeRefStack.pop()
        // look for a parent
        if (!session.folderNodeRefStack.empty()) {
          parentFolderNodeRef = session.folderNodeRefStack.peek()
        }
      } else {
        // going down
        // set the previous 'currentNode' to the parentFolderNodeRef
        parentFolderNodeRef = session.currentNode
        // push the parentFolderNodeRef onto the stack
        session.folderNodeRefStack.push(parentFolderNodeRef)
      }

      // set requested node as current node
      session.currentNode = params.parentFolderNodeRef

      [contentList: childContentList, parentFolderNodeRef: parentFolderNodeRef]
    }
  }
}
