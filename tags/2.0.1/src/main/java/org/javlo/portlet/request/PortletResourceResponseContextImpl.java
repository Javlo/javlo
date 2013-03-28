package org.javlo.portlet.request;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletWindow;

public class PortletResourceResponseContextImpl extends PortletMimeResponseContextImpl implements PortletResourceResponseContext {

	public PortletResourceResponseContextImpl(
			PortletContainer portletContainer, PortletWindow portletWindow,
			HttpServletRequest req, HttpServletResponse resp) {

		super(portletContainer, portletWindow, req, resp, PortletURLProvider.TYPE.RESOURCE);
	}

	@Override
	public void setCharacterEncoding(String charset) {
		getServletResponse().setCharacterEncoding(charset);
	}

	@Override
	public void setContentLength(int len) {
		getServletResponse().setContentLength(len);
	}

	@Override
	public void setLocale(Locale locale) {
		getServletResponse().setLocale(locale);
	}

}
