package org.javlo.portlet;

import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;

public interface PortletEventListener {

	void onEvent(HttpServletRequest req, Event event);

}
