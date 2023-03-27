/*
 * Created on 20 aout 2003
 */
package org.javlo.context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.helper.AjaxHelper.ScheduledRender;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringSecurityUtil;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.log.Log;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.utils.NeverEmptyMap;
import org.javlo.utils.downloader.Html2Directory;

/**
 * @author pvanderm
 */
public class ContentContext {
	
	public static final String COUNT_REQUEST_BY_SESSION_KEY = "__session_request";

	public static final String PREVIEW_ONLY_MODE = "__preview_only_mode";

	public static final String PREVIEW_EDIT_PARAM = "previewEdit";
	
	private static final String INPUT_TRACKING_KEY = "_jikey";

	private static final String HOST_DEFINED_SITE = "____host-defined-site";
	
	private static final String FORCE_PATH_PREFIX = "____force-path-prefix";

	public static final String TAKE_SCREENSHOT = "____screenshot";
	
	public static final String TAKE_SCREENSHOT_PAGE_NAME = "____screenshot_page";

	private static final String FORCE_SPECIAL_RENDERER = "forced-renderer";

	public static final int EDIT_MODE = 1;

	public static final int VIEW_MODE = 2;

	public static final int PREVIEW_MODE = 3;

	public static final int PAGE_MODE = 4;

	public static final int MAILING_MODE = 6;

	public static final int TIME_MODE = 7;

	public static final int MODULE_MODE = 8; // view module outside cms context.
	
	public static final int POST_MODE = 9;
	
	public static final int GET_MODE = 10;
	
	public static final int MODULE_DESKTOP_SPECIAL_MODE = 1000;
	
	public static final int MODULE_MOBILE_SPECIAL_MODE = 1001;

	public static final String FORWARD_PATH_REQUEST_KEY = "forward-path";

	public static final String FORWARD_AJAX = "ajax-request";

	public static final String FORCE_MODE_PARAMETER_NAME = "render-mode";

	public static final String CHANGE_AREA_ATTRIBUTE_NAME = "_change_area";

	public static final String FORCE_ABSOLUTE_URL = "_absolute-url";

	public static String CHARACTER_ENCODING = "UTF-8";

	public static Charset CHARSET_DEFAULT = Charset.forName(CHARACTER_ENCODING);

	public static String PRODUCT_NAME = "Javlo 2";

	public static String FORCED_CONTENT_PREFIX = "forced_content_";

	/**
	 * param for render content with link to local server and not DMZ server.
	 */
	public static final String NO_DMZ_PARAM_NAME = "nodmz";

	public static final String CLEAR_SESSION_PARAM = "_clear_session";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ContentContext.class.getName());

	public static String CONTEXT_REQUEST_KEY = "contentContext";

	public WeakReference<MenuElement> currentPageCached = null;

	private WeakReference<MenuElement> virtualCurrentPage = null;

	private boolean pageAssociation = false;

	private boolean postRequest = false;

	private int titleDepth = 1;

	private boolean clearSession = false;

	private boolean forceCorrectPath = false;

	private Boolean contentStatic = null;

	private Boolean onlyArea = null;

	private boolean stopRendering = false;

	private boolean takeScreenShort = true;
	
	private boolean noCache = false;
	
	private Set<String> mirrorId = new HashSet<>();
	
	private Locale locale = null;
	
	private String forcePageTitle = null;
	

	private static ContentContext createContentContext(HttpServletRequest request, HttpServletResponse response, boolean free, boolean pageManagement) {
		ContentContext ctx = new ContentContext();
		ctx.pageManagement = pageManagement;
		ctx.setFree(free);
		init(ctx, request, response);
		ctx.storeInRequest(request);
		// count request by session
		if (request.getAttribute(COUNT_REQUEST_BY_SESSION_KEY) == null) {
			request.setAttribute(COUNT_REQUEST_BY_SESSION_KEY, true);
			Integer count = (Integer)request.getSession().getAttribute(COUNT_REQUEST_BY_SESSION_KEY);
			if (count == null) {
				count = 0;
			}
			request.getSession().setAttribute(COUNT_REQUEST_BY_SESSION_KEY, count+1);
		}
		return ctx;
	}
	
	public int getRequestCountOnSession() {
		return (int)request.getSession().getAttribute(COUNT_REQUEST_BY_SESSION_KEY);
	}

	/**
	 * return free content ctx, context not linked to a specific page, use in
	 * imagetransform servlet or something like that.
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static ContentContext getFreeContentContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext freeCtx = createContentContext(request, response, true, true);
		return freeCtx;
	}

	public static ContentContext getContentContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return getContentContext(request, response, true, true);
	}
	
	public static ContentContext getContentContextNoPageManagement(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return getContentContext(request, response, true, false);
	}

	public ContentContext getContentContextForInternalLink() {
		if (getRenderMode() != ContentContext.PAGE_MODE) {
			return this;
		} else {
			ContentContext viewContext = new ContentContext(this);
			viewContext.setRenderMode(ContentContext.VIEW_MODE);
			return viewContext;
		}
	}
	
	public static ContentContext getContentContext(HttpServletRequest request, HttpServletResponse response, boolean correctPath) throws Exception {
		return getContentContext(request, response, correctPath, true);
	}
	
	public String getInputTrackingKey() {
		return getInputTrackingKey(getRequest().getSession());
	}
	
	public static String getInputTrackingKey(HttpSession session) {
		return (String)session.getAttribute(INPUT_TRACKING_KEY);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param correctPath
	 *            true for search real page and construct new path with this page.
	 * @return
	 * @throws Exception
	 */
	public static ContentContext getContentContext(HttpServletRequest request, HttpServletResponse response, boolean correctPath, boolean pageManagement) throws Exception {
		ContentContext ctx = (ContentContext) request.getAttribute(CONTEXT_REQUEST_KEY);
		try {
			if (ctx == null) {
				ctx = createContentContext(request, response, true, pageManagement);
				ctx.setFree(false);
				ctx.correctPath = false;
				
				if (request.getParameter(INPUT_TRACKING_KEY) != null) {
					request.getSession().setAttribute(INPUT_TRACKING_KEY, request.getParameter(INPUT_TRACKING_KEY));
				}

			} else {
				ctx.setRequest(request);
				if (response != null) {
					ctx.setResponse(response);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		ctx.pageManagement = pageManagement;

		GlobalContext globalContext = ctx.getGlobalContext();
		EditContext editContext = EditContext.getInstance(globalContext, ctx.getRequest().getSession());

		if (globalContext.getForcedHost().length() > 0) {
			ctx.setHostName(globalContext.getForcedHost());
		}

		if (!ctx.isForceCorrectPath()) {
			correctPath = false;
		}

		if (ctx.getRenderMode() != ContentContext.EDIT_MODE && !editContext.isPreviewEditionMode() && !ctx.correctPath && correctPath || ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			if (!ctx.isAjax()) {
				ctx.correctPath = correctPath;
				ContentService content = ContentService.getInstance(GlobalContext.getInstance(request));
				if (!content.contentExistForContext(ctx)) {
					if (correctPath) {
						MenuElement menu = content.getNavigation(ctx);
						if (menu != null) {
							menu = menu.searchChild(ctx);
							if ((menu != null) && (menu.getChildMenuElements().size() > 0)) {
								ctx.setPath(menu.getChildMenuElements().iterator().next().getPath());
								if (!content.contentExistForContext(ctx)) {
									if (menu.getChildMenuElements().iterator().next().getChildMenuElements().size() > 0) {
										ctx.setPath(menu.getChildMenuElements().iterator().next().getChildMenuElements().iterator().next().getPath());
									}
								}
							}
						}
					}
				}
			}
		}

		return ctx;
	}

	/**
	 * check if there are a contentcontext in the request
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isContentContext(HttpServletRequest request) {
		return (ContentContext) request.getAttribute(CONTEXT_REQUEST_KEY) != null;
	}

	public static String getRenderModeKey(int mode) {
		switch (mode) {
		case EDIT_MODE:
			return "edit";
		case VIEW_MODE:
			return "view";
		case PREVIEW_MODE:
			return "preview";
		case PAGE_MODE:
			return "page";
		case MAILING_MODE:
			return "mailing";
		case TIME_MODE:
			return "time";
		case MODULE_MODE:
			return "module";
		case MODULE_DESKTOP_SPECIAL_MODE:
			return "desktop";
		case MODULE_MOBILE_SPECIAL_MODE:
			return "mobile";
		case GET_MODE:
			return "get";
		case POST_MODE:
			return "post";
		default:
			return "unknown";
		}
	}

	/**
	 * get ContentContext without globalContext
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static ContentContext getEmptyContentContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = new ContentContext();
		ctx.setRequest(request);
		ctx.setResponse(response);
		return ctx;
	}

	private static void init(ContentContext ctx, HttpServletRequest request, HttpServletResponse response) {
		try {
			ctx.ajax = ContentManager.isAjax(request);
			ctx.setRequest(request);
			ctx.setResponse(response);
			ctx.setPath(ContentManager.getPath(request));
			
			RequestService requestService = RequestService.getInstance(request);
			String forcedMode = requestService.getParameter(FORCE_MODE_PARAMETER_NAME, null);
			if (forcedMode != null) {
				ctx.renderMode = Integer.parseInt(forcedMode);
			} else {
				if (ContentManager.isEdit(request)) {
					ctx.renderMode = EDIT_MODE;
				}
				if (ContentManager.isPreview(request)) {
					ctx.renderMode = PREVIEW_MODE;
				}
				if (ContentManager.isMailing(request)) {
					ctx.renderMode = MAILING_MODE;
				}
				if (ContentManager.isTime(request)) {
					ctx.renderMode = TIME_MODE;
				}
			}
			if (StringHelper.isTrue(requestService.getParameter("closePopup", null))) {
				ctx.setClosePopup(true);
			}
			if (requestService.getParameter("parentURL", null) != null) {
				ctx.setParentURL(requestService.getParameter("parentURL", null));
			}
			String lg = ContentManager.getLanguage(ctx);
			ctx.setLanguage(lg);
			String contentLg = ContentManager.getContentLanguage(ctx);

			// TODO : optimise this with option in global context

			GlobalContext globalContext = ctx.getGlobalContext();

			if (contentLg == null) {
				contentLg = lg;
				ctx.setContentLanguage(contentLg);
			} else {
				if (ctx.renderMode != EDIT_MODE && ctx.renderMode != PREVIEW_MODE) {
					if (globalContext.isAutoSwitchToDefaultLanguage()) {
						ctx.setRequestContentLanguage(contentLg);
						ctx.setContentLanguage(lg);
					} else {
						ctx.setContentLanguage(contentLg);
					}
				} else {
					ctx.setContentLanguage(contentLg);
				}
			}

			if (!ctx.isEdit() && !ctx.isAjax()) {
				EditContext editContext = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
				if (!ctx.isPreview() || !editContext.isPreviewEditionMode()) {
					try {
						MenuElement page = ctx.getCurrentPage(true);
						if (page != null) {
							while (page != null && !page.isRealContentAnyLanguage(ctx) && page.getChildMenuElements().size() > 0) {
								Iterator<MenuElement> children = page.getChildMenuElements().iterator();
								page = children.next();
								while (page != null && !page.isActive(ctx) && children.hasNext()) {
									page = children.next();
								}
							}
							if (page != null) {
								if (page.isRealContentAnyLanguage(ctx)) {
									ctx.setCurrentPageCached(page);
									ctx.setPath(page.getPath());
								}
							} else {
								page = ctx.getCurrentPage(true);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (ctx.getDevice() == null) {
				ctx.setDevice(Device.getDevice(ctx));
			}
			StaticConfig config = StaticConfig.getInstance(request.getSession());
			ctx.viewPrefix = config.isViewPrefix();

			ctx.urlFactory = globalContext.getURLFactory(ctx);
			ctx.dmzServerInter = globalContext.getDMZServerInter();
			if (requestService.getParameter(FORCE_ABSOLUTE_URL) != null) {
				ctx.setAbsoluteURL(StringHelper.isTrue(requestService.getParameter(FORCE_ABSOLUTE_URL)));
			}
			if (StringHelper.isTrue(requestService.getParameter(NO_DMZ_PARAM_NAME, null))) {
				ctx.resetDMZServerInter();
			}
			if (StringHelper.isTrue(requestService.getParameter(CLEAR_SESSION_PARAM, null))) {
				ctx.clearSession = true;
			}
			if (requestService.getParameter(FORCE_SPECIAL_RENDERER) != null) {
				IUserFactory fact = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
				if (AdminUserSecurity.getInstance().isAdmin(fact.getCurrentUser(globalContext, request.getSession()))) {
					ctx.setSpecialContentRenderer(requestService.getParameter(FORCE_SPECIAL_RENDERER));
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	private String path = "/";

	private String language = null;

	private String contentLanguage = null;
	
	private String mainLanguage = null;

	private String requestContentLanguage = null;

	boolean array = false;

	int renderMode = VIEW_MODE;

	boolean viewPrefix = false;

	boolean pageRequest = false;

	boolean correctPath = true;

	private boolean componentCache = true;

	IURLFactory urlFactory = null;
	URL dmzServerInter = null;
	boolean visible = true;

	boolean isAbsoluteURL = false;

	private boolean export = false;

	public Boolean contentExistForContext = null;

	public Boolean editPreview = null;

	private boolean checkContentArea = true;
	
	private boolean actionServlet = false;

	/** cache **/

	// private MenuElement currentPageCached = null;

	/**
	 * contain a jsp page to be insered in the content place. content is insered if
	 * this attribute is null.
	 */
	private String specialContentRenderer = null;

	private boolean ajax = false;

	private boolean needRefresh = false;

	private String hostName = null;

	private boolean internalURL = false;

	private int hostPort = -1;

	private HttpServletRequest request;

	// Map requestContents = new HashMap();

	private HttpServletResponse response;;

	private String area = ComponentBean.DEFAULT_AREA;

	private String virtualArea = null;

	private Template currentTemplate = null;

	private Device device = null;

	private String protocol = null;

	private String format = null;

	private boolean free = false;

	private Collection<String> availableLanguages = null;

	private Collection<String> availableContentLanguages = null;

	private boolean refreshParent = false;

	private Boolean closePopup = null;

	private ContentContext() {
	}

	private Map<String, String> ajaxInsideZone = null;
	private Map<String, Object> ajaxData = null;
	private String specificJson = null;
	private Map<String, ScheduledRender> scheduledAjaxInsideZone = null;
	private Map<String, String> ajaxZone = null;

	private Map<? extends Object, ? extends Object> ajaxMap = null;

	private boolean contentFound = true;

	private GlobalContext forceGlobalContext = null;

	private String contextRequestLanguage = null;

	private Map<Integer,Integer> columnableSizes = new NeverEmptyMap<Integer, Integer>(Integer.class);
	
	private int columnableDepth = 0;

	private Map<String, Object> attributes = null;

	private boolean pageManagement = true;
	
	private Boolean prod = null;

	public ContentContext(ContentContext ctx) {
		path = ctx.path;
		language = ctx.language;
		contentLanguage = ctx.contentLanguage;
		requestContentLanguage = ctx.requestContentLanguage;
		setRequest(ctx.getRequest());
		setResponse(ctx.getResponse());
		renderMode = ctx.renderMode;
		area = ctx.area;
		setAbsoluteURL(ctx.isAbsoluteURL);
		setHostName(ctx.hostName);
		setHostPort(ctx.hostPort);

		viewPrefix = ctx.viewPrefix;
		urlFactory = ctx.urlFactory;
		dmzServerInter = ctx.dmzServerInter;
		specialContentRenderer = ctx.specialContentRenderer;
		pageRequest = ctx.pageRequest;
		free = ctx.free;
		device = ctx.getDevice();
		format = ctx.format;
		correctPath = ctx.correctPath;
		export = ctx.export;

		ajaxData = ctx.ajaxData;
		ajaxInsideZone = ctx.ajaxInsideZone;
		ajaxData = ctx.ajaxData;
		ajax = ctx.ajax;
		scheduledAjaxInsideZone = ctx.scheduledAjaxInsideZone;

		pageManagement = ctx.pageManagement;
		
		currentTemplate = ctx.currentTemplate;

		editPreview = ctx.editPreview;

		currentPageCached = ctx.currentPageCached;

		pageAssociation = ctx.pageAssociation;

		componentCache = ctx.componentCache;

		internalURL = ctx.internalURL;

		checkContentArea = ctx.checkContentArea;

		forceGlobalContext = ctx.forceGlobalContext;
		
		forcePageTitle = ctx.forcePageTitle;
	}

	public String getArea() {
		return area;
	}

	/**
	 * @return
	 * @throws Exception
	 * @deprecated Prefer use of {@link #getRequestContentLanguage()}.
	 */
	@Deprecated
	public String getContentLanguage() {
		try {
			if (isCheckContentArea() && getCurrentTemplate() != null && getCurrentTemplate().isNavigationArea(getArea())) {
				return getLanguage();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contentLanguage;
	}

	public ContentContext getFreeContentContext() {
		ContentContext newCtx = new ContentContext(this);
		newCtx.setFree(true);
		return newCtx;
	}

	public List<ContentContext> getContextForAllLanguage() {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		Collection<String> lgs = globalContext.getContentLanguages();
		List<ContentContext> ctxs = new LinkedList<ContentContext>();
		for (String lg : lgs) {
			ContentContext lgCtx = new ContentContext(this);
			lgCtx.setRequestContentLanguage(lg);
			lgCtx.setContentLanguage(lg);
			ctxs.add(lgCtx);
		}
		return ctxs;
	}

	public ContentContext getContextWithoutArea() {
		ContentContext outContext = new ContentContext(this);
		outContext.setArea(null);
		return outContext;
	}

	public ContentContext getContextWithOtherFormat(String format) {
		if (format == null || getFormat().equals(format)) {
			return this;
		} else {
			ContentContext outContext = new ContentContext(this);
			outContext.setFormat(format);
			return outContext;
		}		
	}

	public ContentContext getContextWithArea(String area) {
		ContentContext outContext = new ContentContext(this);
		outContext.setArea(area);
		return outContext;
	}

	public ContentContext getContextWithOtherRenderMode(int mode) {
		ContentContext outContext = new ContentContext(this);
		outContext.setRenderMode(mode);
		return outContext;
	}
	
	public ContentContext getContextForAjax() {
		ContentContext outContext = new ContentContext(this);
		outContext.setAjax(true);
		return outContext;
	}

	public ContentContext getContextWidthOtherRequestLanguage(String lang) {
		ContentContext outContext = new ContentContext(this);
		outContext.setRequestContentLanguage(lang);
		return outContext;
	}

	public ContentContext getContextWithInternalURL() {
		ContentContext outContext = new ContentContext(this);
		outContext.setInternalURL(true);
		outContext.setAbsoluteURL(false);
		return outContext;
	}

	/**
	 * get the edit language in edit mode and view language if other mode.
	 * 
	 * @return
	 */
	public String getContextLanguage() {
		if (getRenderMode() == ContentContext.EDIT_MODE) {
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			return globalContext.getEditLanguage(getRequest().getSession());
		} else {
			return getLanguage();
		}
	}

	/**
	 * return a context with real content (if exist), it can be change the language
	 * (and only this) of the current context. this method use only the default
	 * language list.
	 * 
	 * @return null if no content found.
	 * @throws Exception
	 */
	public ContentContext getContextWithContent(MenuElement page) throws Exception {
		if (page.isRealContent(this)) {
			return getContextOnPage(page);
		} else {
			ContentContext lgCtx = new ContentContext(this);
			lgCtx.setContentLanguage(getLanguage());
			lgCtx.setRequestContentLanguage(null);
			/* is content in current content language ? */
			if (page.isRealContent(lgCtx)) {
				return lgCtx;
			} else {
				GlobalContext globalContext = GlobalContext.getInstance(getRequest());
				Collection<String> lgs = globalContext.getDefaultLanguages();
				for (String lg : lgs) {
					lgCtx.setAllLanguage(lg);
					if (page.isRealContent(lgCtx)) {
						return lgCtx.getContextOnPage(page);
					}
				}
				lgCtx = new ContentContext(this);
				for (String lg : globalContext.getContentLanguages()) {
					lgCtx.setContentLanguage(lg);
					if (page.isRealContent(lgCtx)) {
						return lgCtx.getContextOnPage(page);
					}
				}
			}
		}
		return null;
	}

	/**
	 * return a context with at least one element (if exist), it can be change the
	 * language (and only this) of the current context. this method use only the
	 * default language list.
	 * 
	 * @return null if no content found.
	 * @throws Exception
	 */
	public ContentContext getContextNotEmpty(MenuElement page) throws Exception {
		if (!page.isEmpty(this, ComponentBean.DEFAULT_AREA, false)) {
			return getContextOnPage(page);
		} else {
			ContentContext lgCtx = new ContentContext(this);
			lgCtx.setContentLanguage(getLanguage());
			lgCtx.setRequestContentLanguage(null);
			/* is content in current content language ? */
			if (!page.isEmpty(lgCtx, ComponentBean.DEFAULT_AREA, false)) {
				return lgCtx;
			} else {
				GlobalContext globalContext = GlobalContext.getInstance(getRequest());
				Collection<String> lgs = globalContext.getDefaultLanguages();
				for (String lg : lgs) {
					lgCtx.setAllLanguage(lg);
					if (!page.isEmpty(lgCtx, ComponentBean.DEFAULT_AREA, false)) {
						return lgCtx.getContextOnPage(page);
					}
				}
			}
		}
		return null;
	}

	/**
	 * return a context with at least one title (if exist), it can be change the
	 * language (and only this) of the current context. this method use only the
	 * default language list.
	 * 
	 * @return null if no content found.
	 * @throws Exception
	 */
	public ContentContext getContextWidthTitle(MenuElement page) throws Exception {
		if (page.isTitle(this)) {
			return getContextOnPage(page);
		} else {
			ContentContext lgCtx = new ContentContext(this);
			lgCtx.setAllLanguage(getLanguage());
			/* is content in current content language ? */
			if (page.isTitle(lgCtx)) {
				return lgCtx;
			} else {
				GlobalContext globalContext = GlobalContext.getInstance(getRequest());
				Collection<String> lgs = globalContext.getDefaultLanguages();
				for (String lg : lgs) {
					lgCtx.setAllLanguage(lg);
					if (page.isTitle(lgCtx)) {
						return lgCtx.getContextOnPage(page);
					}
				}
			}
		}
		return null;
	}

	/**
	 * return a context with language (if exist), it can be change the language (and
	 * only this) of the current context). this method use only the default language
	 * list.
	 * 
	 * @return null if no content found.
	 * @throws Exception
	 */
	public ContentContext getContextWithContentDEBUG(MenuElement page) throws Exception {
		System.out.println("***** ContentContext.getContextWithContentDEBUG : START : " + page.getPath());
		if (page.isRealContent(this)) {
			return getContextOnPage(page);
		} else {
			ContentContext lgCtx = new ContentContext(this);
			lgCtx.setArea(ComponentBean.DEFAULT_AREA);
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			Collection<String> lgs = globalContext.getDefaultLanguages();
			for (String lg : lgs) {
				lgCtx.setAllLanguage(lg);
				System.out.println("** lg : " + lg + " " + page.isRealContent(lgCtx));
				if (page.isRealContent(lgCtx)) {
					System.out.println("** FOUND : " + lg);
					return lgCtx.getContextOnPage(page);
				}
			}
		}
		System.out.println("NOT FOUND.");
		return null;
	}

	/**
	 * return a context with language (if exist), it can be change the language (and
	 * only this) of the current context). this method use only the default language
	 * list.
	 * 
	 * @return null if no content found.
	 * @throws Exception
	 */
	public ContentContext getContextWithContentNeverNull(MenuElement page) throws Exception {
		if (page.isRealContent(this)) {
			return new ContentContext(this);
		} else {
			ContentContext lgCtx = new ContentContext(this);
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			Collection<String> lgs = globalContext.getLanguages();
			for (String lg : lgs) {
				lgCtx.setLanguage(lg);
				lgCtx.setContentLanguage(lg);
				lgCtx.setRequestContentLanguage(lg);
				if (page.isRealContent(lgCtx)) {
					return lgCtx.getContextOnPage(page);
				}
			}
		}
		return getContextOnPage(page);
	}

	public ContentContext getContextOnPage(MenuElement page) throws Exception {
		if (page == null || getPath().equals(page.getPath())) {
			return new ContentContext(this);
		} else {
			ContentContext outCtx = new ContentContext(this);
			outCtx.setPath(page.getPath());
			outCtx.setCurrentPageCached(page);
			return outCtx;
		}
	}

	public ContentContext getContextForAbsoluteURL() {
		ContentContext outCtx = new ContentContext(this);
		outCtx.setAbsoluteURL(true);
		return outCtx;
	}

	/**
	 * get the edit language in edit mode and view language if other mode.
	 * 
	 * @return
	 */
	public String getContextRequestLanguage() {
		if (contextRequestLanguage != null) {
			return contextRequestLanguage;
		} else if (getRenderMode() == ContentContext.EDIT_MODE) {
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			return globalContext.getEditLanguage(getRequest().getSession());
		} else {
			return getRequestContentLanguage();
		}
	}

	public void setContextRequestLanguage(String lg) {
		contextRequestLanguage = lg;
	}

	public ContentContext getContextForDefaultLanguage() {
		GlobalContext globalContext = GlobalContext.getInstance(getRequest());
		ContentContext defaultLgCtx = new ContentContext(this);
		defaultLgCtx.setAllLanguage(globalContext.getDefaultLanguage());
		return defaultLgCtx;
	}

	public String getCookieLanguage() {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			logger.fine("cookies not found.");
			return null;
		}

		String name = "";
		String value = "";

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				name = cookie.getName();
				value = cookie.getValue();
				if (name.equals("user-language")) {
					return value;
				}
			}
		}
		return null;
	}
	
	private boolean isPageManagement() {
		if (isActionServlet()) {
			return false;
		}
		return pageManagement;
	}

	private MenuElement getCurrentPage(boolean urlFacotry) throws Exception {
		
		if (!isPageManagement()) {
			return ContentService.getInstance(getGlobalContext()).getNavigation(this);
		}
		
		MenuElement outPage = getCurrentPageCached();
		if (outPage == null) {
			GlobalContext globalContext = getGlobalContext();
			MenuElement root = ContentService.getInstance(globalContext).getNavigation(this);
			
			if (getPath().equals("/")) {
				outPage = root;
			} else {
				if (getPath().trim().length() > 0) {
					MenuElement elem = globalContext.getPageIfExist(this, getPath(), urlFacotry);
					if (elem != null) {
						if (getRenderMode() != EDIT_MODE && !NetHelper.isIPAccepted(this)) {
							if (!StringHelper.isEmpty(elem.getIpSecurityErrorPageName())) {
								logger.warning("unauthorized access ip security : " + elem.getIpSecurityErrorPageName());
								response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
								String pageName = elem.getIpSecurityErrorPageName();
								elem = root.searchChildFromName(pageName);
								if (elem == null) {
									logger.warning("no ip age not found : " + pageName);
								}
								return elem;
							}
						}
					}
					if (elem != null) {
						setCurrentPageCached(elem);
						globalContext.storeUrl(this, getPath(), elem.getId());
					} else if (urlFacotry) {
						globalContext.log(Log.SEVERE, "url", "url not found : " + getPath());
						elem = globalContext.convertOldURL(this, getPath());
						if (elem != null) {
							String newURL = URLHelper.createURL(this, elem);
							globalContext.log(Log.WARNING, "url", "old redirect : " + getPath() + " >> " + newURL);
							logger.info("redirect old url (" + getGlobalContext().getContextKey() + " - " + getPath() + ") --> = " + newURL + " - url renderer:" + globalContext.getURLFactoryClass());
							if (!response.isCommitted()) {
								response.sendRedirect(newURL);
							}
							setCurrentPageCached(elem);
						} else {
							globalContext.log(Log.WARNING, "url", "redirect old url not found : " + getPath());
							setContentFound(false);
							elem = root;
							setPath(root.getPath());
						}
					}
					outPage = elem;
				} else {
					outPage = root;
				}
			}
		}

		if (outPage == null) {
			logger.warning("page not found : " + getPath());
		}

		if (isAsViewMode() && outPage != null && !outPage.isActive(this)) {
			if (outPage.isActive()) {
				logger.info("page not found (" + getGlobalContext().getContextKey() + ") : " + getPath()+" page:"+outPage.getName());
			}
			if (!isFree()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return null;
		} else {
			return outPage;
		}
	}

	public MenuElement getCurrentPage() throws Exception {
		MenuElement outPage = getCurrentPage(false);
		return outPage;
	};

	public MenuElement getCurrentPageCached() {
		if (currentPageCached == null) {
			return null;
		} else {
			return currentPageCached.get();
		}
	}

	public Template getCurrentTemplate() throws Exception {

		if (isFree()) {
			return null;
		}

		if (currentTemplate == null) {
			Template template = null;

			RequestService rs = RequestService.getInstance(request);

			String forceTemplate = rs.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME, null);
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			if (forceTemplate != null) {
				logger.fine("force template : " + forceTemplate);
				template = Template.getApplicationInstance(getRequest().getSession().getServletContext(), this, forceTemplate);
			}
			if (template == null) {
				template = TemplateFactory.getTemplate(this, getCurrentPage());
			}
			if ((template == null) || !template.exist()) {
				if (globalContext.getDefaultTemplate() != null) {
					template = Template.getApplicationInstance(getRequest().getSession().getServletContext(), this, globalContext.getDefaultTemplate());
				}
			}
			if (template != null && getSpecialContentRenderer() != null) {
				if (template.getSpecialRendererTemplate() != null) {
					Template newTemplate = TemplateFactory.getTemplates(getRequest().getSession().getServletContext()).get(template.getSpecialRendererTemplate());
					if (newTemplate != null) {
						template = newTemplate;
					}
				}
			}

			if (template != null && !template.isTemplateInWebapp(this)) {
				template.importTemplateInWebapp(globalContext.getStaticConfig(), this);
			}

			if (template != null) {
				currentTemplate = template.getFinalTemplate(this);
			}
		}

		return currentTemplate;
	}

	public Device getDevice() {
		return device;
	}
	
	public boolean isMobile() {
		Device device = getDevice();
		if (device == null) {
			return false;
		} else {
			return device.isMobileDevice();
		}
	}

	/**
	 * get all device accessible from current template.
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getDeviceNames() throws Exception {
		List<String> deviceNames = new LinkedList<String>(getCurrentTemplate().getRenderers());
		for (int i = 0; i < deviceNames.size(); i++) {
			if (deviceNames.get(i).trim().length() == 0) {
				deviceNames.set(i, Device.DEFAULT);
			}
		}
		return deviceNames;
	}

	public URL getDMZServerInter() {
		return dmzServerInter;
	}

	/**
	 * reset DMZ for force local absolute URL.
	 */
	public void resetDMZServerInter() {
		dmzServerInter = null;
	}

	public String getHostName() {
		if (hostName != null) {
			return hostName;
		}
		return request.getServerName();
	}

	public int getHostPort() {
		if (hostPort >= 0) {
			return hostPort;
		}
		return request.getServerPort();
	}

	/**
	 * @return
	 */
	public String getLanguage() {
		return language;
	}

	public Locale getLocale() {
		if (locale == null) {
			locale = new Locale(getRequestContentLanguage(), getLocalCountry());
		}
		return locale;
	}

	/**
	 * @return a logical path
	 */
	public String getPath() {
		return path;
	}

	public int getRenderMode() {
		return renderMode;
	}

	/**
	 * @return
	 */
	public HttpServletRequest getRequest() {
		return request;
	}
	
	public HttpSession getSession() {
		return request.getSession();
	}
	
	public ServletContext getServletContext()  {
		return request.getSession().getServletContext();
	}

	public RequestService getRequestService() {
		return RequestService.getInstance(request);
	}

	public String getRequestContentLanguage() {
		if (requestContentLanguage == null) {
			return getContentLanguage();
		} else {
			try {
				if (isCheckContentArea() && getCurrentTemplate() != null && getCurrentTemplate().isNavigationArea(getArea())) {
					requestContentLanguage = getLanguage();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return requestContentLanguage;
		}
	}

	public String getRequestContentLanguageRAW() {
		return requestContentLanguage;
	}

	/**
	 * @return
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * redirect.
	 * 
	 * @param url
	 * @throws IOException
	 */
	public void sendRedirect(String url) throws IOException {
		response.sendRedirect(url);
	}

	public String getSpecialContentRenderer() {
		return specialContentRenderer;
	}

	public IURLFactory getURLFactory() {
		return urlFactory;
	}

	public void setURLFactory(IURLFactory urlFactory) {
		this.urlFactory = urlFactory;
	}

	public String getURLProtocolPrefix() {
		if (protocol != null) {
			return protocol;
		} else {
			if (getGlobalContext().isForcedHttps()) {
				protocol = "https";
				return protocol;
			}
			String requestProtocol = request.getProtocol().toLowerCase();
			if (requestProtocol.startsWith("http")) {
				if (request.isSecure()) {
					protocol = "https"; // https dy default if secure
				} else {
					protocol = "http"; // http dy default
				}
			} else if (requestProtocol.startsWith("https")) {
				protocol = "https";
			} else if (requestProtocol.startsWith("ftp")) {
				protocol = "ftp";
			} else {
				logger.warning("protocol not identified : " + requestProtocol);
				if (request.isSecure()) {
					protocol = "https"; // https dy default if secure
				} else {
					protocol = "http"; // http dy default
				}
			}
			return protocol;
		}
	}

	public boolean isAbsoluteURL() {
		return isAbsoluteURL;
	}

	public boolean isAjax() {
		return ajax;
	}

	public boolean isArray() {
		return array;
	}

	public boolean isAsModifyMode() {
		return getRenderMode() == PREVIEW_MODE || getRenderMode() == EDIT_MODE;
	}

	public boolean isAsPreviewMode() {
		return getRenderMode() == PREVIEW_MODE && !isPreviewOnly();
	}
	
	public boolean isAsTimeMode() {
		return getRenderMode() == TIME_MODE;
	}

	public boolean isAsPageMode() {
		return getRenderMode() == PAGE_MODE;
	}

	public boolean isAsEditMode() {
		return getRenderMode() == EDIT_MODE;
	}

	public boolean isAsViewMode() {
		return getRenderMode() == VIEW_MODE || getRenderMode() == TIME_MODE && isPreviewOnly();
	}

	public boolean isVisualMode() {
		return isAsViewMode() || isAsPreviewMode();
	}

	/**
	 * true if the current request must be tracked.
	 * 
	 * @return
	 */
	public boolean isTrackingContext() {
		return isAsViewMode();
	}

	/**
	 * @return true if editable context
	 */
	public boolean isEdit() {
		return renderMode == EDIT_MODE;
	}

	public boolean isInteractiveMode() {
		return getRenderMode() == PREVIEW_MODE || getRenderMode() == TIME_MODE;
	}

	public boolean isNeedRefresh() {
		return needRefresh;
	}

	public boolean isPreview() {
		return renderMode == PREVIEW_MODE;
	}
	
	public boolean isTime() {
		return renderMode == TIME_MODE;
	}

	/**
	 * return true if render mode in preview only (no tools displayed, like view
	 * mode with preview data)
	 * 
	 * @return
	 */
	public boolean isPreviewOnly() {
		RequestService rs = RequestService.getInstance(request);
		return StringHelper.isTrue(rs.getParameter(PREVIEW_ONLY_MODE));
	}
	
	/**
	 * return true if content is editable in preview mode
	 * @return
	 */
	public boolean isPreviewEditionMode() {
		if (!isAsPreviewMode())  {
			return false;
		} else {
			return EditContext.getInstance(getGlobalContext(), request.getSession()).isPreviewEditionMode();
		}
	}

	public boolean isViewPrefix() {
		return viewPrefix;
	}

	public void setViewPrefix(boolean viewPrefix) {
		this.viewPrefix = viewPrefix;
	}

	/**
	 * @return
	 */
	public boolean isVisible() {
		return visible;
	}

	protected void resetCache() {
		resetCurrentPageCached();
		setCurrentTemplate(null);
		contentExistForContext = null;
	}

	public void setAbsoluteURL(boolean isAbsoluteURL) {
		this.isAbsoluteURL = isAbsoluteURL;
	}

	public ContentContext setAjax(boolean ajax) {
		this.ajax = ajax;
		return this;
	}

	/**
	 * return if the current request is for a page access and not a resource (jsp,
	 * img, js...)
	 * 
	 * @return
	 */
	public boolean isPageRequest() {
		return pageRequest;
	}

	public void setPageRequest(boolean pageRequest) {
		this.pageRequest = pageRequest;
	}

	/**
	 * setLanguage, setContentLanguage and setRequestContentLanguage with a lg
	 * 
	 * @param lg
	 */
	public void setAllLanguage(String lg) {
		setLanguage(lg);
		setContentLanguage(lg);
		setRequestContentLanguage(lg);
	}

	public void setArea(String area) {
		this.area = area;
	}

	public void setDefaultArea() {
		this.area = ComponentBean.DEFAULT_AREA;
	}

	public void setArray(boolean array) {
		this.array = array;
	}

	public void setContentLanguage(String lg) {
		if (contentLanguage != null && contentLanguage.equals(lg)) {
			return;
		}
		if (availableContentLanguages == null) {
			availableContentLanguages = GlobalContext.getInstance(request).getContentLanguages();
		}
		if (availableContentLanguages.contains(lg)) {
			contentLanguage = lg;
		} else {
			logger.fine("content language not available : " + lg);
			contentLanguage = getLanguage();
		}
		resetCache();
	}

	public void setCookieLanguage(String lang) {
		Cookie[] cookies = request.getCookies();

		String name = "";

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				name = cookie.getName();
				if (name.equals("user-language")) {
					cookie.setValue(lang);
					cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
					cookie.setPath("/");
					response.addCookie(cookie);
					return;
				}
			}
		}
		Cookie cookie = new Cookie("user-language", lang);
		// cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
		response.addCookie(cookie);
	}

	public void setCurrentPageCached(MenuElement currentPageCached) throws Exception {
		this.currentPageCached = new WeakReference<MenuElement>(currentPageCached);
	}

	public void resetCurrentPageCached() {
		currentPageCached = null;
	}

	/*
	 * public void setCurrentTemplate(Template currentTemplate) {
	 * this.currentTemplate = currentTemplate; }
	 */

	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * @deprecated use setRenderMode()
	 * @param b
	 */
	@Deprecated
	public void setEdit(boolean b) {
		if (b) {
			setRenderMode(EDIT_MODE);
		} else {
			setRenderMode(VIEW_MODE);
		}
	}

	public void setHostName(String inHost) {
		hostName = inHost;
	}

	public void setHostPort(int port) {
		hostPort = port;
	}

	/**
	 * @param lg
	 */
	public void setLanguage(String lg) {
		if (language != null && language.equals(lg)) {
			return;
		}
		if (availableLanguages == null) {
			availableLanguages = GlobalContext.getInstance(request).getLanguages();
		}
		if (availableLanguages.contains(lg)) {
			language = lg;
		} else {
			logger.fine("language not available : " + lg);
			language = GlobalContext.getInstance(request).getDefaultLanguage();
		}
		resetCache();
	}

	public void setNeedRefresh(boolean needRefresh) {
		this.needRefresh = needRefresh;
		if (isAjax()) {
			getAjaxData().put("need-refresh", needRefresh);
		}
	}

	/**
	 * @param string
	 *            a logical path
	 */
	public void setPath(String newPath) {
		if (newPath.contains(";jsessionid=")) {
			newPath = newPath.substring(0, newPath.indexOf(";jsessionid="));
		}
		if (!newPath.equals(path)) {
			path = newPath;
			resetCache();
		}
	}

	/**
	 * @deprecated use setRenderMode()
	 * @param b
	 */
	@Deprecated
	public void setPreview(boolean b) {
		if (b) {
			setRenderMode(PREVIEW_MODE);
		} else {
			setRenderMode(VIEW_MODE);
		}
	}

	public void setRenderMode(int renderMode) {
		this.renderMode = renderMode;
	}

	/**
	 * @param request
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setRequestContentLanguage(String lg) {
		if (requestContentLanguage != null && requestContentLanguage.equals(lg)) {
			return;
		}
		if (availableContentLanguages == null) {
			availableContentLanguages = GlobalContext.getInstance(request).getContentLanguages();
		}
		if (availableContentLanguages.contains(lg)) {
			requestContentLanguage = lg;
		} else {
			logger.fine("request content language not available : " + lg);
			requestContentLanguage = getLanguage();
		}
		resetCache();
	}

	/**
	 * @param response
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setSpecialContentRenderer(String specialContent) {
		specialContentRenderer = specialContent;
	}

	public void setURLProtocolPrefix(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @param b
	 */
	public void setVisible(boolean b) {
		visible = b;
	}

	public void storeInRequest(HttpServletRequest request) {
		request.setAttribute(CONTEXT_REQUEST_KEY, this);
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("path=");
		res.append(path);
		res.append(" - ");
		res.append("renderMode=");
		res.append(renderMode);
		res.append(" - ");
		res.append("lg=");
		res.append(language);
		res.append(" - ");
		res.append("request lg=");
		res.append(getRequestContentLanguage());
		res.append(" - ");
		res.append("visible=");
		res.append(visible);
		res.append(" - ");
		res.append("area=");
		res.append(area);
		res.append(" - ");
		res.append("editPreview=");
		res.append(editPreview);
		res.append(" - ");
		res.append("isEditPreview(request)=");
		res.append(isEditPreview(request));
		res.append(" - ");
		res.append("super.toString=");
		res.append(super.toString());
		return res.toString();
	}

	/**
	 * trace info on stream
	 * 
	 * @param out
	 */
	public void toStream(OutputStream outStream) {
		PrintStream out = new PrintStream(outStream);
		out.println();
		out.println(getClass().getCanonicalName());
		out.println("--");
		out.println("uri : " + request.getRequestURI());
		out.println("path : " + getPath());
		out.println("render mode : " + getRenderMode());
		out.println("edit preview : " + isEditPreview());
		out.println("area : " + getArea());
		out.println("edit user : " + getCurrentEditUser());
		out.println("lg : " + getLanguage());
		out.println("visible : " + isVisible());
		out.println("--");
		out.println("");
		out.close();
	}

	public User getCurrentUser() {
		User user = UserFactory.createUserFactory(getGlobalContext(), request.getSession()).getCurrentUser(getGlobalContext(), request.getSession());
		if (user != null) {
			return user;
		} else {
			return getCurrentEditUser();
		}
	}

	public User getCurrentEditUser() {
		return AdminUserFactory.createUserFactory(getGlobalContext(), request.getSession()).getCurrentUser(getGlobalContext(), request.getSession());
	}

	/**
	 * get the current user id. That can be the edit user or the view user.
	 * 
	 * @return
	 */
	public String getCurrentUserId() {
		if (getCurrentUser() != null) {
			return getCurrentUser().getLogin();
		} else if (getCurrentEditUser() != null) {
			return getCurrentEditUser().getLogin();
		} 
		return null;
	}
	
	public String getCurrentAdminUserId() {
		if (getCurrentEditUser() != null) {
			return getCurrentEditUser().getLogin();
		} 
		return null;
	}

	/**
	 * return a identifiant for the user (session id can be use if no user logged)
	 */
	public String getCurrentUserIdNeverNull() {
		String uid = getCurrentUserId();
		if (uid == null) {
			uid = request.getSession().getId();
		}
		return uid;
	}

	/**
	 * get Ajax zone that will be updated.
	 * 
	 * @return a map with html id as key and xhtml as value.
	 */
	public Map<String, String> getAjaxInsideZone() {
		if (ajaxInsideZone == null) {
			ajaxInsideZone = new HashMap<String, String>();
		}
		return ajaxInsideZone;
	}

	/**
	 * get Ajax data map.
	 * 
	 * @return a map with html id as key and xhtml as value.
	 */
	public Map<String, Object> getAjaxData() {
		if (ajaxData == null) {
			ajaxData = new HashMap<String, Object>();
		}
		return ajaxData;
	}

	public Map<String, ScheduledRender> getScheduledAjaxInsideZone() {
		if (scheduledAjaxInsideZone == null) {
			scheduledAjaxInsideZone = new HashMap<String, ScheduledRender>();
		}
		return scheduledAjaxInsideZone;
	}

	/**
	 * add a ajax zone for update.
	 * 
	 * @param id
	 *            a xhtml id
	 * @param xhtml
	 *            the new content of the zone
	 */
	public void addAjaxData(String key, String value) {
		if (ajaxData == null) {
			ajaxData = new HashMap<String, Object>();
		}
		ajaxData.put(key, value);
	}

	/**
	 * add a ajax zone for update.
	 * 
	 * @param id
	 *            a xhtml id
	 * @param xhtml
	 *            the new content of the zone
	 */
	public void addAjaxInsideZone(String id, String xhtml) {
		if (ajaxInsideZone == null) {
			ajaxInsideZone = new HashMap<String, String>();
		}
		ajaxInsideZone.put(id, xhtml);
	}

	/**
	 * Schedule an ajax zone for update. The uri will be called after the prepare()
	 * of the current module actions.
	 * 
	 * @param id
	 *            a xhtml id
	 * @param xhtml
	 *            the new content of the zone
	 */
	public void scheduleAjaxInsideZone(String id, String uri, Map<String, Object> attributes) {
		if (scheduledAjaxInsideZone == null) {
			scheduledAjaxInsideZone = new HashMap<String, ScheduledRender>();
		}
		scheduledAjaxInsideZone.put(id, new ScheduledRender(uri, attributes));
	}

	/**
	 * get Ajax zone that will be updated.
	 * 
	 * @return a map with html id as key and xhtml as value.
	 */
	public Map<String, String> getAjaxZone() {
		if (ajaxZone == null) {
			ajaxZone = new HashMap<String, String>();
		}
		return ajaxZone;
	}

	/**
	 * add a ajax zone for update.
	 * 
	 * @param id
	 *            a xhtml id
	 * @param xhtml
	 *            the new content of the zone
	 */
	public void addAjaxZone(String id, String xhtml) {
		if (ajaxZone == null) {
			ajaxZone = new HashMap<String, String>();
		}
		ajaxZone.put(id, xhtml);
	}

	public void setAjaxMap(Map<? extends Object, ? extends Object> ajaxMap) {
		this.ajaxMap = ajaxMap;
	}

	public Map<? extends Object, ? extends Object> getAjaxMap() {
		return ajaxMap;
	}

	public List<Template> getCurrentTemplates() throws Exception {
		String KEY = "__current_templates___";
		List<Template> outTemplates = (List<Template>) request.getAttribute(KEY);
		if (outTemplates == null) {
			outTemplates = new LinkedList<Template>();
			GlobalContext globalContext = GlobalContext.getInstance(request);
			Collection<String> templatesNames = globalContext.getTemplatesNames();
			Collection<Template> templates = TemplateFactory.getAllTemplates(request.getSession().getServletContext());
			for (Template template : templates) {
				if (templatesNames.contains(template.getName())) {
					outTemplates.add(template);
				}
			}
			request.setAttribute(KEY, outTemplates);
		}
		return outTemplates;
	}

	/**
	 * check if mode is assimilable to view mode.
	 * 
	 * @return true if mode use "view" data. (page, time...)
	 */
	public boolean isLikeViewRenderMode() {
		return getRenderMode() == VIEW_MODE || getRenderMode() == PAGE_MODE || getRenderMode() == TIME_MODE || isPreviewOnly();
	}

	/**
	 * check if mode is assimilable to edit mode.
	 * 
	 * @return true if mode use "edit" data. (preview)
	 */
	public boolean isLikeEditRenderMode() {
		return getRenderMode() == EDIT_MODE || getRenderMode() == PREVIEW_MODE;
	}

	/**
	 * get the page format.
	 * 
	 * @return sample : pdf, html, png...
	 */
	public String getFormat() {
		if (format == null) {
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			if (isLikeViewRenderMode() && globalContext.getURLFactory(this) != null) {
				format = globalContext.getURLFactory(this).getFormat(this, request.getRequestURI());
			} else {
				format = StringHelper.getFileExtension(request.getRequestURI());
			}
			format = StringHelper.onlyAlphaNumeric(format, true);
		}
		if (format == null || format.trim().length() == 0) {
			return "html";
		} else {
			return format;
		}
	}

	/**
	 * force format
	 * 
	 * @param format
	 *            sample : pdf, html, png...
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * set the current template with id.
	 * 
	 * @param templateId
	 * @return false if template not found else true
	 */
	public boolean setCurrentTemplateId(String templateId) {
		Template template;
		try {
			template = TemplateFactory.getDiskTemplates(request.getSession().getServletContext()).get(templateId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (template == null) {
			return false;
		} else {
			setCurrentTemplate(template);
			return true;
		}
	}

	public void setCurrentTemplate(Template template) {
		this.currentTemplate = template;
		try {
			if (this.currentTemplate != null && getDeviceNames() != null && getDevice() != null) {
				if (!getDeviceNames().contains(getDevice().getCode())) {
					getDevice().setForcedCode(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * return true if link can be linked to gzip
	 * 
	 * @throws Exception
	 */
	public boolean isResourceGZip() throws Exception {

		/*
		 * if (!getGlobalContext().getStaticConfig().isProd()) { return false; }
		 * 
		 * if (!isAsViewMode()) { return false; }
		 * 
		 * boolean outValue = false; if (getCurrentTemplate() != null) { outValue =
		 * getCurrentTemplate().isCompressResources(); } String acceptEncoding =
		 * request.getHeader("Accept-Encoding"); if (acceptEncoding != null) { outValue
		 * = outValue && acceptEncoding.toLowerCase().contains("gzip"); } return
		 * outValue;
		 */
		return false;
	}

	/**
	 * no need template and link to page context.
	 * 
	 * @return
	 */
	public boolean isFree() {
		return free;
	}

	/**
	 * no need template and link to page context.
	 * 
	 * @param tree
	 * @return
	 */
	public void setFree(boolean free) {
		this.free = free;
	}

	public String getHomePageURL() {
		GlobalContext globalContext = GlobalContext.getInstance(getRequest());
		if (globalContext.getHomePage().length() > 0) {
			try {
				return globalContext.getHomePageLink(this);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			MenuElement rootPage;
			try {
				rootPage = ContentService.getInstance(globalContext).getNavigation(this);
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
			return URLHelper.createURL(this, rootPage);
		}
	}

	public static void main(String[] args) {
		String ip = "123.34.54.12 ,34.23.12.34";
		ip = ip.substring(0, ip.indexOf(","));
		System.out.println("ip=" + ip);
	}

	public String getPathPrefix() {
		return getPathPrefix(request);
	}

	public String getPathPrefix(HttpServletRequest request) {
		if (isForceGlobalContext() && !getGlobalContext().isDefinedByHost()) {
			return getGlobalContext().getContextKey();
		}
		return getPathPrefix(getGlobalContext(), request);
	}

	public static String getPathPrefix(GlobalContext globalContext, HttpServletRequest request) {
		if (isHostDefineSite(request)) {
			return "";
		}
		if (request.getAttribute(FORCE_PATH_PREFIX) != null) {
			return request.getAttribute(FORCE_PATH_PREFIX).toString();
		} else {
			return globalContext.getPathPrefix();
		}
	}

	public boolean isHostDefineSite() {
		return isHostDefineSite(request);
	}

	public static boolean isHostDefineSite(HttpServletRequest request) {
		return StringHelper.isTrue(request.getAttribute(HOST_DEFINED_SITE));
	}

	public static void setHostDefineSite(HttpServletRequest request, boolean hostDefineSite) {
		if (hostDefineSite) {
			request.setAttribute(HOST_DEFINED_SITE, "true");
		} else {
			request.removeAttribute(HOST_DEFINED_SITE);
		}
	}

	public void release() {
		request.removeAttribute(CONTEXT_REQUEST_KEY);
	}

	public boolean isRefreshParent() {
		if (StringHelper.isTrue(request.getParameter("closeFrame"))) {
			return true;
		} else {
			return refreshParent;
		}
	}

	public void setRefreshParent(boolean refreshParent) {
		this.refreshParent = refreshParent;
	}

	public String getParentURL() {
		return (String)request.getAttribute("parentURL");
	}

	public void setParentURL(String parentURL) {
		request.setAttribute("parentURL", parentURL);
	}

	public boolean isClosePopup() {
		if (closePopup == null) {
			return false;
		} else {
			return closePopup;
		}
	}

	public void setClosePopup(boolean closePopup) {
		if (this.closePopup == null) {
			this.closePopup = closePopup;
		}
	}

	public boolean isEdition() {
		if (isAsViewMode()) {
			return false;
		} else {
			return EditContext.getInstance(getGlobalContext(), getRequest().getSession()).isPreviewEditionMode() || isAsEditMode();
		}
	}

	/**
	 * edit from a preview page
	 * 
	 * @return
	 */
	public boolean isEditPreview() {
		if (editPreview == null) {
			return isEditPreview(request);
		} else {
			return editPreview;
		}
	}

	public void setEditPreview(boolean editPreview) {
		this.editPreview = editPreview;
	}

	public static boolean isEditPreview(HttpServletRequest request) {
		RequestService rs = RequestService.getInstance(request);
		return StringHelper.isTrue(rs.getParameter(PREVIEW_EDIT_PARAM, null));
	}

	public static void setForcePathPrefix(ServletRequest request, String forcePathPrefix) {
		request.setAttribute(FORCE_PATH_PREFIX, forcePathPrefix);
	}

	public void setForcePathPrefix(String forcePathPrefix) {
		getRequest().setAttribute(FORCE_PATH_PREFIX, forcePathPrefix);
		URLHelper.resetPathPrefix(this);
	}

	/**
	 * render component from export servelt (for expose only one component).
	 * 
	 * @return
	 */
	public boolean isExport() {
		return export;
	}

	public void setExport(boolean export) {
		this.export = export;
	}

	public GlobalContext getGlobalContext() {
		if (forceGlobalContext == null) {
			return GlobalContext.getInstance(request);
		} else {
			return forceGlobalContext;
		}
	}

	public void setForceGlobalContext(GlobalContext forceGlobalContext) {
		this.forceGlobalContext = forceGlobalContext;
		// ElementaryURLHelper.resetPathPrefix(this);
	}

	public boolean isForceGlobalContext() {
		return forceGlobalContext != null;
	}

	public boolean isContentFound() {
		return contentFound;
	}

	public void setContentFound(boolean contentFound) {
		this.contentFound = contentFound;
	}

	/**
	 * set content context if Object implement interface INeedContentContext.
	 * 
	 * @param object
	 *            a object, if object don't implement INeedContentContext interface
	 *            this method do nothing.
	 * @return true if object implement INeedContentContext, false otherwise.
	 */
	public boolean setContentContextIfNeeded(Object object) {
		if (object instanceof INeedContentContext) {
			((INeedContentContext) object).setContentContext(this);
			return true;
		} else {
			return false;
		}
	}

	public void setVirtualCurrentPage(MenuElement virtualCurrentPage) {
		this.currentTemplate = null;
		this.virtualCurrentPage = new WeakReference(virtualCurrentPage);
	}
	
	public MenuElement getVirtualCurrentPage() {
		if (virtualCurrentPage != null) {
			return virtualCurrentPage.get();
		} else {
			return null;
		}
	}

	/**
	 * create a area to render content with other rules than area of content.
	 * 
	 * @return
	 */
	public String getVirtualArea() {
		if (virtualArea != null) {
			return virtualArea;
		} else {
			return getArea();
		}
	}

	public void setVirtualArea(String virtualArea) {
		this.virtualArea = virtualArea;
	}

	public boolean isPageAssociation() {
		return pageAssociation;
	}

	public void setPageAssociation(boolean pageAssociation) {
		this.pageAssociation = pageAssociation;
	}

	/**
	 * for return a specific ajax respones from dedicaded action.
	 * 
	 * @return
	 */
	public String getSpecificJson() {
		return specificJson;
	}

	public void setSpecificJson(String specifixAjax) {
		this.specificJson = specifixAjax;
	}

	public int getEditMode() {
		return EDIT_MODE;
	}

	public boolean isCanUndo() throws Exception {
		if (getCurrentPage().getParent() == null) {
			return false;
		} else {
			if (!getGlobalContext().getStaticConfig().isUndo() || getGlobalContext().getFirstLoadVersion() == null || getGlobalContext().isStopUndo()) {
				return false;
			}
			PersistenceService persistenceService = PersistenceService.getInstance(getGlobalContext());
			if (persistenceService.isAskStore()) {
				return true;
			} else {
				return persistenceService.getVersion() > getGlobalContext().getFirstLoadVersion() && (getGlobalContext().getLatestUndoVersion() == null || persistenceService.getVersion() - 1 != getGlobalContext().getLatestUndoVersion());
			}
		}
	}

	public boolean isComponentCache() {
		return componentCache;
	}

	public void setComponentCache(boolean componentCache) {
		this.componentCache = componentCache;
	}

	/**
	 * don't use proxy for rendering (URL don't use getProxyPrefix on globalContext)
	 * 
	 * @return
	 */
	public boolean isInternalURL() {
		return internalURL;
	}

	public void setInternalURL(boolean internalURL) {
		this.internalURL = internalURL;
	}

	public boolean isCheckContentArea() {
		return checkContentArea;
	}

	public void setCheckContentArea(boolean checkContentArea) {
		this.checkContentArea = checkContentArea;
	}

	public boolean isPostRequest() {
		return postRequest;
	}

	public void setPostRequest(boolean postRequest) {
		this.postRequest = postRequest;
	}

	public int getTitleDepth() {
		return titleDepth;
	}

	public void setTitleDepth(int titleDepth) {
		this.titleDepth = titleDepth;
	}

	public boolean isClearSession() {
		return clearSession;
	}

	public void setClearSession(boolean clearSession) {
		this.clearSession = clearSession;
	}

	public String getRemoteIp() {
		return request.getRemoteAddr();
	}

	private static final String[] HEADERS_TO_TRY = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" };

	public String getRealRemoteIp() {
		return getRealRemoteIp(request);
	}

	public static String getRealRemoteIp(HttpServletRequest request) {
		for (String header : HEADERS_TO_TRY) {
			String ip = request.getHeader(header);
			if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
				if (ip.contains(",")) {
					ip = ip.substring(0, ip.indexOf(","));
				}
				ip = ip.trim();
				if (!StringHelper.isEmpty(ip)) {
					return ip.trim();
				}
			}
		}
		return request.getRemoteAddr();
	}

	public String getRealRemoteIp(boolean anonymised) {
		String ip = getRealRemoteIp();
		if (anonymised) {
			ip = StringSecurityUtil.anonymisedIp(ip);
		}
		return ip;
	}

	public static String getRealRemoteIp(HttpServletRequest request, boolean anonymised) {
		String ip = getRealRemoteIp(request);
		if (anonymised) {
			ip = StringSecurityUtil.anonymisedIp(ip);
		}
		return ip;
	}

	public boolean isForceCorrectPath() {
		return forceCorrectPath;
	}

	public void setForceCorrectPath(boolean forceCorrectPath) {
		this.forceCorrectPath = forceCorrectPath;
	}

	public ContentContext getNewContentContext() {
		return new ContentContext(this);
	}

	public void setContentContent(ContentContext ctx) {
		ctx.storeInRequest(request);
	}

	public ContentContextBean getBean() {
		try {
			return new ContentContextBean(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ContentContext getMasterContentContext() throws IOException {
		ContentContext masterContentContext = new ContentContext(this);
		masterContentContext.setForceGlobalContext(getGlobalContext().getMasterContext(this));
		return masterContentContext;
	}

	/**
	 * current user (edit or view) can manager the current site.
	 * 
	 * @return
	 */
	public boolean isUserWebSiteManager() {
		if (AdminUserSecurity.getInstance().isAdmin(getCurrentEditUser())) {
			return true;
		} else {
			User user = getCurrentUser();
			if (user != null) {
				if (user.getRoles().contains("admin") || user.getRoles().contains("manager")) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isContentStatic() {
		if (contentStatic == null) {
			contentStatic = StringHelper.isTrue(request.getHeader(Html2Directory.STATIC_HEADER_AND_PARAM)) || StringHelper.isTrue(request.getParameter(Html2Directory.STATIC_HEADER_AND_PARAM));
		}
		return contentStatic;
	}

	public boolean isStopRendering() {
		return stopRendering;
	}

	public void setStopRendering(boolean stopRendering) {
		this.stopRendering = stopRendering;
	}

	public boolean isOnlyArea() {
		if (onlyArea == null) {
			onlyArea = request.getParameter("only-area") != null;
		}
		return onlyArea;
	}

	public boolean isTakeScreenShort() {
		return StringHelper.isTrue(request.getParameter(TAKE_SCREENSHOT));
	}

	public int getColumnableSize(int depth) {	
		return columnableSizes.get(depth);
	}

	public void setColumnableSize(int columnableSize, int depth) {
		if (columnableSize < AbstractVisualComponent.MAX_COL_VALUE) {
			this.columnableSizes.put(depth, columnableSize);
		}
	}

	public Object getAttribute(String key) {
		if (attributes == null) {
			return null;
		} else {
			return attributes.get(key);
		}
	}

	public void setAttribute(String key, Object obj) {
		if (attributes == null) {
			attributes = new HashMap<String, Object>();
		}
		attributes.put(key, obj);
	}

	/**
	 * return the language of the inititial request (if language has changed)
	 * @return null if the page displayed is the language asked by the visitor, the language of the visitor otherwise.
	 */
	public String getMainLanguage() {
		return mainLanguage;
	}

	public void setMainLanguage(String mainLanguage) {
		this.mainLanguage = mainLanguage;
	}
	
	public boolean mirrorDone(String id) {
		if (mirrorId.contains(id)) {
			return true;
		} else {
			mirrorId.add(id);
			return false;
		}
	}

	public boolean isActionServlet() {
		return actionServlet;
	}

	public void setActionServlet(boolean actionServlet) {
		this.actionServlet = actionServlet;
	}

	public int getColumnableDepth() {
		return columnableDepth;
	}

	public void setColumnableDepth(int columnableDepth) {
		this.columnableDepth = columnableDepth;
	}

	public boolean isNoCache() {
		return noCache;
	}

	public void setNoCache(boolean noCache) {
		this.noCache = noCache;
	}
	
	public String getLocalCountry() {
		return getGlobalContext().getLocaleCountry();
	}

	public boolean isProd() {
		if (prod == null) {
			return getGlobalContext().getStaticConfig().isProd();
		} else {
			return prod;
		}
	}

	public void setProd(Boolean prod) {
		this.prod = prod;
	}

	public String getForcePageTitle() {
		return forcePageTitle;
	}

	public void setForcePageTitle(String forcePageTitle) {
		this.forcePageTitle = forcePageTitle;
	}

}
