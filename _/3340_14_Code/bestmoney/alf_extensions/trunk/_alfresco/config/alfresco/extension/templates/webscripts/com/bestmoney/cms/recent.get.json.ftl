<#assign content = results.content>
<#assign contentLength = results.totalCount>
<#if results.callbackFunctionName??>
    <#assign callback = results.callbackFunctionName>
</#if>

<#if (callback??)>${callback}(</#if>
{ "contentLength":"${contentLength}",
"content":[
<#list content as item>
{
"locked":"${item.isLocked?string}",
"cmName":"${item.properties.name}",
"cmTitle":"${item.properties.title!''}",
"cmDescription":"${item.properties.description!''}",
"cmModified":"${item.properties.modified?datetime}",
"cmModifier":"${item.properties.modifier!''}",
"path":"${item.displayPath!''}",
"id":"${item.id}",
"childrenSize":"${item.children?size}"
}
    <#if (item_has_next)>,</#if>
</#list>
]
}
<#if (callback??)>);</#if>

