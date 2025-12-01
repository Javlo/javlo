package org.javlo.data;

import org.javlo.bean.DateBean;
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
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.macro.core.IMacro;
import org.javlo.macro.core.MacroFactory;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.admin.MacroBean;
import org.javlo.module.content.ComponentWrapper;
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
import org.javlo.user.*;
import org.javlo.utils.HtmlPart;
import org.joda.time.LocalTime;
import org.owasp.encoder.Encode;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.*;

public class InfoBean {

	public static final String FAKE_PATH_PARAMETER_NAME = "_fake_path";

	private static final String ts = "" + System.currentTimeMillis();

	public static final String REQUEST_KEY = "info";

	// public static final String NEW_SESSION_PARAM = "__new_session";

	private String fakeCurrentURL = null;

	private ImageBean imageHeader = null;

	private Map<String, Boolean> areas = null;

	private Map<String, String> bgAreas = null;

	private String pageNotFoundMessage = null;

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
			ib = createInfoBean(ctx);
			ctx.getRequest().setAttribute(REQUEST_KEY, ib);
		}
		return ib;
	}

	/**
	 * create info bean but don't put in request.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	private static InfoBean createInfoBean(ContentContext ctx) throws Exception {
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
		InfoBean info = getCurrentInfoBean(ctx);
		info.ctx = ctx;
		info.globalContext = ctx.getGlobalContext();
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
		Map<String, String> params = Collections.EMPTY_MAP;
		String popupPath = ctx.getRequest().getParameter(NavigationHelper.POPUP_PARAM);
		if (popupPath != null) {
			params = new HashMap<String, String>();
			params.put(NavigationHelper.POPUP_PARAM, popupPath);
		}
		return URLHelper.createURL(ctx.getContextForAbsoluteURL(), params);
	}

	public String getCurrentAbsoluteURLZIP() throws Exception {
		ContentContext ZIPCtx = ctx.getContextForAbsoluteURL();
		ZIPCtx.setURLFactory(null);
		ZIPCtx.setRenderMode(ContentContext.VIEW_MODE);
		ZIPCtx.setFormat("zip");
		return URLHelper.createURL(ZIPCtx);
	}

	public String getCurrentAbsoluteURLXML() throws Exception {
		ContentContext XMLCtx = ctx.getContextForAbsoluteURL();
		XMLCtx.setURLFactory(null);
		XMLCtx.setRenderMode(ContentContext.VIEW_MODE);
		XMLCtx.setFormat("xml");
		return URLHelper.createURL(XMLCtx);
	}

	public String getCurrentAbsolutePreviewURL() throws Exception {
		ContentContext previewCtx = ctx.getContextForAbsoluteURL();
		previewCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		return URLHelper.createURL(previewCtx);
	}

	public String getCurrentURL() throws Exception {
		if (getFakeCurrentURL() != null) {
			return getFakeCurrentURL();
		} else {
			return URLHelper.createURL(ctx);
		}
	}
	
	public String getCurrentUrl() throws Exception {
		if (getFakeCurrentURL() != null) {
			return getFakeCurrentURL();
		} else {
			return URLHelper.createURL(ctx);
		}
	}

	public String getVirtualCurrentURL() throws Exception {
		if (getFakeCurrentURL() != null) {
			return getFakeCurrentURL();
		} else {
			return URLHelper.createVirtualURL(ctx);
		}
	}

	public String getRealCurrentURL() throws Exception {
		return URLHelper.createURL(ctx);
	}

	public String getCurrentURLWidthDevice() throws Exception {
		if (getFakeCurrentURL() != null) {
			return getFakeCurrentURL();
		} else {
			String url = URLHelper.createURL(ctx);
			if (!url.contains(Device.FORCE_DEVICE_PARAMETER_NAME)) {
				url = URLHelper.addParam(url, Device.FORCE_DEVICE_PARAMETER_NAME, "" + ctx.getDevice());
			}
			return url;
		}
	}

	public String getCurrentAjaxURLWidthDevice() {
		if (getFakeCurrentURL() != null) {
			return getFakeCurrentURL();
		} else {
			String url = URLHelper.createAjaxURL(ctx);
			url = URLHelper.addParam(url, Device.FORCE_DEVICE_PARAMETER_NAME, "" + ctx.getDevice());
			return url;
		}
	}

	public String getCurrentCanonicalURL() throws Exception {
		if (!StringHelper.isEmpty(getCurrentPage().getForward(ctx))) {
			return getCurrentPage().getForward(ctx);
		}
		ContentContext robotCtx = new ContentContext(ctx);
		MenuElement popupPage = NavigationHelper.getPopupPage(ctx);
		if (popupPage != null) {
			robotCtx.setCurrentPageCached(popupPage);
		}
		robotCtx.setDevice(Device.getFakeDevice("robot"));
		robotCtx.setAbsoluteURL(true);
		if (ctx.getGlobalContext().getCanonicalHost() != null) {
			robotCtx.setHostName(ctx.getGlobalContext().getCanonicalHost());
		}
		return URLHelper.createURL(robotCtx);
	}

	private Map<String, String> lgURLs = null;

	private Map<String, String> lgAbsURLs = null;

	private Map<String, String> lgRealContentURLs = null;

	private Map<String, String> lgRealContentAbsoluteURLs = null;


	public Map<String, String> getLanguageURLs() throws Exception {
		if (lgURLs == null) {
			lgURLs = new HashMap<String, String>();
			ContentContext lgCtx = new ContentContext(ctx);
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				lgCtx.setAllLanguage(lg);
				lgURLs.put(lg, URLHelper.createURL(lgCtx));
			}
		}
		return lgURLs;
	}

	public Map<String, String> getLanguageAbsoluteURLs() throws Exception {
		if (lgAbsURLs == null) {
			lgAbsURLs = new HashMap<String, String>();
			ContentContext lgCtx = new ContentContext(ctx);
			lgCtx.setAbsoluteURL(true);
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				lgCtx.setAllLanguage(lg);
				lgAbsURLs.put(lg, URLHelper.createURL(lgCtx));
			}
		}
		return lgAbsURLs;
	}

	public Map<String, String> getLanguageRealContentURLs() throws Exception {
		if (lgRealContentURLs == null) {
			lgRealContentURLs = new HashMap<String, String>();
			ContentContext lgCtx = new ContentContext(ctx);
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				lgCtx.setAllLanguage(lg);
				if (getCurrentPage().isRealContent(lgCtx)) {
					lgRealContentURLs.put(lg, URLHelper.createURL(lgCtx));
				}
			}
		}
		return lgRealContentURLs;
	}

	public Map<String, String> getLanguageRealContentAbsoluteURLs() throws Exception {
		if (lgRealContentAbsoluteURLs == null) {
			lgRealContentAbsoluteURLs = new HashMap<String, String>();
			ContentContext lgCtx = new ContentContext(ctx);
			lgCtx.setAbsoluteURL(true);
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				lgCtx.setAllLanguage(lg);
				if (getCurrentPage().isRealContent(lgCtx)) {
					lgRealContentAbsoluteURLs.put(lg, URLHelper.createURL(lgCtx));
				}
			}
		}
		return lgRealContentAbsoluteURLs;
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

	public String getCurrentPDFURL() throws Exception {
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

	public String getCurrentViewURL() throws Exception {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getFreeContentContext());
	}
	
	public String getCurrentTimeURL() throws Exception {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.TIME_MODE).getFreeContentContext());
	}

	public String getCurrentViewURLWidthDevice() throws Exception {
		String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getFreeContentContext());
		url = URLHelper.addParam(url, Device.FORCE_DEVICE_PARAMETER_NAME, "" + ctx.getDevice());
		return url;
	}

	public String getCurrentEditURL() throws Exception {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE).getFreeContentContext());
	}

	public String getCurrentPreviewURL() throws Exception {
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

	public String getCurrentPageURL() throws Exception {
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

	public String getPDFURL() throws Exception {
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
			return StringHelper.renderDate(ctx.getLocale(), getCurrentPage().getContentDateNeverNull(ctx), globalContext.getShortDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getMediumDate() {
		try {
			return StringHelper.renderDate(ctx.getLocale(), getCurrentPage().getContentDateNeverNull(ctx), globalContext.getMediumDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getFullDate() {
		try {
			return StringHelper.renderDate(ctx.getLocale(), getCurrentPage().getContentDateNeverNull(ctx), globalContext.getFullDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNowDate() {
		try {
			return StringHelper.renderDate(ctx.getLocale(), new Date(), globalContext.getShortDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNowMediumDate() {
		try {
			return StringHelper.renderDate(ctx.getLocale(), new Date(), globalContext.getMediumDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNowFullDate() {
		try {
			return StringHelper.renderDate(ctx.getLocale(), new Date(), globalContext.getFullDateFormat());
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

	public String getNowInputDate() throws ParseException {
		return StringHelper.renderInputDate(new Date());
	}

	public String getMajorInputDate() throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -18);
		return StringHelper.renderInputDate(cal.getTime());
	}

	public String getMaxAgeInputDate() throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -120);
		return StringHelper.renderInputDate(cal.getTime());
	}

	public String getCurrentDate() {
		try {
			return StringHelper.renderDate(new Date(), globalContext.getShortDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public DateBean getCurrentDateBean() {
		return new DateBean(ctx, new Date());
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
			if (getCurrentPage() != null) {
				return getCurrentPage().getGlobalTitle(ctx);
			} else {
				return null;
			}
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
			locale = ctx.getLocale();
		}
		Locale lg = ctx.getLocale();
		return lg.getDisplayName(locale);
	}

	public Locale getLocale() {
		return ctx.getLocale();
	}

	public String getLocaleString() {
		String locale = ctx.getLocale().toString();
		if (locale.length() == 2) {
			if (locale.equalsIgnoreCase("en")) {
				return "en-UK";
			} else {
				return locale + "-" + locale.toUpperCase();
			}
		}
		return locale;
	}

	public String getLanguage() {
		return ctx.getLanguage();
	}

	public List<Locale> getLanguagesLocale() {
		return ctx.getGlobalContext().getLanguagesLocal();
	}

	public String getLanguageOnly() {
		String lang = ctx.getLanguage();
		if (lang.length()==5) {
			return lang.substring(0,2);
		} else {
			return lang;
		}
	}

	public String getCountry() {
		return ctx.getCountry();
	}

	public String getLanguageAndCountry() {
		String lang = ctx.getLanguage();
		if (lang.length() == 2) {
			return lang;
		} else {
			return lang.substring(0,2)+'-'+lang.substring(2,4).toUpperCase();
		}
	}

	public HtmlPart getPageDescription() {
		try {
			String description = getCurrentPage().getMetaDescription(ctx);
			MenuElement popupPage = NavigationHelper.getPopupPage(ctx);
			if (popupPage != null) {
				description = popupPage.getMetaDescription(ctx);
			}
			final String noRecursiveRequestKey = "_pageDescritionCalled";
			if (ctx.getRequest().getAttribute(noRecursiveRequestKey) == null) {
				ctx.getRequest().setAttribute(noRecursiveRequestKey, 1);
				description = XHTMLHelper.replaceJSTLData(ctx, description);
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

	public String getPageDescriptionForAttribute() {
		HtmlPart desc = getPageDescription();
		if (desc != null) {
			return desc.getTextForAttribute();
		} else {
			return "";
		}
	}

	@Deprecated
	public String getPageID() {
		return getCurrentPage().getId();
	}

	public String getPageId() {
		return getCurrentPage().getId();
	}

	public String getPageMetaDescription() {
		try {
			MenuElement popupPage = NavigationHelper.getPopupPage(ctx);
			if (popupPage != null) {
				return popupPage.getMetaDescription(ctx);
			} else {
				return getCurrentPage().getMetaDescription(ctx);
			}
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
		
		if (ctx.getForcePageTitle() != null) {
			return ctx.getForcePageTitle();
		}
		
		try {
			MenuElement popupPage = NavigationHelper.getPopupPage(ctx);
			if (popupPage != null) {
				return popupPage.getPageTitle(ctx);
			} else {
				MenuElement cp = getCurrentPage();
				if (cp != null) {
					return cp.getPageTitle(ctx);
				} else {
					return "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String getPageLabel() {

		try {
			MenuElement popupPage = NavigationHelper.getPopupPage(ctx);
			if (popupPage != null) {
				return popupPage.getLabel(ctx);
			} else {
				MenuElement cp = getCurrentPage();
				if (cp != null) {
					return cp.getLabel(ctx);
				} else {
					return "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String getPageTitleForAttribute() {
		return Encode.forHtmlAttribute(getPageTitle());
	}

	public String getTitle() {
		try {
			MenuElement popupPage = NavigationHelper.getPopupPage(ctx);
			if (popupPage != null) {
				return popupPage.getTitle(ctx);
			} else {
				return getCurrentPage().getTitle(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getTitleForAttribute() {
		return Encode.forHtmlAttribute(getTitle());
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

	public String getAdminUserName() {
		return ctx.getCurrentAdminUserId();
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

	public String getUserEmail() {
		User user = ctx.getCurrentUser();
		if (user == null) {
			return "";
		} else {
			if (StringHelper.isMail(ctx.getCurrentUser().getUserInfo().getEmail())) {
				return ctx.getCurrentUser().getUserInfo().getEmail();
			}
		}
		return null;
	}

	public PageBean getPage() {
		try {
			MenuElement popupPage = NavigationHelper.getPopupPage(ctx);
			if (popupPage != null) {
				return popupPage.getPageBean(ctx);
			}
			MenuElement cp = getCurrentPage();
			if (cp != null) {
				return cp.getPageBean(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
	 * return the root page with this template, stop to parent page with different
	 * template
	 * 
	 * @return
	 * @throws Exception
	 */
	public PageBean getTemplateRoot() throws Exception {
		String template = getCurrentPage().getTemplateIdOnInherited(ctx);
		MenuElement root = getCurrentPage();
		MenuElement parent = getCurrentPage().getParent();
		while (parent != null && parent.getTemplateIdOnInherited(ctx).equals(template)) {
			root = parent;
			parent = parent.getParent();
		}
		return root.getPageBean(ctx);
	}

	/**
	 * get the list of the pages from current to root
	 * 
	 * @return
	 */
	public List<PageBean> getPagePath() {

		MenuElement page = getCurrentPage();
		if (page == null) {
			return Collections.emptyList();
		}

		List<PageBean> pagePath = new LinkedList<PageBean>();
		page = page.getParent();

		while (page != null) {
			try {
				pagePath.add(0, page.getPageBean(ctx));
			} catch (Exception e) {
				e.printStackTrace();
			}
			page = page.getParent();
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

//	public String getEditTemplateModeURL() {
//		if (globalContext.getEditTemplateMode() != null && globalContext.getEditTemplateMode().trim().length() > 0) {
//			String cssLink = URLHelper.mergePath(globalContext.getStaticConfig().getEditTemplateFolder(), "css", "edit_" + globalContext.getEditTemplateMode() + ".css");
//			return URLHelper.createStaticURL(ctx, cssLink);
//		} else {
//			try {
//				if (ctx.getCurrentTemplate() != null && !StringHelper.isEmpty(ctx.getCurrentTemplate().getEditTemplateMode())) {
//					String cssLink = URLHelper.mergePath(globalContext.getStaticConfig().getEditTemplateFolder(), "css", "edit_" + ctx.getCurrentTemplate().getEditTemplateMode() + ".css");
//					return URLHelper.createStaticURL(ctx, cssLink);
//				} else {
//					return null;
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
//	}

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

	public String getFileRootURL() {
		return URLHelper.createFileURL(ctx, "/");
	}

	public String getMediaRootURL() {
		return URLHelper.createMediaURL(ctx, "/");
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
			return URLHelper.createStaticURL(ctx, "/images/avatar.svg");
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
	
	public String getJavloLogoHtml() {
		String logoHtml = "<div class=\"logo-javlo\"><svg version=\"1.1\" id=\"Calque_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\"\r\n"
				+ "	 viewBox=\"0 0 653.4 173.8\" style=\"enable-background:new 0 0 653.4 173.8;\" xml:space=\"preserve\">\r\n"
				+ "<g>"
				+ "	<g>\r\n"
				+ "		<path class=\"st0\" d=\"M0,110v-1.3h32.6v1.3c0,20.9,10.4,33.1,32.1,33.1c21.1,0,31.5-12.3,31.5-33.1V0h33.4v110\r\n"
				+ "			c0,38.8-23.2,63.9-65.2,63.9C23.2,173.9,0,148.8,0,110z\"/>\r\n"
				+ "		<path class=\"st0\" d=\"M296.5,38.3h34.1l38.1,107.2L407,38.3h33.4l-53.3,135.6h-37.3L296.5,38.3z\"/>\r\n"
				+ "		<path class=\"st0\" d=\"M459.3,10h31v164h-31V10z\"/>\r\n"
				+ "		<path class=\"st0\" d=\"M216.1,173.8c-0.4,0-0.9,0-1.3,0c-0.5,0-1,0-1.4,0H216.1z\"/>\r\n"
				+ "		<path class=\"st0\" d=\"M220.9,38.3c-38.7-0.3-69.9,29.5-70.3,68.2c-0.4,43.7,31.2,66.3,62,67.3h8.5h5.2h5.5h0v-29h0h-5.5h-5.2\r\n"
				+ "			c-4.4,0-8.7-0.5-12.9-1.5c-8.8-2.5-15.8-7.8-20.4-15.1c-4.3-6.3-6.9-13.9-6.9-22.1s2.6-15.8,6.9-22.1c4.6-7.3,11.7-12.7,20.4-15.1\r\n"
				+ "			c3.9-1.3,8.1-2,12.4-2c21.8,0,39.6,17.4,39.7,39v0.3v12.1v26.6v29v0.1h28.4v-66.8C288.8,65.9,261,38.6,220.9,38.3z\"/>\r\n"
				+ "		<path class=\"st0\" d=\"M620,92.4c1.4,4.2,2.1,8.8,2.1,13.7c0,22.7-15.1,38.8-37.8,38.8c-22.7,0-38-16.2-38-38.8\r\n"
				+ "			c0-22.7,15.4-38.8,38-38.8c5.2,0,10,0.9,14.4,2.5l22.2-22.2c-10.4-5.9-22.8-9.2-36.6-9.2c-40.6,0-69.3,28.7-69.3,67.7\r\n"
				+ "			s28.6,67.7,69.3,67.7c40.6,0,69.3-28.7,69.3-67.7c0-14-3.7-26.6-10.3-37.1L620,92.4z\"/>\r\n"
				+ "	</g>"
				+ "</g>"
				+ "</svg></div>"
				+ "";
		return logoHtml;
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
		if (getCurrentPage() != null && getCurrentPage().getParent() != null) {
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
		String url = globalContext.getPrivateHelpURL();
		if (url != null) {
			return url.replace("#lang#", ctx.getRequestContentLanguage());
		} else {
			return null;
		}
	}

	public String getCookiesPolicyUrl() throws Exception {
		String url = globalContext.getCookiesPolicyUrl();
		if (StringHelper.isURL(url)) {
			return url.replace("#lang#", ctx.getRequestContentLanguage());
		} else if (url != null) {
			return URLHelper.replacePageReference(ctx, url);
		} else {
			return null;
		}
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

	public String getQrCodeURL() {
		try {
			return URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/qrcode/page/" + ctx.getRequestContentLanguage() + "/" + ctx.getPath());
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
			Collections.sort(templates, new Comparator<TemplateBean>() {
				@Override
				public int compare(TemplateBean o1, TemplateBean o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

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

	/**
	 * return true if the mode is a visual mode (view or preview without editition)
	 * @return
	 */
	public boolean isVisual() {
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			return true;
		}
		return !(ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly());
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

	// public boolean isNewSession() {
	// if
	// (StringHelper.isTrue(ctx.getRequest().getSession().getAttribute(NEW_SESSION_PARAM)))
	// {
	// return true;
	// } else {
	// return ctx.getRequest().getSession().isNew();
	// }
	// }

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
	 * return true if content is editable in preview mode
	 * @return
	 */
	public boolean isPreviewEditionMode() {
		return ctx.isAsPreviewMode();
	}

	/**
	 * timestamp initialised when java VM is started.
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

	public String getRootUrl() {
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

	public PageBean getPageByName(String name) {
		try {
			return getRoot().getPage().searchChildFromName(name).getPageBean(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public PageBean getRegistrationPage() throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement regPage = content.getRegistrationPage(ctx);
		if (regPage != null) {
			return regPage.getPageBean(ctx);
		}
		return null;
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

	public String getUserLanguage() {
		if (ctx.isAsEditMode()) {
			return globalContext.getEditLanguage(ctx.getRequest().getSession());
		} else {
			return getLanguage();
		}
	}

	public String[] getMonths() {
		return DateFormatSymbols.getInstance(new Locale(getUserLanguage())).getMonths();
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
		return URLHelper.getLogoUrl(ctx);
	}

	public String getLogoRawUrl() throws Exception {
		return URLHelper.getLogoRawUrl(ctx);
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
		MacroFactory factory = MacroFactory.getInstance(ctx);
		for (String name : macroName) {
			IMacro macro = factory.getMacro(name);
			if (macro instanceof IInteractiveMacro && macro.isActive()) {
				macros.add(new MacroBean(macro.getName(), macro.getInfo(ctx), ((IInteractiveMacro) macro).getModalSize(), macro.getPriority()));
			}
		}
		return macros;
	}

	public List<IMacro> getAddMacro() {
		List<IMacro> macros = new LinkedList<IMacro>();
		List<String> macroName = globalContext.getMacros();
		MacroFactory factory = MacroFactory.getInstance(ctx);
		for (String name : macroName) {
			IMacro macro = factory.getMacro(name);
			if (macro != null && macro.isActive()) {
				macros.add(macro);
			}
		}
		MacroFactory.sort(macros);
		return macros;
	}

	public List<MacroBean> getMacro() {
		List<MacroBean> macros = new LinkedList<MacroBean>();
		List<String> macroName = globalContext.getMacros();
		MacroFactory factory = MacroFactory.getInstance(ctx);
		for (String name : macroName) {
			if (name.trim().length() > 0) {
				IMacro macro = factory.getMacro(name);
				if (macro != null && !(macro instanceof IInteractiveMacro) && macro.isActive()) {
					macros.add(new MacroBean(macro.getName(), macro.getInfo(ctx), null, macro.getPriority()));
				}
			}
		}
		MacroBean.sort(macros);
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
		if (fakeCurrentURL == null && ctx.getRequest().getParameter(FAKE_PATH_PARAMETER_NAME) != null) {
			return ctx.getRequest().getParameter(FAKE_PATH_PARAMETER_NAME);
		} else {
			return fakeCurrentURL;
		}
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

	public String getNoImageUrl() throws Exception {
		return URLHelper.createNoImageUrl(ctx, "standard");
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

	public Map<String, String> getImageBackgroundForArea() throws Exception {
		if (bgAreas == null) {
			Map<String, ImageTitleBean> bg = ctx.getCurrentPage().getImageBackgroundForArea(ctx);
			if (bg.size() == 0) {
				bgAreas = Collections.EMPTY_MAP;
			} else {
				bgAreas = new HashMap<String, String>();
				for (String area : ctx.getCurrentPage().getImageBackgroundForArea(ctx).keySet()) {
					bgAreas.put(area, URLHelper.createFileURL(ctx, bg.get(area).getResourceURL(ctx)));
				}
			}
		}
		return bgAreas;
	}

	public String getPageNotFoundMessage() {
		return pageNotFoundMessage;
	}

	public void setPageNotFoundMessage(String pageNotFoundMessage) {
		this.pageNotFoundMessage = pageNotFoundMessage;
	}

	public String getAjaxLoginUrl() {
		return URLHelper.createActionURL(ctx, "user.login", ctx.getPath());
	}

	public String getWebactionUrl() {
		return URLHelper.createActionURL(ctx, null, null);
	}

	public String[] getShortDays() {
		String[] days = new String[7];
		Locale locale = new Locale(getUserLanguage());
		Calendar cal = Calendar.getInstance(locale);
		for (int i = 0; i < 7; i++) {
			cal.set(Calendar.DAY_OF_WEEK, i + 1);
			days[i] = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT_FORMAT, locale);
		}
		return days;
	}

	public String[] getLongDays() {
		String[] days = new String[7];
		Locale locale = ctx.getLocale();
		Calendar cal = Calendar.getInstance(locale);
		for (int i = 1; i < 7; i++) {
			cal.set(Calendar.DAY_OF_WEEK, i + 1);
			days[i] = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG_FORMAT, locale);
		}
		return days;
	}

	public String getSearchPageUrl() throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement searchPage = content.getNavigation(ctx).searchChildFromName(ctx.getGlobalContext().getSpecialConfig().getSearchPageName());
		if (searchPage != null) {
			return URLHelper.createURL(ctx, searchPage);
		} else {
			return null;
		}
	}

	public boolean isSearchPage() throws Exception {
		return ctx.getCurrentPage().getName().equals(ctx.getGlobalContext().getSpecialConfig().getSearchPageName());
	}

	public String getRegisterPageUrl() throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement page = content.getNavigation(ctx).searchChildFromName(ctx.getGlobalContext().getSpecialConfig().getRegisterPageName());
		if (page != null) {
			return URLHelper.createURL(ctx, page);
		} else {
			return null;
		}
	}

	public boolean isRegisterPage() throws Exception {
		return ctx.getCurrentPage().getName().equals(ctx.getGlobalContext().getSpecialConfig().getRegisterPageName());
	}

	public String getLoginPageUrl() throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement page = content.getNavigation(ctx).searchChildFromName(ctx.getGlobalContext().getSpecialConfig().getLoginPageName());
		if (page != null) {
			return URLHelper.createURL(ctx, page);
		} else {
			return null;
		}
	}

	public boolean isLoginPage() throws Exception {
		return ctx.getCurrentPage().getName().equals(ctx.getGlobalContext().getSpecialConfig().getLoginPageName());
	}

	public String getNewsPageUrl() throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement page = content.getNavigation(ctx).searchChildFromName(ctx.getGlobalContext().getSpecialConfig().getNewsPageName());
		if (page != null) {
			return URLHelper.createURL(ctx, page);
		} else {
			return null;
		}
	}

	public boolean isNewsPage() throws Exception {
		return ctx.getCurrentPage().getName().equals(ctx.getGlobalContext().getSpecialConfig().getNewsPageName());
	}

	public boolean isHomePage() throws Exception {
		return ctx.getCurrentPage().isLikeRoot(ctx);
	}

	public static void main(String[] args) {
		Locale locale = new Locale("en");
		System.out.println("lcoale="+locale);


	}

	public String getForwardUrl() throws Exception {
		return ctx.getCurrentPage().getForward(ctx);
	}

	public String getDefaultEmailSender() {
		return ctx.getGlobalContext().getAdministratorEmail();
	}

	public String getMarkAllReadNotificationUrl() {
		return URLHelper.createActionURL(ctx, "data.notificationsAsRead");
	}

	public String getHello() throws ServiceException, Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		LocalTime time = LocalTime.now();
		if (time.getHourOfDay() >= 18 && time.getHourOfDay() <= 2) {
			return i18nAccess.getViewText("global.hello.evening");
		} else if (time.getHourOfDay() >= 12 && time.getHourOfDay() < 18) {
			return i18nAccess.getViewText("global.hello.afthernoon");
		} else {
			return i18nAccess.getViewText("global.hello.morning");
		}
	}
	
	public String getJsonSiteMapUrl() {
		return URLHelper.createStaticURL(ctx, "/sitemap.json");
	}

	public Map<String, Collection<ComponentWrapper>> getComponentByType() {
		return new ComponentTypeMap(ctx);
	}



}
