<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>MobileX - Login</title>
</head>
<body>
<div class="body">
  <h1>Login</h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <g:form action='authenticateUser' method='post'>
    <p>
      <label for='j_username'>Username:</label><input type='text' name='j_username' id='j_username'/>
    </p>
    <p>
      <label for='j_password'>Password:</label><input type='password' name='j_password' id='j_password'/>
    </p>
    <p>
      <input type='submit' value='Login'/>
    </p>
  </g:form>
</div>
</body>
</html>