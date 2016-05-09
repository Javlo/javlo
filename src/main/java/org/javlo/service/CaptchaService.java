package org.javlo.service;

import javax.servlet.http.HttpSession;

public class CaptchaService {
	
	private String currentCaptchaCode = null;
	
	private String question = null; 
	
	private static final String KEY = "captchaService";
	
	public static final CaptchaService getInstance(HttpSession session) {
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

	public String getQuestion() {
		if (question == null) {
			int number1 = (int)Math.round(Math.random()*5+1);
			int number2 = (int)Math.round(Math.random()*5+1);
			setCurrentCaptchaCode(""+(number1+number2));
			question = number1+" + "+number2;
		}		
		return question;
	}

	public void reset() {
		this.question = null;
		setCurrentCaptchaCode("");
	}

}
