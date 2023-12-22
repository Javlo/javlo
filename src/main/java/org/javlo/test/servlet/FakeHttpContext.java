package org.javlo.test.servlet;

import java.net.MalformedURLException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

public class FakeHttpContext {
	
	private TestRequest request;
	private TestResponse response;
	
	public FakeHttpContext(String url) {
		try {
			TestSession session = new TestSession();
			session.setServletContext(new TestServletContext());
			this.request  = new TestRequest(session, url);
			this.response = new TestResponse();
		} catch (MalformedURLException e) {		
			e.printStackTrace();
		}
	}
	
	public TestRequest getRequest() {
		return request;
	}

	public TestResponse getResponse() {
		return response;
	}
	
	public HttpSession getSession() {
		return request.getSession();
	}
	
	public ServletContext getServletContext() {
		return request.getSession().getServletContext();
	}

}
