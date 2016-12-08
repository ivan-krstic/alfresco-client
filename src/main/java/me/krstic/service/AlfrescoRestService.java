package me.krstic.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import me.krstic.model.AlfrescoDocument;
import me.krstic.model.AlfrescoError;
import me.krstic.model.AlfrescoFolder;
import me.krstic.model.AlfrescoObject;
import me.krstic.model.AlfrescoPage;
import me.krstic.model.AlfrescoResponse;
import me.krstic.utility.Base64Converter;

@Service
public class AlfrescoRestService {

	private static final Logger log = LoggerFactory.getLogger(AlfrescoRestService.class);
	
	SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
	Map<String, String> parameter = new HashMap<String, String>();
	Session session;
	
	private String username;
	private String password;

	@Value("${alfresco.rest.server}")
	private String alfrescoRestServerPath;

	@Value("${alfresco.server}")
	private String alfrescoServerPath;
	
	OperationContext operationContext;

	public void connect() {
		parameter.put(SessionParameter.USER, username);
		parameter.put(SessionParameter.PASSWORD, password);
		parameter.put(SessionParameter.ATOMPUB_URL, alfrescoServerPath);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		List<Repository> repositories = null;
		try {
			repositories = sessionFactory.getRepositories(parameter);
			session = repositories.get(0).createSession();
			
			operationContext = session.createOperationContext();
			operationContext.setCacheEnabled(false);
		} catch (CmisUnauthorizedException e) {
			log.error("CmisUnauthorizedException.");
		}
	}

	public ResponseEntity<Object> getTree() {
		connect();
		
		AlfrescoFolder folder = new AlfrescoFolder();
		
		if (!session.getRepositoryInfo().getCapabilities().isGetDescendantsSupported()) {
			return new ResponseEntity<Object>(new AlfrescoError(""), HttpStatus.NOT_IMPLEMENTED);
		} else {
			folder.setId(session.getRootFolder().getId());
			folder.setName(session.getRootFolder().getName());
			folder.setDescription(session.getRootFolder().getDescription());
			folder.setCreatedBy(session.getRootFolder().getCreatedBy());
			folder.setCreatedDate(session.getRootFolder().getCreationDate().getTime().toString());
			folder.setModifiedBy(session.getRootFolder().getLastModifiedBy());
			folder.setType(session.getRootFolder().getType().getDisplayName());
			
		    for (Tree<FileableCmisObject> t : session.getRootFolder().getDescendants(-1)) {
		    	folder.addChild(getChild(t));
		    }
		}
		
		return new ResponseEntity<Object>(folder, HttpStatus.OK);
	}

	public ResponseEntity<AlfrescoResponse> getFolderById(String id) {
		connect();
	
		if (id == null || id.isEmpty()) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Folder ID must be set.")), HttpStatus.BAD_REQUEST);
		}
		try {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoFolder((Folder)session.getObject(id, operationContext), true)), HttpStatus.OK);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Folder could not be found with ID: " + id)), HttpStatus.NOT_FOUND);
		}

	}
	
	public ResponseEntity<AlfrescoResponse> getFolderByName(String name, int page, int objectsPerPage) {
		connect();
		
//		operationContext.setMaxItemsPerPage(objectsPerPage);
		
		if (name == null || name.isEmpty()) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Folder name must be set.")), HttpStatus.BAD_REQUEST);
		}
		try {
			List<AlfrescoObject> objects = new ArrayList<>();
			
			int totalPages = 0;
			int totalObjects = 0;
			String previous = null;
			String next = null;
			
			String query = "SELECT * FROM cmis:folder WHERE cmis:name LIKE '%" + name + "%'";
			
			ItemIterable<QueryResult> queryResult = session.query(query, false, operationContext);
			totalObjects = (int)queryResult.getTotalNumItems();
			totalPages = totalObjects / objectsPerPage;
			
			for (QueryResult item : queryResult.skipTo(page*objectsPerPage).getPage(objectsPerPage)) {
				try {
					AlfrescoObject object = new AlfrescoFolder((Folder) session.getObject(item.getPropertyByQueryName("cmis:objectId").getFirstValue().toString(), operationContext), false);
					
					objects.add(object);
				} catch (Exception e) {
					log.error("Folder searchedByName with id: " + item.getPropertyByQueryName("cmis:objectId").getFirstValue() + " not founded.");
				}
			}
			if (page < totalPages) {
				next = createLinkForFolder("name", name, page+1, objectsPerPage);
			}
			if (page > 0) {
				previous = createLinkForFolder("name", name, page-1, objectsPerPage);
			}
			
			return new ResponseEntity<AlfrescoResponse>(new AlfrescoResponse(objects, new AlfrescoPage(page, objectsPerPage, totalObjects, totalPages, previous, next)), HttpStatus.OK);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Folder could not be found: " + name)), HttpStatus.NOT_FOUND);
		}
	}
	
	public ResponseEntity<Object> setFolder(AlfrescoFolder folder) {
		connect();
		
		if (folder.getParentId() == null || folder.getParentId().isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Creating folder failed. Parent ID must be set."), HttpStatus.BAD_REQUEST);
		}
		if (folder.getName() == null || folder.getName().isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Creating folder failed. Name must be set."), HttpStatus.BAD_REQUEST);
		}
		
		Folder parent = null;
		try {
			parent = (Folder)session.getObject(folder.getParentId(), operationContext);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Creating folder failed. Parent could not be found: " + folder.getParentId()), HttpStatus.NOT_FOUND);
		}
			
		Map<String, String> newFolderProps = new HashMap<String, String>();		
		newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		newFolderProps.put(PropertyIds.NAME, folder.getName());
		newFolderProps.put(PropertyIds.DESCRIPTION, folder.getDescription());
		newFolderProps.put(PropertyIds.PARENT_ID, folder.getParentId());
		
		try {
			return new ResponseEntity<Object>(new AlfrescoFolder(parent.createFolder(newFolderProps), true), HttpStatus.OK);
		} catch (CmisUnauthorizedException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Creating folder failed. Unauthorized action."), HttpStatus.UNAUTHORIZED);
		}
	}
	
	public ResponseEntity<Object> updateFolder(AlfrescoFolder folder) {
		connect();
		
		if (folder.getId() == null || folder.getId().isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Updating folder failed. ID must be set."), HttpStatus.BAD_REQUEST);
		}
		if (folder.getName() == null || folder.getName().isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Updating folder failed. Name must be set."), HttpStatus.BAD_REQUEST);
		}
		
		Folder oldFolder = null;
		try {
			oldFolder = (Folder)session.getObject(folder.getId(), operationContext);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Updating folder failed. Folder could not be found: " + folder.getId()), HttpStatus.NOT_FOUND);
		}
			
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PropertyIds.NAME, folder.getName());
		properties.put(PropertyIds.DESCRIPTION, folder.getDescription());
		
		try {
			oldFolder.updateProperties(properties, true);
		} catch (CmisUnauthorizedException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Updating folder failed. Unauthorized action."), HttpStatus.UNAUTHORIZED);
		}
			
		return new ResponseEntity<Object>(new AlfrescoFolder(oldFolder, true), HttpStatus.OK);
	}
	
	public ResponseEntity<Object> deleteFolder(String folderId) {
		connect();
		
		if (folderId == null || folderId.isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Removing folder failed. ID must be set."), HttpStatus.BAD_REQUEST);
		}
		
		Folder folder = null;
		try {
			folder = (Folder)session.getObject(folderId, operationContext);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Removing folder failed. Folder could not be found: " + folderId), HttpStatus.NOT_FOUND);
		}
		
		Folder parent = null;
		try {
			parent = (Folder)session.getObject(folder.getParentId(), operationContext);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Removing folder failed. Folder could not be found: " + folder.getParentId()), HttpStatus.NOT_FOUND);
		}
			
		folder.delete(true);
			
		return new ResponseEntity<Object>(new AlfrescoFolder(parent, true), HttpStatus.OK);
	}
	
	public ResponseEntity<AlfrescoResponse> getDocumentById(String id) {
		connect();
		
		if (id == null || id.isEmpty()) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Document ID must be set.")), HttpStatus.BAD_REQUEST);
		}
		try {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoDocument((Document) session.getObject(id, operationContext))), HttpStatus.OK);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Document could not be found with ID: " + id)), HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<AlfrescoResponse> getDocumentByName(String name, int page, int objectsPerPage) {
		connect();
		
//		operationContext.setMaxItemsPerPage(size);
		
		if (name == null || name.isEmpty()) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Document NAME must be set.")), HttpStatus.BAD_REQUEST);
		}
		try {
			List<AlfrescoObject> objects = new ArrayList<>();
			
			int totalPages = 0;
			int totalObjects = 0;
			String previous = null;
			String next = null;
			
			String query = "SELECT * FROM cmis:document WHERE cmis:name LIKE '%" + name + "%'";
			
			ItemIterable<QueryResult> queryResult = session.query(query, false, operationContext);
			totalObjects = (int)queryResult.getTotalNumItems();
			totalPages = totalObjects / objectsPerPage;
			
			for (QueryResult item : queryResult.skipTo(page*objectsPerPage).getPage(objectsPerPage)) {
				try {
					AlfrescoObject object = new AlfrescoDocument((Document) session.getObject(item.getPropertyByQueryName("cmis:objectId").getFirstValue().toString(), operationContext));
					
					((AlfrescoDocument)object).setContent(null);
					
					objects.add(object);
				} catch (Exception e) {
					log.error("Document searchedByName with ID: " + item.getPropertyByQueryName("cmis:objectId").getFirstValue() + " not founded.");
				}
			}
			if (objects.size() != 0) {
				if (page < totalPages) {
					next = createLinkForDocument("name", name, page+1, objectsPerPage);
				}
				if (page > 0) {
					previous = createLinkForDocument("name", name, page-1, objectsPerPage);
				}
				
				return new ResponseEntity<AlfrescoResponse>(new AlfrescoResponse(objects, new AlfrescoPage(page, objectsPerPage, totalObjects, totalPages, previous, next)), HttpStatus.OK);
			} else {
				return new ResponseEntity<AlfrescoResponse>(new AlfrescoResponse(objects, new AlfrescoPage(page, objectsPerPage, totalObjects, totalPages, null, null)), HttpStatus.NOT_FOUND);
			}
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Document could not be found.")), HttpStatus.NOT_FOUND);
		}
	}
	
	public ResponseEntity<AlfrescoResponse> getDocumentByCreatedBy(String createdBy, int page, int objectsPerPage) {
		connect();
		
		if (createdBy == null || createdBy.isEmpty()) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Document createdBy must be set.")), HttpStatus.BAD_REQUEST);
		}
		try {
			List<AlfrescoObject> objects = new ArrayList<>();
			
			int totalPages = 0;
			int totalObjects = 0;
			String previous = null;
			String next = null;
			
			String query = "SELECT * FROM cmis:document WHERE cmis:createdBy LIKE '%" + createdBy + "%'";
			
			ItemIterable<QueryResult> queryResult = session.query(query, false, operationContext);
			totalObjects = (int)queryResult.getTotalNumItems();
			totalPages = totalObjects / objectsPerPage;
			
			for (QueryResult item : queryResult.skipTo(page*objectsPerPage).getPage(objectsPerPage)) {
				try {
					AlfrescoObject object = new AlfrescoDocument((Document) session.getObject(item.getPropertyByQueryName("cmis:objectId").getFirstValue().toString(), operationContext));
					
					((AlfrescoDocument)object).setContent(null);
					
					objects.add(object);
				} catch (Exception e) {
					log.error("Document searchedByCreatedBy with id: " + item.getPropertyByQueryName("cmis:objectId").getFirstValue() + " not founded.");
				}
			}
			if (objects.size() != 0) {
				if (page < totalPages) {
					next = createLinkForDocument("createdBy", createdBy, page+1, objectsPerPage);
				}
				if (page > 0) {
					previous = createLinkForDocument("createdBy", createdBy, page-1, objectsPerPage);
				}
				
				return new ResponseEntity<AlfrescoResponse>(new AlfrescoResponse(objects, new AlfrescoPage(page, objectsPerPage, totalObjects, totalPages, previous, next)), HttpStatus.OK);
			} else {
				return new ResponseEntity<AlfrescoResponse>(new AlfrescoResponse(objects, new AlfrescoPage(page, objectsPerPage, totalObjects, totalPages, null, null)), HttpStatus.NOT_FOUND);
			}
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Document could not be found.")), HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<AlfrescoResponse> getDocumentByContent(String content, int page, int objectsPerPage) {
		connect();
		
		if (content == null || content.isEmpty()) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Document CONTENT must be set.")), HttpStatus.BAD_REQUEST);
		}
		try {
			List<AlfrescoObject> objects = new ArrayList<>();
			
			int totalPages = 0;
			int totalObjects = 0;
			String previous = null;
			String next = null;
			
			String query = "SELECT * FROM cmis:document WHERE CONTAINS('" + content + "')";
			
			ItemIterable<QueryResult> queryResult = session.query(query, false, operationContext);
			totalObjects = (int)queryResult.getTotalNumItems();
			totalPages = totalObjects / objectsPerPage;
			
			for (QueryResult item : queryResult.skipTo(page*objectsPerPage).getPage(objectsPerPage)) {
				try {
					AlfrescoObject object = new AlfrescoDocument((Document) session.getObject(item.getPropertyByQueryName("cmis:objectId").getFirstValue().toString(), operationContext));
					
					((AlfrescoDocument)object).setContent(null);
					
					objects.add(object);
				} catch (Exception e) {
					log.error("Document searchedByContent with id: " + item.getPropertyByQueryName("cmis:objectId").getFirstValue() + " not founded.");
				}
			}
			if (objects.size() != 0) {
				if (page < totalPages) {
					next = createLinkForDocument("content", content, page+1, objectsPerPage);
				}
				if (page > 0) {
					previous = createLinkForDocument("content", content, page-1, objectsPerPage);
				}
				
				return new ResponseEntity<AlfrescoResponse>(new AlfrescoResponse(objects, new AlfrescoPage(page, objectsPerPage, totalObjects, totalPages, previous, next)), HttpStatus.OK);
			} else {
				return new ResponseEntity<AlfrescoResponse>(new AlfrescoResponse(objects, new AlfrescoPage(page, objectsPerPage, totalObjects, totalPages, null, null)), HttpStatus.NOT_FOUND);
			}
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<>(new AlfrescoResponse(new AlfrescoError("Document could not be found.")), HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<Object> setDocument(AlfrescoDocument document) {
		connect();
		
		if (document.getParentId() == null || document.getParentId().isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Creating document failed. Parent ID must be set."), HttpStatus.BAD_REQUEST);
		}
		if (document.getName() == null || document.getName().isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Creating document failed. Name must be set."), HttpStatus.BAD_REQUEST);
		}
		if (document.getMimetype() == null || document.getMimetype().isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Creating document failed. Media type must be set."), HttpStatus.BAD_REQUEST);
		}
		
		Folder parent = null;
		try {
			parent = (Folder)session.getObject(document.getParentId(), operationContext);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Parent folder could not be found: " + document.getParentId()), HttpStatus.NOT_FOUND);
		}
		
		ContentStream contentStream = null;
		
		try {
			if (document.getContent() != null) {
				contentStream = session.getObjectFactory().createContentStream(document.getName(), document.getContent().length, document.getMimetype(), new ByteArrayInputStream(Base64Converter.decodeBase64(document.getContent())));
			} else {
				contentStream = null;
			}
		} catch (CmisUnauthorizedException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Creating document failed. Unauthorized action."), HttpStatus.UNAUTHORIZED);
		}
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		properties.put(PropertyIds.NAME, document.getName());
		properties.put(PropertyIds.DESCRIPTION, document.getDescription());
		properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, document.getMimetype());

		Document doc = parent.createDocument(properties, contentStream, VersioningState.MAJOR);
		
		return new ResponseEntity<Object>(new AlfrescoDocument(doc), HttpStatus.OK);
	}
	
	public ResponseEntity<Object> updateDocument(AlfrescoDocument document) {
		connect();
		
		Document oldDocument = null;
		try {
			oldDocument = (Document) session.getObject(document.getId(), operationContext);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Updating content failed. Document could not be found: " + document.getId()), HttpStatus.NOT_FOUND);
		}
		
		if (!session.getRepositoryInfo().getCapabilities().getContentStreamUpdatesCapability().equals(CapabilityContentStreamUpdates.ANYTIME)) {
			return new ResponseEntity<Object>(new AlfrescoError("Updating content failed. Update without checkout not supported in this repository."), HttpStatus.NOT_IMPLEMENTED);
		} else {
			if (document.getContent() != null && document.getMimetype() != null && document.getMimetype().equalsIgnoreCase(oldDocument.getContentStreamMimeType())) {
				ContentStream contentStream = session.getObjectFactory().createContentStream(document.getName(), document.getContent().length, document.getMimetype(), new ByteArrayInputStream(Base64Converter.decodeBase64(document.getContent())));
				
				try {
					oldDocument.setContentStream(contentStream, true);
				} catch (CmisUnauthorizedException e) {
					return new ResponseEntity<Object>(new AlfrescoError("Updating content failed. Unauthorized action."), HttpStatus.UNAUTHORIZED);
				}
				
				try {
					return new ResponseEntity<Object>(new AlfrescoDocument((Document) session.getObject(document.getId(), operationContext)), HttpStatus.OK);
				} catch (CmisObjectNotFoundException e) {
					return new ResponseEntity<Object>(new AlfrescoError("Updating content failed. Document could not be found: " + document.getId()), HttpStatus.NOT_FOUND);
				}
			} else if (document.getContent() != null && document.getMimetype() != null && !document.getMimetype().equalsIgnoreCase(oldDocument.getContentStreamMimeType())) {
				return new ResponseEntity<Object>(new AlfrescoError("Updating content failed. Media type must be equal with a present."), HttpStatus.BAD_REQUEST);
			} else if (document.getContent() != null && (document.getMimetype() == null || document.getMimetype().isEmpty())) {
				return new ResponseEntity<Object>(new AlfrescoError("Updating content failed. Media type must be set if a content is present."), HttpStatus.BAD_REQUEST);
			} else if (document.getContent() == null && (document.getMimetype() != null && !document.getMimetype().isEmpty())) {
				return new ResponseEntity<Object>(new AlfrescoError("Updating content failed. Content must be set if a media type is present."), HttpStatus.BAD_REQUEST);
			} else {
				Map<String, String> properties = new HashMap<String, String>();
				
				if (document.getName() != null) {
					properties.put(PropertyIds.NAME, document.getName());
				}
				if (document.getDescription() != null) {
					properties.put(PropertyIds.DESCRIPTION, document.getDescription());
				}
				
				try {
					return new ResponseEntity<Object>(new AlfrescoDocument((Document) session.getObject(oldDocument.updateProperties(properties, true), operationContext)), HttpStatus.OK);
				} catch (CmisObjectNotFoundException e) {
					return new ResponseEntity<Object>(new AlfrescoError("Updating content failed. Document could not be found."), HttpStatus.NOT_FOUND);
				}
			}
		}
	}
	
	public ResponseEntity<Object> deleteDocument(String documentId) {
		connect();
		
		if (documentId == null || documentId.isEmpty()) {
			return new ResponseEntity<Object>(new AlfrescoError("Removing document failed. ID must be set."), HttpStatus.BAD_REQUEST);
		}
		
		Document document = null; 
		try {
			document = (Document)session.getObject(documentId, operationContext);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Removing document failed. Document could not be found: " + documentId), HttpStatus.NOT_FOUND);
		}
		
		
		Folder parent = null;
		try {
			parent = (Folder)session.getObject(document.getParents().get(0).getId(), operationContext);
		} catch (CmisObjectNotFoundException e) {
			return new ResponseEntity<Object>(new AlfrescoError("Removing document failed. Document could not be found: " + document.getParents().get(0).getId()), HttpStatus.NOT_FOUND);
		}	
		
		document.delete(true);
			
		return new ResponseEntity<Object>(new AlfrescoFolder(parent, false), HttpStatus.OK);
	}

//	PRIVATE methods
	private AlfrescoFolder getChild(Tree<FileableCmisObject> tree) {
		AlfrescoFolder folder = new AlfrescoFolder();
		
		folder.setId(tree.getItem().getId());
		folder.setName(tree.getItem().getName());
		folder.setDescription(tree.getItem().getDescription());
		folder.setCreatedBy(tree.getItem().getCreatedBy());
		folder.setCreatedDate(tree.getItem().getCreationDate().getTime().toString());
		folder.setModifiedBy(tree.getItem().getLastModifiedBy());
		folder.setType(tree.getItem().getType().getDisplayName());
		folder.setParentId(tree.getItem().getParents().get(0).getId());
		folder.setParentName(tree.getItem().getParents().get(0).getName());

		for (Tree<FileableCmisObject> t : tree.getChildren()) {
			folder.addChild(getChild(t));
	    }
		
		return folder;
	}
	
	private String createLinkForFolder(String searchType, String searchValue, int page, int size) {
		return alfrescoRestServerPath + "folder" + "?" + searchType.toLowerCase() + "=" + searchValue + "&page=" + page + "&size=" + size;
	}
	
	private String createLinkForDocument(String searchType, String searchValue, int page, int size) {
		return alfrescoRestServerPath + "document" + "?" + searchType.toLowerCase() + "=" + searchValue + "&page=" + page + "&size=" + size;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
