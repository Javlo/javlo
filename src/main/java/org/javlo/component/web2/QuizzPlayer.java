package org.javlo.component.web2;

public class QuizzPlayer {
	
	public QuizzPlayer(String sessionId) {
		super();
		this.sessionId = sessionId;
	}

	private String sessionId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
