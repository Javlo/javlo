package org.javlo.portlet;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.Event;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.driver.PlutoServices;
import org.apache.pluto.container.driver.PortalDriverServices;
import org.apache.pluto.container.driver.PortletRegistryEvent;
import org.apache.pluto.container.driver.PortletRegistryListener;
import org.apache.pluto.container.driver.RequiredContainerServices;
import org.apache.pluto.container.impl.PortletContainerImpl;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.driver.container.PortalDriverServicesImpl;
import org.javlo.component.portlet.AbstractPortletWrapperComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.portlet.request.HttpServletRequestWrapper;
import org.javlo.service.PublishListener;
import org.javlo.service.RequestService;


/**
 * @author plemarchand
 *
 */
public class PortletManager implements PortletRegistryListener, PublishListener {

	protected static Logger logger = Logger.getLogger(PortletManager.class.getName());
	
	private final static String PORTLET_WINDOWS_ATTR_PREFIX = "portlet_windows_";
	private final static String PORTLET_MODE_ATTR_PREFIX = "portlet_mode_";

	private final PortletContainer portletContainer;
	
	private final Map<String,PortletDefinition> portlets = new HashMap<String,PortletDefinition>();

	private final Map<String, Map<String, WeakReference<PortletWindowImpl>>> windows = new HashMap<String, Map<String, WeakReference<PortletWindowImpl>>>();

	
	private PortletManager(final ServletContext ctx) {
		RequiredContainerServices requiredServices = new PortalContainerServices(ctx);
		PortalDriverServices services = new PortalDriverServicesImpl(requiredServices, null);

		services.getPortletRegistryService().addPortletRegistryListener(this);
		
		// initializes singleton for PortletServlet to be able to register
		new PlutoServices(services);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			portletContainer = new PortletContainerImpl("Javlo Portlet Container", services);
			portletContainer.init();
		} catch (PortletContainerException e) {
			throw new InstantiationError();
		}

		// TODO should register itself on some PublishManager
		Collection<PublishListener> listeners = (Collection<PublishListener>) ctx.getAttribute(PublishListener.class.getName());
		if (listeners == null) {
			listeners = new ArrayList<PublishListener>();
			ctx.setAttribute(PublishListener.class.getName(), listeners);
		}
		listeners.add(this);
	}

	public static PortletManager getInstance(ServletContext ctx) {
		PortletManager instance = (PortletManager) ctx.getAttribute(PortletManager.class.getName());
		if (instance == null) {
			instance = new PortletManager(ctx);
			ctx.setAttribute(PortletManager.class.getName(), instance);
		}
		return instance;
	}

	@Override
	public void portletApplicationRegistered(PortletRegistryEvent event) {
		synchronized (portlets) {
			for (PortletDefinition pd : event.getPortletApplication().getPortlets()) {
				logger.info("adding portlet: " + pd.getPortletName());
				this.portlets.put(pd.getPortletName(), pd);
			}
		}
	}

	@Override
	public void portletApplicationRemoved(PortletRegistryEvent event) {
		List<? extends PortletDefinition> portletsToRemove = event.getPortletApplication().getPortlets();
		synchronized (portlets) {
			for (PortletDefinition pd : portletsToRemove) {
				logger.info("removing portlet: " + pd.getPortletName());
				
				// do not remove from portlet list as it seems to happen randomly (remove windows, though)
				//portlets.remove(pd.getPortletName());
			}
		}

		synchronized (windows) {
			for (Map<String, WeakReference<PortletWindowImpl>> portletWindows : windows.values()) {
				Iterator<WeakReference<PortletWindowImpl>> iter = portletWindows.values().iterator();
				while (iter.hasNext()) {
					PortletWindowImpl pw = iter.next().get();
					if (pw == null) {
						iter.remove();
					} else if (portletsToRemove.contains(pw.getPortletDefinition())) {
						releasePortletWindows(pw.getComponent(), pw.getSession());
						iter.remove();
					}
				}
			}
		}
	}


	public PortletContainer getPortletContainer() {
		return portletContainer;
	}

	/**
	 * @return a Map with portletName as keys and context paths as values
	 */
	public Map<String, PortletDefinition> getPortlets() {
		return Collections.unmodifiableMap(portlets);
	}
	
	/**
	 * @return a Map with portlet id's as keys and portlet windows as values for all portlet modes
	 */
	public Collection<PortletWindowImpl> getPortletWindows() {
		Collection<PortletWindowImpl> result = new ArrayList<PortletWindowImpl>();
		synchronized (windows) {
			for (Map<String, WeakReference<PortletWindowImpl>> modeWindows : windows.values()) {
				Iterator<WeakReference<PortletWindowImpl>> iter = modeWindows.values().iterator();
				while (iter.hasNext()) {
					PortletWindowImpl pw = iter.next().get();
					if (pw == null) {
						iter.remove();
					} else {
						result.add(pw);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * @param comp id and portletName must be well set in comp
	 * @param ctx
	 * @return a created window if not found
	 */
	public PortletWindowImpl getPortletWindow(AbstractPortletWrapperComponent comp, ContentContext ctx) {
		return getPortletWindow(comp, ctx, false);
	}
	private PortletWindowImpl getPortletWindow(AbstractPortletWrapperComponent comp, ContentContext ctx, boolean remove) {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String portletModeParam = requestService.getParameter("portletMode", null);

		PortletMode portletMode;
		if (portletModeParam != null) {
			portletMode = new PortletMode(portletModeParam);
		} else {
			portletMode = (PortletMode) ctx.getRequest().getSession().getAttribute(PORTLET_MODE_ATTR_PREFIX + comp.getId());
		}
		List<PortletMode> portletModes = comp.getPortletModes(ctx.getRenderMode());
		if (portletMode == null || !portletModes.contains(portletMode)) {
			if (portletModes.size() > 0) {
				portletMode = portletModes.get(0);
			} else {
				portletMode = PortletMode.VIEW;
			}
		}
		ctx.getRequest().getSession().setAttribute(PORTLET_MODE_ATTR_PREFIX + comp.getId(), portletMode);
		
		Map<String, PortletWindowImpl> sessionWindows = (Map<String, PortletWindowImpl>) ctx.getRequest().getSession().getAttribute(PORTLET_WINDOWS_ATTR_PREFIX + comp.getId());
		if (sessionWindows == null) {
			sessionWindows = new HashMap<String, PortletWindowImpl>();
			ctx.getRequest().getSession().setAttribute(PORTLET_WINDOWS_ATTR_PREFIX + comp.getId(), sessionWindows);
		}

		String windowIDPrefix = comp.getWindowIdPrefix(ctx.getRenderMode(), portletMode);
		PortletWindowImpl pw;
		if (remove) {
			pw = sessionWindows.remove(windowIDPrefix);
		} else {
			pw = sessionWindows.get(windowIDPrefix);
			if (pw == null) {
				pw = createPortletWindow(comp, windowIDPrefix, ctx);
				if (pw != null) {
					sessionWindows.put(windowIDPrefix, pw);
				}
			}
			if (pw != null) {
				if (!portletMode.equals(pw.getPortletMode())) {
					pw.setPortletMode(portletMode);
				}
				String windowStateParam = requestService.getParameter("windowState", null);
				if (windowStateParam != null && (pw.getWindowState() == null || !windowStateParam.equals(pw.getWindowState().toString()))) {
					pw.setWindowState(new WindowState(windowStateParam));
				}
			}
		}
		return pw;
	}
	
	public void releasePortletWindows(AbstractPortletWrapperComponent comp, HttpSession session) {
		try {
			session.removeAttribute(PORTLET_WINDOWS_ATTR_PREFIX + comp.getId());
			session.removeAttribute(PORTLET_MODE_ATTR_PREFIX + comp.getId());
		} catch (Exception e) {
		}
	}
	
	private PortletWindowImpl createPortletWindow(final AbstractPortletWrapperComponent comp, String windowPrefix, final ContentContext ctx) {
		PortletWindowImpl pw = null;
		final PortletDefinition pd = this.portlets.get(comp.getPortletName());
		if (pd != null) {
			
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String hostPrefix = StringHelper.stringToFileName(globalContext.getContextKey());

			final String windowId = windowPrefix + hostPrefix + ctx.getPath()
					+ '/' + ctx.getRequestContentLanguage() + '/' + comp.getId();
			
			// TODO: id shouold be unique ?
			pw = new PortletWindowImpl(windowId, comp, pd, ctx);

			HttpServletRequest req = new HttpServletRequestWrapper(ctx, pw.getId().getStringId());
			try {
				portletContainer.doLoad(pw, req, ctx.getResponse());
				portletContainer.doEvent(pw, req, ctx.getResponse(), new Event() {
					
					@Override
					public Serializable getValue() {
						return comp.getPortletValue(ctx);
					}
					@Override
					public QName getQName() {
						return new QName(pd.getApplication().getDefaultNamespace(), getName());
					}
					@Override
					public String getName() {
						return comp.getInitPortletValueEventName();
					}
				});

				synchronized (windows) {
					String portletKey = pw.getComponent().getId();
					Map<String, WeakReference<PortletWindowImpl>> portletWindows = windows.get(portletKey);
					if (portletWindows == null) {
						portletWindows = new HashMap<String, WeakReference<PortletWindowImpl>>();
						windows.put(portletKey, portletWindows);
					}
					String windowKey = pw.getId().toString() + "_" + ctx.getRequest().getSession().getId();
					portletWindows.put(windowKey, new WeakReference<PortletWindowImpl>(pw));
				}
			} catch (Exception e) {
				logger.log(Level.INFO, "error loading Portlet: " + pw.getId().getStringId(), e);
				pw = null;
			}
		}
		return pw;
	}

	// TODO be sure all possible windows are created, to propagate to all windowIds
	public void deleteComponent(final AbstractPortletWrapperComponent comp, final ContentContext ctx) {
		synchronized (windows) {
			Map<String, WeakReference<PortletWindowImpl>> portletWindows = windows.get(comp.getId());
			if (portletWindows != null) {
				for (WeakReference<PortletWindowImpl> ref : portletWindows.values()) {
					PortletWindowImpl pw = ref.get();
					if (pw != null) {
						releasePortletWindows(pw.getComponent(), pw.getSession());

						final PortletDefinition pd = this.portlets.get(comp.getPortletName());
						HttpServletRequest req = new HttpServletRequestWrapper(ctx, pw.getId().getStringId());

						try {
							portletContainer.doEvent(pw, req, ctx.getResponse(), new Event() {
								
								@Override
								public Serializable getValue() {
									return true;
								}
								@Override
								public QName getQName() {
									return new QName(pd.getApplication().getDefaultNamespace(), getName());
								}
								@Override
								public String getName() {
									return comp.getDeletePortletEventName();
								}
							});

						} catch (Exception e) {
							logger.log(Level.INFO, "error removing Portlet: " + pw.getId().getStringId(), e);
						}
					}
				}
				windows.remove(comp.getId());
			}
		}
	}

	@Override
	public void onPublish(ContentContext ctx) {
		synchronized (windows) {
			Iterator<Map<String, WeakReference<PortletWindowImpl>>> iter = windows.values().iterator();
			while (iter.hasNext()) {
				for (WeakReference<PortletWindowImpl> ref : iter.next().values()) {
					PortletWindowImpl pw = ref.get();
					if (pw != null) {
						releasePortletWindows(pw.getComponent(), pw.getSession());
					}
				}
				iter.remove();
			}
		}
		windows.clear();
	}

	@Override
	protected void finalize() throws Throwable {
		portletContainer.destroy();
		super.finalize();
	}
}
