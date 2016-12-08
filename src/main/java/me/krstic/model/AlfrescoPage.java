package me.krstic.model;

public class AlfrescoPage {

	private int page;
	private int objectsPerPage;
	private int totalPages;
	private int totalObjects;
	private String previous;
	private String next;
	
	public AlfrescoPage() {
	}
	
	public AlfrescoPage(int page, int objectsPerPage) {
		this.page = page;
		this.objectsPerPage = objectsPerPage;
	}

	public AlfrescoPage(int page, int objectsPerPage, int totalObjects, int totalPages, String previous, String next) {
		this.page = page;
		this.objectsPerPage = objectsPerPage;
		this.totalObjects = totalObjects;
		this.totalPages = totalPages;
		this.previous = previous;
		this.next = next;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getObjectsPerPage() {
		return objectsPerPage;
	}

	public void setObjectsPerPage(int objectsPerPage) {
		this.objectsPerPage = objectsPerPage;
	}

	public int getTotalObjects() {
		return totalObjects;
	}

	public void setTotalObjects(int totalObjects) {
		this.totalObjects = totalObjects;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}
}
