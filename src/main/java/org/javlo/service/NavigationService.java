package org.javlo.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.cache.ICache;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.PageMirrorComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.service.exception.ServiceException;

public class NavigationService {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(NavigationService.class.getName());

	private static final String KEY = NavigationService.class.getName();

	private Object lock;

	public static NavigationService getInstance(GlobalContext globalContext) throws ServiceException {
		NavigationService service = (NavigationService) globalContext.getAttribute(KEY);
		if (service == null) {
			service = new NavigationService();
			service.persistenceService = PersistenceService.getInstance(globalContext);
			service.viewPageCache = globalContext.getCache("navigation-cache-view");
			service.previewPageCache = globalContext.getCache("navigation-cache-preview");
			service.lock = globalContext.getLockLoadContent();
			globalContext.setAttribute(KEY, service);
		}
		return service;
	}

	// private Map<String, MenuElement> viewPageCache = new HashMap<String,
	// MenuElement>();

	// private Map<String, MenuElement> previewPageCache = new HashMap<String,
	// MenuElement>();

	private PersistenceService persistenceService;

	private ICache viewPageCache = null;

	// private Cache previewPageCache = null;

	private ICache previewPageCache = null;

	public void clearAllPage() {
		synchronized (lock) {
			viewPageCache.removeAll();
			previewPageCache.removeAll();
		}
	}

	public void clearAllViewPage() {
		synchronized (lock) {
			viewPageCache.removeAll();
		}
	}

	public void clearPage(ContentContext ctx) {
		synchronized (lock) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (!globalContext.isPreviewMode()) {
				viewPageCache.removeAll();
				previewPageCache.removeAll();
			} else {
				if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
					viewPageCache.removeAll();
				} else {
					previewPageCache.removeAll();
				}
			}
		}
	}

	public MenuElement getPage(ContentContext ctx, String pageKey) throws Exception {

		ICache cache;
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			cache = viewPageCache;
		} else {
			cache = previewPageCache;
		}
		MenuElement menuElement = (MenuElement) cache.get(pageKey);
		if (menuElement != null) {
			return menuElement;
		}
		synchronized (lock) {
			if (cache.getSize() == 0) { // init cache
				logger.info("reload page cache. (mode:" + ctx.getRenderMode() + ')');
				ContentService content = ContentService.getInstance(ctx.getRequest());
				MenuElement root = content.getNavigation(ctx);
				cache.put(root.getName(), root);
				cache.put(root.getId(), root);
				cache.put(root.getPath(), root);				
				for (MenuElement childpage : root.getAllChildrenList()) {
					cache.put(childpage.getName(), childpage);
					cache.put(childpage.getId(), childpage);
					cache.put(childpage.getPath(), childpage);
					List<String> vPaths = childpage.getAllVirtualPath();
					for (String vPath : vPaths) {
						cache.put(vPath, childpage);
					}
				}
			}
			menuElement = (MenuElement) cache.get(pageKey);
			if (menuElement != null) {
				return menuElement;
			} else {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				MenuElement root = content.getNavigation(ctx);				
				for (MenuElement childpage : root.getAllChildrenList()) {
					if (childpage.getName().equals(pageKey) || childpage.getId().equals(pageKey) || childpage.getPath().equals(pageKey)) {
						cache.put(childpage.getName(), childpage);
						cache.put(childpage.getId(), childpage);
						cache.put(childpage.getPath(), childpage);
						List<String> vPaths = childpage.getAllVirtualPath();
						for (String vPath : vPaths) {
							cache.put(vPath, childpage);
						}
						return childpage;
					}
				}
				return null;
			}
		}
	}

	public void removeNavigation(ContentContext ctx, MenuElement elem) throws Exception {
		Set<String> pageDeleted = new HashSet<String>();
		removeNavigationRec(pageDeleted, elem);
		persistenceService.setAskStore(true);
		for (IContentVisualComponent comp : ComponentFactory.getAllComponentsFromContext(ctx)) {
			if (comp instanceof PageMirrorComponent) {
				PageMirrorComponent pageMirror = (PageMirrorComponent) comp;
				if (pageMirror.isDeleteIfNoSource() && !pageDeleted.contains(pageMirror.getPage().getId())) {
					removeNavigationRec(pageDeleted, comp.getPage());
				}
			}
		}
	}

	public void removeNavigationNoStore(ContentContext ctx, MenuElement elem) throws Exception {
		Set<String> pageDeleted = new HashSet<String>();
		removeNavigationRec(pageDeleted, elem);
	}

	private void removeNavigationRec(Collection<String> deletedPage, MenuElement elem) throws Exception {
		Collection<MenuElement> children = new LinkedList<MenuElement>(elem.getChildMenuElements());
		for (MenuElement element : children) {
			removeNavigationRec(deletedPage, element);
		}
		if (elem.getParent() != null) {
			elem.getParent().removeChild(elem);
		}
	}

	public static void checkSameUrl(ContentContext ctx) throws Exception {
		ContentContext lgCtx = new ContentContext(ctx);
		IURLFactory urlFactory = ctx.getGlobalContext().getURLFactory(lgCtx);
		if (urlFactory != null) {
			Collection<String> lgs = ctx.getGlobalContext().getContentLanguages();
			Map<String, String> pages = new HashMap<String, String>();
			for (String lg : lgs) {
				lgCtx.setRequestContentLanguage(lg);				
				for (MenuElement menuElement : ContentService.getInstance(ctx.getGlobalContext()).getNavigation(lgCtx).getAllChildrenList()) {
					String url = lgCtx.getRequestContentLanguage() + urlFactory.createURL(lgCtx, menuElement);
					int i = 0;
					if (!menuElement.isLikeRoot(lgCtx)) {
						while (pages.keySet().contains(url) && i < 1000) {
							menuElement.setUrlNumber(menuElement.getUrlNumber() + 1);
							url = lgCtx.getRequestContentLanguage() + urlFactory.createURL(lgCtx, menuElement);
							i++;
						}
					}
					if (i == 1000) {
						logger.severe("impossible to create different url for all pages width : " + urlFactory.getClass().getName());
					}
					logger.fine("page:" + menuElement.getPath() + " as new URL number : " + menuElement.getUrlNumber());
					pages.put(url, menuElement.getName());
				}
			}
		}
	}

}
