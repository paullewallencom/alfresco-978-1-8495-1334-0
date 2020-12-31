<#assign items = results.content>

<html>
<body>
<b>Items:</b>
<br/>
<#list items as item>
<a href="${url.serviceContext}/repo/node/${item.id}">${item.name}</a>
<br/>
</#list>
<hr/>
</body>
</html>

