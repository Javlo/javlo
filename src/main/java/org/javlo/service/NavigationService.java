package org.javlo.service;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.exception.ServiceException;

public class NavigationService {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(NavigationService.class.getName());

	private static final String KEY = NavigationService.class.getName();

	public static NavigationService getInstance(GlobalContext globalContext, HttpSession session) throws ServiceException {		
		NavigationService service = (NavigationService) globalContext.getAttribute(KEY);
		if (service == null) {
			service = new NavigationService();
			service.persistenceService = PersistenceService.getInstance(globalContext);
			service.viewPageCache = globalContext.getCache("navigation-cache-view");
			service.previewPageCache = globalContext.getCache("navigation-cache-preview");
			globalContext.setAttribute(KEY, service);
		}
		return service;
	}

	// private Map<String, MenuElement> viewPageCache = new HashMap<String, MenuElement>();

	// private Map<String, MenuElement> previewPageCache = new HashMap<String, MenuElement>();

	private PersistenceService persistenceService;

	private Cache viewPageCache = null;

	// private Cache previewPageCache = null;

	private Cache previewPageCache = null;

	public void clearAllPage() {
		synchronized (KEY) {
			viewPageCache.removeAll();
			previewPageCache.removeAll();
		}
	}
	
	public void clearAllViewPage() {
		synchronized (KEY) {
			viewPageCache.removeAll();			
		}
	}

	public void clearPage(ContentContext ctx) {
		synchronized (KEY) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (!globalContext.isView()) {
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
		synchronized (KEY) {
			Cache cache;
			if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
				cache = viewPageCache;
			} else {
				cache = previewPageCache;
			}
			if (cache.getSize() == 0) { // init cache
				logger.info("reload page cache. (mode:" + ctx.getRenderMode() + ')');
				ContentService content = ContentService.createContent(ctx.getRequest());
				MenuElement root = content.getNavigation(ctx);
				cache.put(new Element(root.getName(), root));
				cache.put(new Element(root.getId(), root));
				cache.put(new Element(root.getPath(), root));
				MenuElement[] pageChildren = root.getAllChilds();
				for (MenuElement childpage : pageChildren) {
					cache.put(new Element(childpage.getName(), childpage));
					cache.put(new Element(childpage.getId(), childpage));
					cache.put(new Element(childpage.getPath(), childpage));
					List<String> vPaths = childpage.getAllVirtualPath();
					for (String vPath : vPaths) {
						cache.put(new Element(vPath, childpage));
					}
				}
			}
			Element outElem = cache.get(pageKey);
			if (outElem != null) {
				return (MenuElement) outElem.getValue();
			} else {
				ContentService content = ContentService.createContent(ctx.getRequest());
				MenuElement root = content.getNavigation(ctx);
				MenuElement[] pageChildren = root.getAllChilds();
				for (MenuElement childpage : pageChildren) {
					if (childpage.getName().equals(pageKey) || childpage.getId().equals(pageKey) || childpage.getPath().equals(pageKey)) {
						cache.put(new Element(childpage.getName(), childpage));
						cache.put(new Element(childpage.getId(), childpage));
						cache.put(new Element(childpage.getPath(), childpage));
						List<String> vPaths = childpage.getAllVirtualPath();
						for (String vPath : vPaths) {
							cache.put(new Element(vPath, childpage));
						}		
						return childpage;
					}
				}
				return null;
			}
		}
	}

	public void removeNavigation(ContentContext ctx, MenuElement elem) throws Exception {
		removeNavigationRec(elem);
		persistenceService.store(ctx);
	}

	public void removeNavigationNoStore(ContentContext ctx, MenuElement elem) throws Exception {
		removeNavigationRec(elem);
	}

	private void removeNavigationRec(MenuElement elem) throws Exception {
		MenuElement[] children = elem.getChildMenuElements();
		for (MenuElement element : children) {
			removeNavigationRec(element);
		}
		if (elem.getParent() != null) {
			elem.getParent().removeChild(elem);
		}
	}

}
