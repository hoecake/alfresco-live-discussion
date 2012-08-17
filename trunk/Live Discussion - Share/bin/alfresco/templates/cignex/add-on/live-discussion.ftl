<#include "../../org/alfresco/include/alfresco-template.ftl" />
<@templateHeader>
   <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/document-details/document-details-panel.css" />
   <@templateHtmlEditorAssets />
</@>

<@templateBody>
   <div id="alf-hd">
      <@region id="header" scope="global"/>
      <@region id="title" scope="template"/>
   </div>
   
   <div id="bd">
		<div style="float: left; width:50%; height: 100%;"><@region id="topic-posts" scope="template" /></div>
		<div style="float: right; width:50%;"><@region id="web-preview" scope="template" /></div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global"/>
   </div>
</@>
