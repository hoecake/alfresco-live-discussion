package com.cignex.addons.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.Status;

import com.cignex.util.Constants;

public class TopicGET extends DeclarativeWebScript{

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
			String nodeRefParam = request.getParameter(Constants.PARAM_NODEREF);
			String errorMessage = "";
			JSONArray topicListJSON = new JSONArray();
			JSONObject resultJSON = new JSONObject();
			JSONObject errorJSON = new JSONObject();
			
			if(null != nodeRefParam && !nodeRefParam.isEmpty() && NodeRef.isNodeRef(nodeRefParam)){
				NodeRef nodeRef = new NodeRef(nodeRefParam);
				String path = this.serviceRegistry.getNodeService().getPath(nodeRef).toPrefixString(this.serviceRegistry.getNamespaceService());
				String query = "PATH:'"+ path + "/*'" + " AND TYPE:'fm:forum'";
				ResultSet resultSet = this.serviceRegistry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);
				if(null != resultSet && resultSet.getNodeRefs().size() == 1){
					query = "PATH:'"+ path + "/fm:discussion/*'" + " AND TYPE:'fm:topic'";
					resultSet = this.serviceRegistry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);
					for(ResultSetRow row : resultSet){
						JSONObject topicJSON = new JSONObject();
						topicJSON.put(Constants.PARAM_NODEREF, row.getNodeRef().toString());
						topicJSON.put(Constants.PARAM_NAME, this.serviceRegistry.getNodeService().getProperty(row.getNodeRef(), ContentModel.PROP_NAME));
						topicListJSON.put(topicJSON);
					}
				}
				resultJSON.put(Constants.PARAM_TOPICS, topicListJSON);
			} else {
				errorMessage = "NodeRef parameter is incorrect or not found";
			}
			
			if(errorMessage.isEmpty()){
				model.put(Constants.KEY_RESULT, resultJSON.toString());
			} else {
				errorJSON.put(Constants.PARAM_ERROR, errorMessage);
				model.put(Constants.KEY_RESULT, (new JSONObject().put(Constants.PARAM_ERROR, errorMessage)).toString());
			}
		}catch (Exception e){
			e.printStackTrace();
			model.put(Constants.KEY_RESULT, "{\""+Constants.PARAM_ERROR+"\" : \""+e.getMessage()+"\"}");
		}
		return model;	
	}
}
