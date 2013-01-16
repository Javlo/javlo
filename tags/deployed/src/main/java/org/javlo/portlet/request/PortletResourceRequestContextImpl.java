package org.javlo.portlet.request;

import java.util.Collections;
import java.util.Map;

import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.apache.pluto.container.PortletWindow;

public class PortletResourceRequestContextImpl extends PortletRequestContextImpl implements PortletResourceRequestContext {

	private final String resourceID;
	
	public PortletResourceRequestContextImpl(
			PortletContainer portletContainer, PortletWindow portletWindow,
			HttpServletRequest containerRequest, HttpServletResponse containerResponse) {

		super(portletContainer, portletWindow, containerRequest, containerResponse);
		
		this.resourceID = containerRequest.getParameter("javlo-portlet-resource");
	}

	@Override
	public String getCacheability() {
		return ResourceURL.PAGE;
	}

	@Override
	public Map<String, String[]> getPrivateRenderParameterMap() {
		return Collections.emptyMap();
	}

	@Override
	public String getResourceID() {
		return resourceID;
	}

}
