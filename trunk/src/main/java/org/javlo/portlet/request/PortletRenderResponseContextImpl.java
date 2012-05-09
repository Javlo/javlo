package org.javlo.portlet.request;

import java.util.Collection;

import javax.portlet.PortletMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletWindow;

public class PortletRenderResponseContextImpl extends PortletMimeResponseContextImpl implements PortletRenderResponseContext {

	public PortletRenderResponseContextImpl(
			PortletContainer portletContainer, PortletWindow portletWindow,
			HttpServletRequest req, HttpServletResponse resp) {
		
		super(portletContainer, portletWindow, req, resp, PortletURLProvider.TYPE.RENDER);
	}

	@Override
	public void setNextPossiblePortletModes(Collection<PortletMode> portletModes) {
		// not supported
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}
}
