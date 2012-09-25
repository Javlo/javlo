package org.javlo.component.core;

import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.utils.TimeMap;

public class ComponentContext {

	private static Logger logger = Logger.getLogger(ComponentContext.class.getName());

	private static final String INSERTION_COMPONENT_KEY = "_insertion_component";

	private static final String KEY = ComponentContext.class.getName();

	private final java.util.List<IContentVisualComponent> newComponentId = new LinkedList<IContentVisualComponent>();

	private boolean renderLink = true;

	public static ComponentContext getInstance(HttpServletRequest request) {
		ComponentContext outInstance = (ComponentContext) request.getAttribute(KEY);
		if (outInstance == null) {
			outInstance = new ComponentContext();
			request.setAttribute(KEY, outInstance);
		}
		return outInstance;
	}

	public void addNewComponent(IContentVisualComponent comp) {
		if (comp != null) {
			newComponentId.add(comp);
		}
	}

	public void clearComponents() {
		newComponentId.clear();
	}

	public IContentVisualComponent[] getNewComponents() {
		IContentVisualComponent[] outComponentId = new IContentVisualComponent[newComponentId.size()];
		newComponentId.toArray(outComponentId);
		return outComponentId;
	}

	public boolean isRenderLink() {
		return renderLink;
	}

	public void setRenderLink(boolean renderLink) {
		this.renderLink = renderLink;
	}

	public static void prepareComponentInsertion(HttpSession session, IContentVisualComponent comp) {
		session.setAttribute(INSERTION_COMPONENT_KEY, comp);
	}

	public static IContentVisualComponent getPreparedComponent(HttpSession session) {
		return (IContentVisualComponent) session.getAttribute(INSERTION_COMPONENT_KEY);
	}

	public static void clearPreparedComponent(HttpSession session) {
		session.removeAttribute(INSERTION_COMPONENT_KEY);
	}

	private Map<String, String> getHelpCache(GlobalContext globalContext) {
		final String KEY = "help-cache";
		Map<String, String> helpCache = (Map<String, String>) globalContext.getAttribute(KEY);
		if (helpCache == null) {
			helpCache = new TimeMap<String, String>(24 * 60); // 1 day cache
			globalContext.setAttribute(KEY, helpCache);
		}
		return helpCache;
	}

	public String getHelpHTML(ContentContext ctx, IContentVisualComponent comp) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String fullURL = comp.getHelpURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE), globalContext.getEditLanguage()) + "?force-template=notemplate";
		String helpCacheKey = comp.getType() + '-' + globalContext.getEditLanguage();
		String xhtml = getHelpCache(globalContext).get(helpCacheKey);
		if (xhtml == null) {
			logger.info("load help for component helpCacheKey : " + helpCacheKey);
			try {
				xhtml = NetHelper.readPage(fullURL, false);
				if (xhtml != null && xhtml.trim().length() > 0) {
					xhtml = "<div class=\"help-link\"><a title=\"help : " + comp.getType() + "\" target=\"_blank\" href=\"" + fullURL + "\">" + fullURL + "</a></div>" + xhtml;
				}
			} catch (Exception e) {
				logger.warning(e.getMessage());
			}
			getHelpCache(globalContext).put(helpCacheKey, StringHelper.neverNull(xhtml));
		}
		if (xhtml != null && xhtml.trim().length() == 0) {
			xhtml = null;
		}
		return xhtml;
	}

}
