<#assign spaces = results.spaces>
<#assign docs = results.documents>

<html>
<body>
<b>Spaces:</b>
<br/>
<#list spaces as space>
<a href="${url.serviceContext}/repo/node/${space.id}">${space.name}</a>
<br/>
</#list>
<hr/>
<b>Documents:</b>
<br/>
<#list docs as doc>
<a href="${url.serviceContext}/repo/node/${doc.id}">${doc.name}</a>
<br/>
</#list>
<hr/>
</body>
</html>

