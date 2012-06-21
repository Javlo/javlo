package org.javlo.portlet.request;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletWindow;

public class PortletRequestContextImpl extends PortletContextImpl implements PortletRequestContext {

	private PortletConfig portletConfig;
	private ServletContext servletContext;
	private Map<String,String[]> properties;
	
	public PortletRequestContextImpl(PortletContainer portletContainer, PortletWindow portletWindow,
			HttpServletRequest containerRequest, HttpServletResponse containerResponse) {
		
		super(portletContainer, portletWindow, containerRequest, containerResponse);
	}
	
	@Override
	public void init(PortletConfig portletConfig, ServletContext servletContext,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		
		super.init(servletRequest, servletResponse);

		this.portletConfig = portletConfig;
		this.servletContext = servletContext;
		
		this.properties = new HashMap<String, String[]>();
		String[] remoteAddr = { getContainerRequest().getRemoteAddr() };
		this.properties.put("REMOTE_ADDR", remoteAddr);
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public PortletConfig getPortletConfig() {
		return portletConfig;
	}

	@Override
	public Object getAttribute(String name) {
		return getContainerRequest().getAttribute(name);
	}

	@Override
	public Object getAttribute(String name, ServletRequest servletRequest) {
		return servletRequest.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return getContainerRequest().getAttributeNames();
	}

	@Override
	public void setAttribute(String name, Object value) {
		getServletRequest().setAttribute(name, value);
	}

	@Override
	public Cookie[] getCookies() {
		return getServletRequest().getCookies();
	}

	@Override
	public Locale getPreferredLocale() {
		return getServletRequest().getLocale();
	}

	@Override
	public Map<String, String[]> getPrivateParameterMap() {
		return getServletRequest().getParameterMap();
	}

	@Override
	public Map<String, String[]> getProperties() {
		return properties;
	}

	@Override
	public Map<String, String[]> getPublicParameterMap() {
		return Collections.emptyMap();
	}

	@Override
	public Object getAttribute(String name, ServletRequest servletRequest) {
		//TODO : check this method, I have fo this only for compilation.
		return servletRequest.getAttribute(name);
	}
}
