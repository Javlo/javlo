package org.javlo.service.messaging;

import java.util.Date;

import org.javlo.helper.StringHelper;

public class Message {
	
	private static final String SP = " - ";
	
	private String id = null;
	private String userName;
	private String body;
	private Date date = null;

	public Message() {
		id = StringHelper.getRandomId();
		date = new Date();
	}
	
	public Message(String body) {
		id = StringHelper.getRandomId();
		date = new Date();
		this.body = body;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	@Override
	public String toString() {		
		return StringHelper.renderTime(date)+SP+id+SP+userName+SP+body;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
