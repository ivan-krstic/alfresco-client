package me.krstic.model;

import org.apache.chemistry.opencmis.client.api.Document;

import me.krstic.utility.Base64Converter;

public class AlfrescoDocument extends AlfrescoObject {

	private String mimetype;
	private byte[] content;
	private String versioningState;
	
	public AlfrescoDocument() {
	}
	
	public AlfrescoDocument(Document document) {
		this.setId(document.getId());
		this.setName(document.getName());
		this.setDescription(document.getDescription());
		this.setType(document.getType().getDisplayName());
		this.setCreatedBy(document.getCreatedBy());
		this.setCreatedDate(document.getCreationDate().getTime().toString());
		this.setModifiedBy(document.getLastModifiedBy());
		this.setModifiedDate(document.getLastModificationDate().getTime().toString());
		this.setParentId(document.getParents().get(0).getId());
		this.setParentName(document.getParents().get(0).getName());
		this.setMimetype(document.getContentStreamMimeType());
		this.setContent(Base64Converter.encodeBase64(document.getContentStream()));
		this.setVersioningState(document.getVersionLabel());
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getVersioningState() {
		return versioningState;
	}

	public void setVersioningState(String versioningState) {
		this.versioningState = versioningState;
	}
}
