<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>MobileX - Folder Browsing</title>
</head>
<body>

<div class="nav">
  <span class="menuButton">
    <a class="home" href="${resource(dir: '', file: 'index.gsp')}">Home</a>
  </span>
  <span class="menuButton">
    <g:link class="list" controller="search">Search</g:link>
  </span>
  <g:if test="${parentFolderNodeRef}">
    <span class="menuButton">
      <g:link class="edit" action="list" params="[parentFolderNodeRef:parentFolderNodeRef]">Up</g:link>
    </span>
  </g:if>
</div>

<div class="body">
  <g:if test="${flash.message}">
    <div class="message">
      ${flash.message}
    </div>
  </g:if>

  <div class="list">
    <table>
      <thead>
        <tr>
          <th>&nbsp;</th><th>Name</th><th>Size</th><th>Owner</th><th>Modified</th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${contentList}" status="i" var="contentItem">
          <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td style="vertical-align:middle;horizontal-align:center;">
              <img src="${contentItem.iconUrl}" alt="${contentItem.type}" height="16" width="16"/>
            </td>
            <td>
              <g:if test="${contentItem.type == 'document'}">
                <a href="${contentItem.documentUrl}?alf_ticket=${session.alf_ticket}" target="_blank">${contentItem.name}</a>
              </g:if>
              <g:else>
                <i><g:link action="list" params="[parentFolderNodeRef:contentItem.contentId]">${contentItem.name}</g:link></i>
              </g:else>
            </td>
            <td>${contentItem.fileSize}</td>
            <td>${contentItem.author}</td>
            <td><g:formatDate date="${contentItem.modifiedDate}" type="datetime" style="SHORT"/></td>
          </tr>
        </g:each>
      </tbody>
    </table>
  </div>
</div>

</body>
</html>