package org.javlo.portlet.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletWindow;

public class PortletActionResponseContextImpl extends PortletStateAwareResponseContextImpl implements PortletActionResponseContext {

	public PortletActionResponseContextImpl(
			PortletContainer portletContainer, PortletWindow portletWindow,
			HttpServletRequest req, HttpServletResponse resp) {
		
		super(portletContainer, portletWindow, req, resp, PortletURLProvider.TYPE.ACTION);
	}

	@Override
	public String getResponseURL() {
		return urlProvider.toURL();
	}

	@Override
	public boolean isRedirect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRedirect(String location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRedirect(String location, String renderURLParamName) {
		// TODO Auto-generated method stub

	}
}
