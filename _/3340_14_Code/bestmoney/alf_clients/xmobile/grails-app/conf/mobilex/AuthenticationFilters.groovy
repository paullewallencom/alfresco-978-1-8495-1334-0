package mobilex

class AuthenticationFilters {
  def filters = {
    // If user don't have a ticket, send to Login page
    loginCheck(controller: '*', action: '*') {
      before = {
        if ((session == null || !session?.alf_ticket) && !controllerName.equals('authentication')) {
          redirect(controller: 'authentication', action: 'showLoginPage')
          return false
        }
      }
    }
  }
}
