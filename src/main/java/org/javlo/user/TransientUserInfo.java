package org.javlo.user;

import javax.servlet.http.HttpSession;

public class TransientUserInfo {
	
	private static final String SESSION_KEY = "transientUserInfo";
	
	private String token;

	private TransientUserInfo() {
	}
	
	public static TransientUserInfo getInstance(HttpSession session) {
		TransientUserInfo outUserInfo = (TransientUserInfo)session.getAttribute(SESSION_KEY);
		if (outUserInfo == null) {
			outUserInfo = new TransientUserInfo();
			session.setAttribute(SESSION_KEY, outUserInfo);
		}
		return outUserInfo;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
