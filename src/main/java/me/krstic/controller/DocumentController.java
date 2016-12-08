package me.krstic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.krstic.model.AlfrescoDocument;
import me.krstic.model.AlfrescoResponse;
import me.krstic.service.AlfrescoRestService;
import me.krstic.utility.Base64Converter;

@RestController
//@RequestMapping("/alfresco")
public class DocumentController {

	private AlfrescoRestService alfrescoRestService;
	
	@Autowired
	public DocumentController(AlfrescoRestService alfrescoRestService) {
		this.alfrescoRestService = alfrescoRestService;
	}
	
	@RequestMapping(value = "/document", method = RequestMethod.POST)
	public ResponseEntity<Object> create(@RequestBody AlfrescoDocument document, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.setDocument(document);
	}

	@RequestMapping(value = "/document", params = {"id"}, method = RequestMethod.GET)
	public ResponseEntity<AlfrescoResponse> readById(@RequestParam(name="id") String id, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.getDocumentById(id.toLowerCase());	
	}
	
	@RequestMapping(value = "/document", params = {"name", "page", "size"}, method = RequestMethod.GET)
	public ResponseEntity<AlfrescoResponse> readByName(@RequestParam(name="name") String name, @RequestParam(name="page") int page,  @RequestParam(name="size") int size, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.getDocumentByName(name.toLowerCase(), page, size);	
	}
	
	@RequestMapping(value = "/document", params = {"createdby", "page", "size"}, method = RequestMethod.GET)
	public ResponseEntity<AlfrescoResponse> readByCreatedBy(@RequestParam(name="createdby") String createdby, @RequestParam(name="page") int page,  @RequestParam(name="size") int size, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.getDocumentByCreatedBy(createdby.toLowerCase(), page, size);	
	}
	
	@RequestMapping(value = "/document", params = {"content", "page", "size"}, method = RequestMethod.GET)
	public ResponseEntity<AlfrescoResponse> readByContent(@RequestParam(name="content") String content, @RequestParam(name="page") int page,  @RequestParam(name="size") int size, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.getDocumentByContent(content.toLowerCase(), page, size);	
	}

	@RequestMapping(value = "/document", method = RequestMethod.PUT)
	public ResponseEntity<Object> update(@RequestBody AlfrescoDocument document, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.updateDocument(document);
	}
	
	@RequestMapping(value = "/document", method = RequestMethod.DELETE)
	public ResponseEntity<Object> delete(@RequestParam(name="id") String id, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.deleteDocument(id.toLowerCase());	
	}
}
