package org.javlo.portlet.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.Event;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.pluto.container.EventProvider;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletEventResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletWindow;

public class PortletStateAwareResponseContextImpl extends PortletResponseContextImpl implements PortletEventResponseContext {

	private final List<Event> events = new ArrayList<Event>();
	
	public PortletStateAwareResponseContextImpl(
			PortletContainer portletContainer, PortletWindow portletWindow,
			HttpServletRequest req, HttpServletResponse resp, PortletURLProvider.TYPE type) {
		
		super(portletContainer, portletWindow, req, resp, type);
	}

	@Override
	public EventProvider getEventProvider() {
		return new EventProvider() {
			
			@Override
			public Event createEvent(final QName name, final Serializable value) throws IllegalArgumentException {
				return new Event() {
					
					@Override
					public Serializable getValue() {
						return value;
					}
					
					@Override
					public QName getQName() {
						return name;
					}
					
					@Override
					public String getName() {
						return name.getLocalPart();
					}
				};
			}
		};
	}

	@Override
	public List<Event> getEvents() {
		return events;
	}

	@Override
	public PortletMode getPortletMode() {
		return urlProvider.getPortletMode();
	}

	@Override
	public Map<String, String[]> getPublicRenderParameters() {
		return urlProvider.getPublicRenderParameters();
	}

	@Override
	public Map<String, String[]> getRenderParameters() {
		return urlProvider.getRenderParameters();
	}

	@Override
	public WindowState getWindowState() {
		return urlProvider.getWindowState();
	}

	@Override
	public void setPortletMode(PortletMode portletMode) {
		if (portletMode != null) {
			urlProvider.setPortletMode(portletMode);
		}
	}

	@Override
	public void setWindowState(WindowState windowState) {
		if (windowState != null) {
			urlProvider.setWindowState(windowState);
		}
	}
}
