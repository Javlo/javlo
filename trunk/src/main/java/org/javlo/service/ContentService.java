/*
 * Created on 20 aout 2003
 */
package org.javlo.service;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
			PersistenceService.getInstance(globalContext).loadVersion();
		}
		TemplateFactory.clearTemplate(ctx.getRequest().getSession().getServletContext());
		TemplateFactory.cleanAllRenderer(ctx, false);
	}

	public static void clearCache(ContentContext ctx, GlobalContext globalContext) throws Exception {
		logger.info("refresh context : " + globalContext.getContextKey());
		ContentService content = ContentService.getInstance(globalContext);
		content.releaseAll(ctx, globalContext);
		PersistenceService.getInstance(globalContext).loadVersion();
	}

	public static void clearAllCache(ContentContext ctx, GlobalContext globalContext) throws Exception {
		logger.info("refresh context, content and template");
		ContentService content = ContentService.getInstance(globalContext);
		content.releaseAll(ctx, globalContext);
		PersistenceService.getInstance(globalContext).loadVersion();
		TemplateFactory.clearTemplate(ctx.getRequest().getSession().getServletContext());
		TemplateFactory.cleanAllRenderer(ctx, false);
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

	private Map<String, MenuElement> shortURLMap = null;

	private MenuElement previewNav = null;

	private MenuElement timeTravelerNav = null;

	static final String CONTENT_KEY = "__dc_content__";

	private final Map<String, WeakReference<IContentVisualComponent>> components = new Hashtable<String, WeakReference<IContentVisualComponent>>();

	private Map<String, String> viewGlobalMap;

	private Map<String, String> previewGlobalMap;

	private Map<String, String> timeTravelerGlobalMap;

	private boolean previewMode = true;

	public static ContentService getInstance(HttpServletRequest request) {
		return getInstance(GlobalContext.getInstance(request));
	}

	public MenuElement getViewNav() {
		if (previewMode) {
			return viewNav;
		} else {
			return previewNav;
		}
	}

	public void setViewNav(MenuElement nav) {
		if (previewMode) {
			viewNav = nav;
		}
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
		content.previewMode = globalContext.isPreviewMode();
		return content;
	}

	private static IContentVisualComponent searchComponent(ContentContext ctx, MenuElement page, String id) throws Exception {
		ContentContext noAreaCtx = ctx.getContextWithoutArea();
		ContentContext ctxWithContent = noAreaCtx.getContextWithContent(page);
		if (ctxWithContent == null) {
			ctxWithContent = noAreaCtx;
		}
		/** search on current language **/
		ContentElementList content = page.getAllContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent elem = content.next(noAreaCtx);
			if (elem.getId().equals(id)) {
				return elem;
			}
		}
		/** search on content with real content **/
		content = page.getAllContent(ctxWithContent);
		while (content.hasNext(ctxWithContent)) {
			IContentVisualComponent elem = content.next(ctxWithContent);
			if (elem.getId().equals(id)) {
				return elem;
			}
		}

		MenuElement[] children = page.getAllChildren();
		for (MenuElement menuElement : children) {
			ctxWithContent = noAreaCtx.getContextWithContent(menuElement);
			if (ctxWithContent == null) {
				ctxWithContent = noAreaCtx;
			}
			content = menuElement.getAllContent(noAreaCtx);
			while (content.hasNext(noAreaCtx)) {
				IContentVisualComponent elem = content.next(noAreaCtx);
				if (elem.getId().equals(id)) {
					return elem;
				}
			}
			content = menuElement.getAllContent(ctxWithContent);
			while (content.hasNext(ctxWithContent)) {
				IContentVisualComponent elem = content.next(ctxWithContent);
				if (elem.getId().equals(id)) {
					return elem;
				}
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
			if (ctx.getCurrentPage() != null) {
				ctx.contentExistForContext = !ctx.getCurrentPage().isEmpty(ctx, ComponentBean.DEFAULT_AREA);
			} else {
				ctx.contentExistForContext = false;
			}
		}
		return ctx.contentExistForContext;
	}

	public String createContent(ContentContext ctx, MenuElement page, ComponentBean inBean, String parentId, boolean releaseCache) throws Exception {
		String id = StringHelper.getRandomId();
		String lg = inBean.getLanguage();
		if (lg == null) {
			lg = ctx.getRequestContentLanguage();
		}
		ComponentBean bean = new ComponentBean(id, inBean.getType(), inBean.getValue(), lg, false);
		bean.setList(inBean.isList());
		bean.setStyle(inBean.getStyle());
		bean.setArea(inBean.getArea());
		bean.setRepeat(inBean.isRepeat());
		bean.setRenderer(inBean.getRenderer());
		page.addContent(parentId, bean, releaseCache);
		return id;
	}

	public String createContent(ContentContext ctx, ComponentBean inBean, String parentId, boolean releaseCache) throws Exception {
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, inBean.getType(), inBean.getValue(), ctx.getContentLanguage(), false);
		bean.setList(inBean.isList());
		bean.setStyle(inBean.getStyle());
		bean.setArea(inBean.getArea());
		bean.setRepeat(inBean.isRepeat());
		bean.setRenderer(inBean.getRenderer());
		MenuElement elem = ctx.getCurrentPage();
		elem.addContent(parentId, bean, releaseCache);
		return id;
	}

	public String createContent(ContentContext ctx, MenuElement page, Collection<ComponentBean> inBean, String parentId, boolean releaseCache) throws Exception {
		for (ComponentBean bean : inBean) {
			parentId = createContent(ctx, page, bean, parentId, false);
		}
		if (releaseCache) {
			ctx.getCurrentPage().releaseCache();
		}
		return parentId;
	}

	public String createContent(ContentContext ctx, Collection<ComponentBean> inBean, String parentId, boolean releaseCache) throws Exception {
		return createContent(ctx, ctx.getCurrentPage(), inBean, parentId, releaseCache);
	}

	public String createContent(ContentContext ctx, String parentId, String type, String content, boolean releaseCache) throws Exception {
		if (content == null) {
			content = "";
		}
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, type, content, ctx.getContentLanguage(), false);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		bean.setArea(editCtx.getCurrentArea());
		bean.setAuthors(ctx.getCurrentEditUser().getLogin());
		MenuElement elem = ctx.getCurrentPage();
		elem.addContent(parentId, bean, releaseCache);
		return id;
	}

	public String createContent(ContentContext ctx, String parentId, String type, String content, boolean repeat, String renderer) throws Exception {
		if (content == null) {
			content = "";
		}
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, type, content, ctx.getContentLanguage(), repeat);
		bean.setRenderer(renderer);
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
				try {
					getNavigation(ctx);
					if (viewGlobalMap == null) {
						return null;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
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
		WeakReference<IContentVisualComponent> ref = components.get(id + ctx.getRenderMode());
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
		WeakReference<IContentVisualComponent> ref = components.get(id + ctx.getRenderMode());
		IContentVisualComponent component = null;
		if (ref != null) {
			component = ref.get();
		}
		if (component == null) {
			component = searchComponent(ctx, getNavigation(ctx), id);
			if (component != null) {
				components.put(id + ctx.getRenderMode(), new WeakReference<IContentVisualComponent>(component));
			}
		}
		if (component == null) {
			components.remove(id + ctx.getRenderMode());
		}
		return component;
	}

	public IContentVisualComponent getComponentAllLanguage(ContentContext ctx, String id) throws Exception {
		if (id == null) {
			return null;
		}
		WeakReference<IContentVisualComponent> ref = components.get(id + ctx.getRenderMode());
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
				components.put(id + ctx.getRenderMode(), new WeakReference<IContentVisualComponent>(component));
			}
		}
		if (component == null) {
			components.remove(id + ctx.getRenderMode());
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		synchronized (globalContext) {
			if (ctx.getRenderMode() == ContentContext.TIME_MODE && globalContext.getTimeTravelerContext().getTravelTime() != null) {
				if (timeTravelerNav == null) {
					Date timeTravelDate = globalContext.getTimeTravelerContext().getTravelTime();
					if (timeTravelDate != null && timeTravelDate.after(globalContext.getPublishDate())) {
						timeTravelDate = null;
					}
					PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
					Map<String, String> contentAttributeMap = new HashMap<String, String>();
					timeTravelerNav = persistenceService.load(ctx, ContentContext.VIEW_MODE, contentAttributeMap, timeTravelDate);
					timeTravelerGlobalMap = contentAttributeMap;
				}
				res = timeTravelerNav;
			} else if (!ctx.isAsViewMode() || !previewMode) {
				if (previewNav == null) {
					PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
					logger.info("reload preview navigation");
					Map<String, String> contentAttributeMap = new HashMap<String, String>();
					previewNav = persistenceService.load(ctx, ContentContext.PREVIEW_MODE, contentAttributeMap, null);
					previewGlobalMap = contentAttributeMap;
				}
				res = previewNav;
			} else {
				if (getViewNav() == null) {
					PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
					Map<String, String> contentAttributeMap = new HashMap<String, String>();
					setViewNav(persistenceService.load(ctx, ContentContext.VIEW_MODE, contentAttributeMap, null));
					viewGlobalMap = contentAttributeMap;
				}
				res = getViewNav();
			}
		}
		return res;
	}

	/**
	 * check if navigation was allready loaded for a specific render mode.
	 */
	public boolean isNavigationLoaded(ContentContext ctx) {
		synchronized (GlobalContext.getInstance(ctx.getRequest())) {
			if (ctx.getRenderMode() == ContentContext.TIME_MODE) {
				return timeTravelerNav != null;
			} else if (!ctx.isAsViewMode()) {
				return previewNav != null;
			} else {
				return getViewNav() != null;
			}
		}
	}

	public MenuElement getTimeTravelerNav() {
		return timeTravelerNav;
	}

	public int getWordCount(ContentContext ctx) throws Exception {
		String KEY = "__word_count_" + ctx.getRequestContentLanguage();
		HttpSession session = ctx.getRequest().getSession();
		if (session.getAttribute(KEY) == null) {
			MenuElement[] allPages = getNavigation(ctx).getAllChildren();
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
		if (getViewNav() == null) {
			res = false;
		} else {
			if (previewNav == null) {
				res = true;
			} else {
				res = !getViewNav().equals(previewNav);
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
		MenuElement[] children = newViewNav.getAllChildren();
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
		synchronized (globalContext) {
			setViewNav(newViewNav);
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
	 * 
	 * @param ctx
	 *            if null context will not be updated (and content not reloaded now).
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

	public void releaseTimeTravelerNav(ContentContext ctx) throws Exception {
		clearComponentCache();
		setTimeTravelerNav(null);
		if (this.timeTravelerGlobalMap != null) {
			this.timeTravelerGlobalMap.clear();
		}
		if (ctx != null) {
			ctx.setCurrentPageCached(null);
			InfoBean.updateInfoBean(ctx);
		}
	}

	public void releaseViewNav(ContentContext ctx, GlobalContext globalContext) throws Exception {
		setViewNav(null);
		globalContext.releaseAllCache();
		clearComponentCache();
		shortURLMap = null;
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

	public List<IContentVisualComponent> getAllContent(ContentContext ctx) throws Exception {
		String KEY = "__ALL_CONTENT__" + ctx.getRenderMode() + "__" + ctx.getRequestContentLanguage() + "__" + ctx.getArea();
		List<IContentVisualComponent> outList = (List<IContentVisualComponent>) ctx.getRequest().getAttribute(KEY);
		if (outList == null) {
			outList = new LinkedList<IContentVisualComponent>();

			ContentContext freeCtx = ctx.getContextWithArea(null);
			freeCtx.setFree(true);
			MenuElement page = getNavigation(freeCtx);
			ContentElementList content = page.getAllContent(freeCtx);
			while (content.hasNext(freeCtx)) {
				outList.add(content.next(freeCtx));
			}
			MenuElement[] children = page.getAllChildren();
			for (MenuElement child : children) {
				content = child.getAllContent(freeCtx);
				while (content.hasNext(freeCtx)) {
					outList.add(content.next(freeCtx));
				}
			}

			outList = Collections.unmodifiableList(outList);
			ctx.getRequest().setAttribute(KEY, outList);
		}
		return outList;
	}

	public void setPreviewNav(MenuElement previewNav) {
		this.previewNav = previewNav;
	}

	public void setTimeTravelerNav(MenuElement timeTravelerNav) {
		this.timeTravelerNav = timeTravelerNav;
	}

	public boolean isViewNav() {
		return getViewNav() != null;
	}

	public boolean isPreviewNav() {
		return previewNav != null;
	}

	public MenuElement getPageWithShortURL(ContentContext ctx, String shortURL) throws Exception {
		if (ctx.isAsViewMode()) {
			if (shortURLMap == null) {
				shortURLMap = new HashMap<String, MenuElement>();
				MenuElement root = getNavigation(ctx);
				if (root.isShortURL()) {
					shortURLMap.put(root.getShortURL(ctx), root);
				}
				for (MenuElement child : root.getAllChildren()) {
					if (child.isShortURL()) {
						shortURLMap.put(child.getShortURL(ctx), child);
					}
				}
			}
			return shortURLMap.get(shortURL);
		} else {
			return null;
		}
	}
}
