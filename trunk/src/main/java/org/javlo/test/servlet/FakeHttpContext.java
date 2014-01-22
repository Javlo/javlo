package org.javlo.test.servlet;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FakeHttpContext {
	
	private HttpServletRequest servletContext;
	private TestSession session;
	
	private FakeHttpContext() throws MalformedURLException {
		init();
	}
	
	public static FakeHttpContext getInstance() throws MalformedURLException {
		return new FakeHttpContext();
	}

	public TestRequest getRequest(String url) throws MalformedURLException {
		return new TestRequest(session, url);
	}

	public HttpServletResponse getResponse() {
		return new TestResponse();
	}
	
	public void init() throws MalformedURLException {		
		 session = new TestSession();
		 session.setServletContext(new TestServletContext());
	}

}
