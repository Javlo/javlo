package org.javlo.module.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.javlo.bean.LinkToRenderer;
import org.javlo.bean.ParentLink;
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
	protected GlobalContext globalContext;
	private String currentLink;
	private String renderer;
	private final Map<String, Integer> wizardStep = new HashMap<String, Integer>();

	public static final AbstractModuleContext getInstance(HttpSession session, GlobalContext globalContext, Module module, Class<? extends AbstractModuleContext> implementationClass) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
		AbstractModuleContext context = (AbstractModuleContext) globalContext.getSessionAttribute(session, implementationClass.getName());
		if (context == null) {
			AbstractModuleContext outCtx = implementationClass.newInstance();
			outCtx.i18nAccess = I18nAccess.getInstance(globalContext, session);
			outCtx.module = module;
			outCtx.globalContext = globalContext;
			outCtx.init();
			globalContext.setSessionAttribute(session, implementationClass.getName(), outCtx);
			context = outCtx;
		}

		context.i18nAccess = I18nAccess.getInstance(globalContext, session);
		context.module = module;

		session.setAttribute(KEY, context);
		return context;
	}

	public static final AbstractModuleContext getCurrentInstance(HttpSession session) {
		AbstractModuleContext outContext = (AbstractModuleContext) session.getAttribute(KEY);
		return outContext;
	}

	/**
	 * get the navigation of the module
	 * 
	 * @return
	 */
	public abstract List<LinkToRenderer> getNavigation();

	private static void createFlatNavigation(List<LinkToRenderer> outList, LinkToRenderer link) {
		outList.add(link);
		if (link.getChildren() != null) {
			for (ParentLink linkToRenderer : link.getChildren()) {
				createFlatNavigation(outList, (LinkToRenderer) linkToRenderer);
			}
		}
	}

	public List<LinkToRenderer> getFlatNavigation() {
		List<LinkToRenderer> flatNavigation = new LinkedList<LinkToRenderer>();
		for (LinkToRenderer link : getNavigation()) {
			createFlatNavigation(flatNavigation, link);
		}
		return flatNavigation;
	}

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
		this.renderer = renderer;
	}

	public String getRenderer() {
		return renderer;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public int getWizardStep(String boxName) {
		Integer out = wizardStep.get(boxName);
		if (out == null) {
			out = 1;
		}
		return out;
	}

	public void setWizardStep(String boxName, Integer wizardStep) {
		this.wizardStep.put(boxName, wizardStep);
	}

}
