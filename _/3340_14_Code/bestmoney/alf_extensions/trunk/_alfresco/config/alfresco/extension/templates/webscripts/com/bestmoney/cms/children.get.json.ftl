<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#assign spaces = results.spaces>
<#assign docs = results.documents>
<#assign contentLength = results.totalCount>
<#if results.callbackFunctionName??>
    <#assign callback = results.callbackFunctionName>
</#if>

<#if (callback??)>${callback}(</#if>
{ "contentLength":"${contentLength}",
"content":[
<#list spaces as folder>
{
"type":"folder",
"locked":"${folder.isLocked?string}",
"cmName":"${folder.properties.name}",
"cmTitle":"${folder.properties.title!''}",
"cmDescription":"${folder.properties.description!''}",
"cmCreated":"${folder.properties.created?datetime}",
"cmCreator":"${folder.properties.creator!''}",
"path":"${folder.displayPath!''}",
"id":"${folder.id}",
"childrenSize":"${folder.children?size}"
}
    <#if (folder_has_next)>,</#if>
</#list>
<#if spaces?size != 0 && docs?size != 0>,</#if>
<#list docs as document>
{
"type":"document",
"locked":"${document.isLocked?string}",
"cmName":"${document.properties.name}",
"cmTitle":"${document.properties.title!''}",
"cmDescription":"${document.properties.description!''}",
"cmCreated":"${document.properties.created?datetime}",
"cmCreator":"${document.properties.creator!''}",
"path":"${document.displayPath!''}",
"id":"${document.id}",
"childrenSize":"0"
}
    <#if (document_has_next)>,</#if>
</#list>
]
}
<#if (callback??)>);</#if>