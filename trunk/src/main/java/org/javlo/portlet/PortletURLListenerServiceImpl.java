package org.javlo.portlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.PortletURLGenerationListener;

import org.apache.pluto.container.PortletURLListenerService;
import org.apache.pluto.container.om.portlet.Listener;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;

public class PortletURLListenerServiceImpl implements PortletURLListenerService {
	
	private static final Logger log = Logger.getLogger(PortletURLListenerServiceImpl.class.getName());

	@Override
	public List<PortletURLGenerationListener> getPortletURLGenerationListeners(PortletApplicationDefinition app) {
        List<PortletURLGenerationListener> listeners = new ArrayList<PortletURLGenerationListener>();
		for (Listener listener : app.getListeners()) {
            Class<? extends Object> clazz;
            try {
                clazz = Class.forName(listener.getListenerClass());
                if (clazz != null){
                    listeners.add((PortletURLGenerationListener) clazz.newInstance());
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "listener class " + listener.getListenerClass() + "not loaded", e);
            }
		}
		return listeners;
	}
}
