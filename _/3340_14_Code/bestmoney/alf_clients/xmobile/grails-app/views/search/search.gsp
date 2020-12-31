<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
  <title>
    <g:if test="${params.keyword && params.keyword?.trim() != ''}">${params.keyword} -</g:if>Search
  </title>
  <script type="text/javascript">
    var focusQueryInput = function() {
      document.getElementById("keyword").focus();
    }
  </script>
</head>
<body onload="focusQueryInput();">
<div class="nav">
  <span class="menuButton">
    <a class="home" href="${resource(dir: '', file: 'index.gsp')}">Home</a>
  </span>
</div>
<div class="body">
  <h1>Search</h1>

  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>

  <g:form url='[controller: "search", action: "search"]' id="searchableForm" name="searchableForm" method="get">
    <g:textField name="keyword" value="${params.keyword}" size="50"/>
    <input type="submit" value="Search"/>
  </g:form>

  <div>
    <g:set var="haveQuery" value="${params.keyword?.trim()}"/>
    <g:set var="haveResults" value="${searchResult}"/>

    <div>
      <span>
        <g:if test="${haveQuery && haveResults}">
          Showing <strong>${searchResult.size()}</strong> results for <strong>${params.keyword}</strong>
        </g:if>
        <g:else>
          &nbsp;
        </g:else>
      </span>
    </div>

    <g:if test="${haveQuery && !haveResults}">
      <p>Nothing matched your query: <strong>${params.keyword}</strong></p>
    </g:if>

    <g:if test="${haveResults}">
      <div class="list">
        <table>
          <thead>
          <tr>
            <th>&nbsp;</th><th>Name</th><th>Size</th><th>Owner</th><th>Modified</th></tr>
          </thead>
          <tbody>
          <g:each in="${searchResult}" status="i" var="document">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
              <td style="vertical-align:middle;horizontal-align:center;">
                <img src="${document.iconUrl}" alt="${document.type}" height="16" width="16"/>
              </td>
              <td><a href="${document.documentUrl}?alf_ticket=${session.alf_ticket}" target="_blank">${document.name}</a></td>
              <td>${document.fileSize}</td>
              <td>${document.author}</td>
              <td><g:formatDate date="${document.modifiedDate}" type="datetime" style="SHORT"/></td>
            </tr>
          </g:each>
          </tbody>
        </table>
      </div>
    </g:if>
  </div>

</div>
</body>
</html>
