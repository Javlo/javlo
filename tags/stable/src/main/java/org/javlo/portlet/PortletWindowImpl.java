package org.javlo.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpSession;

import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.javlo.component.portlet.AbstractPortletWrapperComponent;
import org.javlo.context.ContentContext;


/**
 * session scope
 */
public class PortletWindowImpl implements PortletWindow {

	private final PortletWindowID portletWindowID;
	private PortletMode portletMode = PortletMode.VIEW;
	private WindowState windowState = WindowState.NORMAL;
	private final PortletDefinition portletDefinition;
	private final AbstractPortletWrapperComponent component; // app scope (1 per render mode)
	private final transient HttpSession session;
	

	public PortletWindowImpl(final String id, AbstractPortletWrapperComponent comp, PortletDefinition pd, ContentContext ctx) {
		this.component = comp;
		this.portletDefinition = pd;
		this.session = ctx.getRequest().getSession();

		this.portletWindowID = new PortletWindowID() {
			public String getStringId() {
				return id;
			}
		};
	}


	public HttpSession getSession() {
		return session;
	}

	public AbstractPortletWrapperComponent getComponent() {
		return component;
	}

	@Override
	public PortletWindowID getId() {
		return portletWindowID;
	}

	public void setPortletMode(PortletMode portletMode) {
		this.portletMode = portletMode;
	}

	@Override
	public PortletMode getPortletMode() {
		return portletMode;
	}

	public void setWindowState(WindowState windowState) {
		this.windowState = windowState;
	}

	@Override
	public WindowState getWindowState() {
		return windowState;
	}

	@Override
	public PortletDefinition getPortletDefinition() {
		return portletDefinition;
	}
}
