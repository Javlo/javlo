package org.javlo.test.servlet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.javlo.utils.IteratorAsEnumeration;

public class TestServletContext implements ServletContext {

	private Map<String, Object> attributes = new HashMap<String, Object>();

	@Override
	public Object getAttribute(String key) {		
		return attributes.get(key);
	}

	@Override
	public Enumeration getAttributeNames() {
		return new IteratorAsEnumeration(attributes.keySet().iterator());
	}


	@Override
	public ServletContext getContext(String arg0) {		
		return null;
	}

	@Override
	public String getContextPath() {
		return "/";
	}

	@Override
	public String getInitParameter(String arg0) {
		return null;
	}

	@Override
	public Enumeration getInitParameterNames() {
		return null;
	}

	@Override
	public int getMajorVersion() {
		return 0;
	}

	@Override
	public String getMimeType(String arg0) {
		return null;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}

	@Override
	public String getRealPath(String path) {
		return path;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		return new ByteArrayInputStream(new byte[0]);
	}

	@Override
	public Set getResourcePaths(String arg0) {
		return null;
	}

	@Override
	public String getServerInfo() {
		return null;
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		return null;
	}

	@Override
	public String getServletContextName() {
		return "test";
	}

	@Override
	public Enumeration getServletNames() {
		return null;
	}

	@Override
	public Enumeration getServlets() {
		return null;
	}

	@Override
	public void log(String log) {
		System.out.println(log);
	}

	@Override
	public void log(Exception e, String log) {
		log(log);
		e.printStackTrace();
		
	}

	@Override
	public void log(String log, Throwable t) {
		log(log);
		t.printStackTrace();
	}

	@Override
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}	

}
