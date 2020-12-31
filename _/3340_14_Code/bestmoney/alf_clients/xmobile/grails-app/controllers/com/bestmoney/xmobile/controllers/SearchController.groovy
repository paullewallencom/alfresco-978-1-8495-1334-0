package com.bestmoney.xmobile.controllers

class SearchController {
  def cmisService

  /**
   * Default action according to conf/UrlMappings.groovy
   */
  def index = {
    render(view: 'search')
  }
  

  def search = {
    /** No keyword specified, then return empty search result  */
    if (!params.keyword?.trim()) {
      return [:]
    }

    def resultList
    try {
      resultList = cmisService.keywordSearch(params.keyword)
    } catch (Exception e) {
      log.error "Exception: ${e.message}"
      flash.message = "Could not access Alfresco via CMIS"
    }

    return [searchResult: resultList]
  }
}
