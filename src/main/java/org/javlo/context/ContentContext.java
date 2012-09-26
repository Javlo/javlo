/*
 * Created on 20 ao?t 2003
 */
package org.javlo.context;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.helper.AjaxHelper.ScheduledRender;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

/**
 * @author pvanderm
 */
public class ContentContext {

	public static final int EDIT_MODE = 1;

	public static final int ADMIN_MODE = 5;

	public static final int VIEW_MODE = 2;

	public static final int PREVIEW_MODE = 3;

	public static final int PAGE_MODE = 4;

	public static final int MAILING_MODE = 6;

	public static final int TIME_MODE = 7;

	public static final String FORWARD_PATH_REQUEST_KEY = "forward-path";

	public static final String FORCE_MODE_PARAMETER_NAME = "_render-mode";

	public static final String FORCE_ABSOLUTE_URL = "_absolute-url";

	public static String CHARACTER_ENCODING = "UTF-8";

	public static Charset CHARSET_DEFAULT = Charset.forName(CHARACTER_ENCODING);

	public static String PRODUCT_NAME = "WCMS";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ContentContext.class.getName());

	public static String CONTEXT_REQUEST_KEY = "contentContext";

	private static ContentContext createContentContext(HttpServletRequest request, HttpServletResponse response, boolean free) {
		ContentContext ctx = new ContentContext();
		ctx.setFree(free);
		init(ctx, request, response);
		return ctx;
	}

	/**
	 * return free content ctx, context not linked to a specific page, use in imagetransform servlet or something like that.
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static ContentContext getFreeContentContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext freeCtx = createContentContext(request, response, true);
		return freeCtx;
	}

	public static ContentContext getContentContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = (ContentContext) request.getAttribute(CONTEXT_REQUEST_KEY);
		try {
			if (ctx == null) {
				ctx = createContentContext(request, response, false);
				ctx.setFree(false);
				if (ctx.getRenderMode() != ContentContext.EDIT_MODE && ctx.getRenderMode() != ContentContext.ADMIN_MODE) {
					ContentService content = ContentService.getInstance(GlobalContext.getInstance(request));
					if (!content.contentExistForContext(ctx)) {
						boolean editPreview = false;
						if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
							GlobalContext globalContext = GlobalContext.getInstance(request);
							EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
							editPreview = editCtx.isEditPreview();
						}
						if (!editPreview) {
							MenuElement menu = content.getNavigation(ctx);
							if (menu != null) {
								menu = menu.searchChild(ctx);
								if ((menu != null) && (menu.getChildMenuElements().length > 0)) {
									// TODO: clean this system with a recursive system
									ctx.setPath(menu.getChildMenuElements()[0].getPath());
									if (!content.contentExistForContext(ctx)) {
										if ((menu != null) && (menu.getChildMenuElements()[0].getChildMenuElements().length > 0)) {
											ctx.setPath(menu.getChildMenuElements()[0].getChildMenuElements()[0].getPath());
										}
									}
								}
							}
						}
					}
				}
				ctx.setUser();
			} else {
				ctx.setRequest(request);
				ctx.setResponse(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return ctx;
	}

	public void setUser() {
		/** set user **/
		GlobalContext globalContext = GlobalContext.getInstance(getRequest());
		IUserFactory fact = UserFactory.createUserFactory(globalContext, getRequest().getSession());
		currentUser = fact.getCurrentUser(request.getSession());
		fact = AdminUserFactory.createUserFactory(globalContext, getRequest().getSession());
		setCurrentEditUser(fact.getCurrentUser(request.getSession()));
		storeInRequest(request);
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
				if (ContentManager.isAdmin(request)) {
					ctx.renderMode = ADMIN_MODE;
				}
				if (ContentManager.isMailing(request)) {
					ctx.renderMode = MAILING_MODE;
				}
				if (ContentManager.isTime(request)) {
					ctx.renderMode = TIME_MODE;
				}
			}

			ctx.setRequest(request);
			ctx.setResponse(response);
			ctx.setPath(ContentManager.getPath(request));
			String lg = ContentManager.getLanguage(ctx);
			ctx.setLanguage(lg);
			String contentLg = ContentManager.getContentLanguage(ctx);

			if (ctx.getDevice() == null) {
				ctx.setDevice(Device.getDevice(request));
			}

			// TODO : optimise this with option in global context

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			if (contentLg == null) {
				contentLg = lg;
				ctx.setContentLanguage(contentLg);
			} else {
				if (ctx.renderMode != EDIT_MODE) {
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

			StaticConfig config = StaticConfig.getInstance(request.getSession());
			ctx.viewPrefix = config.isViewPrefix();

			ctx.urlFactory = globalContext.getURLFactory(ctx);
			ctx.dmzServerInter = globalContext.getDMZServerInter();
			if (request.getParameter(FORCE_ABSOLUTE_URL) != null) {
				ctx.setAbsoluteURL(StringHelper.isTrue(request.getParameter(FORCE_ABSOLUTE_URL)));
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

	}

	private String path = "/";

	private String language = null;

	private String contentLanguage = null;

	private String requestContentLanguage = null;

	boolean mailing = false;

	boolean array = false;

	int renderMode = VIEW_MODE;

	boolean viewPrefix = false;

	boolean pageRequest = false;

	IURLFactory urlFactory = null;
	URL dmzServerInter = null;
	boolean visible = true;

	boolean isAbsoluteURL = false;

	public Boolean contentExistForContext = null;

	/** cache **/

	private MenuElement currentPageCached = null;

	/**
	 * contain a jsp page to be insered in the content place. content is insered if this attribute is null.
	 */
	private String specialContentRenderer = null;

	private boolean ajax = false;

	private boolean needRefresh = false;

	private String hostName = null;

	private int hostPort = -1;

	private HttpServletRequest request;

	// Map requestContents = new HashMap();

	private HttpServletResponse response;;

	private String area = ComponentBean.DEFAULT_AREA;

	private Template currentTemplate = null;

	private Device device = null;

	private String protocol = null;

	private String format = null;

	private boolean free = false;

	private ContentContext() {
	}

	private final Map<String, String> ajaxInsideZone = new HashMap<String, String>();
	private final Map<String, ScheduledRender> scheduledAjaxInsideZone = new HashMap<String, ScheduledRender>();
	private final Map<String, String> ajaxZone = new HashMap<String, String>();;

	private User currentUser = null;
	private User currentEditUser = null;

	private Map<? extends Object, ? extends Object> ajaxMap = null;

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
		currentUser = ctx.currentUser;
		currentEditUser = ctx.currentEditUser;
		free = ctx.free;
	}

	public String getArea() {
		return area;
	}

	/**
	 * @return
	 * @deprecated Prefer use of {@link #getRequestContentLanguage()}.
	 */
	@Deprecated
	public String getContentLanguage() {
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
		ContentContext outContext = new ContentContext(this);
		outContext.setFormat(format);
		return outContext;
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

	/**
	 * get the edit language in edit mode and view language if other mode.
	 * 
	 * @return
	 */
	public String getContextLanguage() {
		if (getRenderMode() == ContentContext.EDIT_MODE || getRenderMode() == ContentContext.ADMIN_MODE) {
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			return globalContext.getEditLanguage();
		} else {
			return getLanguage();
		}
	}

	/**
	 * return a context with language (if exist), it can be change the language (and only this) of the current context). this method use only the default language list.
	 * 
	 * @return null if no content found.
	 * @throws Exception
	 */
	public ContentContext getContextWithContent(MenuElement page) throws Exception {
		if (page.isRealContent(this)) {
			return this;
		} else {
			ContentContext lgCtx = new ContentContext(this);
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			Collection<String> lgs = globalContext.getDefaultLanguages();
			for (String lg : lgs) {
				lgCtx.setLanguage(lg);
				lgCtx.setRequestContentLanguage(lg);
				if (page.isRealContent(lgCtx)) {
					return lgCtx;
				}
			}
		}
		return null;
	}

	public ContentContext getContextOnPage(MenuElement page) throws Exception {
		if (getPath().equals(page.getPath())) {
			return this;
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
		if (getRenderMode() == ContentContext.EDIT_MODE || getRenderMode() == ContentContext.ADMIN_MODE) {
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			return globalContext.getEditLanguage();
		} else {
			return getRequestContentLanguage();
		}
	}

	public String getCookieLanguage() {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			logger.warning("cookies not found.");
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

	public MenuElement getCurrentPage() throws Exception {
		if (getCurrentPageCached() != null) {
			return getCurrentPageCached();
		}
		GlobalContext globalContext = GlobalContext.getInstance(request);
		MenuElement root = ContentService.getInstance(globalContext).getNavigation(this);
		if (getPath().equals("/")) {
			return root;
		} else {
			if (getPath().trim().length() > 0) {
				MenuElement elem = globalContext.getPageIfExist(this, getPath());
				if (elem != null) {
					setCurrentPageCached(elem);
				}
				return elem;
			} else {
				return root;
			}
		}
	};

	public MenuElement getCurrentPageCached() {
		return currentPageCached;
	}

	public Template getCurrentTemplate() throws Exception {

		if (isFree()) {
			return null;
		}

		if (currentTemplate == null) {
			Template template = null;

			if (getRenderMode() == ContentContext.PAGE_MODE) {
				mailing = true;
			}
			String forceTemplate = getRequest().getParameter(Template.FORCE_TEMPLATE_PARAM_NAME);
			GlobalContext globalContext = GlobalContext.getInstance(getRequest());
			if (forceTemplate != null) {
				logger.info("force template : " + forceTemplate);
				template = Template.getApplicationInstance(getRequest().getSession().getServletContext(), this, forceTemplate);
			}
			if (template == null) {
				MenuElement elem = getCurrentPage();
				if (elem != null) {
					template = TemplateFactory.getTemplates(getRequest().getSession().getServletContext()).get(elem.getTemplateId());
					if (template == null || !template.exist()) {

						while (elem.getParent() != null && ((template == null) || (!template.exist()) || (template.getRendererFullName(this) == null))) {
							elem = elem.getParent();
							template = TemplateFactory.getTemplates(getRequest().getSession().getServletContext()).get(elem.getTemplateId());
						}
					}
				}
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

			currentTemplate = template;
		}

		return currentTemplate;
	}

	public Device getDevice() {
		return device;
	}

	public URL getDMZServerInter() {
		return dmzServerInter;
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

	public String getRequestContentLanguage() {
		if (requestContentLanguage == null) {
			return getContentLanguage();
		} else {
			try {
				if (getCurrentTemplate() != null) {
					if (getCurrentTemplate() != null && getCurrentTemplate().isNavigationArea(getArea())) {
						return getLanguage();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return requestContentLanguage;
		}
	}

	/**
	 * @return
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	public String getSpecialContentRenderer() {
		return specialContentRenderer;
	}

	public IURLFactory getURLFactory() {
		return urlFactory;
	}

	public String getURLProtocolPrefix() {
		if (protocol != null) {
			return protocol;
		} else {
			String requestProtocol = request.getProtocol().toLowerCase();
			if (requestProtocol.startsWith("http")) {
				protocol = "http";
			} else if (requestProtocol.startsWith("https")) {
				protocol = "https";
			} else if (requestProtocol.startsWith("ftp")) {
				protocol = "ftp";
			} else {
				logger.warning("protocol not identified : " + requestProtocol);
				protocol = "http"; // http dy default
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

	public boolean isAsEditMode() {
		return getRenderMode() == EDIT_MODE;
	}

	public boolean isAsViewMode() {
		return getRenderMode() == VIEW_MODE || getRenderMode() == TIME_MODE;
	}

	/**
	 * @deprecated user getRenderMode()
	 * @return true if editable context
	 */
	@Deprecated
	public boolean isEdit() {
		return renderMode == EDIT_MODE;
	}

	public boolean isInteractiveMode() {
		return getRenderMode() == PREVIEW_MODE || getRenderMode() == TIME_MODE;
	}

	public boolean isNeedRefresh() {
		return needRefresh;
	}

	/**
	 * @deprecated use getRenderMode()
	 * @return
	 */
	@Deprecated
	public boolean isPreview() {
		return renderMode == PREVIEW_MODE;
	}

	public boolean isViewPrefix() {
		return viewPrefix;
	}

	/**
	 * @return
	 */
	public boolean isVisible() {
		return visible;
	}

	protected void resestCache() {
		setCurrentPageCached(null);
		setCurrentTemplate(null);
		contentExistForContext = null;
	}

	public void setAbsoluteURL(boolean isAbsoluteURL) {
		this.isAbsoluteURL = isAbsoluteURL;
	}

	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}

	/**
	 * return if the current request is for a page access and not a ressource (jsp, img, js...)
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

	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
		resestCache();
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

	public void setCurrentPageCached(MenuElement currentPageCached) {
		this.currentPageCached = currentPageCached;
	}

	/*
	 * public void setCurrentTemplate(Template currentTemplate) { this.currentTemplate = currentTemplate; }
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
		language = lg;
		resestCache();
	}

	public void setNeedRefresh(boolean needRefresh) {
		this.needRefresh = needRefresh;
	}

	/**
	 * @param string
	 *            a logical path
	 */
	public void setPath(String string) {
		path = string;
		resestCache();
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

	public void setRequestContentLanguage(String requestContentLanguage) {
		this.requestContentLanguage = requestContentLanguage;
		resestCache();
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
		res.append("hashCode=");
		res.append(hashCode());
		return res.toString();
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public User getCurrentEditUser() {
		return currentEditUser;
	}

	public void setCurrentEditUser(User currentEditUser) {
		this.currentEditUser = currentEditUser;
	}

	/**
	 * get the current user id. That can be the edit user or the view user.
	 * 
	 * @return
	 */
	public String getCurrentUserId() {
		if (getCurrentEditUser() != null) {
			return getCurrentEditUser().getLogin();
		} else if (getCurrentUser() != null) {
			return getCurrentUser().getLogin();
		}
		return null;
	}

	/**
	 * get Ajax zone that will be updated.
	 * 
	 * @return a map with html id as key and xhtml as value.
	 */
	public Map<String, String> getAjaxInsideZone() {
		return ajaxInsideZone;
	}

	public Map<String, ScheduledRender> getScheduledAjaxInsideZone() {
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
	public void addAjaxInsideZone(String id, String xhtml) {
		ajaxInsideZone.put(id, xhtml);
	}

	/**
	 * Schedule an ajax zone for update. The uri will be called after the prepare() of the current module actions.
	 * 
	 * @param id
	 *            a xhtml id
	 * @param xhtml
	 *            the new content of the zone
	 */
	public void scheduleAjaxInsideZone(String id, String uri, Map<String, Object> attributes) {
		scheduledAjaxInsideZone.put(id, new ScheduledRender(uri, attributes));
	}

	/**
	 * get Ajax zone that will be updated.
	 * 
	 * @return a map with html id as key and xhtml as value.
	 */
	public Map<String, String> getAjaxZone() {
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
		ajaxZone.put(id, xhtml);
	}

	public void setAjaxMap(Map<? extends Object, ? extends Object> ajaxMap) {
		this.ajaxMap = ajaxMap;
	}

	public Map<? extends Object, ? extends Object> getAjaxMap() {
		return ajaxMap;
	}

	public List<Template> getCurrentTemplates() throws IOException {
		String KEY = "__current_templates___";
		List<Template> outTemplates = (List<Template>) request.getAttribute(KEY);
		if (outTemplates == null) {
			outTemplates = new LinkedList<Template>();
			GlobalContext globalContext = GlobalContext.getInstance(request);
			Collection<String> templatesNames = globalContext.getTemplatesNames();
			Collection<Template> templates = TemplateFactory.getAllDiskTemplates(request.getSession().getServletContext());
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
		return getRenderMode() == VIEW_MODE || getRenderMode() == PAGE_MODE || getRenderMode() == TIME_MODE;
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
				format = globalContext.getURLFactory(this).getFormat(request.getRequestURI());
			} else {
				format = StringHelper.getFileExtension(request.getRequestURI());
			}
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

	public void setCurrentTemplate(Template template) {
		this.currentTemplate = template;
	}

	/**
	 * return true if link can be linked to gzip
	 * 
	 * @throws Exception
	 */
	public boolean isResourceGZip() throws Exception {

		if (!isAsViewMode()) {
			return false;
		}

		boolean outValue = false;
		if (getCurrentTemplate() != null) {
			outValue = getCurrentTemplate().isCompressResources();
		}
		String acceptEncoding = request.getHeader("Accept-Encoding");
		if (acceptEncoding != null) {
			outValue = outValue && acceptEncoding.toLowerCase().contains("gzip");
		}
		return outValue;
	}

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public String getHomePageURL() {
		GlobalContext globalContext = GlobalContext.getInstance(getRequest());
		if (globalContext.getHomePage().length() > 0) {
			return globalContext.getHomePage();
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
}
