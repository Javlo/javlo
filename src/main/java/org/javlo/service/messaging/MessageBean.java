package org.javlo.service.messaging;

import org.javlo.context.ContentContext;

public class MessageBean {
	
	private String body;
	private String userName;
	private boolean mine = true;
	
	public MessageBean (ContentContext ctx, Message msg) {
		this.setBody(msg.getBody());
		this.setUserName(msg.getUserName());
		mine = ctx.getCurrentUserIdNeverNull().equals(msg.getUserName());
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isMine() {
		return mine;
	}

	public void setMine(boolean mine) {
		this.mine = mine;
	}

}
