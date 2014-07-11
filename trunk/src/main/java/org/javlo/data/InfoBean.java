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

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.RSSRegistration;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.NavigationMapByName;
import org.javlo.navigation.PageBean;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.visitors.VisitorsMessageService;
import org.javlo.servlet.AccessServlet;
import org.javlo.template.Template;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class InfoBean {

	private static final String ts = "" + System.currentTimeMillis();

	public static final String REQUEST_KEY = "info";

	public static final String NEW_SESSION_PARAM = "__new_session";

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
	 * create info bean in request (key=info) for jstp call in template.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	public static InfoBean updateInfoBean(ContentContext ctx) throws Exception {
		InfoBean info = new InfoBean();

		info.currentPage = ctx.getCurrentPage();
		info.ctx = ctx;
		info.globalContext = GlobalContext.getInstance(ctx.getRequest());

		ctx.getRequest().setAttribute(REQUEST_KEY, info);

		return info;
	}

	private MenuElement currentPage;
	private ContentContext ctx;
	private GlobalContext globalContext;
	private boolean tools = true;
	private Map<IUserInfo, String> avatarFakeMap;

	public String getCmsName() {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getCmsName();
	}

	public String getCurrentAbsoluteURL() {
		return URLHelper.createURL(ctx.getContextForAbsoluteURL());
	}

	public String getCurrentAbsolutePreviewURL() {
		ContentContext previewCtx = ctx.getContextForAbsoluteURL();
		previewCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		return URLHelper.createURL(previewCtx);
	}

	public String getCurrentURL() {
		return URLHelper.createURL(ctx);
	}
	
	public String getCurrentAbsoluteURLQRCode() {		
		return URLHelper.createQRCodeLink(ctx, getShortLanguageURL());
	}
	
	public String getCurrentModuleURL() {
		ModulesContext modulesCtx;
		try {
			modulesCtx = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext);
			return URLHelper.mergePath(URLHelper.createStaticURL(ctx, "/"),modulesCtx.getCurrentModule().getPath());
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

	public String getCurrentViewURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getFreeContentContext());
	}

	public String getCurrentEditURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE).getFreeContentContext());
	}

	public String getCurrentPreviewURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE).getFreeContentContext());
	}

	public String getCurrentPageURL() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE).getFreeContentContext());
	}

	public String getCurrentAjaxURL() {
		String path = ctx.getPath();
		try {
			if (ctx.getVirtualCurrentPage() != null) {
				path = ctx.getVirtualCurrentPage().getPath();
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

	public String getDate() {
		try {
			return StringHelper.renderDate(currentPage.getContentDateNeverNull(ctx), globalContext.getShortDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getCurrentDate() {
		try {
			return StringHelper.renderDate(new Date(), globalContext.getShortDateFormat());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getSortableDate() {
		try {
			return StringHelper.renderSortableDate(currentPage.getContentDateNeverNull(ctx));	
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
			return currentPage.getGlobalTitle(ctx);
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

	public String getLanguage() {
		return ctx.getLanguage();
	}

	public String getPageDescription() {
		try {
			return currentPage.getDescription(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPageID() {
		return currentPage.getId();
	}

	public String getPageMetaDescription() {
		try {
			return currentPage.getMetaDescription(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPageName() {
		return currentPage.getName();
	}

	public String getPageHumanName() {
		return currentPage.getHumanName();
	}

	public String getPageTitle() {
		try {
			return currentPage.getTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getTime() {
		try {
			return StringHelper.renderTime(ctx, currentPage.getContentDateNeverNull(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getUserName() {
		return ctx.getCurrentUserId();
	}

	public PageBean getPage() {
		try {
			return currentPage.getPageBean(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public PageBean getRoot() {
		try {
			return currentPage.getRoot().getPageBean(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<PageBean> getPagePath() {

		MenuElement page = currentPage;

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

	public String getEditTemplateURL() {
		return URLHelper.createStaticURL(ctx, ctx.getGlobalContext().getStaticConfig().getEditTemplateFolder());
	}

	public String getStaticRootURL() {
		return URLHelper.createStaticURL(ctx, "/");
	}

	public String getContextDownloadURL() {
		return URLHelper.createStaticURL(ctx, "/context");
	}

	public String getCaptchaURL() {
		return URLHelper.createStaticURL(ctx, "/captcha.jpg");
	}

	public String getCurrentUserAvatarUrl() {
		return URLHelper.createAvatarUrl(ctx, ctx.getCurrentUser().getUserInfo());
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
		if (currentPage.getParent() != null) {
			try {
				return currentPage.getParent().getPageBean(ctx);
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
				return ctx.getCurrentTemplate().getFolder(globalContext);
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

	public String getAbsoluteTemplateFolder() {
		try {
			if (ctx.getCurrentTemplate() != null) {
				return URLHelper.createStaticTemplateURL(ctx, "/");
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


	public Template getTemplate() {
		try {
			return ctx.getCurrentTemplate();
		} catch (Exception e) {
			return null;
		}
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
		return AdminUserSecurity.getInstance().isGod(ctx.getCurrentUser());
	}

	public boolean isAdmin() {
		return AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentUser());
	}

	public String getPath() {
		return ctx.getPath();
	}

	public boolean isPreview() {
		return ctx.getRenderMode() == ContentContext.PREVIEW_MODE;
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
		User user = AdminUserFactory.createUserFactory(ctx.getRequest()).getCurrentUser(ctx.getRequest().getSession());
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
	 * this method return true at the first call for current session and false
	 * afer.
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
		String localModulePath = ctx.getRequest().getSession().getServletContext().getRealPath("/webstart/localmodule.jnlp.jsp");
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
		return URLHelper.createQRCodeLink(ctx, (IContentVisualComponent)null);
	}

	public String getBackURL() {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		return requestService.getParameter(ElementaryURLHelper.BACK_PARAM_NAME, null);
	}

	public String getRootURL() {
		return URLHelper.createURL(ctx, "/");
	}
	
	public String getRSSAllURL()  {
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


	public Map<String, String> getStaticData() {
		return staticData;
	}

	public String getPageBookmark() {
		try {
			return NavigationHelper.getPageBookmark(ctx, currentPage);
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

	public static void main(String[] args) {
		String url = "http://localhost:8080";

		System.out.println(url);
	}

	public String getAjaxLoaderURL() {
		return URLHelper.createStaticURL(ctx, "/images/ajax_loader.gif");
	}

	public String getRandomId() {
		return StringHelper.getRandomId();
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
		return (Boolean)ctx.getRequest().getSession().getAttribute(RSS_SESSION_KEY);
	}
	
	/**
	 * get the current total depth of navigation.
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
			return depth+1;
		} else {
			return depth;
		}
	}
	
	public String getI18nAjaxURL() throws ModuleException, Exception {
		Map<String,String> params = new HashMap<String, String>();		
		params.put("module", ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext()).getCurrentModule().getName());
		return URLHelper.createStaticURL(ctx, "/i18n/"+ctx.getRequestContentLanguage(),params);
	}
	
	public String getLogoUrl() throws Exception {
		String logo = ctx.getGlobalContext().getTemplateData().getLogo();
		if (logo == null) {
			return null;
		} else {
			return URLHelper.createTransformURL(ctx, URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(),logo), "logo");
		}
	}
	
	public boolean isCookiesMessage() throws Exception {
		if (NetHelper.getCookie(ctx.getRequest(), ctx.getCurrentTemplate().getCookiesMessageName()) != null) {
			VisitorsMessageService.getInstance(ctx.getRequest().getSession()).markAsDisplayed("cookies");
		} 
		return !VisitorsMessageService.getInstance(ctx.getRequest().getSession()).isAllReadyDisplayed("cookies");		
	}

}
