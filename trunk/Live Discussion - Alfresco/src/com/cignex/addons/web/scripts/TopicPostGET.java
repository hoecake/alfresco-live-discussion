package com.cignex.addons.web.scripts;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.cignex.util.Constants;

public class TopicPostGET extends DeclarativeWebScript{
	
	private ServiceRegistry serviceRegistry;

	private TicketComponent ticketComponent;
	
	public TicketComponent getTicketComponent() {
		return ticketComponent;
	}

	public void setTicketComponent(TicketComponent ticketComponent) {
		this.ticketComponent = ticketComponent;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	protected Map<String, Object> executeImpl(final WebScriptRequest request, final Status status, final Cache cache){
		Map<String, Object> model = new HashMap<String, Object>();
		try
		{
			String topicNodeRefParam = request.getParameter(Constants.PARAM_TOPICNODEREF);
			String errorMessage = "";
			JSONArray postListJSON = new JSONArray();
			JSONObject resultJSON = new JSONObject();
			JSONObject errorJSON = new JSONObject();
			if(null != topicNodeRefParam && !topicNodeRefParam.isEmpty() && NodeRef.isNodeRef(topicNodeRefParam)){
				final NodeRef topicNodeRef = new NodeRef(topicNodeRefParam);
				List<ChildAssociationRef> childAssocList = this.serviceRegistry.getNodeService().getChildAssocs(topicNodeRef);
				Map<Long, JSONObject> postsMap = new HashMap<Long, JSONObject>();
				List<Long> dateStampList = new LinkedList<Long>();
				for(ChildAssociationRef childAssocRef : childAssocList){
					final Map<QName, Serializable> properties = this.serviceRegistry.getNodeService().getProperties(childAssocRef.getChildRef());
					JSONObject postJSON = new JSONObject();
					if(null != properties.get(ContentModel.PROP_CONTENT)){
			            ContentReader reader = this.serviceRegistry.getContentService().getReader(childAssocRef.getChildRef(), ContentModel.PROP_CONTENT);
			            postJSON.put(Constants.PARAM_CONTENT, null != reader ? reader.getContentString() : "");
					} else {
						postJSON.put(Constants.PARAM_CONTENT, "");
					}
					postJSON.put(Constants.PARAM_DATE, properties.get(ContentModel.PROP_MODIFIED));
					postJSON.put(Constants.PARAM_AUTHOR, properties.get(ContentModel.PROP_MODIFIER));
					if(this.ticketComponent.getUsersWithTickets(true).contains((String)properties.get(ContentModel.PROP_MODIFIER))){
						postJSON.put(Constants.PARAM_ONLINE, Boolean.TRUE.toString());
					} else {
						postJSON.put(Constants.PARAM_ONLINE, Boolean.FALSE.toString());
					}
					postsMap.put(((Date)properties.get(ContentModel.PROP_MODIFIED)).getTime(), postJSON);
					dateStampList.add(((Date)properties.get(ContentModel.PROP_MODIFIED)).getTime());
				}
				
				Collections.sort(dateStampList);
				Collections.reverse(dateStampList);
				for(Long modifiedTime : dateStampList){
					postListJSON.put(postsMap.get(modifiedTime));
				}

				resultJSON.put(Constants.PARAM_POSTS, postListJSON);
			} else {
				errorMessage = "Topic NodeRef parameter is incorrect or not found";
			}
			
			if(errorMessage.isEmpty()){
				model.put(Constants.KEY_RESULT, resultJSON.toString());
			} else {
				errorJSON.put("error", errorMessage);
				model.put(Constants.KEY_RESULT, (new JSONObject().put(Constants.PARAM_ERROR, errorMessage)).toString());
			}
			
		} catch (Exception e){
			e.printStackTrace();
			model.put(Constants.KEY_RESULT, "{\""+Constants.PARAM_ERROR+"\" : \""+e.getMessage()+"\"}");
		}
		return model;	
	}
}
