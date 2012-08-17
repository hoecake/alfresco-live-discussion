<div class="dashlet resizable yui-resize">
    <div class="title">
      ${title}
    </div>
   <div class="body scrollableList" >
    	<#if error??>
    		<span style="padding-left: 10px;" class="faded">${error}</span>
    	<#elseif result.invitations??>
    		<#if result.invitations?size &gt; 0>
				<#list result.invitations as invitation>
					<div class="detail-list-item ">
					    <div>
					    	<div class="icon">
					    		<a href="/share/page/user/${invitation.userName}/profile"> 
					    		<img src="/share/proxy/alfresco/slingshot/profile/avatar/${invitation.userName}/thumbnail/avatar32"/>
					    		</a>
					    	</div>
					    	<div class="details">
					    		<h4><a href="/share/page/live-discussion?nodeRef=${invitation.nodeRef}&topicNodeRef=${invitation.topicNodeRef}">Invited to discuss on '${invitation.topics}'</a></h4>
					            <div>
					               Sent on ${invitation.date} by  ${invitation.userName}
					            </div			    		
					    	</div>
						</div>
					</div>						
				</#list>
    		<#else>
	  			<span style="padding-left: 10px;" class="faded">No invitations found at this time</span>
    		</#if>
  		<#else>
  			<span style="padding-left: 10px;" class="faded">No invitations found at this time</span>
  		</#if>
  </div>
</div>
