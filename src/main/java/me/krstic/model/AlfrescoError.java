package me.krstic.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlfrescoError {

	private String type;
	private String message;
	private String date;
	
	public AlfrescoError() {
		this.type = "cmis:error";
		this.date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
	}
	
	public AlfrescoError(String message) {
		this.type = "cmis:error";
		this.message = message;
		this.date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
	}
	
	public String getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public String getDate() {
		return date;
	}
}
