<html>
  <head>
    <title>Welcome to MobileX</title>
    <meta name="layout" content="main"/>
  </head>
  <body>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog" style="margin-left:20px;width:60%;">
      <ul>
        <li><g:link controller="folderNavigation">Browse Folders</g:link></li>
        <li><g:link controller="search">Search for Documents</g:link></li>
        <li><g:link controller="authentication" action="logout">Logout</g:link></li>
      </ul>
    </div>
  </body>
</html>