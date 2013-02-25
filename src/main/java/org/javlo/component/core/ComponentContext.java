package org.javlo.component.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.utils.TimeMap;

public class ComponentContext {

	private static Logger logger = Logger.getLogger(ComponentContext.class.getName());

	private static final String INSERTION_COMPONENT_KEY = "_insertion_component";

	private static final String KEY = "componentContext";

	private final List<IContentVisualComponent> newComponentId = new LinkedList<IContentVisualComponent>();

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

	public List<IContentVisualComponent> getNewComponents() {
		return newComponentId;
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
		String fullURL = comp.getHelpURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE), globalContext.getEditLanguage(ctx.getRequest().getSession()));
		String url = fullURL;
		if (fullURL == null) {
			return null;
		} else {
			fullURL = fullURL + "?force-template=notemplate";
		}
		String helpCacheKey = comp.getType() + '-' + globalContext.getEditLanguage(ctx.getRequest().getSession());
		String xhtml = getHelpCache(globalContext).get(helpCacheKey);
		if (xhtml == null) {
			logger.info("load help for component fullURL : " + fullURL);
			try {
				xhtml = NetHelper.readPage(fullURL, false);
				if (xhtml != null && xhtml.trim().length() > 0) {
					xhtml = XHTMLHelper.extractBody(xhtml);
					url = URLHelper.changeMode(url, "");
					xhtml = "<div class=\"help-link\"><a title=\"help : " + comp.getType() + "\" target=\"_blank\" href=\"" + url + "\">" + url + "</a></div>" + xhtml;
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
