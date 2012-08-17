$(document).ready(function () {
	$("#dialogMessage").dialog({
		autoOpen: false,
		show: "fade",
		hide: "fade",
		closeText: ""
	});
	
	$( "#createTopicDialog" ).dialog({
		autoOpen: false,
		modal: true,
		closeText: "",
		minWidth: 400,
		position: "center"
	});
	
	$("#userListPopup").dialog({
		autoOpen: false,
		modal: true,
		closeText: "",
		minWidth: 400,
		position: "center"
	});

	var nodeRef = qs('nodeRef');
	if(nodeRef != null){
		loadTopics(nodeRef);
		var topicNodeRef = qs('topicNodeRef');
		if(topicNodeRef != null) {
			$("#topics").val(topicNodeRef);
			loadPosts(topicNodeRef);
		}
	} else {
		message('NodeRef parameter is missing');
	}
	refreshPosts();
});

function qs(key) {
	var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars[key];
}

function loadTopics(nodeRef){
	$("#conversation").hide();
	$.ajax({
        type: "GET",
        url: "/share/proxy/alfresco/cignex/addons/topic?nodeRef="+nodeRef,
        dataType: "json",
		cache:false,
		async:false,
        contentType: "application/json",
        success: function(data) {
        	if(data['error'] != null){
        		if(data['error'].indexOf("Access") == -1){
            		message('Error encountered: '+data['error']);
        		} else {
        			message('Access Denied to create new topic. Please ask owner of this content to provide with necessary access');
        		}
        	} else {
        		var optionHtml = "<option style=\"color: grey;\" value=\"\">Select discussion topic</option>";
            	$.each(data.topics, function(i,topic){
            		optionHtml += "<option value=\""+topic.nodeRef+"\">"+topic.name+"</option>";
            	});
                $("#topics").html(optionHtml);
            	if(optionHtml != "<option style=\"color: grey;\" value=\"\">Select discussion topic</option>") {
                    $("#conversation").show();
            	}
        	}
        },
        error: function(xhr, status, error) {
    		message('Reponse Status: '+xhr.status);
        }
	});
}

function loadPosts(topicNodeRef){
	if(topicNodeRef != ""){
		$.ajax({
	        type: "GET",
	        url: "/share/proxy/alfresco/cignex/addons/topic-post?topicNodeRef="+topicNodeRef,
	        dataType: "json",
			cache:false,
			async:false,
	        contentType: "application/json",
	        success: function(data) {
	        	if(data['error'] != null){
	        		message('Error encountered: '+data['error']);
	        	} else {
	        		var postsHtml = "";
	            	$.each(data.posts, function(i,post){
	            		if(post.online == "true"){
		            		postsHtml += "<tr class=\"chatMessage\"><td width=\"10%\"><img style=\"background:url(/share/proxy/alfresco/slingshot/profile/avatar/"+post.author
		            		+")\" src=\"/share/res/components/cignex/add-ons/images/online.gif\" alt=\"Avatar\"/></td><td><p style=\"color: black;\">"+post.content+"</p><br/><i>Posted by "+post.author+" on "
		            		+post.date+"</i></td></tr>";
	            		} else {
		            		postsHtml += "<tr class=\"chatMessage\"><td width=\"10%\"><img src=\"/share/proxy/alfresco/slingshot/profile/avatar/"+post.author
		            		+"\" alt=\"Avatar\"/></td><td><p style=\"color: black;\">"+post.content+"</p><br/><i>Posted by "+post.author+" on "
		            		+post.date+"</i></td></tr>";
	            		}
	            	});
	            	if(postsHtml == "") {
	            		$("#posts").html("<tr><td>No posts found for this topic. Be the first to post!!</td></tr>");
	            	} else if($("#posts").html() != postsHtml){
	                   	$("#posts").html(postsHtml);
	            	}
	        	}
	        },
	        error: function(xhr, status, error) {
	    		message('Reponse Status: '+xhr.status);
	        }
		});
	} else {
		$("#posts").html("");
	}
}

function createTopic(){
	$("#errorMessage").html("");
	var nodeRef = qs('nodeRef');
	if(null != nodeRef){
		if($("#topicName").val() == "" || $("#topicName").val() == "undefined"){
			$("#errorMessage").html($("#errorMessage").html()+"<br/>Please enter the Topic Name");
			return;
		}
		if($("#firstPost").val() == "" || $("#firstPost").val() == "undefined"){
			$("#errorMessage").html($("#errorMessage").html()+"<br/>Please enter the Message");
			return;
		}
		var jsonRequestObject = new Object();
		jsonRequestObject.nodeRef = nodeRef;
		jsonRequestObject.title = $("#topicName").val();
		jsonRequestObject.message = $("#firstPost").val();
		var req = JSON.stringify(jsonRequestObject);
		$.ajax({
	        type: "POST",
	        url: "/share/proxy/alfresco/cignex/addons/topic",
	        dataType: "json",
	        data: req,
			cache:false,
			async:false,
	        contentType: "application/json",
	        success: function(data) {
	        	if(data['error'] != null){
	        		if(data['error'].indexOf("Access") == -1){
	            		message('Error encountered: '+data['error']);
	        		} else {
	        			message('Access Denied to create new topic. Please ask owner of this content to provide with necessary access');
	        		}
	        	} else if(data['topicNodeRef'] != null){
	        			var newTopicHtml = $("#topics").html() + "<option value=\""+data['topicNodeRef']+"\">"+$("#topicName").val()+"</option>";
		        		$("#createTopicDialog").dialog("close");
		        		message("New Topic is created");
		        		$("#topics").html(newTopicHtml);
		        		$("#conversation").show();
	        	} else {
	        		message("There was a problem creating topic");
	        	}
	        },
	        error: function(xhr, status, error) {
	    		$("#createTopicDialog").dialog("close");
	    		message('Reponse Status: '+xhr.status);
	        }
		});
	} else {
		$("#createTopicDialog").dialog("close");
		message("NodeRef parameter missing in the page url");
	}
}

function createPost(){
	var topicNodeRef = $("#topics").val();
	if(topicNodeRef == null || topicNodeRef == "" || topicNodeRef == "undefined"){
		$("#postText").val("");
		message("Please select the topic");
		return;
	}
	var jsonRequestObject = new Object();
	jsonRequestObject.topicNodeRef = topicNodeRef;
	jsonRequestObject.message = $("#postText").val();
	var req = JSON.stringify(jsonRequestObject);
	$.ajax({
        type: "POST",
        url: "/share/proxy/alfresco/cignex/addons/topic-post",
        dataType: "json",
        data: req,
		cache:false,
		async:false,
        contentType: "application/json",
        success: function(data) {
        	if(data['error'] != null){
        		$("#postText").val("");
        		if(data['error'].indexOf("Access") == -1){
            		message('Error encountered: '+data['error']);
        		} else {
        			message('Access Denied to reply on this topic. Please ask owner of this content to provide with necessary access');
        		}
        	} else {
        		message("Message posted");
        		$("#postText").val("");
        	}
        },
        error: function(xhr, status, error) {
    		$("#postText").val("");
    		message('Reponse Status: '+xhr.status);
        }
	});
}


function searchUsers(){
	var userName = $("#userNameQuery").val();
	if(userName == null || userName == "" || userName == "undefined" || userName.length < 3){
		return;
	}
	$.ajax({
        type: "GET",
        url: "/share/proxy/alfresco/api/people?filter="+userName,
        dataType: "json",
		cache:false,
		async:false,
        contentType: "application/json",
        success: function(data) {
    		var userListHtml = "";
        	$.each(data.people, function(i,person){
        		userListHtml += "<option value=\""+person.userName+"\">"+person.firstName+" "+person.lastName+"</option>"
        	});
        	$("#userList").html(userListHtml);
        },
        error: function(xhr, status, error) {
    		message('Reponse Status: '+xhr.status);
        }
	});
}


function inviteUsers(){
	var userNameList = "";
	$("#userList option:selected").each(function () {
		userNameList += $(this).val() + ";";
	});
	if(userNameList != ""){
		userNameList = userNameList.substring(0, userNameList.lastIndexOf(";"));
		if(userNameList != ""){
			var jsonRequestObject = new Object();
			jsonRequestObject.topicNodeRef = $("#topics").val();
			jsonRequestObject.userNameList = userNameList;
			var req = JSON.stringify(jsonRequestObject);
			$.ajax({
		        type: "POST",
		        url: "/share/proxy/alfresco/cignex/addons/discussioninvite",
		        dataType: "json",
		        data: req,
				cache:false,
				async:false,
		        contentType: "application/json",
		        success: function(data) {
		        	$("#userListPopup").dialog("close");
		        	if(data['error'] != null){
		        		messageWithoutAutoClose('Error encountered: '+data['error']);
		        	} else {
		        		message("User invitation sent");
		        	}		        	
		        },
		        error: function(xhr, status, error) {
		        	$("#userListPopup").dialog("close");
		    		message('Reponse Status: '+xhr.status);
		        }
			});
		} else {
			message("Please select users from the list");
		}
	} else {
		message("Please select users from the list");
	}
}

function openInviteUserPopup(){
	if($("#topics").val() != ""){
		$("#userListPopup").dialog("open");
		$("#inviteUsers").focus();
	} else {
		message("Please select the topic to invite users");
	}
	
}

function openTopicDialog(){
	$("#createTopicDialog").dialog("open");
}

function message(textMessage){
	textMessage = '<span>'+textMessage+'</span>';
	$("#dialogMessage").find('div').html(textMessage);
	$( "#dialogMessage" ).dialog( "open" );
	setTimeout(function() 
	{ 
		$("#dialogMessage").dialog("close"); 
	}, 5000);
}

function messageWithoutAutoClose(textMessage){
	textMessage = '<span>'+textMessage+'</span>';
	$("#dialogMessage").find('div').html(textMessage);
	$( "#dialogMessage" ).dialog( "open" );
}


function refreshPosts(){
	setTimeout(function() 
	{ 
		loadPosts($("#topics").val()); 
		refreshPosts();
	}, 5000);
}