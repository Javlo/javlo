package org.javlo.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortalContext;
import javax.portlet.Portlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;
import javax.servlet.ServletContext;

import org.apache.pluto.container.EventCoordinationService;
import org.apache.pluto.container.FilterManager;
import org.apache.pluto.container.FilterManagerService;
import org.apache.pluto.container.PortletRequestContextService;
import org.apache.pluto.container.PortletURLListenerService;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.RequiredContainerServices;

public class PortalContainerServices implements RequiredContainerServices {

	private final PortalContextImpl portalContext;
	
	
	public PortalContainerServices(ServletContext ctx) {
		portalContext = new PortalContextImpl(ctx);
	}
	
	
	@Override
	public PortalContext getPortalContext() {
		return portalContext;
	}

	@Override
	public PortletURLListenerService getPortletURLListenerService() {
		return new PortletURLListenerServiceImpl();
	}
	
	@Override
	public PortletRequestContextService getPortletRequestContextService() {
		return new PortletRequestContextServiceImpl();
	}
	
	@Override
	public EventCoordinationService getEventCoordinationService() {
		return new EventCoordinationServiceImpl(portalContext.getServletContext());
	}

	@Override
	public FilterManagerService getFilterManagerService() {
		return new FilterManagerService() {
			
			@Override
			public FilterManager getFilterManager(PortletWindow window, String lifeCycle) {
				return new FilterManager() {
					
					@Override
					public void processFilter(EventRequest req, EventResponse res,
							EventPortlet eventPortlet, PortletContext portletContext)
							throws PortletException, IOException {

						eventPortlet.processEvent(req, res);
					}
					
					@Override
					public void processFilter(ResourceRequest req, ResourceResponse res,
							ResourceServingPortlet resourceServingPortlet,
							PortletContext portletContext) throws PortletException, IOException {

						resourceServingPortlet.serveResource(req, res);
					}
					
					@Override
					public void processFilter(RenderRequest req, RenderResponse res,
							Portlet portlet, PortletContext portletContext)
							throws PortletException, IOException {

						portlet.render(req, res);
					}
					
					@Override
					public void processFilter(ActionRequest req, ActionResponse res,
							Portlet portlet, PortletContext portletContext)
							throws PortletException, IOException {

						portlet.processAction(req, res);
					}
				};
			}
		};
	}
}
