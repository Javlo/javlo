package org.javlo.portlet;

import javax.servlet.http.HttpServletRequest;

public interface PortletResponsePropertyListener {

	void onPropertySet(HttpServletRequest req, String name, String value);
	
}
