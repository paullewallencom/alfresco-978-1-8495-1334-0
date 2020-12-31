package com.bestmoney.xmobile.controllers

class AuthenticationController {
  def cmisService

  def showLoginPage = {
    render(view: 'authentication')
  }

  def authenticateUser = {
    flash.message = "Credentials set"

    try {
      def ticket = cmisService.login(params.j_username, params.j_password)
      session.alf_ticket = ticket
    } catch (ConnectException ce) {
      flash.message = "Cannot connect to CMIS repository."
    }

    redirect(uri: '/')
  }

  def logout = {
    cmisService.logout()
    session.invalidate()
    redirect(uri: '/')
  }
}
