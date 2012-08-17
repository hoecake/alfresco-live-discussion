package com.cignex.addons.web.scripts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.cignex.util.Constants;
import com.cignex.util.ServiceUtil;
import com.ibm.icu.text.SimpleDateFormat;

public class InvitationPOST extends DeclarativeWebScript{

	private ServiceRegistry serviceRegistry;

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> executeImpl(final WebScriptRequest request, final Status status, final Cache cache){
		Map<String, Object> model = new HashMap<String, Object>();
		try
		{
			ServiceUtil serviceUtil = new ServiceUtil();
			JSONObject requestParamJSON = new JSONObject(null != request.getContent() ? request.getContent().getContent() : "");
			String topicNodeRefParam = requestParamJSON.getString(Constants.PARAM_TOPICNODEREF);
			String userNameListParam = requestParamJSON.getString(Constants.PARAM_USERNAMELIST);
			JSONObject resultJSON = new JSONObject();
			if(serviceUtil.checkParam(topicNodeRefParam) && NodeRef.isNodeRef(topicNodeRefParam) && serviceUtil.checkParam(userNameListParam)){
				NodeRef topicNodeRef = new NodeRef(topicNodeRefParam);
				if(this.serviceRegistry.getNodeService().getType(topicNodeRef).equals(ForumModel.TYPE_TOPIC)){
					AuthenticationUtil.setRunAsUserSystem();
					List<String> nonExistingUserList = new ArrayList<String>();
					Set<String> accessDeniedUserSet = new HashSet<String>();
					SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
					List<String> invitedUserList = new ArrayList<String>();
					Set<AccessPermission> permissionSet = this.serviceRegistry.getPermissionService().getAllSetPermissions(topicNodeRef);
					for(String userName : userNameListParam.split(";")){
						if(this.serviceRegistry.getPersonService().personExists(userName)){
							for(AccessPermission perm : permissionSet){
								if(perm.getAuthorityType().equals(AuthorityType.GROUP) && perm.getAccessStatus().equals(AccessStatus.ALLOWED)){
									if(this.serviceRegistry.getAuthorityService().getContainedAuthorities(AuthorityType.USER, perm.getAuthority(), false).contains(userName)){
										invitedUserList.add(userName);
									}
								} else if(perm.getAuthorityType().equals(AuthorityType.EVERYONE) && perm.getAccessStatus().equals(AccessStatus.ALLOWED)){
									invitedUserList.add(userName);
								}
								else if(perm.getAuthorityType().equals(AuthorityType.USER) && perm.getAuthority().equals(userName) && perm.getAccessStatus().equals(AccessStatus.ALLOWED)){
									invitedUserList.add(userName);
								}
							}
							if(!invitedUserList.contains(userName)){
								accessDeniedUserSet.add(userName);
							} else {
								NodeRef personRef = this.serviceRegistry.getPersonService().getPerson(userName);
								if(!this.serviceRegistry.getNodeService().hasAspect(personRef, Constants.ASPECT_INVITABLE)){
									this.serviceRegistry.getNodeService().addAspect(personRef, Constants.ASPECT_INVITABLE, null);
								}
								List<String> invitationList = new ArrayList<String>();
								Serializable valueList = this.serviceRegistry.getNodeService().getProperty(personRef, Constants.PROP_NODEREFS);
								if(null != valueList){
									invitationList = (List<String>) valueList;
								}
								invitationList.add(df.format(new Date())+";"+topicNodeRef.toString()+";"+this.serviceRegistry.getAuthenticationService().getCurrentUserName());
								this.serviceRegistry.getNodeService().setProperty(personRef, Constants.PROP_NODEREFS, (Serializable) invitationList);
							}
						} else {
							nonExistingUserList.add(userName);
						}
					}
					boolean invitationSentToAll = true;
					String message = "Invitation sent to all users but failed for below users<br/>";
					if(nonExistingUserList.size() > 0) {
						message += nonExistingUserList+" users does not exist";
						invitationSentToAll = false;
					}
					if(accessDeniedUserSet.size() > 0){
						message += "<br/>"+accessDeniedUserSet+" users does not have required access to view. Please provide necessary privileges";
						invitationSentToAll = false;
					}
					if(invitationSentToAll){
						resultJSON.put(Constants.PARAM_SUCCESS,"true");
					} else {
						resultJSON.put(Constants.PARAM_ERROR,message);
					}
					if(invitedUserList.size() > 0){
						Action mailAction = this.serviceRegistry.getActionService().createAction(MailActionExecuter.NAME);
						mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "You are invited to a discussion topic");        
						mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, (Serializable)invitedUserList);
						mailAction.setParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, true);
						mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "Hello,\nYou are invited to a discussion topic. Please visit your Alfresco Share Dashboard for more information\n\nThank You");
						this.serviceRegistry.getActionService().executeAction(mailAction, null);								
					}
				} else {
					resultJSON.put(Constants.PARAM_ERROR,"topicNodeRef is not a topic");
				}
			} else {
				resultJSON.put(Constants.PARAM_ERROR,"topicNodeRef Or userNameList parameter is incorrect or not found");
			}
			model.put(Constants.KEY_RESULT, resultJSON.toString());
		}catch (Exception e){
			e.printStackTrace();
			model.put(Constants.KEY_RESULT, "{\""+Constants.PARAM_ERROR+"\" : \""+e.getMessage()+"\"}");
		}
		return model;	
	}
}
