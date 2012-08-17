<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.21/jquery-ui.min.js" type="text/javascript"></script>
<link rel="stylesheet" href="http://code.jquery.com/ui/1.8.21/themes/base/jquery-ui.css" type="text/css" media="all" />
<script src="${page.url.context}/res/components/cignex/add-ons/js/topic-posts.js" type="text/javascript"></script>
<link rel="stylesheet" href="${page.url.context}/res/components/cignex/add-ons/css/topic-posts.css">
<table height="100%" width="100%">
<tr height="10%"><td>
	<table width="100%">
		<tr>
			<td width="90%">
				<select id="topics" style="width: 100%; height: 40px; font-size: 20px;" onChange="loadPosts(this.value);">
				<option style="color: grey;" value="">Select discussion topic</option>
				</select>
			</td>
			<td>
				<img style="padding-left: 5px;" src="${page.url.context}/res/components/cignex/add-ons/images/topic_add.png" height="42" width="40" title="Add new Topic" onClick="openTopicDialog();"/>
			</td>
		</tr>
	</table>
</td></tr>
<tr id="conversation" height="80%"><td height="100%">
	<p align="top">
		<table width="100%">
			<tr>
				<td width="89%"><textarea id="postText" placeholder="Reply here to below conversation..." style="width: 100%;" class="chatbox" cols="100" rows="60"></textarea></td>
				<td><img style="padding-left: 10px;" src="${page.url.context}/res/components/cignex/add-ons/images/reply.png" height="40" width="40" onClick="createPost()" title="Reply"></td>
			</tr>
		</table>
	</p>
	<p align="right">
		<img style="padding-top: 10px; padding-right: 10px;" src="${page.url.context}/res/components/cignex/add-ons/images/add_users.jpg"  height="40" width="40" onClick="openInviteUserPopup();" title="Add user to this conversation">
	</p>
	<p align="top">
		<table id="posts" width="100%">
		</table>
	</p>
</td></tr>
</table>
<div id="dialogMessage" title="Message" style="background-color: #808080; color: white; font-family: arial; font-size: 14px;">
	<div style="padding: 20px; "></div>
</div>
<div id="createTopicDialog" title="Start New Topic" style="color: black; font-family: arial; font-size: 12px;">
	<table>
		<tr>
			<td>
				*<span>Topic Name:</span><br/><input id="topicName" size="25"></input>			
			</td>
		</tr>	
		<tr>
			<td>
				*<span>Message:</span><br/><textarea id="firstPost" style="min-height:100px; min-width:300px;"></textarea><br/>
				<p id="errorMessage" style="color:red"/>
			</td>
		</tr>	
		<tr>
			<td>
				<p align="center"><input id="createTopicButton" type="button" value="Create Topic" onClick="createTopic();"/></p>
			</td>
		</tr>	
	</table>	
</div>
<div id="userListPopup" title="Invite users to discussion topic" style="color: black; width: 400px; font-family: arial; font-size: 12px;">
    <input id="userNameQuery" type="text" style="height: 25px; width: 247px; font-size: 14px;" onKeypress="searchUsers();" value="" placeholder="Search user to be invited"/>
    <input id="inviteUsers" type="button" value="Invite Users" onClick="inviteUsers();" style="padding-left: 10px"/>
    <br/>
    <select id="userList" multiple="multiple" size="10" style="width: 250px;">
    </select>
</div>