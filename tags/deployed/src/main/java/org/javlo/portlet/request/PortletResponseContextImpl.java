package org.javlo.portlet.request;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.ResourceURLProvider;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.portlet.PortletResponsePropertyListener;
import org.javlo.portlet.PortletURLProviderImpl;
import org.javlo.portlet.PortletWindowImpl;
import org.javlo.portlet.ResourceURLProviderImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

public abstract class PortletResponseContextImpl extends PortletContextImpl implements PortletResponseContext {

	private PortletResponsePropertyListener listener;
	protected final PortletURLProvider urlProvider;
	
	public PortletResponseContextImpl(
			PortletContainer portletContainer, PortletWindow portletWindow,
			HttpServletRequest req, HttpServletResponse resp, PortletURLProvider.TYPE type) {

		super(portletContainer, portletWindow, req, resp);
		
		this.urlProvider = getPortletURLProvider(type);

		// TODO : several listeners
		this.listener = ((PortletWindowImpl)portletWindow).getComponent();
	}

	@Override
	public ResourceURLProvider getResourceURLProvider() {
		ContentContext ctx = (ContentContext) getContainerRequest().getAttribute(ContentContext.CONTEXT_REQUEST_KEY);
		if (ctx != null) {
			return new ResourceURLProviderImpl(URLHelper.createAbsoluteURL(ctx, ""));
		} else {
			String basePath = getContainerRequest().getScheme() + "://" + getContainerRequest().getServerName();
			if (getContainerRequest().getServerPort() != 80) {
				basePath = basePath + ":" + getContainerRequest().getServerPort();
			}
			return new ResourceURLProviderImpl(basePath);
		}
	}

	protected PortletURLProvider getPortletURLProvider(TYPE type) {
		ContentContext ctx = (ContentContext) getContainerRequest().getAttribute(ContentContext.CONTEXT_REQUEST_KEY);
//		ContentContext newCtx = new ContentContext(ctx);
//		newCtx.setContentLanguage(ctx.getRequestContentLanguage());
		if (ctx != null) {
			return new PortletURLProviderImpl(type, getPortletWindow(), URLHelper.createURL(ctx), getContainer().getContainerServices().getPortalContext());
		} else {
			return new PortletURLProviderImpl(type, getPortletWindow(), getContainerRequest().getRequestURI(), getContainer().getContainerServices().getPortalContext());
		}
	}

	@Override
	public void addProperty(Cookie cookie) {
		getServletResponse().addCookie(cookie);
	}

	@Override
	public void addProperty(String key, Element element) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProperty(String key, String value) {
		listener.onPropertySet(getContainerRequest(), key, value);
	}

	@Override
	public Element createElement(String tagName) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}
}
