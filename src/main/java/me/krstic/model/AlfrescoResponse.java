package me.krstic.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlfrescoResponse {

	private AlfrescoObject object;
	private List<AlfrescoObject> objects;
	private AlfrescoPage page;
	private AlfrescoError error;
	
	public AlfrescoResponse(AlfrescoObject object) {
		this.object = object;
	}
	
	public AlfrescoResponse(List<AlfrescoObject> objects, AlfrescoPage page) {
		this.objects = objects;
		this.page = page;
	}
	
	public AlfrescoResponse(AlfrescoError error) {
		this.error = error;
	}

	public AlfrescoObject getObject() {
		return object;
	}

	public void setObject(AlfrescoObject object) {
		this.object = object;
	}

	public List<AlfrescoObject> getObjects() {
		return objects;
	}
	
	public List<AlfrescoObject> addObject(AlfrescoObject object) {
		objects.add(object);
		return objects;
	}

	public void setObjects(List<AlfrescoObject> objects) {
		this.objects = objects;
	}

	public AlfrescoPage getPage() {
		return page;
	}

	public void setPage(AlfrescoPage page) {
		this.page = page;
	}

	public AlfrescoError getError() {
		return error;
	}

	public void setError(AlfrescoError error) {
		this.error = error;
	}
}
