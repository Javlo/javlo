/*
 * Created on 20 aout 2003
 */
package org.javlo.service;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.data.InfoBean;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.template.TemplateFactory;

/**
 * @author pvanderm represent a content
 */
public class ContentService {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ContentService.class.getName());

	public static void clearAllContextCache(ContentContext ctx) throws Exception {
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession());
		for (GlobalContext globalContext : allContext) {
			logger.info("refresh context : " + globalContext.getContextKey());
			ContentService content = ContentService.getInstance(globalContext);
			content.releaseAll(ctx, globalContext);
			PersistenceService.getInstance(globalContext).loadState();
		}
		TemplateFactory.clearTemplate(ctx.getRequest().getSession().getServletContext());
		TemplateFactory.cleanAllRenderer(ctx, true, false);
		TemplateFactory.cleanAllRenderer(ctx, false, false);
	}

	public static void clearAllCache(ContentContext ctx, GlobalContext globalContext) throws Exception {
		logger.info("refresh context, content and template");
		ContentService content = ContentService.getInstance(globalContext);
		content.releaseAll(ctx, globalContext);
		PersistenceService.getInstance(globalContext).loadState();
		TemplateFactory.clearTemplate(ctx.getRequest().getSession().getServletContext());
		TemplateFactory.cleanAllRenderer(ctx, true, false);
		TemplateFactory.cleanAllRenderer(ctx, false, false);
		content.loadViewNav(ctx); // reload the content
	}

	public static void main(String[] args) {
		StringBuffer test = new StringBuffer("patrick");
		WeakReference<StringBuffer> weakTest = new WeakReference<StringBuffer>(test);
		System.out.println("*** weakTest 1 : " + weakTest.get());
		test = null;
		System.out.println("*** weakTest 2 : " + weakTest.get());
	}

	private MenuElement viewNav = null;

	private MenuElement previewNav = null;

	private MenuElement timeTravelerNav = null;

	private boolean isView = true;

	static final String CONTENT_KEY = "__dc_content__";

	private final Map<String, WeakReference<IContentVisualComponent>> components = new Hashtable<String, WeakReference<IContentVisualComponent>>();

	private Map<String, String> viewGlobalMap;

	private Map<String, String> previewGlobalMap;

	private Map<String, String> timeTravelerGlobalMap;

	private static final Object LOCK_LOAD_NAVIGATION = new Object();

	@Deprecated
	public static ContentService createContent(HttpServletRequest request) {
		return getInstance(GlobalContext.getInstance(request));
	}

	public static ContentService getInstance(GlobalContext globalContext) {
		ContentService content = null;
		if (globalContext != null) {
			content = (ContentService) globalContext.getAttribute(ContentService.class.getName());
		}
		if (content == null) {
			content = new ContentService();
			if (globalContext != null) {
				globalContext.setAttribute(ContentService.class.getName(), content);
			}
		}
		if (globalContext != null) {
			content.isView = globalContext.isView();
		}
		return content;
	}

	private static IContentVisualComponent searchComponent(ContentContext ctx, MenuElement page, String id) throws Exception {
		ContentElementList content = page.getAllContent(ctx); /* --TRACE-- 23 juil. 2009 13:20:26 */// TODO: remove trace);

		while (content.hasNext(ctx)) {
			IContentVisualComponent elem = content.next(ctx);
			if (elem.getId().equals(id)) {
				return elem;
			}
		}
		Collection<MenuElement> children = page.getChildMenuElementsList();
		for (MenuElement menuElement : children) {
			IContentVisualComponent comp = searchComponent(ctx, menuElement, id);
			if (comp != null) {
				return comp;
			}
		}
		return null;
	}

	private ContentService() {
	}

	public void clearComponentCache() {
		components.clear();
	}

	/**
	 * check if the content exist for this context.
	 * 
	 * @param ctx
	 *            the current context.
	 * @return true if content exist for this language false else.
	 * @throws DAOException
	 */
	public boolean contentExistForContext(ContentContext ctx) throws Exception {
		if (ctx.contentExistForContext == null) {
			ctx.contentExistForContext = !ctx.getCurrentPage().isEmpty(ctx, ComponentBean.DEFAULT_AREA);
		}
		return ctx.contentExistForContext;
	}

	public String createContent(ContentContext ctx, ComponentBean inBean, String parentId) throws Exception {
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, inBean.getType(), inBean.getValue(), ctx.getContentLanguage(), false);
		bean.setList(inBean.isList());
		bean.setStyle(inBean.getStyle());
		bean.setArea(inBean.getArea());
		bean.setRepeat(inBean.isRepeat());
		MenuElement elem = ctx.getCurrentPage();
		elem.addContent(parentId, bean);
		return id;
	}

	public String createContent(ContentContext ctx, String parentId, String type, String content) throws Exception {
		if (content == null) {
			content = "";
		}
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, type, content, ctx.getContentLanguage(), false);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		bean.setArea(editCtx.getCurrentArea());
		MenuElement elem = ctx.getCurrentPage();
		elem.addContent(parentId, bean);
		return id;
	}

	public synchronized void deleteKeys(String prefix) {
		if (previewGlobalMap != null) {
			Collection<String> keys = previewGlobalMap.keySet();
			Collection<String> toBeDeleted = new LinkedList<String>();
			for (String key : keys) {
				if (key.startsWith(prefix)) {
					toBeDeleted.add(key);
				}
			}
			for (String delKey : toBeDeleted) {
				previewGlobalMap.remove(delKey);
			}
		}
	}

	public String getAttribute(ContentContext ctx, String key) {
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			if (viewGlobalMap == null) {
				return null;
			}
			return viewGlobalMap.get(key);
		} else if (ctx.getRenderMode() == ContentContext.TIME_MODE) {
			if (timeTravelerGlobalMap == null) {
				return null;
			}
			return timeTravelerGlobalMap.get(key);
		} else {
			if (previewGlobalMap == null) {
				return null;
			}
			return previewGlobalMap.get(key);
		}
	}

	public String getAttribute(ContentContext ctx, String key, String defaultValue) {
		String value = getAttribute(ctx, key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public IContentVisualComponent getCachedComponent(ContentContext ctx, String id) throws Exception {
		if (id == null) {
			return null;
		}
		WeakReference<IContentVisualComponent> ref = components.get(id);
		IContentVisualComponent component = null;
		if (ref != null) {
			component = ref.get();
		}
		return component;
	}

	public IContentVisualComponent getComponent(ContentContext ctx, String id) throws Exception {
		if (id == null) {
			return null;
		}
		WeakReference<IContentVisualComponent> ref = components.get(id);
		IContentVisualComponent component = null;
		if (ref != null) {
			component = ref.get();
		}
		if (component == null) {
			component = searchComponent(ctx, getNavigation(ctx), id);
			if (component != null) {
				components.put(id, new WeakReference<IContentVisualComponent>(component));
			}
		}
		if (component == null) {
			components.remove(id);
		}
		return component;
	}

	public IContentVisualComponent getComponentAllLanguage(ContentContext ctx, String id) throws Exception {
		if (id == null) {
			return null;
		}
		WeakReference<IContentVisualComponent> ref = components.get(id);
		IContentVisualComponent component = null;
		if (ref != null) {
			component = ref.get();
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> languages = globalContext.getContentLanguages().iterator();

		while (component == null && languages.hasNext()) {
			ContentContext localContext = new ContentContext(ctx);
			localContext.setRequestContentLanguage(languages.next());
			component = searchComponent(localContext, getNavigation(localContext), id);
			if (component != null) {
				components.put(id, new WeakReference<IContentVisualComponent>(component));
			}
		}
		if (component == null) {
			components.remove(id);
		}
		return component;
	}

	/**
	 * return the globalmap in readonly mode.
	 * 
	 * @return
	 */
	public Map<String, String> getGlobalMap(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			if (viewGlobalMap == null) {
				return Collections.EMPTY_MAP;
			} else {
				return Collections.unmodifiableMap(viewGlobalMap);
			}
		} else if (ctx.getRenderMode() == ContentContext.TIME_MODE) {
			if (timeTravelerGlobalMap == null) {
				return Collections.EMPTY_MAP;
			} else {
				return Collections.unmodifiableMap(timeTravelerGlobalMap);
			}
		} else {
			if (previewGlobalMap == null) {
				return Collections.EMPTY_MAP;
			} else {
				return Collections.unmodifiableMap(previewGlobalMap);
			}
		}
	}

	/**
	 * return all the content.
	 */
	public MenuElement getNavigation(ContentContext ctx) throws Exception {
		MenuElement res = null;
		synchronized (LOCK_LOAD_NAVIGATION) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (ctx.getRenderMode() == ContentContext.TIME_MODE && globalContext.getTimeTravelerContext().getTravelTime() != null) {
				if (timeTravelerNav == null) {
					PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
					Map<String, String> contentAttributeMap = new HashMap<String, String>();
					timeTravelerNav = persistenceService.load(ctx, ContentContext.VIEW_MODE, contentAttributeMap, globalContext.getTimeTravelerContext().getTravelTime());
					timeTravelerGlobalMap = contentAttributeMap;
				}
				res = timeTravelerNav;
			} else if (!ctx.isAsViewMode() || (!isView)) {
				if (previewNav == null) {
					PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
					logger.info("reload preview navigation");
					Map<String, String> contentAttributeMap = new HashMap<String, String>();
					previewNav = persistenceService.load(ctx, ContentContext.PREVIEW_MODE, contentAttributeMap, null);
					previewGlobalMap = contentAttributeMap;
				}
				res = previewNav;
			} else {
				if (viewNav == null) {
					PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
					Map<String, String> contentAttributeMap = new HashMap<String, String>();
					viewNav = persistenceService.load(ctx, ContentContext.VIEW_MODE, contentAttributeMap, null);
					viewGlobalMap = contentAttributeMap;
				}
				res = viewNav;
			}
			DebugHelper.checkAssert(res == null, "the return of getNavigation can be never null.");
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			res.setTextCut(staticConfig.getMaxMenuTitleSize());
		}
		return res;
	}

	public MenuElement getTimeTravelerNav() {
		return timeTravelerNav;
	}

	public int getWordCount(ContentContext ctx) throws Exception {
		String KEY = "__word_count_" + ctx.getRequestContentLanguage();
		HttpSession session = ctx.getRequest().getSession();
		if (session.getAttribute(KEY) == null) {
			MenuElement[] allPages = getNavigation(ctx).getAllChilds();
			int wordCount = 0;
			for (MenuElement child : allPages) {
				ContentElementList content = child.getContent(ctx);
				while (content.hasNext(ctx)) {
					wordCount = wordCount + content.next(ctx).getWordCount(ctx);
				}
			}
			session.setAttribute(KEY, wordCount);
		}
		return (Integer) session.getAttribute(KEY);
	}

	/**
	 * return true if the production site is different than the edit site.
	 * 
	 * @return true if modified false else.
	 */
	public boolean isModified() {
		boolean res;
		if (viewNav == null) {
			res = false;
		} else {
			if (previewNav == null) {
				res = true;
			} else {
				res = !viewNav.equals(previewNav);
			}
		}
		return res;
	}

	public void loadViewNav(ContentContext ctx) throws Exception {
		loadViewNav(ctx, GlobalContext.getInstance(ctx.getRequest()));
	}

	public void loadViewNav(ContentContext ctx, GlobalContext globalContext) throws Exception {
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		Map<String, String> contentAttributeMap = new HashMap<String, String>();
		MenuElement newViewNav = persistenceService.load(ctx, ContentContext.VIEW_MODE, contentAttributeMap, null);

		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		int depth = staticConfig.getPublishLoadingDepth();
		MenuElement[] children = newViewNav.getAllChilds();
		ContentContext viewCtx = new ContentContext(ctx);
		Collection<String> lgs = globalContext.getLanguages();
		for (String lg : lgs) {
			viewCtx.setLanguage(lg);
			viewCtx.setRequestContentLanguage(lg);
			for (MenuElement menuElement : children) {
				if (menuElement.getDepth() <= depth) {
					ContentElementList content = menuElement.getContent(viewCtx);
					while (content.hasNext(viewCtx)) {
						content.next(viewCtx).getXHTMLCode(viewCtx); // load cache
					}
				}
			}
		}
		synchronized (LOCK_LOAD_NAVIGATION) {
			viewNav = newViewNav;
			viewGlobalMap = contentAttributeMap;
		}
	}

	public void releaseAll(ContentContext ctx, GlobalContext globalContext) throws Exception {
		components.clear();
		releasePreviewNav(ctx);
		releaseViewNav(ctx, globalContext);
	}

	/**
	 * release the preview nav.
	 * @param ctx if null context will not be updated (and content not reloaded now).
	 * @throws Exception
	 */
	public void releasePreviewNav(ContentContext ctx) throws Exception {
		logger.fine("release preview nav");
		clearComponentCache();
		setPreviewNav(null);
		if (this.previewGlobalMap != null) {
			this.previewGlobalMap.clear();
		}
		if (ctx != null) {
			ctx.setCurrentPageCached(null);
			InfoBean.updateInfoBean(ctx);
		}
	}

	public void releaseTimeTravelerNav() {
		clearComponentCache();
		setTimeTravelerNav(null);
		if (this.timeTravelerGlobalMap != null) {
			this.timeTravelerGlobalMap.clear();
		}
	}

	public void releaseViewNav(ContentContext ctx, GlobalContext globalContext) throws Exception {
		globalContext.releaseAllCache();
		clearComponentCache();
		viewNav = null;

		/*
		 * ContentService content = ContentService.getInstance(globalContext); content.loadViewNav(ctx);
		 */
	}

	public void removeAttribute(ContentContext ctx, String key) {
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			if (viewGlobalMap == null) {
				viewGlobalMap = new Hashtable<String, String>();
			}
			viewGlobalMap.remove(key);
		} else if (ctx.getRenderMode() == ContentContext.TIME_MODE) {
			if (timeTravelerGlobalMap == null) {
				timeTravelerGlobalMap = new Hashtable<String, String>();
			}
			timeTravelerGlobalMap.remove(key);
		} else {
			if (previewGlobalMap == null) {
				previewGlobalMap = new Hashtable<String, String>();
			}
			previewGlobalMap.remove(key);
		}
	}

	public void removeComponentFromCache(String id) {
		components.remove(id);
	}

	public synchronized void renameKeys(String oldKeyPrefix, String newKeyPrefix) {
		Collection<String> keys = previewGlobalMap.keySet();
		Collection<String> toBeModified = new LinkedList<String>();
		for (Object keyObj : keys) {
			String key = (String) keyObj;
			if (key.startsWith(oldKeyPrefix)) {
				toBeModified.add(key);
			}
		}
		for (String key : toBeModified) {
			if (key.startsWith(oldKeyPrefix)) {
				String newKey = StringUtils.replaceOnce(key, oldKeyPrefix, newKeyPrefix);
				previewGlobalMap.put(newKey, previewGlobalMap.get(key));
				previewGlobalMap.remove(key);
			}
		}
	}

	public void setAttribute(ContentContext ctx, String key, String value) {
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			if (viewGlobalMap == null) {
				viewGlobalMap = new HashMap<String, String>();
			}
			viewGlobalMap.put(key, value);
		} else if (ctx.getRenderMode() == ContentContext.TIME_MODE) {
			if (timeTravelerGlobalMap == null) {
				timeTravelerGlobalMap = new HashMap<String, String>();
			}
			timeTravelerGlobalMap.put(key, value);
		} else {
			if (previewGlobalMap == null) {
				previewGlobalMap = new HashMap<String, String>();
			}
			previewGlobalMap.put(key, value);
		}
	}

	public void setCachedComponent(IContentVisualComponent comp) throws Exception {
		components.put(comp.getId(), new WeakReference<IContentVisualComponent>(comp));
	}

	public void setPreviewNav(MenuElement previewNav) {
		this.previewNav = previewNav;
	}

	public void setTimeTravelerNav(MenuElement timeTravelerNav) {
		this.timeTravelerNav = timeTravelerNav;
	}

	public boolean isViewNav() {
		return viewNav != null;
	}

	public boolean isPreviewNav() {
		return previewNav != null;
	}
}
