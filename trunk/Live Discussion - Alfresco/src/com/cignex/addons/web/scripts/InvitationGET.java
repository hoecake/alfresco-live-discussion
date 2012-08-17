package com.cignex.addons.web.scripts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.cignex.util.Constants;

public class InvitationGET extends DeclarativeWebScript{
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
			JSONObject resultJSON = new JSONObject();
			JSONArray invitationArrayJSON = new JSONArray();
			NodeRef personRef = this.serviceRegistry.getPersonService().getPerson(this.serviceRegistry.getAuthenticationService().getCurrentUserName());
			if(null != personRef){
				Serializable propValue = this.serviceRegistry.getNodeService().getProperty(personRef, Constants.PROP_NODEREFS);
				if(null != propValue){
					String[] invitationDetails;
					for(String invitationString : (List<String>)propValue){
						invitationDetails = invitationString.split(";");
						if(invitationDetails.length > 2 && NodeRef.isNodeRef(invitationDetails[1])){
							try {
								JSONObject invitation = new JSONObject();
								NodeRef topicNodeRef = new NodeRef(invitationDetails[1]);
								invitation.put(Constants.PARAM_DATE, invitationDetails[0]);
								invitation.put(Constants.PARAM_TOPICS, this.serviceRegistry.getNodeService().getProperty(topicNodeRef, ContentModel.PROP_NAME));
								invitation.put(Constants.PARAM_USERNAME, invitationDetails[2]);
								for(int index = 3; index < invitationDetails.length; index++){ // incase username consists of ;
									invitation.put(Constants.PARAM_USERNAME, invitation.get(Constants.PARAM_USERNAME)+invitationDetails[index]);
								}
								ChildAssociationRef childAssocRef = this.serviceRegistry.getNodeService().getPrimaryParent(topicNodeRef);
								childAssocRef = this.serviceRegistry.getNodeService().getPrimaryParent(childAssocRef.getParentRef());
								invitation.put(Constants.PARAM_TOPICNODEREF, topicNodeRef.toString());
								invitation.put(Constants.PARAM_NODEREF, childAssocRef.getParentRef().toString());
								invitationArrayJSON.put(invitation);
							}catch(Exception e){
								// Skip this entry
							}
						}
					}
					resultJSON.put(Constants.PARAM_INVITATIONS, invitationArrayJSON);
				}
			} else {
				resultJSON.put(Constants.PARAM_ERROR, "User does not exists");
			}
			model.put(Constants.KEY_RESULT, resultJSON.toString());
		}catch (Exception e){
			e.printStackTrace();
			model.put(Constants.KEY_RESULT, "{\""+Constants.PARAM_ERROR+"\" : \""+e.getMessage()+"\"}");
		}
		return model;	
	}

}
