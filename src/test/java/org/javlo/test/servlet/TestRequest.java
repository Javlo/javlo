package org.javlo.test.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.test.javlo.TestGlobalContext;
import org.javlo.utils.IteratorAsEnumeration;

public class TestRequest implements HttpServletRequest {

	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Map<String, String> parameters = new HashMap<String, String>();
	private URL url;
	private HttpSession session;

	public TestRequest(HttpSession inSession, String inURL) throws MalformedURLException {
		url = new URL(inURL);
		String q = url.getQuery();
		for (String param : StringUtils.split(q, '&')) {
			String[] splittedParam = StringUtils.split(q, '=');
			parameters.put(splittedParam[0], splittedParam[1]);
		}
		session = inSession;
	}

	@Override
	public Object getAttribute(String key) {
		if (key.equals("globalContext")) {
			return TestGlobalContext.getInstance(this);
		} else {
			return attributes.get(key);
		}
	}

	@Override
	public Enumeration getAttributeNames() {
		return new IteratorAsEnumeration(attributes.keySet().iterator());
	}

	@Override
	public String getCharacterEncoding() {
		return ContentContext.CHARACTER_ENCODING;
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return "127.0.0.1";
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 80;
	}

	@Override
	public Locale getLocale() {
		return Locale.ENGLISH;
	}

	@Override
	public Enumeration getLocales() {
		LinkedList<Locale> locales = new LinkedList<Locale>();
		locales.add(getLocale());
		return new IteratorAsEnumeration<Locale>(locales.iterator());
	}

	@Override
	public String getParameter(String key) {
		return parameters.get(key);
	}

	@Override
	public Map getParameterMap() {
		return parameters;
	}

	@Override
	public Enumeration getParameterNames() {
		return new IteratorAsEnumeration(parameters.keySet().iterator());
	}

	@Override
	public String[] getParameterValues(String arg0) {
		String[] outValues = new String[parameters.size()];
		parameters.values().toArray(outValues);
		return outValues;
	}

	@Override
	public String getProtocol() {
		return "http";
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return "255.255.255.255";
	}

	@Override
	public String getRemoteHost() {
		return "remote-host";
	}

	@Override
	public int getRemotePort() {
		return 80;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return url.getHost();
	}

	@Override
	public int getServerPort() {
		if (url.getPort() < 0) {
			return 80;
		} else {
			return url.getPort();
		}
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);

	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		return "";
	}

	@Override
	public Cookie[] getCookies() {
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		return 0;
	}

	@Override
	public String getHeader(String arg0) {
		return null;
	}

	@Override
	public Enumeration getHeaderNames() {
		return Collections.emptyEnumeration();
	}

	@Override
	public Enumeration getHeaders(String arg0) {
		return null;
	}

	@Override
	public int getIntHeader(String arg0) {
		return 0;
	}

	@Override
	public String getMethod() {
		return "get";
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getQueryString() {
		return url.getQuery();
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return url.getPath();
	}

	@Override
	public StringBuffer getRequestURL() {
		StringBuffer outBuffer = new StringBuffer();
		outBuffer.append(getProtocol());
		outBuffer.append("://");
		outBuffer.append(getServerName());
		outBuffer.append(':');
		outBuffer.append(getServerPort());		
		outBuffer.append(getRequestURI());
		return outBuffer;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
