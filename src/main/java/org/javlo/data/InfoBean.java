package org.javlo.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.servlet.AccessServlet;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class InfoBean {

	public static final String REQUEST_KEY = "info";

	public static InfoBean getCurrentInfoBean(HttpServletRequest request) {
		return (InfoBean) request.getAttribute(REQUEST_KEY);
	}

	/**
	 * create info bean in request (key=info) for jstp call in template.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	public static InfoBean updateInfoBean(ContentContext ctx) throws Exception {		
		InfoBean info = new InfoBean();
		MenuElement currentPage = ctx.getCurrentPage();
		info.setPage(currentPage.getPageBean(ctx));		
		ContentService content = ContentService.createContent(ctx.getRequest());
		info.setRoot(content.getNavigation(ctx).getPageBean(ctx));
		info.setPageTitle(currentPage.getTitle(ctx));
		info.setPageSubTitle(currentPage.getSubTitle(ctx));
		info.setPageID(currentPage.getId());
		info.setPageDescription(currentPage.getDescription(ctx));
		info.setPageMetaDescription(currentPage.getMetaDescription(ctx));
		info.setPageCategory(currentPage.getCategory(ctx));
		info.setGlobalTitle(currentPage.getGlobalTitle(ctx));
		if (currentPage.getParent() != null) {
			info.setParentPageName(currentPage.getParent().getName());
			info.setParentPageTitle(currentPage.getParent().getTitle(ctx));
			info.setParentPageURL(URLHelper.createURL(ctx, currentPage.getParent().getPath()));
		}
		info.setCurrentURL(URLHelper.createURL(ctx));		
		info.setStaticRootURL(URLHelper.createStaticURL(ctx, "/"));
		ContentContext lCtx = new ContentContext(ctx);
		lCtx.setAbsoluteURL(true);
		info.setCurrentAbsoluteURL(URLHelper.createURL(lCtx));
		info.setHomeAbsoluteURL(URLHelper.createURL(lCtx, "/"));
		info.setPageName(currentPage.getName());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		User currentUser = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession()).getCurrentUser(ctx.getRequest().getSession());
		if (currentUser != null) {
			info.setUserName(currentUser.getLogin());
		}
		info.setContentLanguage(ctx.getRequestContentLanguage());
		/*if (ctx.getRenderMode() != ContentContext.EDIT_MODE) {
			info.setContentLanguage(ctx.getRequestContentLanguage());
		} else {
			info.setContentLanguage(globalContext.getEditLanguage());
		}*/
		info.setEditLanguage(globalContext.getEditLanguage());
		info.setContentLanguages(globalContext.getContentLanguages());
		info.setLanguages(globalContext.getLanguages());
		info.setLanguage(ctx.getLanguage());		
		info.setDate(StringHelper.renderDate(currentPage.getContentDateNeverNull(ctx), globalContext.getShortDateFormat()));
		info.setTime(StringHelper.renderTime(ctx, currentPage.getContentDateNeverNull(ctx)));
		info.device = ctx.getDevice();
		ctx.getRequest().setAttribute(REQUEST_KEY, info);

		if (ctx.getCurrentTemplate() != null) {
			info.setTemplateFolder(ctx.getCurrentTemplate().getFolder(globalContext));
			info.setAbsoluteTemplateFolder(URLHelper.createStaticTemplateURL(ctx,"/"));
		}

		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		info.globalMessage = messageRepository.getGlobalMessage();
		
		MenuElement page = ctx.getCurrentPage();
		while (page.getParent() != null) {
			page = page.getParent();
			info.pagePath.add(0,page.getPageBean(ctx));
		}
		
		EditContext editContext = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		info.setEditTemplateURL(editContext.getEditTemplateFolder());

		return info;
	}

	private String userName;
	private String globalTitle;
	private String pageTitle;
	private String pageSubTitle;
	private String pageDescription;
	private String pageMetaDescription;
	private String contentLanguage;
	private Device device;
	private String language;
	private String editLanguage;
	private String currentURL;
	private String currentAbsoluteURL;
	private String staticRootURL;
	private String homeAbsoluteURL;
	private String pageName;
	private String pageCategory;
	private String pageID;
	private String parentPageName = "";
	private String parentPageTitle = "";
	private String parentPageURL = null;
	private String editTemplateURL = null;
	private String date;
	private String time;
	private String templateFolder = "";
	private Collection<String> contentLanguages;
	private Collection<String> languages;
	private MenuElement.PageBean page = null;
	private MenuElement.PageBean root = null;
	private List<MenuElement.PageBean> pagePath = new LinkedList<MenuElement.PageBean>();
	
	private GenericMessage globalMessage;

	private String encoding = ContentContext.CHARACTER_ENCODING;
	private String absoluteTemplateFolder;

	public String getCurrentAbsoluteURL() {
		return currentAbsoluteURL;
	}

	public String getCurrentURL() {
		return currentURL;
	}

	public String getDate() {
		return date;
	}

	public Device getDevice() {
		return device;
	}

	public String getEditLanguage() {
		return editLanguage;
	}

	public String getEncoding() {
		return encoding;
	}

	public GenericMessage getGlobalMessage() {
		return globalMessage;
	}

	public String getGlobalTitle() {
		return globalTitle;
	}

	public String getHomeAbsoluteURL() {
		return homeAbsoluteURL;
	}

	public String getContentLanguage() {
		return contentLanguage;
	}

	public String getLanguage() {
		return language;
	}

	public String getPageDescription() {
		return pageDescription;
	}

	public String getPageID() {
		return pageID;
	}

	public String getPageMetaDescription() {
		return pageMetaDescription;
	}

	public String getPageName() {
		return pageName;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public String getParentPageName() {
		return parentPageName;
	}

	public String getParentPageTitle() {
		return parentPageTitle;
	}

	public String getParentPageURL() {
		return parentPageURL;
	}

	public String getTemplateFolder() {
		return templateFolder;
	}

	public String getTime() {
		return time;
	}

	public String getUserName() {
		return userName;
	}

	public void setCurrentAbsoluteURL(String currentAbsoluteURL) {
		this.currentAbsoluteURL = currentAbsoluteURL;
	}

	public void setCurrentURL(String currentURL) {
		this.currentURL = currentURL;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public void setEditLanguage(String editLanguage) {
		this.editLanguage = editLanguage;
	}

	public void setGlobalMessage(GenericMessage globalMessage) {
		this.globalMessage = globalMessage;
	}

	public void setGlobalTitle(String globalTitle) {
		this.globalTitle = globalTitle;
	}

	public void setHomeAbsoluteURL(String homeAbsoluteURL) {
		this.homeAbsoluteURL = homeAbsoluteURL;
	}

	public void setContentLanguage(String language) {
		this.contentLanguage = language;
	}

	public void setLanguage(String navigationLanguage) {
		this.language = navigationLanguage;
	}

	public void setPageDescription(String pageDescription) {
		this.pageDescription = pageDescription;
	}

	public void setPageID(String pageID) {
		this.pageID = pageID;
	}

	public void setPageMetaDescription(String pageMetaDescription) {
		this.pageMetaDescription = pageMetaDescription;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public void setParentPageName(String parentPageName) {
		this.parentPageName = parentPageName;
	}

	public void setParentPageTitle(String parentPageTitle) {
		this.parentPageTitle = parentPageTitle;
	}

	public void setParentPageURL(String parentPageURL) {
		this.parentPageURL = parentPageURL;
	}

	public void setTemplateFolder(String templateFolder) {
		this.templateFolder = templateFolder;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPageSubTitle() {
		return pageSubTitle;
	}

	public void setPageSubTitle(String pageSubTitle) {
		this.pageSubTitle = pageSubTitle;
	}

	public String getAbsoluteTemplateFolder() {
		return absoluteTemplateFolder;
	}

	public void setAbsoluteTemplateFolder(String absoluteTemplateFolder) {
		this.absoluteTemplateFolder = absoluteTemplateFolder;
	}

	public String getPageCategory() {
		return pageCategory;
	}

	public void setPageCategory(String pageCategory) {
		this.pageCategory = pageCategory;
	}

	public MenuElement.PageBean getPage() {
		return page;
	}

	public void setPage(MenuElement.PageBean page) {
		this.page = page;
	}

	public MenuElement.PageBean getRoot() {
		return root;
	}

	public void setRoot(MenuElement.PageBean root) {
		this.root = root;
	}
	public List<MenuElement.PageBean> getPagePath() {
		return pagePath;
	}
	public String getVersion() {
		return AccessServlet.VERSION;
	}

	public Collection<String> getContentLanguages() {
		return contentLanguages;
	}
	
	public Collection<String> getLanguages() {
		return languages;
	}
	
	private void setLanguages(Set<String> inLanguages) {
		this.languages = inLanguages;		
	}

	public void setContentLanguages(Collection<String> contentLanguages) {
		this.contentLanguages = contentLanguages;
	}

	public String getEditTemplateURL() {
		return editTemplateURL;
	}

	public void setEditTemplateURL(String editTemplateURL) {
		this.editTemplateURL = editTemplateURL;
	}

	public String getStaticRootURL() {
		return staticRootURL;
	}

	public void setStaticRootURL(String staticROOTURL) {
		this.staticRootURL = staticROOTURL;
	}
}
