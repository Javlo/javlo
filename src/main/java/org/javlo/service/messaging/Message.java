package org.javlo.service.messaging;

import java.util.Date;

import org.javlo.helper.StringHelper;
import org.javlo.user.IUserInfo;

public class Message {
	
	private static final String SP = " - ";
	
	private String id = null;
	private IUserInfo user;
	private String body;
	private Date date = null;

	public Message() {
		id = StringHelper.getRandomId();
		date = new Date();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IUserInfo  getUser() {
		return user;
	}

	public void setUser(IUserInfo user) {
		this.user = user;
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
		return StringHelper.renderTime(date)+SP+id+SP+user.getLogin()+SP+body;
	}

}
