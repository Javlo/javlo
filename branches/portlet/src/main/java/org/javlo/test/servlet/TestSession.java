package org.javlo.test.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.javlo.test.javlo.TestGlobalContext;
import org.javlo.utils.IteratorAsEnumeration;

public class TestSession implements HttpSession {
	
	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	private ServletContext servletContext;

	@Override
	public Object getAttribute(String key) {
		if (key.equals("globalContext")) {
			return new TestGlobalContext();
		} else {
			return attributes.get(key);
		}
	}

	@Override
	public Enumeration getAttributeNames() {
		return new IteratorAsEnumeration(attributes.keySet().iterator());
	}

	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public String getId() {
		return "id";
	}

	@Override
	public long getLastAccessedTime() {
		return 0;
	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void putValue(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	@Override
	public void removeValue(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);

	}

	@Override
	public void setMaxInactiveInterval(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
