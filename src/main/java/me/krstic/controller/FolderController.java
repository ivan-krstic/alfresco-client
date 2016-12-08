package me.krstic.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.krstic.model.AlfrescoFolder;
import me.krstic.model.AlfrescoResponse;
import me.krstic.service.AlfrescoRestService;
import me.krstic.utility.Base64Converter;

@RestController
//@RequestMapping("/alfresco")
public class FolderController {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FolderController.class);
	
	private AlfrescoRestService alfrescoRestService;
	
	@Autowired
	public FolderController(AlfrescoRestService alfrescoRestService) {
		this.alfrescoRestService = alfrescoRestService;
	}

	public FolderController() {
	}

	@RequestMapping("/")
	public ResponseEntity<Object> list(@RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.getTree();
	}
	
	@RequestMapping(value = "/folder", method = RequestMethod.POST)
	public ResponseEntity<Object> create(@RequestBody AlfrescoFolder folder, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.setFolder(folder);
	}

	@RequestMapping(value = "/folder", params = {"id"}, method = RequestMethod.GET)
	public ResponseEntity<AlfrescoResponse> readById(@RequestParam(name="id") String id, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.getFolderById(id.toLowerCase());
	}
	
	@RequestMapping(value = "/folder", params = {"name", "page", "size"}, method = RequestMethod.GET)
	public ResponseEntity<AlfrescoResponse> readByName(@RequestParam(name="name") String name, @RequestParam(name="page") int page, @RequestParam(name="size") int size, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.getFolderByName(name.toLowerCase(), page, size);
	}

	@RequestMapping(value = "/folder", method = RequestMethod.PUT)
	public ResponseEntity<Object> update(@RequestBody AlfrescoFolder document, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.updateFolder(document);
	}

	@RequestMapping(value = "/folder", params = {"id"}, method = RequestMethod.DELETE)
	public ResponseEntity<Object> delete(@RequestParam(name="id") String id, @RequestHeader("Authorization") String auth) {
		String[] authorization = Base64Converter.decodeBase64Authorization(auth);
		
		alfrescoRestService.setUsername(authorization[0]);
		alfrescoRestService.setPassword(authorization[1]);
		
		return alfrescoRestService.deleteFolder(id.toLowerCase());
	}
}
