package org.javlo.portlet.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletWindow;

public abstract class PortletContextImpl {

	private final PortletContainer portletContainer;
	private final PortletWindow portletWindow;
	private final HttpServletRequest containerRequest;
	private final HttpServletResponse containerResponse;

	private HttpServletRequest servletRequest;
	private HttpServletResponse servletResponse;

	public PortletContextImpl(
			PortletContainer portletContainer, PortletWindow portletWindow,
			HttpServletRequest req, HttpServletResponse resp) {
		
		this.portletContainer = portletContainer;
		this.portletWindow = portletWindow;
		this.containerRequest = req;
		this.containerResponse = resp;
	}
	
	public void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		
		this.servletRequest = servletRequest;
		this.servletResponse = servletResponse;
	}
	
	public PortletContainer getContainer() {
		return portletContainer;
	}

	public PortletWindow getPortletWindow() {
		return portletWindow;
	}

	public HttpServletRequest getContainerRequest() {
		return containerRequest;
	}

	public HttpServletResponse getContainerResponse() {
		return containerResponse;
	}

	
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public HttpServletResponse getServletResponse() {
		return servletResponse;
	}
}
