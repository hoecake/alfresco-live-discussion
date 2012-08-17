package com.cignex.addons.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.cignex.util.Constants;
import com.cignex.util.ServiceUtil;

public class TopicPostPOST  extends DeclarativeWebScript{
	
	private ServiceRegistry serviceRegistry;

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
			ServiceUtil serviceUtil = new ServiceUtil();
			JSONObject requestParamJSON = new JSONObject(null != request.getContent() ? request.getContent().getContent() : "");
			String topicNodeRefParam = requestParamJSON.getString(Constants.PARAM_TOPICNODEREF);
			String messageParam = requestParamJSON.getString(Constants.PARAM_MESSAGE);
			JSONObject resultJSON = new JSONObject();
			if(serviceUtil.checkParam(topicNodeRefParam) && NodeRef.isNodeRef(topicNodeRefParam)){
				NodeRef topicNodeRef = new NodeRef(topicNodeRefParam);
				serviceUtil.createPost(messageParam, topicNodeRef, this.serviceRegistry);
			    resultJSON.put(Constants.PARAM_SUCCESS, true);
			} else {
				resultJSON.put(Constants.PARAM_ERROR,"Unable to create forum");
			}
			model.put(Constants.KEY_RESULT, resultJSON.toString());
		} catch (Exception e){
			e.printStackTrace();
			model.put(Constants.KEY_RESULT, "{\""+Constants.PARAM_ERROR+"\" : \""+e.getMessage()+"\"}");
		}
		return model;	
	}
}
