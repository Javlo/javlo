package org.javlo.portlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;

import org.javlo.config.StaticConfig;


/**
 * @author plemarchand
 *
 * application scope
 */
public class PortalContextImpl implements PortalContext, Observer {

	public static final String ACTION_KEY_ATTR_NAME = "portal.action-param-key";
	public static final String DEFAULT_ACTION_PARAM_KEY = "javlo-portlet-action";
	
	protected static final Logger logger = Logger.getLogger(PortalContextImpl.class.getName());

	private final ServletContext ctx;
	private Properties properties = new Properties();

	
	public PortalContextImpl(ServletContext ctx) {
		this.ctx = ctx;
		
		StaticConfig config = StaticConfig.getInstance(ctx);
		init(config);
		config.addObserver(this);
	}

	public ServletContext getServletContext() {
		return this.ctx;
	}
	
	private void init(StaticConfig config) {
		synchronized(this.properties) {
			
			// TODO: check potential problems
			this.properties = config.getProperties();
		}
	}
	
	public String getActionParamName() {
		String result = getProperty(ACTION_KEY_ATTR_NAME);
		if (result == null) {
			result = DEFAULT_ACTION_PARAM_KEY;
		}
		return result;
	}
	
	public String getProperty(String name) {
		return properties.getProperty(name);
	}

	public Enumeration<String> getPropertyNames() {
		return Collections.enumeration(properties.stringPropertyNames());
	}

	public Enumeration<PortletMode> getSupportedPortletModes() {
		Set<PortletMode> supportedPortletModes = new HashSet<PortletMode>();
		
// TODO
//		supportedPortletModes.add(PortletMode.HELP);
		supportedPortletModes.add(PortletMode.VIEW);
		supportedPortletModes.add(PortletMode.EDIT);
		supportedPortletModes.add(new PortletMode("admin"));
		
		return Collections.enumeration(supportedPortletModes);
	}

	public Enumeration<WindowState> getSupportedWindowStates() {
		Set<WindowState> supportedWindowStates = new HashSet<WindowState>();
		
		supportedWindowStates.add(WindowState.MINIMIZED);
		supportedWindowStates.add(WindowState.NORMAL);
		supportedWindowStates.add(WindowState.MAXIMIZED);
		
		return Collections.enumeration(supportedWindowStates);
	}

	public String getPortalInfo() {
		return "Javlo/1.4";
	}

	public void update(Observable o, Object arg) {
		if (o instanceof StaticConfig) {
			init((StaticConfig) o);
		}
	}
}
