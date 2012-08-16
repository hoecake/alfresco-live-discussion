package com.cignex.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.forums.ForumsBean;
import org.alfresco.web.ui.common.Utils;

public class ServiceUtil {

	public NodeRef createPost(String messageParam, NodeRef topicNodeRef, ServiceRegistry serviceRegistry){
		final String fileName = ForumsBean.createPostFileName();
		  
		NodeRef postNodeRef = serviceRegistry.getFileFolderService().create(topicNodeRef,
			      fileName, ForumModel.TYPE_POST).getNodeRef();
		  
		// apply the titled aspect - title and description
		Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(3, 1.0f);
		titledProps.put(ContentModel.PROP_TITLE, fileName);
		serviceRegistry.getNodeService().addAspect(postNodeRef, ContentModel.ASPECT_TITLED, titledProps);
		  
		Map<QName, Serializable> editProps = new HashMap<QName, Serializable>(1, 1.0f);
		editProps.put(ApplicationModel.PROP_EDITINLINE, true);
		serviceRegistry.getNodeService().addAspect(postNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, editProps);
		  
		// get a writer for the content and put the file
		ContentWriter writer = serviceRegistry.getContentService().getWriter(postNodeRef, ContentModel.PROP_CONTENT, true);
		// set the mimetype and encoding
		writer.setMimetype(serviceRegistry.getMimetypeService().guessMimetype(fileName));
		writer.setEncoding("UTF-8");
		writer.putContent(Utils.replaceLineBreaks(messageParam, false));
		
		return postNodeRef;
	}

	public NodeRef createForumNodeRef(NodeRef discussingNodeRef, ServiceRegistry serviceRegistry) {
        NodeRef forumNodeRef = null;
        
        if (!serviceRegistry.getNodeService().hasAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE))
        {
            // Add the discussable aspect
            serviceRegistry.getNodeService().addAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
        }
        
        // The discussion aspect create the necessary child
        List<ChildAssociationRef> destChildren = serviceRegistry.getNodeService().getChildAssocs(
              discussingNodeRef,
              ForumModel.ASSOC_DISCUSSION,
              RegexQNamePattern.MATCH_ALL);
        // Take the first one
        if (destChildren.size() == 0)
        {
           // Drop the aspect and recreate it.  This should not happen, but just in case ...
        	serviceRegistry.getNodeService().removeAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE);
        	serviceRegistry.getNodeService().addAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
           // The discussion aspect create the necessary child
           destChildren = serviceRegistry.getNodeService().getChildAssocs(
                 discussingNodeRef,
                 ForumModel.ASSOC_DISCUSSION,
                 RegexQNamePattern.MATCH_ALL);
        }
        if (destChildren.size() == 0)
        {
           throw new AlfrescoRuntimeException("The discussable aspect behaviour is not creating a topic");
        }
        else
        {
           // We just take the first one
           ChildAssociationRef discussionAssoc = destChildren.get(0);
           forumNodeRef = discussionAssoc.getChildRef();
        }
        
        return forumNodeRef;
	}

	public boolean checkParam(String param){
		return (null != param && !param.isEmpty());
	}

}
