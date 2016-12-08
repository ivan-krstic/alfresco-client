package me.krstic.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;

public class AlfrescoFolder extends AlfrescoObject {

	private List<AlfrescoFolder> children = new ArrayList<AlfrescoFolder>();
	
	public AlfrescoFolder() {
	}
	
	public AlfrescoFolder(Folder folder, boolean showChildren) {
		this.setId(folder.getId());
		this.setName(folder.getName());
		this.setDescription(folder.getDescription());
		this.setCreatedBy(folder.getCreatedBy());
		this.setCreatedDate(folder.getCreationDate().getTime().toString());
		this.setModifiedBy(folder.getLastModifiedBy());
		this.setModifiedDate(folder.getLastModificationDate().getTime().toString());
		this.setType(folder.getType().getDisplayName());
		this.setParentId(folder.getParents().get(0).getId());
		this.setParentName(folder.getParents().get(0).getName());
		
		if (showChildren == true) {
			for (CmisObject o : folder.getChildren()) {
				AlfrescoFolder child = new AlfrescoFolder();
				
				child.setId(o.getId());
				child.setName(o.getName());
				child.setDescription(o.getDescription());
				child.setCreatedBy(o.getCreatedBy());
				child.setCreatedDate(o.getCreationDate().getTime().toString());
				child.setModifiedBy(o.getLastModifiedBy());
				child.setModifiedDate(o.getLastModificationDate().getTime().toString());
				child.setType(o.getType().getDisplayName());
				child.setParentId(this.getId());
				child.setParentName(this.getName());
				
				this.addChild(child);
			}
		}
	}
	
	public List<AlfrescoFolder> getChildren() {
		return children;
	}
	
	public List<AlfrescoFolder> addChild(AlfrescoFolder child) {
		child.setParentId(this.getId());
		child.setParentName(this.getName());
		children.add(child);
		
		return children;
	}

	public void setChildren(List<AlfrescoFolder> children) {
		this.children = children;
	}
}
