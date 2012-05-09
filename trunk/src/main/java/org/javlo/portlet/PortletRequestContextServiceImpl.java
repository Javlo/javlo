package org.javlo.portlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletEventResponseContext;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletRequestContextService;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.javlo.portlet.request.PortletActionResponseContextImpl;
import org.javlo.portlet.request.PortletRenderResponseContextImpl;
import org.javlo.portlet.request.PortletRequestContextImpl;
import org.javlo.portlet.request.PortletResourceRequestContextImpl;
import org.javlo.portlet.request.PortletResourceResponseContextImpl;
import org.javlo.portlet.request.PortletStateAwareResponseContextImpl;

public class PortletRequestContextServiceImpl implements PortletRequestContextService {

	@Override
	public PortletResourceResponseContext getPortletResourceResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		return new PortletResourceResponseContextImpl(container, window, containerRequest, containerResponse);
	}
	
	@Override
	public PortletResourceRequestContext getPortletResourceRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		return new PortletResourceRequestContextImpl(container, window, containerRequest, containerResponse);
	}
	
	@Override
	public PortletRenderResponseContext getPortletRenderResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		return new PortletRenderResponseContextImpl(container, window, containerRequest, containerResponse);
	}
	
	@Override
	public PortletRequestContext getPortletRenderRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		return new PortletRequestContextImpl(container, window, containerRequest, containerResponse);
	}
	
	@Override
	public PortletEventResponseContext getPortletEventResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		return new PortletStateAwareResponseContextImpl(container, window, containerRequest, containerResponse, null);
	}
	
	@Override
	public PortletRequestContext getPortletEventRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		return new PortletRequestContextImpl(container, window, containerRequest, containerResponse);
	}
	
	@Override
	public PortletActionResponseContext getPortletActionResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		return new PortletActionResponseContextImpl(container, window, containerRequest, containerResponse);
	}
	
	@Override
	public PortletRequestContext getPortletActionRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		return new PortletRequestContextImpl(container, window, containerRequest, containerResponse);
	}
}
