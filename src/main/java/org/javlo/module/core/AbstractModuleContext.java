package org.javlo.module.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.javlo.bean.LinkToRenderer;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;

/**
 * represent the context of a module, for create a context extends this class and for instanciate you class use getInstance with you class for implementationClass param.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public abstract class AbstractModuleContext {

	private static Logger logger = Logger.getLogger(AbstractModuleContext.class.getName());

	private static final String KEY = "moduleContext";
	protected I18nAccess i18nAccess = null;
	protected Module module;
	private String currentLink;

	public static final AbstractModuleContext getInstance(HttpSession session, GlobalContext globalContext, Module module, Class<? extends AbstractModuleContext> implementationClass) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {		
		final String KEY = implementationClass.getName() + '_' + globalContext.getContextKey();
		Object context = session.getAttribute(KEY);
		if (context == null) {
			AbstractModuleContext outCtx = implementationClass.newInstance();
			outCtx.i18nAccess = I18nAccess.getInstance(globalContext, session);
			outCtx.module = module;
			outCtx.init();
			session.setAttribute(KEY, outCtx);
			context = outCtx;
		}
		session.setAttribute(getKey(), context);
		return (AbstractModuleContext) context;
	}

	public  static final AbstractModuleContext getCurrentInstance(HttpSession session) {
		AbstractModuleContext outContext = (AbstractModuleContext)session.getAttribute(getKey());				
		return outContext;
	}

	protected static String getKey() {
		return KEY;
	}

	/**
	 * get the navigation of the module
	 * 
	 * @return
	 */
	public abstract List<LinkToRenderer> getNavigation();

	public abstract LinkToRenderer getHomeLink();

	public abstract void init();

	public String getCurrentLink() {
		if (currentLink == null) {
			if (getHomeLink() != null) {
				return getHomeLink().getName();
			} else {
				logger.severe("error no current link defined : " + this.getClass().getName());
				return null;
			}
		}
		return currentLink;
	}

	public void setCurrentLink(String currentLink) {
		this.currentLink = currentLink;
	}

	/**
	 * when user click on a generic navigation, navigation engine call this method for change the renderer, by default module.setRenderer is called but it can be the renderer of a box.
	 * 
	 * @param renderer
	 *            link to a jsp file.
	 */
	public void setRendererFromNavigation(String renderer) {
		module.setRenderer(renderer);
	}
}
