/*
 * Created on 20 aout 2003
 */
package org.javlo.service;

import java.io.PrintStream;
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

import org.apache.commons.lang3.StringUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.Unknown;
import org.javlo.component.links.MirrorComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.data.InfoBean;
import org.javlo.helper.LangHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nResource;
import org.javlo.module.core.IPrintInfo;
import org.javlo.navigation.MenuElement;
import org.javlo.template.TemplateFactory;

/**
 * @author pvanderm represent a content
 */
public class ContentService implements IPrintInfo {

	public static final String TRASH_PAGE_NAME = "_trash_page_";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ContentService.class.getName());

	public static void clearAllContextCache(ContentContext ctx) throws Exception {
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext());
		for (GlobalContext globalContext : allContext) {
			logger.info("refresh context : " + globalContext.getContextKey());
			ContentService content = ContentService.getInstance(globalContext);
			content.releaseAll(ctx, globalContext);
			PersistenceService.getInstance(globalContext).resetVersion();
			NavigationService.getInstance(globalContext).clearAllPage();
		}
		TemplateFactory.clearTemplate(ctx.getRequest().getSession().getServletContext());
		TemplateFactory.cleanAllRenderer(ctx, false);
	}

	public static void clearCache(ContentContext ctx, GlobalContext globalContext) throws Exception {
		logger.info("refresh context : " + globalContext.getContextKey());
		ContentService content = ContentService.getInstance(globalContext);
		content.releaseAll(ctx, globalContext);
		PersistenceService.getInstance(globalContext).resetVersion();
		NavigationService.getInstance(globalContext).clearAllPage();
	}

	public static void clearAllCache(ContentContext ctx, GlobalContext globalContext) throws Exception {
		logger.info("refresh context, content and template : " + globalContext.getContextKey());
		ContentService content = ContentService.getInstance(globalContext);
		content.releaseAll(ctx, globalContext);
		PersistenceService.getInstance(globalContext).resetVersion();
		TemplateFactory.clearTemplate(ctx.getRequest().getSession().getServletContext());
		TemplateFactory.cleanAllRenderer(ctx, false);
		// content.loadViewNav(ctx); // reload the content
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

	private final Map<String, IContentVisualComponent> components = new Hashtable<String, IContentVisualComponent>();

	private Map<String, String> viewGlobalMap;

	private Map<String, String> previewGlobalMap;

	private Map<String, String> timeTravelerGlobalMap;

	private boolean previewMode = true;

	public static ContentService getInstance(HttpServletRequest request) {
		return getInstance(GlobalContext.getInstance(request));
	}

	protected MenuElement getViewNav() {
		if (previewMode) {
			return viewNav;
		} else {
			return previewNav;
		}
	}

	protected void setViewNav(MenuElement nav) {
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

	private static IContentVisualComponent searchComponent(ContentContext ctx, MenuElement page, String id, boolean noRealContentType) throws Exception {
		ContentContext noAreaCtx = ctx.getContextWithoutArea();

		/** search on current language **/
		ContentElementList content = page.getAllContent(noAreaCtx);
		/*
		 * while (content.hasNext(noAreaCtx)) { IContentVisualComponent elem =
		 * content.next(noAreaCtx); if (elem.getId().equals(id)) { return elem;
		 * } }
		 */

		if (noRealContentType) {
			ContentContext ctxLg = new ContentContext(noAreaCtx);
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				ctxLg.setAllLanguage(lg);
				content = page.getAllContent(ctxLg);
				while (content.hasNext(ctxLg)) {
					IContentVisualComponent elem = content.next(ctxLg);
					if (elem.getId().equals(id)) {
						return elem;
					}
				}
				for (MenuElement child : page.getAllChildrenList()) {
					content = child.getAllContent(ctxLg);
					while (content.hasNext(ctxLg)) {
						IContentVisualComponent elem = content.next(ctxLg);
						if (elem.getId().equals(id)) {
							return elem;
						}
					}
				}
			}
		} else {
			ContentContext ctxWithContent = noAreaCtx.getContextWithContent(page);
			if (ctxWithContent == null) {
				ctxWithContent = noAreaCtx;
			}
			/** search on content with real content **/
			content = page.getAllContent(ctxWithContent);
			while (content.hasNext(ctxWithContent)) {
				IContentVisualComponent elem = content.next(ctxWithContent);
				if (elem.getId().equals(id)) {
					return elem;
				}
			}
			for (MenuElement menuElement : page.getAllChildrenList()) {
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
				ctx.contentExistForContext = !ctx.getCurrentPage().isLocalEmpty(ctx, ComponentBean.DEFAULT_AREA);
			} else {
				ctx.contentExistForContext = false;
			}
		}
		return ctx.contentExistForContext;
	}

	public String createContentMirrorIfNeeded(ContentContext ctx, MenuElement newPage, IContentVisualComponent comp, String parentId, boolean b) throws Exception {
		if (comp.isMirroredByDefault(ctx) && !ctx.getGlobalContext().isMailingPlatform()) {
			ComponentBean mirrorComponentBean = new ComponentBean(MirrorComponent.TYPE, comp.getId(), ctx.getRequestContentLanguage());
			return createContent(ctx, newPage, mirrorComponentBean, parentId, b);
		} else {
			return createContent(ctx, newPage, comp.getComponentBean(), parentId, b);
		}
	}

	public String createContent(ContentContext ctx, MenuElement page, ComponentBean inBean, String parentId, boolean releaseCache) throws Exception {
		String id = StringHelper.getRandomId();
		String lg = inBean.getLanguage();
		if (lg == null) {
			lg = ctx.getRequestContentLanguage();
		}
		ComponentBean bean = new ComponentBean(inBean);
		bean.setLanguage(lg);
		bean.setId(id);
		if (bean.getArea() == null) {
			bean.setArea(ctx.getArea());
		}
		bean.setAuthors(ctx.getCurrentUserId());
		page.addContent(parentId, bean, releaseCache);
		return id;
	}

	public String createContent(ContentContext ctx, ComponentBean inBean, String parentId, boolean releaseCache) throws Exception {
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, inBean.getType(), inBean.getValue(), ctx.getRequestContentLanguage(), false, ctx.getCurrentEditUser());
		bean.setList(inBean.isList());
		bean.setStyle(inBean.getStyle());
		IContentVisualComponent previousComp = ContentService.getInstance(ctx.getRequest()).getComponent(ctx, parentId);
		if (previousComp != null) {
			bean.setArea(previousComp.getArea());
		} else {
			if (inBean.getArea() != null) {
				bean.setArea(inBean.getArea());
			} else {
				bean.setArea(ctx.getArea());
			}
		}
		bean.setRepeat(inBean.isRepeat());
		bean.setRenderer(inBean.getRenderer());
		bean.setModify(true);
		MenuElement elem = ctx.getCurrentPage();
		if (elem.isChildrenAssociation() && elem.getChildMenuElements().size() > 0) {
			elem = elem.getChildMenuElements().iterator().next();
		}
		elem.addContent(parentId, bean, releaseCache);
		return id;
	}

	public String createContentAtEnd(ContentContext ctx, ComponentBean inBean, boolean releaseCache) throws Exception {
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, inBean.getType(), inBean.getValue(), ctx.getRequestContentLanguage(), false, ctx.getCurrentEditUser());
		bean.setList(inBean.isList());
		bean.setStyle(inBean.getStyle());
		bean.setArea(inBean.getArea());
		bean.setRepeat(inBean.isRepeat());
		bean.setRenderer(inBean.getRenderer());
		bean.setModify(true);
		MenuElement elem = ctx.getCurrentPage();
		if (elem.isChildrenAssociation() && elem.getChildMenuElements().size() > 0) {
			elem = elem.getChildMenuElements().iterator().next();
		}
		ContentElementList list = elem.getContent(ctx);
		IContentVisualComponent comp = list.next(ctx);
		while (list.hasNext(ctx)) {
			comp = list.next(ctx);
		}
		if (comp != null) {
			elem.addContent(comp.getId(), bean, releaseCache);
		} else {
			elem.addContent("0", bean, releaseCache);
		}
		return id;
	}

	public String createContent(ContentContext ctx, MenuElement page, Iterable<ComponentBean> inBean, String parentId, boolean releaseCache) throws Exception {
		for (ComponentBean bean : inBean) {
			IContentVisualComponent comp = ComponentFactory.createComponent(ctx, bean, null, null, null);
			if (!comp.isUnique() || page.getContentByType(ctx, comp.getType()).size() == 0) {
				if (bean.getAuthors() == null || bean.getAuthors().length() == 0 && ctx.getCurrentEditUser() != null) {
					bean.setAuthors(ctx.getCurrentEditUser().getLogin());
				}
				parentId = createContent(ctx, page, bean, parentId, false);
			}
		}
		if (releaseCache) {
			page.releaseCache();
		}
		return parentId;
	}

	public String createContent(ContentContext ctx, Collection<ComponentBean> inBean, String parentId, boolean releaseCache) throws Exception {
		return createContent(ctx, ctx.getCurrentPage(), inBean, parentId, releaseCache);
	}

	public String createContent(ContentContext ctx, MenuElement page, String area, String parentId, String type, String content, boolean releaseCache) throws Exception {
		if (content == null) {
			content = "";
		}
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, type, content, ctx.getRequestContentLanguage(), false, ctx.getCurrentEditUser());

		bean.setArea(area);
		bean.setAuthors(ctx.getCurrentEditUser().getLogin());
		page.addContent(parentId, bean, releaseCache);

		return id;
	}

	public String createContent(ContentContext ctx, MenuElement page, String area, String parentId, ComponentBean inBean, boolean releaseCache) throws Exception {
		ComponentBean bean = new ComponentBean(inBean);
		bean.setId(StringHelper.getRandomId());
		bean.setArea(area);
		bean.setAuthors(ctx.getCurrentEditUser().getLogin());
		bean.setLanguage(ctx.getRequestContentLanguage());
		page.addContent(parentId, bean, releaseCache);
		return bean.getId();
	}

	public String createContent(ContentContext ctx, String parentId, String type, String content, boolean releaseCache) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		return createContent(ctx, ctx.getCurrentPage(), editCtx.getCurrentArea(), parentId, type, content, releaseCache);
	}

	public String createContent(ContentContext ctx, String parentId, String type, String content, boolean repeat, String renderer) throws Exception {
		if (content == null) {
			content = "";
		}
		String id = StringHelper.getRandomId();
		ComponentBean bean = new ComponentBean(id, type, content, ctx.getRequestContentLanguage(), repeat, ctx.getCurrentEditUser());
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
		if (key != null) {
			key = key.replace("&", "_and_");
		}
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE && ctx.getGlobalContext().isPreviewMode()) {
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

	private static String getComponentKey(ContentContext ctx, String id) {
		int mode = ctx.getRenderMode();
		if (!ctx.getGlobalContext().isPreviewMode()) {
			mode = ContentContext.EDIT_MODE;
		} else if (mode == ContentContext.PREVIEW_MODE) {
			mode = ContentContext.EDIT_MODE;
		}
		return id + '-' + mode;
	}

	public IContentVisualComponent getCachedComponent(ContentContext ctx, String id) throws Exception {
		if (id == null || !ctx.isComponentCache()) {
			return null;
		}
		IContentVisualComponent component = components.get(getComponentKey(ctx, id));
		if (component == null) {
			components.remove(getComponentKey(ctx, id));
		}
		return component;
	}

	public IContentVisualComponent getComponentNoRealContentType(ContentContext ctx, String id) throws Exception {
		if (id == null) {
			return null;
		}
		IContentVisualComponent component = components.get(getComponentKey(ctx, id));
		String compKey = getComponentKey(ctx, id);
		if (component == null) {
			component = searchComponent(ctx, getNavigation(ctx), id, true);
			if (component != null) {
				components.put(compKey, component);
			} else {
				components.put(compKey, Unknown.INSTANCE);
			}
		}
		if (component == null) {
			components.remove(compKey);
		}
		if (component == Unknown.INSTANCE) {
			return null;
		} else {
			return component;
		}
	}

	public IContentVisualComponent getComponent(ContentContext ctx, String id) throws Exception {
		if (id == null) {
			return null;
		}
		IContentVisualComponent component = components.get(getComponentKey(ctx, id));
		if (component == null) {
			component = searchComponent(ctx, getNavigation(ctx), id, false);
			if (component != null) {
				components.put(getComponentKey(ctx, id), component);
			}
		}
		if (component == null) {
			components.remove(getComponentKey(ctx, id));
		}
		return component;
	}

	public List<IContentVisualComponent> getComponentByType(ContentContext ctx, String type) throws Exception {
		List<IContentVisualComponent> outContent = new LinkedList<IContentVisualComponent>();
		MenuElement root = getNavigation(ctx);
		outContent.addAll(root.getContentByType(ctx, type));
		for (MenuElement child : root.getAllChildrenList()) {
			outContent.addAll(child.getContentByType(ctx, type));
		}
		return outContent;
	}

	public IContentVisualComponent getComponentAllLanguage(ContentContext ctx, String id) throws Exception {
		if (id == null) {
			return null;
		}
		IContentVisualComponent component = components.get(getComponentKey(ctx, id));
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> languages = globalContext.getContentLanguages().iterator();

		while (component == null && languages.hasNext()) {
			ContentContext localContext = new ContentContext(ctx);
			localContext.setRequestContentLanguage(languages.next());
			component = searchComponent(localContext, getNavigation(localContext), id, true);
			if (component != null) {
				components.put(getComponentKey(ctx, id), component);
			}
		}
		if (component == null) {
			components.remove(getComponentKey(ctx, id));
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
		GlobalContext globalContext = ctx.getGlobalContext();

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
		} else if (!ctx.isAsViewMode() || !previewMode) { // TODO: check the
															// test was with :
															// || !previewMode
			if (previewNav == null) {
				synchronized (ctx.getGlobalContext().getLockLoadContent()) {
					if (previewNav == null) {
						long startTime = System.currentTimeMillis();
						PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
						Map<String, String> contentAttributeMap = new HashMap<String, String>();
						previewNav = persistenceService.load(ctx, ContentContext.PREVIEW_MODE, contentAttributeMap, null);
						previewGlobalMap = contentAttributeMap;
						logger.info("load preview of '" + globalContext.getContextKey() + "' nav in " + StringHelper.renderTimeInSecond((System.currentTimeMillis() - startTime) / 1000) + " sec.");
					}
				}
			}
			res = previewNav;
		} else {
			if (getViewNav() == null) {
				synchronized (ctx.getGlobalContext().getLockLoadContent()) {
					if (getViewNav() == null) {
						long startTime = System.currentTimeMillis();
						PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
						Map<String, String> contentAttributeMap = new HashMap<String, String>();
						MenuElement page = persistenceService.load(ctx, ContentContext.VIEW_MODE, contentAttributeMap, null);
						setViewNav(page);
						viewGlobalMap = contentAttributeMap;
						NavigationService.checkSameUrl(ctx);
						logger.info("load view of '" + globalContext.getContextKey() + "' nav in " + StringHelper.renderTimeInSecond((System.currentTimeMillis() - startTime) / 1000) + " sec.");
					}
				}
			}
			res = getViewNav();
		}

		return res;
	}

	public MenuElement getTrashPage(ContentContext ctx) throws Exception {
		MenuElement root = getNavigation(ctx);
		MenuElement trashPage = getNavigation(ctx).searchChildFromName(TRASH_PAGE_NAME);
		if (trashPage == null) {
			trashPage = MenuElement.getInstance(ctx.getGlobalContext());
			trashPage.setName(TRASH_PAGE_NAME);
			trashPage.setPriority(9999);
			trashPage.setActive(false);
			trashPage.setVisible(false);
			root.addChildMenuElement(trashPage);
		}
		return trashPage;
	}

	/**
	 * check if navigation was allready loaded for a specific render mode.
	 */
	public boolean isNavigationLoaded(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.TIME_MODE) {
			return timeTravelerNav != null;
		} else if (!ctx.isAsViewMode()) {
			return previewNav != null;
		} else {
			return getViewNav() != null;
		}
	}

	public MenuElement getTimeTravelerNav() {
		return timeTravelerNav;
	}

	public int getWordCount(ContentContext ctx) throws Exception {
		String KEY = "__word_count_" + ctx.getRequestContentLanguage();
		HttpSession session = ctx.getRequest().getSession();
		if (session.getAttribute(KEY) == null) {			
			int wordCount = 0;
			for (MenuElement child : getNavigation(ctx).getAllChildrenList()) {
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

	/*
	 * public void loadViewNav(ContentContext ctx) throws Exception {
	 * loadViewNav(ctx, GlobalContext.getInstance(ctx.getRequest())); }
	 * 
	 * public void loadViewNav(ContentContext ctx, GlobalContext globalContext)
	 * throws Exception { PersistenceService persistenceService =
	 * PersistenceService.getInstance(globalContext); Map<String, String>
	 * contentAttributeMap = new HashMap<String, String>(); MenuElement
	 * newViewNav = persistenceService.load(ctx, ContentContext.VIEW_MODE,
	 * contentAttributeMap, null);
	 * 
	 * StaticConfig staticConfig =
	 * StaticConfig.getInstance(ctx.getRequest().getSession()); int depth =
	 * staticConfig.getPublishLoadingDepth(); MenuElement[] children =
	 * newViewNav.getAllChildren(); ContentContext viewCtx = new
	 * ContentContext(ctx); Collection<String> lgs =
	 * globalContext.getLanguages(); for (String lg : lgs) {
	 * viewCtx.setLanguage(lg); viewCtx.setRequestContentLanguage(lg); for
	 * (MenuElement menuElement : children) { if (menuElement.getDepth() <=
	 * depth) { ContentElementList content = menuElement.getContent(viewCtx);
	 * while (content.hasNext(viewCtx)) {
	 * content.next(viewCtx).getXHTMLCode(viewCtx); // load cache } } } }
	 * synchronized (globalContext.getLockLoadContent()) {
	 * setViewNav(newViewNav); viewGlobalMap = contentAttributeMap; } }
	 */

	public void releaseAll(ContentContext ctx, GlobalContext globalContext) throws Exception {
		components.clear();
		releasePreviewNav(ctx);
		releaseViewNav(ctx, globalContext);
		I18nResource.getInstance(globalContext).clearAllCache();
	}

	/**
	 * release the preview nav.
	 * 
	 * @param ctx
	 *            if null context will not be updated (and content not reloaded
	 *            now).
	 * @throws Exception
	 */
	public void releasePreviewNav(ContentContext ctx) throws Exception {
		logger.fine("release preview nav");
		clearComponentCache();
		setPreviewNav(null);
		PersistenceService.getInstance(ctx.getGlobalContext()).resetVersion();
		if (this.previewGlobalMap != null) {
			this.previewGlobalMap.clear();
		}
		if (ctx != null) {
			ctx.resetCurrentPageCached();
			InfoBean.updateInfoBean(ctx);
			ctx.getGlobalContext().releaseAllCache();
		}
	}

	public void releaseTimeTravelerNav(ContentContext ctx) throws Exception {
		clearComponentCache();
		setTimeTravelerNav(null);
		if (this.timeTravelerGlobalMap != null) {
			this.timeTravelerGlobalMap.clear();
		}
		if (ctx != null) {
			ctx.resetCurrentPageCached();
			InfoBean.updateInfoBean(ctx);
		}
	}

	public void releaseViewNav(ContentContext ctx, GlobalContext globalContext) throws Exception {
		synchronized (globalContext.RELEASE_CACHE) {
			setViewNav(null);
			globalContext.releaseAllCache();
			clearComponentCache();
			shortURLMap = null;
		}
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

	public synchronized int renameKeys(String oldKeyPrefix, String newKeyPrefix) {
		Collection<String> keys = previewGlobalMap.keySet();
		Collection<String> toBeModified = new LinkedList<String>();
		int c = 0;
		for (Object keyObj : keys) {
			String key = (String) keyObj;
			if (key.startsWith(oldKeyPrefix)) {
				toBeModified.add(key);
			}
		}
		for (String key : toBeModified) {
			String newKey = StringUtils.replaceOnce(key, oldKeyPrefix, newKeyPrefix);
			previewGlobalMap.put(newKey, previewGlobalMap.get(key));
			previewGlobalMap.remove(key);
			c++;
		}
		return c;
	}

	public synchronized int duplicateKeys(String oldKeyPrefix, String newKeyPrefix) {
		Collection<String> keys = previewGlobalMap.keySet();
		Collection<String> toBeModified = new LinkedList<String>();
		int c = 0;
		for (Object keyObj : keys) {
			String key = (String) keyObj;
			if (key.startsWith(oldKeyPrefix)) {
				toBeModified.add(key);
			}
		}
		for (String key : toBeModified) {
			String newKey = StringUtils.replaceOnce(key, oldKeyPrefix, newKeyPrefix);
			previewGlobalMap.put(newKey, previewGlobalMap.get(key));
			c++;
		}
		return c;
	}

	public void setAttribute(ContentContext ctx, String key, String value) {
		if (key != null) {
			key = key.replace("&", "_and_");
		}
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

	public void setCachedComponent(ContentContext ctx, IContentVisualComponent comp) throws Exception {
		components.put(getComponentKey(ctx, comp.getId()), comp);
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
			/*while (content.hasNext(freeCtx)) {
				IContentVisualComponent comp = content.next(freeCtx);
				if (comp.getId().equals("147551884124890725710")) {
					System.out.println("***** ContentService.getAllContent : 1.comp = "+comp+" page:"+comp.getPage().getName()); //TODO: remove debug trace
				}
				outList.add(comp);				
			}
			MenuElement[] children = page.getAllChildrenLi();*/
			for (MenuElement child : page.getAllChildrenList()) {				
				content = child.getAllContent(freeCtx);
				while (content.hasNext(freeCtx)) {
					IContentVisualComponent comp = content.next(freeCtx);
					outList.add(comp);
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
				for (MenuElement child : root.getAllChildrenList()) {
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

	@Override
	public void printInfo(ContentContext ctx, PrintStream out) {
		out.println("****");
		out.println("**** ContentService print info.");
		out.println("****");
		out.println("**** #components            = " + components.size());
		out.println("**** #viewGlobalMap         = " + viewGlobalMap.size());
		out.println("**** #previewGlobalMap      = " + previewGlobalMap.size());
		if (timeTravelerGlobalMap != null) {
			out.println("**** #timeTravelerGlobalMap = " + timeTravelerGlobalMap.size());
		} else {
			out.println("**** #timeTravelerGlobalMap = 0 (null).");
		}
		out.println("****");
	}

}
