package org.javlo.test.servlet;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FakeHttpContext {
	
	private HttpServletRequest servletContext;
	private TestSession session;
	
	private FakeHttpContext() throws MalformedURLException {
		init();
	}
	
	public static FakeHttpContext getInstance() {
		try {
			return new FakeHttpContext();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public TestRequest getRequest(String url) {
		try {
			return new TestRequest(session, url);
		} catch (MalformedURLException e) {		
			e.printStackTrace();
			return null;
		}
	}

	public HttpServletResponse getResponse() {
		return new TestResponse();
	}
	
	public void init() throws MalformedURLException {		
		 session = new TestSession();
		 session.setServletContext(new TestServletContext());
	}

}
