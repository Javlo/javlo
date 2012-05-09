/**
 * 
 */
package org.javlo.portlet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.portlet.Event;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.EventCoordinationService;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.om.portlet.EventDefinitionReference;

public class EventCoordinationServiceImpl implements EventCoordinationService {

	private final ServletContext ctx;

	public EventCoordinationServiceImpl(ServletContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void processEvents(
			PortletContainer container, PortletWindow portletWindow,
			HttpServletRequest request, HttpServletResponse response, List<Event> events) {

		PortletManager portletManager = PortletManager.getInstance(ctx);
		
		Set<String> publishingEventNames = new HashSet<String>();
		for (EventDefinitionReference publishingEvent : portletWindow.getPortletDefinition().getSupportedPublishingEvents()) {
			publishingEventNames.add(publishingEvent.getName());
		}
		for (Event event : events) {
			if (publishingEventNames.contains(event.getName())) {
				for (PortletWindowImpl pw : portletManager.getPortletWindows()) {
					Set<String> processingEventNames = new HashSet<String>();
					for (EventDefinitionReference processingEvent : pw.getPortletDefinition().getSupportedProcessingEvents()) {
						processingEventNames.add(processingEvent.getName());
					}
					if (processingEventNames.contains(event.getName())) {
						try {
							portletManager.getPortletContainer().doEvent(pw, request, response, event);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				PortletEventListener listener = ((PortletWindowImpl) portletWindow).getComponent();
				if (listener != null) {
					listener.onEvent(request, event);
				}
			}
		}
	}
}