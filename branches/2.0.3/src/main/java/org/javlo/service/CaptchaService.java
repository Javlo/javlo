package org.javlo.service;

import javax.servlet.http.HttpSession;

public class CaptchaService {
	
	private String currentCaptchaCode = null;
	
	public static final CaptchaService getInstance(HttpSession session) {
		final String KEY = CaptchaService.class.getName();
		CaptchaService outService = (CaptchaService)session.getAttribute(KEY);
		if (outService == null) {
			outService = new CaptchaService();
			session.setAttribute(KEY,outService);
		}
		return outService;
	}

	public String getCurrentCaptchaCode() {
		return currentCaptchaCode;
	}
	
	public void setCurrentCaptchaCode(String currentCaptchaCode) {
		this.currentCaptchaCode = currentCaptchaCode;
	}

}
