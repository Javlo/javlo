package org.javlo.data;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.ISubTitle;
import org.javlo.component.core.SubTitleBean;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.image.ImageBean;
import org.javlo.component.image.ImageTitleBean;
import org.javlo.component.links.RSSRegistration;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.macro.core.IMacro;
import org.javlo.macro.core.MacroFactory;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.admin.MacroBean;
import org.javlo.module.content.Edit;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.ticket.TicketAction;
import org.javlo.module.ticket.TicketUserWrapper;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.NavigationMapByName;
import org.javlo.navigation.PageBean;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.visitors.CookiesService;
import org.javlo.service.visitors.VisitorsMessageService;
import org.javlo.servlet.AccessServlet;
import org.javlo.template.Template;
import org.javlo.template.Template.TemplateBean;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.utils.HtmlPart;

public class InfoBean {

	private static final String ts = "" + System.currentTimeMillis();

	public static final String REQUEST_KEY = "info";

	public static final String NEW_SESSION_PARAM = "__new_session";

	private String fakeCurrentURL = null;

	private ImageBean imageHeader = null;

	private Map<String, Boolean> areas = null;
	
	private Map<String,String> bgAreas = null;

	private static final Map<String, String> staticData = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put("compId", IContentVisualComponent.COMP_ID_REQUEST_PARAM);
			put("forceTemplateParamName", Template.FORCE_TEMPLATE_PARAM_NAME);
			put("forceDeviceParameterName", Device.FORCE_DEVICE_PARAMETER_NAME);
		}
	});

	public static InfoBean getCurrentInfoBean(HttpServletRequest request) {
		return (InfoBean) request.getAttribute(REQUEST_KEY);
	}

	public static InfoBean getCurrentInfoBean(ContentContext ctx) throws Exception {
		InfoBean ib = getCurrentInfoBean(ctx.getRequest());
		if (ib == null) {
			ib = updateInfoBean(ctx);
		}
		return ib;
	}

	/**
	 * create info bean but don't put in request.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	public static InfoBean createInfoBean(ContentContext ctx) throws Exception {
		InfoBean info = new InfoBean();
		info.ctx = ctx;
		info.globalContext = GlobalContext.getInstance(ctx.getRequest());
		return info;
	}

	/**
	 * create info bean in request (key=info) for jstp call in template.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	public static InfoBean updateInfoBean(ContentContext ctx) throws Exception {
		InfoBean info = createInfoBean(ctx);
		ctx.getRequest().setAttribute(REQUEST_KEY, info);
		return info;
	}

	private ContentContext ctx;
	private GlobalContext globalContext;
	private boolean tools = true;
	private Map<IUserInfo, String> avatarFakeMap;

	public String getCmsName() {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getCmsName();
	}

	public ContentContext getContentContext() {
		return ctx;
	}

	public String getCurrentAbsoluteURL() {
		return URLHelper.createURL(ctx.getContextForAbsoluteURL());
	}

	public String getCurrentAbsoluteURLZIP() {
		ContentContext ZIPCtx = ctx.getContextForAbsoluteURL();
		ZIPCtx.setURLFactory(null);
		ZIPCtx.setRenderMode(ContentContext.VIEW_MODE);
		ZIPCtx.setFormat("zip");
		return URLHelper.createURL(ZIPCtx);
	}

	public String getCurrentAbsoluteURLXML() {
		ContentContext XMLCtx = ctx.getContextForAbsoluteURL();
		XMLCtx.setURLFactory(null);
		XMLCtx.setRenderMode(ContentContext.VIEW_MODE);
		XMLCtx.setFormat("xml");
		return URLHelper.createURL(XMLCtx);
	}

	public String getCurrentAbsolutePreviewURL() {
		ContentContext previewCtx = ctx.getContextForAbsoluteURL();
		previewCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		return URLHelper.createURL(previewCtx);
	}

	public String getCurrentURL() {
		if (fakeCurrentURL != null) {
			return fakeCurrentURL;
		} else {
			return URLHelper.createURL(ctx);
		}
	}

	public String getCurrentURLWidthDevice() {
		if (fakeCurrentURL != null) {
			return fakeCurrentURL;
		} else {
			String url = URLHelper.createURL(ctx);
			if (!url.contains(Device.FORCE_DEVICE_PARAMETER_NAME)) {
				url = URLHelper.addParam(url, Device.FORCE_DEVICE_PARAMETER_NAME, "" + ctx.getDevice());
			}
			return url;
		}
	}

	public String getCurrentAjaxURLWidthDevice() {
		if (fakeCurrentURL != null) {
			return fakeCurrentURL;
		} else {
			String url = URLHelper.createAjaxURL(ctx);
			url = URLHelper.addParam(url, Device.FORCE_DEVICE_PARAMETER_NAME, "" + ctx.getDevice());
			return url;
		}
	}

	public String getCurrentCanonicalURL() {
		ContentContext robotCtx = new ContentContext(ctx);
		robotCtx.setDevice(Device.getFakeDevice("robot"));
		robotCtx.setAbsoluteURL(true);
		return URLHelper.createURL(robotCtx);
	}

	public Map<String, String> getLanguageURLs() {
		Map<String, String> urls = new HashMap<String, String>();
		for (String lg : ctx.getGlobalContext().getContentLanguages()) {
			ContentContext lgCtx = new ContentContext(ctx);
			lgCtx.setAllLanguage(lg);
			urls.put(lg, URLHelper.createURL(lgCtx));
		}
		return urls;
	}

	public String getCurrentAbsoluteURLQRCode() {
		return URLHelper.createQRCodeLink(ctx, getShortLanguageURL());
	}

	public String getCurrentModuleURL() {
		ModulesContext modulesCtx;
		try {
			modulesCtx = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext);
			return URLHelper.mergePath(URLHelper.createStaticURL(ctx, "/"), modulesCtx.getCurrentModule().getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getCurrentPDFURL() {
		ContentContext pdfCtx = ctx.getFreeContentContext();
		pdfCtx.setFormat("pdf");
		return URLHelper.createURL(pdfCtx);
	}

	public String getUploadURL() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("webaction", "data.upload");
		return URLHelper.createAjaxURL(ctx, params);
	}

	public String getUploadSharedURL() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("webaction", "data.uploadShared");
		return URLHelper.createAjaxURL(ctx, params);
	}

	public String getCurrentViewURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getFreeContentContext());
	}

	public String getCurrentViewURLWidthDevice() {
		String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getFreeContentContext());
		url = URLHelper.addParam(url, Device.FORCE_DEVICE_PARAMETER_NAME, "" + ctx.getDevice());
		return url;
	}

	public String getCurrentEditURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE).getFreeContentContext());
	}

	public String getCurrentPreviewURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE).getFreeContentContext());
	}

	public String getCurrentPageICalURL() throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getEvent(ctx) == null) {
			return null;
		} else {
			ContentContext icalCtx = new ContentContext(ctx);
			icalCtx.setFormat("ical");
			return URLHelper.createURL(icalCtx);
		}
	}

	public String getCurrentPageURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE).getFreeContentContext());
	}

	public String getCurrentAjaxURL() {
		String path = ctx.getPath();
		try {
			if (ctx.getCurrentPage() != null) {
				path = ctx.getCurrentPage().getPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return URLHelper.createAjaxURL(ctx, path);
	}

	public String getPDFURL() {
		ContentContext pdfCtx = new ContentContext(ctx);
		pdfCtx.setFormat("pdf");
		if (pdfCtx.getRenderMode() == ContentContext.PAGE_MODE) {
			pdfCtx.setRenderMode(ContentContext.VIEW_MODE);
		}
		return URLHelper.createURL(pdfCtx);
	}

	public MenuElement getCurrentPage() {
		try {
			return ctx.getCurrentPage();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public String getDate() {
		try {
			return StringHelper.renderDate(new Locale(ctx.getRequestContentLanguage()), getCurrentPage().getContentDateNeverNull(ctx), globalContext.getShortDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getMediumDate() {
		try {
			return StringHelper.renderDate(new Locale(ctx.getRequestContentLanguage()), getCurrentPage().getContentDateNeverNull(ctx), globalContext.getMediumDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getFullDate() {
		try {
			return StringHelper.renderDate(new Locale(ctx.getRequestContentLanguage()), getCurrentPage().getContentDateNeverNull(ctx), globalContext.getFullDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNowDate() {
		try {
			return StringHelper.renderDate(new Locale(ctx.getRequestContentLanguage()), new Date(), globalContext.getShortDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNowMediumDate() {
		try {
			return StringHelper.renderDate(new Locale(ctx.getRequestContentLanguage()), new Date(), globalContext.getMediumDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNowFullDate() {
		try {
			return StringHelper.renderDate(new Locale(ctx.getRequestContentLanguage()), new Date(), globalContext.getFullDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Date getNow() {
		return new Date();
	}

	public String getNowSortable() {
		return StringHelper.renderSortableDate(new Date());
	}

	public String getCurrentDate() {
		try {
			return StringHelper.renderDate(new Date(), globalContext.getShortDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getCurrentDateRFC3339() {
		try {
			return StringHelper.renderDate(new Date(), "yyyy-MM-dd");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getCurrentTimeRFC3339() {
		try {
			return StringHelper.renderDate(new Date(), "yyyy-MM-dd'T'HH:mm:ssXXX");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getSortableDate() {
		try {
			return StringHelper.renderSortableDate(getCurrentPage().getContentDateNeverNull(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Device getDevice() {
		return ctx.getDevice();
	}

	public String getEditLanguage() {
		return globalContext.getEditLanguage(ctx.getRequest().getSession());
	}

	public String getEncoding() {
		return ContentContext.CHARACTER_ENCODING;
	}

	public GenericMessage getGlobalMessage() {
		return MessageRepository.getInstance(ctx).getGlobalMessage();
	}

	public String getGlobalTitle() {
		try {
			return getCurrentPage().getGlobalTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getHomeAbsoluteURL() {
		return URLHelper.createURL(ctx.getContextForAbsoluteURL(), "/");
	}

	public String getContentLanguage() {
		return ctx.getContentLanguage();
	}

	public String getRequestContentLanguage() {
		return ctx.getRequestContentLanguage();
	}
	
	public String getRequestContentLanguageName() {
		Locale locale;
		if (ctx.isAsModifyMode()) {
			locale = new Locale(ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession()));
		} else {
			locale = new Locale(ctx.getLanguage());
		}
		Locale lg = new Locale(ctx.getRequestContentLanguage());
		return lg.getDisplayName(locale);
	}

	public String getLanguage() {
		return ctx.getLanguage();
	}

	public HtmlPart getPageDescription() {
		try {
			final String noRecursiveRequestKey = "_pageDescritionCalled";
			if (ctx.getRequest().getAttribute(noRecursiveRequestKey) == null) {
				ctx.getRequest().setAttribute(noRecursiveRequestKey, 1);
				String description = XHTMLHelper.replaceJSTLData(ctx, getCurrentPage().getMetaDescription(ctx));
				ctx.getRequest().setAttribute(noRecursiveRequestKey, null);
				return new HtmlPart(description);
			} else {
				return getCurrentPage().getDescription(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPageID() {
		return getCurrentPage().getId();
	}

	public String getPageMetaDescription() {
		try {
			return getCurrentPage().getMetaDescription(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPageName() {
		return getCurrentPage().getName();
	}

	public String getPageHumanName() {
		return getCurrentPage().getHumanName();
	}

	public String getPageTitle() {
		try {
			return getCurrentPage().getPageTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getTitle() {
		try {
			return getCurrentPage().getTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getTime() {
		try {
			return StringHelper.renderTime(ctx, getCurrentPage().getContentDateNeverNull(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getSortableTime() {
		try {
			return StringHelper.renderSortableTime(new Date());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getUserName() {
		return ctx.getCurrentUserId();
	}

	public String getUserLabel() {
		User user = ctx.getCurrentUser();
		if (user == null) {
			return "";
		} else {
			if (!StringHelper.isAllEmpty(user.getUserInfo().getFirstName(), user.getUserInfo().getLastName())) {
				return (user.getUserInfo().getFirstName() + " " + user.getUserInfo().getLastName()).trim();
			}
		}
		return ctx.getCurrentUserId();
	}

	public PageBean getPage() {
		try {
			return getCurrentPage().getPageBean(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * return the root page if the current page is a children of association
	 * 
	 * @return
	 */
	public PageBean getMainPage() {
		try {
			MenuElement page = getCurrentPage();
			if (page.isDirectChildrenOfAssociation()) {
				return page.getParent().getPageBean(ctx);
			} else {
				return page.getPageBean(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public PageBean getRoot() {
		try {
			return getCurrentPage().getRoot().getPageBean(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * get the list of the pages from current to root
	 * 
	 * @return
	 */
	public List<PageBean> getPagePath() {

		MenuElement page = getCurrentPage();

		List<PageBean> pagePath = new LinkedList<PageBean>();

		while (page.getParent() != null) {
			page = page.getParent();
			try {
				pagePath.add(0, page.getPageBean(ctx));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return pagePath;
	}

	public String getVersion() {
		return AccessServlet.VERSION;
	}

	public Collection<String> getContentLanguages() {
		return globalContext.getContentLanguages();
	}

	public Collection<String> getLanguages() {
		return globalContext.getLanguages();
	}
	
	public Collection<PageBean> getPagesForAnyLanguages() throws Exception {
		Collection<PageBean> pages = new LinkedList<PageBean>();
		for (ContentContext lgCtx : ctx.getContextForAllLanguage()) {
			lgCtx.setAllLanguage(lgCtx.getRequestContentLanguage());
			pages.add(ctx.getCurrentPage().getPageBean(lgCtx));
		}
		return pages;
	}

	public String getEditTemplateURL() {
		return URLHelper.createStaticURL(ctx, ctx.getGlobalContext().getStaticConfig().getEditTemplateFolder());
	}

	public String getEditTemplateFolder() {
		return ctx.getGlobalContext().getStaticConfig().getEditTemplateFolder();
	}

	public String getEditTemplateModeURL() {
		if (globalContext.getEditTemplateMode() != null && globalContext.getEditTemplateMode().trim().length() > 0) {
			String cssLink = URLHelper.mergePath(globalContext.getStaticConfig().getEditTemplateFolder(), "css", "edit_" + globalContext.getEditTemplateMode() + ".css");
			return URLHelper.createStaticURL(ctx, cssLink);
		} else {
			try {
				if (ctx.getCurrentTemplate() != null && !StringHelper.isEmpty(ctx.getCurrentTemplate().getEditTemplateMode())) {
					String cssLink = URLHelper.mergePath(globalContext.getStaticConfig().getEditTemplateFolder(), "css", "edit_" + ctx.getCurrentTemplate().getEditTemplateMode() + ".css");
					return URLHelper.createStaticURL(ctx, cssLink);
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public String getPreviewTemplateModeURL() {
		if (globalContext.getEditTemplateMode() != null && globalContext.getEditTemplateMode().trim().length() > 0) {
			return URLHelper.mergePath(URLHelper.createStaticURL(ctx, "/"), globalContext.getStaticConfig().getEditTemplateFolder(), "/preview/" + globalContext.getEditTemplateMode() + "/css/edit_preview.css");
		} else {
			try {
				if (ctx.getCurrentTemplate() != null && !StringHelper.isEmpty(ctx.getCurrentTemplate().getEditTemplateMode())) {
					return URLHelper.mergePath(URLHelper.createStaticURL(ctx, "/"), globalContext.getStaticConfig().getEditTemplateFolder(), "/preview/" + ctx.getCurrentTemplate().getEditTemplateMode() + "/css/edit_preview.css");
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public String getResourceRootURL() {
		return URLHelper.createResourceURL(ctx, "/");
	}

	public String getStaticRootURL() {
		return URLHelper.createStaticURL(ctx, "/");
	}

	public String getContextKey() {
		return ctx.getGlobalContext().getContextKey();
	}

	public String getContextDownloadURL() {
		return URLHelper.createStaticURL(ctx, "/context");
	}

	public String getCaptchaURL() {
		return URLHelper.createStaticURL(ctx, "/captcha.jpg");
	}

	public String getCurrentUserAvatarUrl() {
		if (ctx.getCurrentUser() != null) {
			return URLHelper.createAvatarUrl(ctx, ctx.getCurrentUser().getUserInfo());
		} else {
			return URLHelper.createStaticURL(ctx, "/images/avatar.png");
		}
	}

	public Map<IUserInfo, String> getAvatarURL() {
		if (avatarFakeMap == null) {
			avatarFakeMap = new HashMap<IUserInfo, String>(0) {
				private static final long serialVersionUID = 1L;

				@Override
				public String get(Object key) {
					return URLHelper.createAvatarUrl(ctx, (IUserInfo) key);
				}
			};
		}
		return avatarFakeMap;
	}

	public String getLogoURL() {
		return URLHelper.createStaticURL(ctx, "/logo.svg");
	}

	public String getBackgroundURL() {
		File bgFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getStaticFolder(), "background.png"));
		if (!bgFile.exists()) {
			bgFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getStaticFolder(), "background.jpg"));
		}
		if (bgFile.exists()) {
			try {
				return URLHelper.createTransformURL(ctx, "/static/" + bgFile.getName(), "background");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public int getPreviewVersion() {
		try {
			return PersistenceService.getInstance(globalContext).getVersion();
		} catch (ServiceException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public Collection<String> getRoles() {
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		return userFactory.getAllRoles(globalContext, ctx.getRequest().getSession());
	}

	public PageBean getParent() {
		if (getCurrentPage().getParent() != null) {
			try {
				return getCurrentPage().getParent().getPageBean(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getCopiedPath() {
		ContentContext copyCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession()).getContextForCopy(ctx);
		if (copyCtx != null) {
			if (!ctx.getPath().startsWith(copyCtx.getPath())) {
				return copyCtx.getPath();
			}
		}
		return null;
	}

	public String getPrivateHelpURL() {
		return globalContext.getPrivateHelpURL();
	}

	public Collection<String> getAdminRoles() {
		return globalContext.getAdminUserRoles();
	}

	public boolean isOpenExternalLinkAsPopup() {
		return globalContext.isOpenExternalLinkAsPopup();
	}

	public String getTemplateFolder() {
		try {
			if (ctx.getCurrentTemplate() != null) {
				String contextKey = globalContext.getContextKey();
				if (ctx.getDMZServerInter() != null && ctx.getRenderMode() == ContentContext.PAGE_MODE) {
					contextKey = ServletHelper.getContextKey(ctx.getDMZServerInter());
				}
				return ctx.getCurrentTemplate().getFolder(contextKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getRootTemplateFolder() {
		try {
			return URLHelper.mergePath(ctx.getCurrentTemplate().getLocalWorkTemplateFolder(), getTemplateFolder());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getRootTemplateURL() {
		try {
			return URLHelper.createStaticURL(ctx, URLHelper.mergePath(ctx.getCurrentTemplate().getLocalWorkTemplateFolder(), getTemplateFolder()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getAbsoluteRootTemplateURL() {
		try {
			String url = URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), URLHelper.mergePath(ctx.getCurrentTemplate().getLocalWorkTemplateFolder(), getTemplateFolder()));
			return url;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getAbsoluteTemplateFolder() {
		try {
			if (ctx.getCurrentTemplate() != null) {
				return URLHelper.createStaticTemplateURL(ctx.getContextForAbsoluteURL(), "/");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getShortURL() {
		try {
			return URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), ctx.getCurrentPage().getShortURL(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getShortLanguageURL() {
		try {
			return URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), ctx.getCurrentPage().getShortLanguageURL(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Collection<String> getTags() {
		return globalContext.getTags();
	}

	public String getTagsRaw() {
		return StringHelper.collectionToString(globalContext.getTags(), ",");
	}

	/**
	 * return the name of the first level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSection() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return "page-not-found";
		}
		if (page == null) {
			return "page-not-found";
		}
		if (page.getParent() == null) {
			return "root";
		} else {
			while (page.getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		return page.getName();
	}

	public int getSectionNumber() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return -1;
		}
		if (page == null) {
			return -1;
		}
		if (page.getParent() == null) {
			return -1;
		} else {
			while (page.getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		return page.getParent().getChildPosition(page);
	}

	/**
	 * return the url of the first level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSectionUrl() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return "page-not-found";
		}
		if (page == null) {
			return "page-not-found";
		}
		if (page.getParent() == null) {
			return "root";
		} else {
			while (page.getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		return URLHelper.createURL(ctx, page);
	}

	/**
	 * return the name of the second level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSubSection() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return "page-not-found";
		}
		if (page == null) {
			return "page-not-found";
		}
		if (page.getParent() == null || page.getParent().getParent() == null) {
			return "";
		} else {
			while (page.getParent().getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		return page.getName();
	}

	/**
	 * return the url of the second level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSubSectionUrl() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return "page-not-found";
		}
		if (page == null) {
			return "page-not-found";
		}
		if (page.getParent() == null || page.getParent().getParent() == null) {
			return "";
		} else {
			while (page.getParent().getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		return URLHelper.createURL(ctx, page);
	}

	/**
	 * return the name of the second level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSubSectionTitle() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return "page-not-found";
		}
		if (page == null) {
			return "page-not-found";
		}
		if (page.getParent() == null || page.getParent().getParent() == null) {
			return "";
		} else {
			while (page.getParent().getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		try {
			return page.getTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * return the name of the second level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSubSectionTechnicalTitle() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return "page-not-found";
		}
		if (page == null) {
			return "page-not-found";
		}
		if (page.getParent() == null || page.getParent().getParent() == null) {
			return "";
		} else {
			while (page.getParent().getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		try {
			return page.getTechnicalTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * return the name of the first level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSectionTitle() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return "page-not-found";
		}
		if (page == null) {
			return "page-not-found";
		}
		if (page.getParent() == null) {
			return "root";
		} else {
			while (page.getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		try {
			return page.getTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * return the name of the first level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSectionTechnicalTitle() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
		} catch (Exception e) {
			return "page-not-found";
		}
		if (page == null) {
			return "page-not-found";
		}
		if (page.getParent() == null) {
			return "root";
		} else {
			while (page.getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		try {
			return page.getTechnicalTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Template getTemplate() {
		try {
			return ctx.getCurrentTemplate();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public TemplateBean getTemplateBean() {
		try {
			return new TemplateBean(ctx, ctx.getCurrentTemplate());
		} catch (Exception e) {
			return null;
		}
	}

	public List<TemplateBean> getTemplates() {
		try {
			List<TemplateBean> templates = new LinkedList<Template.TemplateBean>();
			for (Template template : TemplateFactory.getAllTemplatesFromContext(globalContext)) {
				templates.add(new TemplateBean(ctx, template));
			}
			return templates;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, Template> getTemplatesMap() throws Exception {
		return TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext());
	}

	public String getTemplateName() {
		try {
			return ctx.getCurrentTemplate().getName();
		} catch (Exception e) {
			return null;
		}
	}

	public String getPathPrefix() {
		return URLHelper.getPathPrefix(ctx);
	}

	public boolean isGod() {
		return AdminUserSecurity.getInstance().isGod(ctx.getCurrentEditUser());
	}

	public boolean isAdmin() {
		return AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser());
	}

	public boolean isMaster() {
		return AdminUserSecurity.getInstance().isMaster(ctx.getCurrentEditUser());
	}

	public String getPath() {
		return ctx.getPath();
	}

	public boolean isPreview() {
		return ctx.getRenderMode() == ContentContext.PREVIEW_MODE;
	}

	public boolean isView() {
		return ctx.getRenderMode() == ContentContext.VIEW_MODE;
	}

	public boolean isPageMode() {
		return ctx.getRenderMode() == ContentContext.PAGE_MODE;
	}

	public String getPublishDate() {
		try {
			return StringHelper.renderShortDate(ctx, globalContext.getPublishDate());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isAccountSettings() {
		User user = AdminUserFactory.createUserFactory(ctx.getRequest()).getCurrentUser(ctx.getGlobalContext(), ctx.getRequest().getSession());
		if ((!globalContext.isMaster() && AdminUserSecurity.getInstance().isMaster(user))) {
			return false;
		} else if (AdminUserSecurity.getInstance().isGod(user)) {
			return false;
		} else {
			return true;
		}

	}

	public boolean isNewSession() {
		if (StringHelper.isTrue(ctx.getRequest().getSession().getAttribute(NEW_SESSION_PARAM))) {
			return true;
		} else {
			return ctx.getRequest().getSession().isNew();
		}
	}

	/**
	 * this method return true at the first call for current session and false afer.
	 * 
	 * @return
	 */
	public boolean isFirstCallForSession() {
		String KEY = "firscall_" + InfoBean.class.getCanonicalName();
		if (ctx.getRequest().getSession().getAttribute(KEY) != null) {
			return false;
		} else {
			ctx.getRequest().getSession().setAttribute(KEY, true);
			return true;
		}
	}

	public String getArea() {
		return ctx.getArea();
	}

	public boolean isEditPreview() {
		return ctx.isEditPreview();
	}

	public boolean isPreviewEdit() {
		return ctx.isEditPreview();
	}

	public EditContext getEditContext() {
		return EditContext.getInstance(globalContext, ctx.getRequest().getSession());
	}

	public PageBean getFirstLevelPage() {
		MenuElement page;
		try {
			page = ctx.getCurrentPage();
			if (page.getParent() == null) {
				return page.getPageBean(ctx);
			} else {
				while (page.getParent().getParent() != null) {
					page = page.getParent();
				}
			}
			return page.getPageBean(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public User getEditUser() {
		return ctx.getCurrentEditUser();
	}

	public String getAbsoluteURLPrefix() {
		return URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/");
	}

	public String getAbsoluteLocalURLPrefix() {
		ContentContext localCtx = new ContentContext(ctx);
		localCtx.resetDMZServerInter();
		return URLHelper.createStaticURL(localCtx.getContextForAbsoluteURL(), "/");
	}

	public String getHostURLPrefix() {
		String url = getAbsoluteURLPrefix();
		String noProtocol = url.substring(url.indexOf("//") + 2);
		if (noProtocol.contains("/")) {
			noProtocol = noProtocol.substring(noProtocol.indexOf('/'));
			if (noProtocol.length() > 1) {
				url = url.substring(0, url.indexOf(noProtocol));
			}
		}
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	public String getServerTime() {
		return StringHelper.renderTime(new Date());
	}

	public boolean isTools() {
		return tools;
	}

	public void setTools(boolean actionBar) {
		this.tools = actionBar;
	}

	public boolean isLocalModule() {
		// String localModulePath =
		// ctx.getRequest().getSession().getServletContext().getRealPath("/webstart/localmodule.jnlp.jsp");
		String localModulePath = ResourceHelper.getRealPath(ctx.getRequest().getSession().getServletContext(), "/webstart/localmodule.jnlp.jsp");
		return (new File(localModulePath)).isFile();
	}

	/**
	 * timestamp initialised when java VM is staded.
	 * 
	 * @return
	 */
	public String getTs() {
		return ts;
	}

	public String getQRCodeLinkPrefix() {
		return URLHelper.createQRCodeLink(ctx, (IContentVisualComponent) null);
	}

	public String getBackURL() {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		return requestService.getParameter(ElementaryURLHelper.BACK_PARAM_NAME, null);
	}

	/**
	 * if back page name is setted in as _back_page param this method return the
	 * PageBean.
	 * 
	 * @return
	 * @throws Exception
	 */
	public PageBean getBackPage() throws Exception {
		String page = ctx.getRequest().getParameter("_back_page");
		if (page != null) {
			return ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromName(page).getPageBean(ctx);
		} else {
			return null;
		}
	}

	public String getRootURL() {
		return URLHelper.createURL(ctx, "/");
	}

	public String getRootURLPageMode() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE), "/");
	}

	public String getRootURLViewMode() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), "/");
	}

	public String getRSSAllURL() {
		try {
			return URLHelper.createRSSURL(ctx, "all");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getRootAbsoluteURL() {
		return URLHelper.createURL(ctx.getContextForAbsoluteURL(), "/");
	}

	public String getRootAbsoluteViewURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getContextForAbsoluteURL(), "/");
	}

	public Map<String, String> getStaticData() {
		return staticData;
	}

	public String getPageBookmark() {
		try {
			return NavigationHelper.getPageBookmark(ctx, getCurrentPage());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, PageBean> getPageByName() {
		try {
			return new NavigationMapByName(ctx, ctx.getCurrentPage().getRoot());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getHostPort() {
		return ctx.getHostPort();
	}

	public int getCurrentYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	public int getCurrentDay() {
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	}

	public int getCurrentMonth() {
		return Calendar.getInstance().get(Calendar.MONTH);
	}

	public String[] getMonths() {
		String language = globalContext.getEditLanguage(ctx.getRequest().getSession());
		return DateFormatSymbols.getInstance(new Locale(language)).getMonths();
	}

	public String getAjaxLoaderURL() {
		return URLHelper.createStaticURL(ctx, "/images/ajax_loader.gif");
	}

	public String getViewAjaxLoaderURL() {
		return URLHelper.createStaticURL(ctx, "/images/ajax-loader-circle-white.gif");
	}

	public String getRandomId() {
		return StringHelper.getRandomId();
	}

	/**
	 * return a random number between 0 and 9
	 */
	public long getRandom10() {
		return (long) (Math.random() * 10);
	}

	/**
	 * return a random number between 0 and 99
	 */
	public long getRandom100() {
		return (long) (Math.random() * 100);
	}

	/**
	 * return a random number between 0 and 9999
	 */
	public long getRandom1000() {
		return (long) (Math.random() * 1000);
	}

	public boolean isRSSFeed() {
		final String RSS_SESSION_KEY = "__RSS_SESSION_KEY";
		if (ctx.getRequest().getSession().getAttribute(RSS_SESSION_KEY) == null) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			try {
				ctx.getRequest().getSession().setAttribute(RSS_SESSION_KEY, content.getNavigation(ctx).getAllChildrenWithComponentType(ctx, RSSRegistration.TYPE).size() > 0);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return (Boolean) ctx.getRequest().getSession().getAttribute(RSS_SESSION_KEY);
	}

	public Map<String, Boolean> getAreaEmpty() {
		Map<String, Boolean> emptyArea = new HashMap<String, Boolean>();
		try {
			for (String area : ctx.getCurrentTemplate().getAreas()) {
				if (ctx.getCurrentPage().isEmpty(ctx, area, true)) {
					emptyArea.put(area, new Boolean(true));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return emptyArea;
	}

	public Map<String, Boolean> getQuietArea() {
		Map<String, Boolean> quietArea = new HashMap<String, Boolean>();
		try {
			for (String area : ctx.getGlobalContext().getQuietArea()) {
				quietArea.put(area, new Boolean(true));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return quietArea;
	}

	public Map<String, Boolean> getAreas() throws Exception {
		if (areas != null) {
			return areas;
		} else {
			areas = new HashMap<String, Boolean>();
			List<String> quietAreas = ctx.getGlobalContext().getQuietArea();
			for (String area : ctx.getCurrentTemplate().getAreas()) {
				if (!quietAreas.contains(area)) {
					areas.put(area, true);
				} else {
					areas.put(area, false);
				}
			}
		}
		return areas;
	}

	/**
	 * get the current total depth of navigation.
	 * 
	 * @return
	 * @throws Exception
	 */
	public int getCurrentDepth() throws Exception {
		int depth = 0;
		MenuElement parent = ctx.getCurrentPage().getParent();
		while (parent != null) {
			parent = parent.getParent();
			depth++;
		}
		if (getPage().isVisibleChildren()) {
			return depth + 1;
		} else {
			return depth;
		}
	}

	public String getI18nAjaxURL() throws ModuleException, Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("module", ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext()).getCurrentModule().getName());
		return URLHelper.createStaticURL(ctx, "/i18n/" + ctx.getRequestContentLanguage(), params);
	}

	public String getLogoUrl() throws Exception {
		String logo = ctx.getGlobalContext().getTemplateData().getLogo();
		if (logo == null || logo.equals("null")) {
			return null;
		} else {
			return URLHelper.createTransformURL(ctx, URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), logo), "logo");
		}
	}

	public String getLogoRawUrl() throws Exception {
		String logo = ctx.getGlobalContext().getTemplateData().getLogo();
		if (logo == null || logo.equals("null")) {
			return null;
		} else {
			return URLHelper.createResourceURL(ctx, URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), logo));
		}
	}

	public boolean isCookiesMessage() throws Exception {
		if (NetHelper.getCookie(ctx.getRequest(), ctx.getCurrentTemplate().getCookiesMessageName()) != null) {
			VisitorsMessageService.getInstance(ctx.getRequest().getSession()).markAsDisplayed("cookies");
		}
		return !VisitorsMessageService.getInstance(ctx.getRequest().getSession()).isAlReadyDisplayed("cookies");
	}

	public Boolean isCookiesAccepted() throws Exception {
		return CookiesService.getInstance(ctx).getAccepted();
	}

	public ContentContext getContextForCopy() {
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		return editCtx.getContextForCopy(ctx);
	}

	public List<PageBean> getParentPageList() throws Exception {
		LinkedList<PageBean> pages = new LinkedList<PageBean>();
		MenuElement page = ctx.getCurrentPage();
		while (page != null) {
			pages.add(0, page.getPageBean(ctx));
			page = page.getParent();
		}
		return pages;
	}

	public String getWaitURL() {
		return URLHelper.createStaticURL(ctx, "/wait.html");
	}

	public List<MacroBean> getInteractiveMacro() {
		List<MacroBean> macros = new LinkedList<MacroBean>();
		List<String> macroName = globalContext.getMacros();
		MacroFactory factory = MacroFactory.getInstance(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()));
		for (String name : macroName) {
			IMacro macro = factory.getMacro(name);
			if (macro instanceof IInteractiveMacro) {
				macros.add(new MacroBean(macro.getName(), macro.getInfo(ctx)));
			}
		}
		return macros;
	}

	public List<IMacro> getAddMacro() {
		List<IMacro> macros = new LinkedList<IMacro>();
		List<String> macroName = globalContext.getMacros();
		MacroFactory factory = MacroFactory.getInstance(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()));
		for (String name : macroName) {
			IMacro macro = factory.getMacro(name);
			if (macro != null && macro.isAdd()) {
				macros.add(macro);
			}
		}
		return macros;
	}

	public List<MacroBean> getMacro() {
		List<MacroBean> macros = new LinkedList<MacroBean>();
		List<String> macroName = globalContext.getMacros();
		MacroFactory factory = MacroFactory.getInstance(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()));
		for (String name : macroName) {
			if (name.trim().length() > 0) {
				IMacro macro = factory.getMacro(name);
				if (!(macro instanceof IInteractiveMacro)) {
					macros.add(new MacroBean(macro.getName(), macro.getInfo(ctx)));
				}
			}
		}
		return macros;
	}

	public List<SubTitleBean> getSubTitles() throws Exception {
		List<SubTitleBean> outList = new LinkedList<SubTitleBean>();
		ContentElementList contentList = ctx.getCurrentPage().getContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent comp = contentList.next(ctx);
			if (comp instanceof ISubTitle) {
				outList.add(new SubTitleBean(ctx, ((ISubTitle) comp)));
			}
		}
		return outList;
	}

	public String getFakeCurrentURL() {
		return fakeCurrentURL;
	}

	public void setFakeCurrentURL(String fakeCurrentURL) {
		this.fakeCurrentURL = fakeCurrentURL;
	}

	/**
	 * check if current user can edit the current page
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isPageEditable() throws Exception {
		return Edit.checkPageSecurity(ctx);
	}

	public ImageBean getImageHeader() throws Exception {
		return getImageBackground();
	}

	public ImageBean getImageBackground() throws Exception {
		if (imageHeader != null) {
			return imageHeader;
		} else {
			IImageTitle imageTitle = ctx.getCurrentPage().getImageBackground(ctx);
			if (imageTitle != null) {
				return new ImageBean(ctx, imageTitle, "main-background");
			} else {
				return null;
			}
		}
	}

	public List<TicketUserWrapper> getUnreadTickets() throws Exception {
		List<TicketUserWrapper> out = new LinkedList<TicketUserWrapper>();
		Map<String, TicketUserWrapper> tickets = TicketAction.getMyTicket(ctx);
		for (TicketUserWrapper ticket : tickets.values()) {
			if (!ticket.isRead() && ticket.isForMe()) {
				out.add(ticket);
			}
		}
		return out;
	}

	public String getLibUrl() {
		return URLHelper.createStaticURL(ctx, "/lib/");
	}

	public boolean isInternetAccess() {
		return ctx.getGlobalContext().getStaticConfig().isInternetAccess();
	}

	public Map<String, String> getTranslateLanguageMap() {
		LanguageMap langMap = new LanguageMap();
		langMap.setLang(ctx.getLanguage());
		return langMap;
	}

	public Map<String, String> getLanguageMap() {
		LanguageMap langMap = new LanguageMap();
		langMap.setLang(null);
		return langMap;
	}

	public String getAccountPageUrl() throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement elem = content.getNavigation(ctx).searchChildFromName("account");
		if (elem != null) {
			return URLHelper.createURL(ctx, elem);
		} else {
			return null;
		}
	}

	public String getNoImageURL() throws Exception {
		return URLHelper.createTransformURL(ctx, "/noimage.png", "standard");
	}

	public boolean isArchive() {
		return ctx.getPath().toLowerCase().contains("archive");
	}

	public String getFacebookImageUrl() throws Exception {
		ContentContext absCtx = ctx.getContextForAbsoluteURL();
		IImageTitle image = ctx.getCurrentPage().getImage(absCtx);
		if (image != null) {
			ImageBean imageBean = new ImageBean(absCtx, image, "facebook");
			return imageBean.getPreviewURL();
		} else {
			return null;
		}
	}
	
	public Map<String,String> getImageBackgroundForArea() throws Exception {
		if (bgAreas == null) {
			Map<String, ImageTitleBean> bg = ctx.getCurrentPage().getImageBackgroundForArea(ctx);
			if (bg.size()==0) {
				bgAreas = Collections.EMPTY_MAP;
			} else {
				bgAreas = new HashMap<String,String>();
				for (String area : ctx.getCurrentPage().getImageBackgroundForArea(ctx).keySet()) {
					bgAreas.put(area, URLHelper.createFileURL(ctx, bg.get(area).getResourceURL(ctx)));
				}
			}
		}
		return bgAreas;
	}

}
