package org.javlo.module.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.MetaTitle;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IMacro;
import org.javlo.macro.core.MacroFactory;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.ContentService;
import org.javlo.service.LogService;
import org.javlo.service.RequestService;
import org.javlo.service.shared.SharedContentService;
import org.javlo.template.Template;
import org.javlo.template.Template.TemplateData;
import org.javlo.template.TemplateFactory;
import org.javlo.template.TemplatePlugin;
import org.javlo.template.TemplatePluginFactory;
import org.javlo.tracking.Tracker;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.exception.JavloSecurityException;
import org.javlo.ztatic.FileCache;

public class AdminAction extends AbstractModuleAction {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminAction.class.getName());

	public static class GlobalContextBean {

		public static final class SortOnKey implements Comparator<GlobalContextBean> {
			@Override
			public int compare(GlobalContextBean o1, GlobalContextBean o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		}

		private String key;
		private String administrator;
		private String aliasOf;
		private List<GlobalContextBean> alias = new LinkedList<AdminAction.GlobalContextBean>();
		private String creationDate;
		private String latestLoginDate;
		private String defaultTemplate;
		private String globalTitle;
		private String defaultLanguage;
		private String defaultLanguages;
		private String languages;
		private String contentLanguages;
		private String size;
		private String folder;
		private String usersAccess;
		private String googleAnalyticsUACCT;
		private String tags;
		private String blockPassword;
		private String homepage;
		private String urlFactory;
		private String userRoles;
		private String adminUserRoles;
		private String proxyPathPrefix;
		private boolean autoSwitchToDefaultLanguage;
		private boolean extendMenu;
		private boolean previewMode;
		private boolean openExternalLinkAsPopup = false;
		private boolean openFileAsPopup = false;
		private boolean wizz = false;
		private boolean onlyCreatorModify = false;
		private boolean collaborativeMode = false;
		private String noPopupDomain;
		private String URIAlias;
		private boolean master = false;
		private String forcedHost = "";
		private String editTemplateMode = null;
		private String DMZServerInter = "";
		private String DMZServerIntra = "";
		private String platformType = StaticConfig.WEB_PLATFORM;

		private String shortDateFormat;
		private String mediumDateFormat;
		private String fullDateFormat;

		private String helpURL;
		private String privateHelpURL;

		private int countUser;
		private boolean view;
		private boolean edit;
		private boolean visibility;
		private boolean editability;
		private String userFactoryClassName = "";
		private String adminUserFactoryClassName = "";
		
		private String mailingSenders = "";
		private String mailingSubject = "";
		private String mailingReport = "";
		
		private String smtphost;
		private String smtpport;
		private String smtpuser;
		private String smtppassword;
		
		private boolean reversedlink;

		private TemplateData templateData = null;

		public GlobalContextBean(GlobalContext globalContext, HttpSession session) throws NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
			if (globalContext == null) {
				return;
			}
			setKey(globalContext.getContextKey());
			setFolder(globalContext.getFolder());
			setAdministrator(globalContext.getAdministrator());
			setAliasOf(globalContext.getAliasOf());
			setCountUser(globalContext.getCountUser());
			setCreationDate(StringHelper.renderSortableTime(globalContext.getCreationDate()));
			setLatestLoginDate(StringHelper.renderSortableTime(globalContext.getLatestLoginDate()));
			setView(ContentService.getInstance(globalContext).isViewNav());
			setEdit(ContentService.getInstance(globalContext).isPreviewNav());
			setVisibility(globalContext.isView());
			setWizz(globalContext.isWizz());
			setEditability(globalContext.isEditable());
			setDefaultTemplate(globalContext.getDefaultTemplate());
			setExtendMenu(globalContext.isExtendMenu());
			setPreviewMode(globalContext.isPreviewMode());
			setPlatformType(globalContext.getPlatformType());

			setShortDateFormat(globalContext.getShortDateFormat());
			setMediumDateFormat(globalContext.getMediumDateFormat());
			setFullDateFormat(globalContext.getFullDateFormat());

			setHelpURL(globalContext.getHelpURL());
			setPrivateHelpURL(globalContext.getPrivateHelpURL());

			setForcedHost(globalContext.getForcedHost());

			setEditTemplateMode(globalContext.getEditTemplateMode());

			setSize(StringHelper.renderSize(globalContext.getAccountSize()));
			setGlobalTitle(globalContext.getGlobalTitle());
			setDefaultLanguage(globalContext.getDefaultLanguage());
			setDefaultLanguages(globalContext.getDefaultLanguagesRAW());
			setLanguages(StringHelper.collectionToString(globalContext.getLanguages(), ";"));
			setAutoSwitchToDefaultLanguage(globalContext.isAutoSwitchToDefaultLanguage());
			setContentLanguages(StringHelper.collectionToString(globalContext.getContentLanguages(), ";"));
			setHomepage(globalContext.getHomePage());
			setUrlFactory(globalContext.getURLFactoryClass());

			setUserRoles(StringHelper.collectionToString(globalContext.getUserRoles(), ","));
			setAdminUserRoles(StringHelper.collectionToString(globalContext.getAdminUserRoles(), ","));

			setGoogleAnalyticsUACCT(globalContext.getGoogleAnalyticsUACCT());
			setTags(globalContext.getRAWTags());
			setBlockPassword(globalContext.getBlockPassword());

			setMaster(globalContext.isMaster());

			setOnlyCreatorModify(globalContext.isOnlyCreatorModify());
			setCollaborativeMode(globalContext.isCollaborativeMode());

			setTemplateData(globalContext.getTemplateData());

			setProxyPathPrefix(globalContext.getProxyPathPrefix());
			
			setReversedlink(globalContext.isReversedLink());
			
			setMailingSenders(globalContext.getMailingSenders());
			setMailingSubject(globalContext.getMailingSubject());
			setMailingReport(globalContext.getMailingReport());
			
			setSmtphost(globalContext.getSMTPHost());
			setSmtpport(globalContext.getSMTPPort());
			setSmtpuser(globalContext.getSMTPUser());
			setSmtppassword(globalContext.getSMTPPassword());

			Properties properties = new Properties();
			properties.putAll(globalContext.getURIAlias());
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			try {
				properties.store(outStream, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			setURIAlias(new String(outStream.toByteArray()));

			setOpenFileAsPopup(globalContext.isOpenFileAsPopup());
			setOpenExternalLinkAsPopup(globalContext.isOpenExternalLinkAsPopup());
			setNoPopupDomain(globalContext.getNoPopupDomainRAW());

			outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			List<String> usersAccess = globalContext.getUsersAccess();
			for (String userName : usersAccess) {
				out.println(userName);
			}
			out.close();
			setUsersAccess(new String(outStream.toByteArray()));

			/** user engine **/
			setUserFactoryClassName(globalContext.getUserFactoryClassName());
			setAdminUserFactoryClassName(globalContext.getAdminUserFactory(session).getClass().getName());

			/** remote **/
			if (globalContext.getDMZServerInter() != null) {
				setDMZServerInter(globalContext.getDMZServerInter().toString());
			}
			if (globalContext.getDMZServerIntra() != null) {
				setDMZServerIntra(globalContext.getDMZServerIntra().toString());
			}

		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getAdministrator() {
			return administrator;
		}

		public void setAdministrator(String administrator) {
			this.administrator = administrator;
		}

		public String getAliasOf() {
			return aliasOf;
		}

		public void setAliasOf(String aliasOf) {
			this.aliasOf = aliasOf;
		}

		public String getCreationDate() {
			return creationDate;
		}

		public void setCreationDate(String createDate) {
			this.creationDate = createDate;
		}

		public String getLatestLoginDate() {
			return latestLoginDate;
		}

		public void setLatestLoginDate(String latestLoginDate) {
			this.latestLoginDate = latestLoginDate;
		}

		public int getCountUser() {
			return countUser;
		}

		public void setCountUser(int countUser) {
			this.countUser = countUser;
		}

		public boolean isView() {
			return view;
		}

		public void setView(boolean view) {
			this.view = view;
		}

		public boolean isEdit() {
			return edit;
		}

		public void setEdit(boolean edit) {
			this.edit = edit;
		}

		public boolean isVisibility() {
			return visibility;
		}

		public void setVisibility(boolean visibility) {
			this.visibility = visibility;
		}

		public boolean isEditability() {
			return editability;
		}

		public void setEditability(boolean editability) {
			this.editability = editability;
		}

		public String getDefaultTemplate() {
			return defaultTemplate;
		}

		public void setDefaultTemplate(String defaultTemplate) {
			this.defaultTemplate = defaultTemplate;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getGlobalTitle() {
			return globalTitle;
		}

		public void setGlobalTitle(String globalTitle) {
			this.globalTitle = globalTitle;
		}

		public String getDefaultLanguage() {
			return defaultLanguage;
		}

		public void setDefaultLanguage(String defaultLanguage) {
			this.defaultLanguage = defaultLanguage;
		}

		public String getLanguages() {
			return languages;
		}

		public void setLanguages(String languages) {
			this.languages = languages;
		}

		public String getContentLanguages() {
			return contentLanguages;
		}

		public void setContentLanguages(String contentLanguages) {
			this.contentLanguages = contentLanguages;
		}

		public String getFolder() {
			return folder;
		}

		public void setFolder(String folder) {
			this.folder = folder;
		}

		public String getUserFactoryClassName() {
			return userFactoryClassName;
		}

		public void setUserFactoryClassName(String userFactoryClassName) {
			this.userFactoryClassName = userFactoryClassName;
		}

		public String getAdminUserFactoryClassName() {
			return adminUserFactoryClassName;
		}

		public void setAdminUserFactoryClassName(String adminUserFactoryClassName) {
			this.adminUserFactoryClassName = adminUserFactoryClassName;
		}

		public String getUsersAccess() {
			return usersAccess;
		}

		public void setUsersAccess(String usersAccess) {
			this.usersAccess = usersAccess;
		}

		public String getGoogleAnalyticsUACCT() {
			return googleAnalyticsUACCT;
		}

		public void setGoogleAnalyticsUACCT(String googleAnalyticsUACCT) {
			this.googleAnalyticsUACCT = googleAnalyticsUACCT;
		}

		public String getTags() {
			return tags;
		}

		public void setTags(String tags) {
			this.tags = tags;
		}

		public String getBlockPassword() {
			return blockPassword;
		}

		public void setBlockPassword(String blockPassword) {
			this.blockPassword = blockPassword;
		}

		public String getHomepage() {
			return homepage;
		}

		public void setHomepage(String homepage) {
			this.homepage = homepage;
		}

		public boolean isAutoSwitchToDefaultLanguage() {
			return autoSwitchToDefaultLanguage;
		}

		public void setAutoSwitchToDefaultLanguage(boolean autoSwitchToDefaultLanguage) {
			this.autoSwitchToDefaultLanguage = autoSwitchToDefaultLanguage;
		}

		public String getUserRoles() {
			return userRoles;
		}

		public void setUserRoles(String userRoles) {
			this.userRoles = userRoles;
		}

		public String getShortDateFormat() {
			return shortDateFormat;
		}

		public void setShortDateFormat(String shortDateFormat) {
			this.shortDateFormat = shortDateFormat;
		}

		public String getMediumDateFormat() {
			return mediumDateFormat;
		}

		public void setMediumDateFormat(String mediumDateFormat) {
			this.mediumDateFormat = mediumDateFormat;
		}

		public String getFullDateFormat() {
			return fullDateFormat;
		}

		public void setFullDateFormat(String fullDateFormat) {
			this.fullDateFormat = fullDateFormat;
		}

		public String getHelpURL() {
			return helpURL;
		}

		public void setHelpURL(String helpURL) {
			this.helpURL = helpURL;
		}

		public String getPrivateHelpURL() {
			return privateHelpURL;
		}

		public void setPrivateHelpURL(String privateHelpURL) {
			this.privateHelpURL = privateHelpURL;
		}

		public String getUrlFactory() {
			return urlFactory;
		}

		public void setUrlFactory(String urlFactory) {
			this.urlFactory = urlFactory;
		}

		public String getDefaultLanguages() {
			return defaultLanguages;
		}

		public void setDefaultLanguages(String defaultLanguages) {
			this.defaultLanguages = defaultLanguages;
		}

		public boolean isExtendMenu() {
			return extendMenu;
		}

		public void setExtendMenu(boolean extendMenu) {
			this.extendMenu = extendMenu;
		}

		public String getAdminUserRoles() {
			return adminUserRoles;
		}

		public void setAdminUserRoles(String adminUserRoles) {
			this.adminUserRoles = adminUserRoles;
		}

		public boolean isOpenExternalLinkAsPopup() {
			return openExternalLinkAsPopup;
		}

		public void setOpenExternalLinkAsPopup(boolean openLinkAsPopup) {
			this.openExternalLinkAsPopup = openLinkAsPopup;
		}

		public boolean isOpenFileAsPopup() {
			return openFileAsPopup;
		}

		public void setOpenFileAsPopup(boolean openFileAsPopup) {
			this.openFileAsPopup = openFileAsPopup;
		}

		public String getNoPopupDomain() {
			return noPopupDomain;
		}

		public void setNoPopupDomain(String noPopupDomain) {
			this.noPopupDomain = noPopupDomain;
		}

		public boolean isPreviewMode() {
			return previewMode;
		}

		public void setPreviewMode(boolean previewMode) {
			this.previewMode = previewMode;
		}

		public String getURIAlias() {
			return URIAlias;
		}

		public void setURIAlias(String uRIAlias) {
			URIAlias = uRIAlias;
		}

		public boolean isMaster() {
			return master;
		}

		public void setMaster(boolean master) {
			this.master = master;
		}

		public boolean isWizz() {
			return wizz;
		}

		public void setWizz(boolean wizz) {
			this.wizz = wizz;
		}

		public boolean isOnlyCreatorModify() {
			return onlyCreatorModify;
		}

		public void setOnlyCreatorModify(boolean onlyCreatorModify) {
			this.onlyCreatorModify = onlyCreatorModify;
		}

		public boolean isCollaborativeMode() {
			return collaborativeMode;
		}

		public void setCollaborativeMode(boolean displayCreator) {
			this.collaborativeMode = displayCreator;
		}

		public String getForcedHost() {
			return forcedHost;
		}

		public void setForcedHost(String forcedHost) {
			this.forcedHost = forcedHost;
		}

		public List<GlobalContextBean> getAlias() {
			return alias;
		}

		public void addAlias(GlobalContextBean context) {
			alias.add(context);
		}

		public String getEditTemplateMode() {
			return editTemplateMode;
		}

		public void setEditTemplateMode(String editTemplateMode) {
			this.editTemplateMode = editTemplateMode;
		}

		public String getDMZServerInter() {
			return DMZServerInter;
		}

		public void setDMZServerInter(String dMZServerInter) {
			DMZServerInter = dMZServerInter;
		}

		public String getDMZServerIntra() {
			return DMZServerIntra;
		}

		public void setDMZServerIntra(String dMZServerIntra) {
			DMZServerIntra = dMZServerIntra;
		}

		public TemplateData getTemplateData() {
			return templateData;
		}

		public void setTemplateData(TemplateData templateData) {
			this.templateData = templateData;
		}

		public String getProxyPathPrefix() {
			return proxyPathPrefix;
		}

		public void setProxyPathPrefix(String proxyPathPrefix) {
			this.proxyPathPrefix = proxyPathPrefix;
		}

		public String getPlatformType() {
			return platformType;
		}

		public void setPlatformType(String platform) {
			this.platformType = platform;
		}

		public boolean isReversedlink() {
			return reversedlink;
		}

		public void setReversedlink(boolean reversedlink) {
			this.reversedlink = reversedlink;
		}

		public String getMailingSenders() {
			return mailingSenders;
		}

		public void setMailingSenders(String mailingSenders) {
			this.mailingSenders = mailingSenders;
		}

		public String getMailingSubject() {
			return mailingSubject;
		}

		public void setMailingSubject(String mailingSubject) {
			this.mailingSubject = mailingSubject;
		}

		public String getMailingReport() {
			return mailingReport;
		}

		public void setMailingReport(String mailingReport) {
			this.mailingReport = mailingReport;
		}

		public String getSmtphost() {
			return smtphost;
		}

		public void setSmtphost(String smtphost) {
			this.smtphost = smtphost;
		}

		public String getSmtpport() {
			return smtpport;
		}

		public void setSmtpport(String smtpport) {
			this.smtpport = smtpport;
		}

		public String getSmtpuser() {
			return smtpuser;
		}

		public void setSmtpuser(String smtpuser) {
			this.smtpuser = smtpuser;
		}

		public String getSmtppassword() {
			return smtppassword;
		}

		public void setSmtppassword(String smtppassword) {
			this.smtppassword = smtppassword;
		}

	}

	public static class ComponentBean {

		ContentContext ctx;
		IContentVisualComponent comp;

		public ComponentBean(ContentContext ctx, IContentVisualComponent comp) {
			this.ctx = ctx;
			this.comp = comp;
		}

		public IContentVisualComponent getComponent() {
			return comp;
		}

		public int getComplexityLevel() {
			return comp.getComplexityLevel(ctx);
		}

		public boolean isListable() {
			return comp.isListable();
		}

		public boolean isCacheable() {
			return comp.isContentCachable(ctx);
		}

		public String getType() {
			return comp.getType();
		}

		public String getHexColor() {
			return comp.getHexColor();
		}
	}

	@Override
	public String getActionGroupName() {
		return "admin";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {
		
		if (ctx.getCurrentEditUser() == null) {
			return null;
		}

		super.prepare(ctx, moduleContext);

		HttpServletRequest request = ctx.getRequest();
		ServletContext application = request.getSession().getServletContext();

		ContentContext viewCtx = new ContentContext(ctx);
		Module currentModule = moduleContext.getCurrentModule();

		String msg = "";
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);

		/*** current context ***/

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		currentModule.restoreToolsRenderer();
		if (!globalContext.isMaster()) {
			User user = ctx.getCurrentEditUser();
			if (user.getUserInfo().getToken() != null && user.getUserInfo().getToken().length() > 1) {
				Map<String, String> params = new HashMap<String, String>();
				params.put("j_token", user.getUserInfo().getToken());
				params.put("webaction", "mobile.init");
				ContentContext absCtx = ctx.getContextForAbsoluteURL();
				absCtx.setRenderMode(ContentContext.VIEW_MODE);
				String editAutoURL = URLHelper.createAjaxURL(absCtx, "/", params);				
				String qrcodeImg = URLHelper.createQRCodeLink(ctx, editAutoURL);
				ctx.getRequest().setAttribute("editAutoURL", editAutoURL);
				ctx.getRequest().setAttribute("qrcode", qrcodeImg);
			}
			if (request.getAttribute("componentsPreview") == null) {
				editGlobalContext(ctx, currentModule, globalContext);
			} else {
				currentModule.setToolsRenderer(null);
			}
			currentModule.setBreadcrumb(false);
		} else {
			AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();

			/* breadcrumb */
			if (currentModule.getBreadcrumbList() == null || currentModule.getBreadcrumbList().size() == 0) {
				currentModule.pushBreadcrumb(new Module.HtmlLink(URLHelper.createURL(ctx), I18nAccess.getInstance(request).getText("global.home"), ""));
			}

			Collection<GlobalContextBean> ctxAllBean = new LinkedList<GlobalContextBean>();
			Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(request.getSession().getServletContext());
			Map<String, GlobalContextBean> masterCtx = new HashMap<String, AdminAction.GlobalContextBean>();
			for (GlobalContext context : allContext) {
				if (ctx.getCurrentEditUser() != null) {
					if (adminUserSecurity.isAdmin(ctx.getCurrentEditUser()) || context.getUsersAccess().contains(ctx.getCurrentEditUser().getLogin())) {
						GlobalContextBean contextBean = new GlobalContextBean(context, ctx.getRequest().getSession());
						ctxAllBean.add(contextBean);
						if (context.getAliasOf() == null || context.getAliasOf().length() == 0) {
							masterCtx.put(context.getContextKey(), contextBean);
						}
					}
				}
			}
			for (GlobalContextBean context : ctxAllBean) {
				if (!masterCtx.containsKey(context.getKey()) && masterCtx.containsKey(context.getAliasOf())) {
					masterCtx.get(context.getAliasOf()).addAlias(context);
				}
			}

			List<GlobalContextBean> sortedContext = new LinkedList<AdminAction.GlobalContextBean>(masterCtx.values());
			Collections.sort(sortedContext, new GlobalContextBean.SortOnKey());
			request.setAttribute("contextList", sortedContext);
		}

		String currentContextValue = null;
		if (globalContext.isMaster()) {
			currentContextValue = request.getParameter("context");
		}
		if (currentContextValue != null || request.getAttribute("prepareContext") != null || !globalContext.isMaster()) {
			GlobalContext currentGlobalContext;
			if (currentContextValue != null) {
				request.setAttribute("context", currentContextValue);
				currentGlobalContext = GlobalContext.getRealInstance(request.getSession(), currentContextValue);
			} else if (globalContext.isMaster()) {
				currentGlobalContext = (GlobalContext) request.getAttribute("prepareContext");
				request.setAttribute("context", currentGlobalContext.getContextKey());
			} else {
				currentGlobalContext = globalContext;
				request.setAttribute("context", currentGlobalContext.getContextKey());
			}
			request.setAttribute("currentContext", new GlobalContextBean(currentGlobalContext, request.getSession()));
			if (currentGlobalContext != null) {
				List<Template> templates = TemplateFactory.getAllTemplates(request.getSession().getServletContext());
				Collections.sort(templates);

				Template defaultTemplate = TemplateFactory.getTemplates(request.getSession().getServletContext()).get(currentGlobalContext.getDefaultTemplate());

				if (defaultTemplate != null) {
					try {
						if (!defaultTemplate.isTemplateInWebapp(viewCtx)) {
							defaultTemplate.importTemplateInWebapp(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()), ctx);
						}
						String templateImageURL = URLHelper.createTransformStaticTemplateURL(ctx, defaultTemplate, "template", defaultTemplate.getVisualFile());
						request.setAttribute("templateImageUrl", templateImageURL);
					} catch (Exception e) {
						e.printStackTrace();
						MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(e.getMessage(), GenericMessage.ERROR));
					}
				}
				/*** component list ***/
				List<String> currentComponents = null;
				currentComponents = currentGlobalContext.getComponents();
				IContentVisualComponent[] componentsType = ComponentFactory.getComponents(currentGlobalContext);
				Collection<ComponentBean> components = new LinkedList<ComponentBean>();
				for (int i = 0; i < componentsType.length; i++) {
					if (!componentsType[i].isHidden(ctx) && !(componentsType[i] instanceof MetaTitle)) {
						components.add(new ComponentBean(ctx, componentsType[i]));
					}
				}
				request.setAttribute("components", components);
				request.setAttribute("currentComponents", currentComponents);

				request.setAttribute("allModules", moduleContext.getAllModules());
				request.setAttribute("currentModules", currentGlobalContext.getModules());

				List<String> templatesName = currentGlobalContext.getTemplatesNames();
				List<Template.TemplateBean> selectedTemplate = new LinkedList<Template.TemplateBean>();
				for (String name : templatesName) {
					Template template = TemplateFactory.getDiskTemplate(request.getSession().getServletContext(), name);
					if (template != null) {
						if (!template.isTemplateInWebapp(ctx)) {
							template.importTemplateInWebapp(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()), ctx);
						}
						selectedTemplate.add(new Template.TemplateBean(ctx, template));
					} else {
						currentGlobalContext.removeTemplate(name);
						logger.warning("template not found : " + name);
					}
				}
				request.setAttribute("templates", selectedTemplate);

				Map<String, String> params = LangHelper.objStr(LangHelper.entry("webaction", "changeRenderer"), LangHelper.entry("list", "allmtemplates"));

				request.setAttribute("linkUrl", URLHelper.createInterModuleURL(ctx, ctx.getPath(), "template", params));

				params = new HashMap<String, String>();
				params.put("webaction", "admin.selectTemplate");
				params.put("context", currentGlobalContext.getContextKey());
				String backUrl = URLHelper.createModuleURL(ctx, ctx.getPath(), currentModule.getName(), params);
				currentModule.setBackUrl(backUrl);

				/** macro **/
				MacroFactory macroFactory = MacroFactory.getInstance(currentGlobalContext.getStaticConfig());
				Collection<String> macrosName = new LinkedList<String>();
				for (IMacro macro : macroFactory.getMacros()) {
					macrosName.add(macro.getName());
				}
				request.setAttribute("macros", macrosName);
				Map<String, String> selectedMacros = new HashMap<String, String>();
				for (String selected : currentGlobalContext.getMacros()) {
					selectedMacros.put(selected, StringHelper.SOMETHING);
				}
				request.setAttribute("selectedMacros", selectedMacros);

				/** template plugin **/
				ctx.getRequest().setAttribute("templatePlugins", TemplatePluginFactory.getInstance(application).getAllTemplatePlugin());

				Map<String, String> selectedPlugin = new HashMap<String, String>();
				for (String selected : currentGlobalContext.getTemplatePlugin()) {
					selectedPlugin.put(selected, StringHelper.SOMETHING);
				}

				ctx.getRequest().setAttribute("selectedTemplatePlugins", selectedPlugin);
				ctx.getRequest().setAttribute("templatePluginConfig", currentGlobalContext.getTemplatePluginConfig());

				if (currentGlobalContext.getTemplateData().getLogo() != null && currentGlobalContext.getTemplateData().getLogo().trim().length() > 0) {
					ContentContext absoluteURLCtx = new ContentContext(ctx);
					absoluteURLCtx.setAbsoluteURL(true);
					String newLogoURL;
					try {
						newLogoURL = URLHelper.createTransformURL(absoluteURLCtx, null, URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), currentGlobalContext.getTemplateData().getLogo()), "logo", null);
						ctx.getRequest().setAttribute("logoPreview", newLogoURL);
					} catch (Exception e) {
						throw new IOException(e);
					}
				}

			} else {
				msg = "bad context : " + currentContextValue;
				currentModule.restoreRenderer();
				currentModule.restoreToolsRenderer();
			}
		} else if (request.getAttribute("config_content") != null) {
			currentModule.setRenderer("/jsp/config.jsp");
			// currentModule.setToolsRenderer(null);
		} else {
			currentModule.restoreRenderer();
			currentModule.restoreToolsRenderer();
			currentModule.clearBreadcrump();
			currentModule.pushBreadcrumb(new Module.HtmlLink(URLHelper.createURL(ctx), I18nAccess.getInstance(request).getText("global.home"), ""));
		}

		return msg;
	}

	public static final void editGlobalContext(ContentContext ctx, Module currentModule, GlobalContext globalContext) throws FileNotFoundException, IOException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (globalContext != null) {
			ctx.getRequest().setAttribute("prepareContext", globalContext);
		}
		currentModule.setRenderer("/jsp/site_properties.jsp");
		// currentModule.setToolsRenderer(null);
		currentModule.pushBreadcrumb(new Module.HtmlLink(null, I18nAccess.getInstance(ctx.getRequest()).getText("global.change") + " : " + ctx.getRequest().getParameter("context"), ""));
	}

	public static final String performChangeSite(HttpServletRequest request, RequestService requestService, ContentContext ctx, Module currentModule) throws FileNotFoundException, IOException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (requestService.getParameter("change", null) != null) {
			editGlobalContext(ctx, currentModule, null);
		} else if (requestService.getParameter("components", null) != null) {
			currentModule.setRenderer("/jsp/components.jsp");
			currentModule.setToolsRenderer("/jsp/components_actions.jsp");
			String uri = request.getRequestURI();
			currentModule.pushBreadcrumb(new Module.HtmlLink(uri, I18nAccess.getInstance(request).getText("command.admin.components") + " : " + request.getParameter("context"), ""));
		} else if (requestService.getParameter("modules", null) != null) {
			currentModule.setRenderer("/jsp/modules.jsp");
			currentModule.setToolsRenderer(null);
			String uri = request.getRequestURI();
			currentModule.pushBreadcrumb(new Module.HtmlLink(uri, I18nAccess.getInstance(request).getText("command.admin.modules") + " : " + request.getParameter("context"), ""));
		}
		return null;
	}
	
	public static final String performPreviewEditComponent(HttpServletRequest request, RequestService requestService, ContentContext ctx, Module currentModule) throws FileNotFoundException, IOException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		request.setAttribute("componentsPreview", true);
		currentModule.setRenderer("/jsp/components.jsp");
		currentModule.setToolsRenderer("/jsp/components_actions.jsp");
		return null;
	}
	

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		/*
		 * AdminUserSecurity adminUserSecurity =
		 * AdminUserSecurity.getInstance(); if (adminUserSecurity.isAdmin(user))
		 * { return true; }
		 */

		if (user == null) {
			return false;
		}

		try {
			/*
			 * Collection<GlobalContext> allContext =
			 * GlobalContextFactory.getAllGlobalContext
			 * (session.getServletContext()); for (GlobalContext globalContext :
			 * allContext) { if
			 * (globalContext.getUsersAccess().contains(user.getLogin())) {
			 * return true; } }
			 */
		} catch (Exception e) {
			throw new ModuleException(e.getMessage());
		}
		return null;
	}

	public static void checkRight(ContentContext ctx, GlobalContext globalContext) throws org.javlo.user.exception.JavloSecurityException {
		User user = ctx.getCurrentEditUser();
		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
		if (adminUserSecurity.isAdmin(user)) {
			return;
		}
		if (!globalContext.getUsersAccess().contains(user.getLogin())) {
			throw new org.javlo.user.exception.JavloSecurityException("You have no sufisant right.");
		}
	}

	public static String performUpdateGlobalContext(RequestService requestService, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule) throws Exception {
		String msg = null;
		if (requestService.getParameter("back", null) != null) {
			currentModule.restoreRenderer();
			currentModule.restoreToolsRenderer();
			currentModule.clearBreadcrump();
			currentModule.pushBreadcrumb(new Module.HtmlLink(URLHelper.createURL(ctx), i18nAccess.getText("global.home"), ""));
		} else {
			String currentContextKey = requestService.getParameter("context", null);
			if (currentContextKey != null) {

				GlobalContext currentGlobalContext;
				currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest().getSession(), currentContextKey);

				if (currentGlobalContext != null) {
					checkRight(ctx, currentGlobalContext);
					currentGlobalContext.setGlobalTitle(requestService.getParameter("global-title", null));
					currentGlobalContext.setAliasOf(requestService.getParameter("alias", "").trim());
					currentGlobalContext.setDefaultTemplate(requestService.getParameter("default-template", null));
					currentGlobalContext.setRAWLanguages(requestService.getParameter("languages", null));
					currentGlobalContext.setRAWContentLanguages(requestService.getParameter("content-languages", null));
					currentGlobalContext.setAutoSwitchToDefaultLanguage(requestService.getParameter("switch-default-language", null) != null);
					currentGlobalContext.setRAWTags(requestService.getParameter("tags", null));
					currentGlobalContext.setAdministrator(requestService.getParameter("administrator", ""));
					currentGlobalContext.setHomePage(requestService.getParameter("homepage", ""));
					currentGlobalContext.setOnlyCreatorModify(StringHelper.isTrue(requestService.getParameter("only-creator-modify", null)));
					currentGlobalContext.setCollaborativeMode((StringHelper.isTrue(requestService.getParameter("collaborative-mode", null))));
					currentGlobalContext.setPlatformType(requestService.getParameter("platform", StaticConfig.MAILING_PLATFORM));
					currentGlobalContext.setReversedLink(requestService.getParameter("reversedlink", null) != null);
					currentGlobalContext.setContentIntegrity(requestService.getParameter("integrity", ""));
					try {
						currentGlobalContext.setURLFactory(requestService.getParameter("urlfactory", ""));
					} catch (Exception e1) {
						messageRepository.setGlobalMessage(new GenericMessage(e1.getMessage(), GenericMessage.ERROR));
						e1.printStackTrace();
					}

					String uriAlias = requestService.getParameter("uri-alias", null);
					if (uriAlias != null) {
						Properties properties = new Properties();
						InputStream in = new ByteArrayInputStream(uriAlias.getBytes());
						properties.load(in);
						in.close();
						currentGlobalContext.setAliasURI(properties);
					} else {
						return "uri-alias parameter not found.";
					}

					String forcedHost = requestService.getParameter("forced-host", "");
					if (forcedHost.trim().length() > 0) {
						currentGlobalContext.setForcedHost(forcedHost);
					} else {
						currentGlobalContext.setForcedHost("");
					}

					currentGlobalContext.setUserRoles(new HashSet<String>(StringHelper.stringToCollection(requestService.getParameter("user-roles", ""), ",")));
					currentGlobalContext.setAdminUserRoles(new HashSet<String>(StringHelper.stringToCollection(requestService.getParameter("admin-user-roles", ""), ",")));

					currentGlobalContext.setHelpURL(requestService.getParameter("help-url", ""));
					currentGlobalContext.setPrivateHelpURL(requestService.getParameter("private-help-url", ""));

					currentGlobalContext.setOpenExernalLinkAsPopup(requestService.getParameter("link-as-popup", null) != null);
					currentGlobalContext.setOpenFileAsPopup(requestService.getParameter("file-as-popup", null) != null);
					currentGlobalContext.setNoPopupDomainRAW(requestService.getParameter("nopup-domain", ""));

					currentGlobalContext.setPreviewMode(requestService.getParameter("preview-mode", null) != null);

					currentGlobalContext.setEditTemplateMode(requestService.getParameter("template-mode", null));

					currentGlobalContext.setWizz(requestService.getParameter("wizz", null) != null);

					currentGlobalContext.setDMZServerInter(requestService.getParameter("dmz-inter", ""));
					currentGlobalContext.setDMZServerIntra(requestService.getParameter("dmz-intra", ""));
					currentGlobalContext.setProxyPathPrefix(requestService.getParameter("proxy-prefix", ""));
					
					currentGlobalContext.setMailingSenders(requestService.getParameter("mailing-senders", ""));
					currentGlobalContext.setMailingSubject(requestService.getParameter("mailing-subject", ""));
					currentGlobalContext.setMailingReport(requestService.getParameter("mailing-report", ""));
					
					/** SMTP **/
					currentGlobalContext.setSMTPHost(requestService.getParameter("mailing-smtphost",null));
					currentGlobalContext.setSMTPPort(requestService.getParameter("mailing-smtpport",null));
					currentGlobalContext.setSMTPUser(requestService.getParameter("mailing-smtpuser",null));
					String pwd = requestService.getParameter("mailing-smtppassword","");
					if (pwd.length() > 0) {					
						currentGlobalContext.setSMTPPassword(pwd);
					}
					if (!StringHelper.isEmpty(currentGlobalContext.getSMTPHost())) {
						Transport t = null;
						try {
							t = MailService.getMailTransport(new MailConfig(currentGlobalContext, null, null));
							logger.info("smtp:"+currentGlobalContext.getSMTPHost()+" connected ? "+t.isConnected());							
						} catch (MessagingException e) {
							messageRepository.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
							e.printStackTrace();
						} finally {
							if (t != null) {
								try {
									t.close();
								} catch (MessagingException e) {
									e.printStackTrace();
								}
							}
						}
					}
					

					String dateFormat = requestService.getParameter("short-date", null);
					if (dateFormat != null) {
						try {
							new SimpleDateFormat(dateFormat);
							currentGlobalContext.setShortDateFormat(dateFormat);
						} catch (Exception e) {
							messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.bad-date-format") + dateFormat, GenericMessage.ERROR));
						}
					}

					dateFormat = requestService.getParameter("medium-date", null);
					if (dateFormat != null) {
						try {
							new SimpleDateFormat(dateFormat);
							currentGlobalContext.setMediumDateFormat(dateFormat);
						} catch (Exception e) {
							messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.bad-date-format") + dateFormat, GenericMessage.ERROR));
						}
					}

					dateFormat = requestService.getParameter("full-date", null);
					if (dateFormat != null) {
						try {
							new SimpleDateFormat(dateFormat);
							currentGlobalContext.setFullDateFormat(dateFormat);
						} catch (Exception e) {
							messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.bad-date-format") + dateFormat, GenericMessage.ERROR));
						}
					}

					String usersAccess = requestService.getParameter("users-access", "");
					if (usersAccess.trim().length() > 0) {
						currentGlobalContext.setUsersAccess(StringHelper.textToList(usersAccess));
					}

					String defaultLanguage = requestService.getParameter("default-languages", null);
					currentGlobalContext.setDefaultLanguages(defaultLanguage);

					currentGlobalContext.setExtendMenu(requestService.getParameter("extend-menu", null) != null);

					currentGlobalContext.setGoogleAnalyticsUACCT(requestService.getParameter("google-ana", ""));

					/** security **/
					String userFacotryClass = requestService.getParameter("user-factory", null);
					try {
						Class.forName(userFacotryClass).newInstance();
						currentGlobalContext.setUserFactoryClassName(userFacotryClass);
					} catch (Exception e) {
						e.printStackTrace();
						messageRepository.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
					}

					userFacotryClass = requestService.getParameter("admin-user-factory", null);
					try {
						Class.forName(userFacotryClass).newInstance();
						currentGlobalContext.setAdminUserFactoryClassName(userFacotryClass);
					} catch (Exception e) {
						e.printStackTrace();
						messageRepository.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
					}

					currentGlobalContext.setBlockPassword(requestService.getParameter("block-password", ""));

					/** macro **/
					MacroFactory macroFactory = MacroFactory.getInstance(currentGlobalContext.getStaticConfig());
					List<String> macros = new LinkedList<String>();
					for (IMacro macro : macroFactory.getMacros()) {
						if (requestService.getParameter(macro.getName(), null) != null) {
							macros.add(macro.getName());
						}
					}
					currentGlobalContext.setMacros(macros);

					/** template plugin **/
					String templatePluginConfig = requestService.getParameter("template-plugin-config", "");
					boolean importTemplate = false;
					if (!templatePluginConfig.equals(currentGlobalContext.getTemplatePluginConfig())) {
						currentGlobalContext.setTemplatePluginConfig(templatePluginConfig);
						importTemplate = true;
					}

					Collection<TemplatePlugin> templatePlugins = TemplatePluginFactory.getInstance(ctx.getRequest().getSession().getServletContext()).getAllTemplatePlugin();
					Collection<String> templatePluginsSelection = new LinkedList<String>();
					for (TemplatePlugin templatePlugin : templatePlugins) {
						if (requestService.getParameter(templatePlugin.getId(), null) != null) {
							templatePluginsSelection.add(templatePlugin.getId());
						}
					}
					if (!currentGlobalContext.getTemplatePlugin().equals(templatePluginsSelection)) {
						currentGlobalContext.setTemplatePlugin(templatePluginsSelection);
						importTemplate = true;
					}

					/** template data **/
					TemplateData td = currentGlobalContext.getTemplateData();
					td.setBackground(StringHelper.parseColor(requestService.getParameter("background", "" + td.getBackground())));
					td.setBackgroundActive(StringHelper.parseColor(requestService.getParameter("backgroundActive", "" + td.getBackgroundActive())));
					td.setForeground(StringHelper.parseColor(requestService.getParameter("foreground", "" + td.getForeground())));
					td.setBorder(StringHelper.parseColor(requestService.getParameter("border", "" + td.getBorder())));
					td.setBackgroundMenu(StringHelper.parseColor(requestService.getParameter("backgroundMenu", "" + td.getBackgroundMenu())));
					td.setText(StringHelper.parseColor(requestService.getParameter("text", "" + td.getText())));
					td.setTextMenu(StringHelper.parseColor(requestService.getParameter("textMenu", "" + td.getTextMenu())));
					td.setLink(StringHelper.parseColor(requestService.getParameter("link", "" + td.getLink())));
					td.setTitle(StringHelper.parseColor(requestService.getParameter("title", "" + td.getTitle())));
					td.setSpecial(StringHelper.parseColor(requestService.getParameter("special", "" + td.getSpecial())));
					td.setFont(requestService.getParameter("font", "" + td.getFont()));
					
					MailService.resetInstance();
					
					for (FileItem file : requestService.getAllFileItem()) {
						if (file.getFieldName().equals("logo")) {							
							File oldLogo = null;
							if (td.getLink() != null) {
								oldLogo = new File(URLHelper.mergePath(currentGlobalContext.getStaticFolder(), td.getLogo()));
							}
							if (file.getName().trim().length() > 0) {
								String logoPath = URLHelper.mergePath("logo", file.getName());
								File logo = new File(URLHelper.mergePath(currentGlobalContext.getStaticFolder(), logoPath));
								td.setLogo(logoPath);
								ResourceHelper.writeStreamToFile(file.getInputStream(), logo);								
								if (oldLogo != null && oldLogo.exists() && !oldLogo.getName().equals(file.getName())) {
									oldLogo.delete();
								}
							}
						}
					}

					currentGlobalContext.setTemplateData(td);

					if (importTemplate) {
						TemplateFactory.cleanRenderer(ctx, currentGlobalContext.getTemplatesNames(), true);
					}
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.context-updated"), GenericMessage.INFO));
				} else {
					msg = "context not found : " + currentContextKey;
				}
			}
		}
		return msg;
	}

	public static final String performReleaseContent(HttpServletRequest request, ContentContext ctx, RequestService requestService, GlobalContext globalContext, EditContext editCtx, MessageRepository messageRepository, I18nAccess i18nAccess, AdminUserSecurity security) throws Exception {
		boolean view = requestService.getParameter("view", null) != null;
		String msg = null;
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.security.noright-onpage"), GenericMessage.ERROR));
		} else {
			String contextName = requestService.getParameter("context", null);
			if (contextName != null) {
				GlobalContext currentGlobalContext = GlobalContext.getRealInstance(request.getSession(), contextName);
				checkRight(ctx, currentGlobalContext);
				if (globalContext != null) {
					ContentService content = ContentService.getInstance(currentGlobalContext);
					if (view) {
						content.releaseViewNav(ctx, currentGlobalContext);
						globalContext.releaseAllCache();
						messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.release-view"), GenericMessage.INFO));
					} else {
						content.setPreviewNav(null);
						messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.release-preview"), GenericMessage.INFO));
					}
				} else {
					msg = "context not found : " + contextName;
				}
			} else {
				msg = "bad request structure : no context.";
			}
		}
		return msg;
	}

	public static final String performComponentsSelect(HttpServletRequest request, ContentContext ctx, User user, RequestService requestService, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule) throws Exception {
		
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "no suffisant right!";
		}
		
		String msg = null;
		if (requestService.getParameter("back", null) != null) {
			currentModule.restoreAll();
		} else {
			String currentContextKey = requestService.getParameter("context", null);
			if (currentContextKey != null) {
				GlobalContext currentGlobalContext = GlobalContext.getRealInstance(request.getSession(), currentContextKey);
				if (currentGlobalContext != null) {
					checkRight(ctx, currentGlobalContext);
					IContentVisualComponent[] componentsType = ComponentFactory.getComponents(currentGlobalContext);
					List<String> components = new LinkedList<String>();
					for (IContentVisualComponent comp : componentsType) {
						if (requestService.getParameter(comp.getClassName(), null) != null) {
							components.add(comp.getClassName());
						}

					}
					currentGlobalContext.setComponents(components);

					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.components-selected"), GenericMessage.INFO));

				} else {
					msg = "context not found : " + currentContextKey;
				}
			} else {
				msg = "bad request structure need 'context' as parameter.";
			}
			if (ctx.isEditPreview()) {
				ctx.setClosePopup(true);
			}
		}
		return msg;
	}

	public static final String performModulesSelect(HttpServletRequest request, ContentContext ctx, RequestService requestService, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule, ModulesContext moduleContext) throws Exception {
		String msg = null;
		if (requestService.getParameter("back", null) != null) {
			currentModule.restoreAll();
		} else {
			String currentContextKey = requestService.getParameter("context", null);
			if (currentContextKey != null) {
				GlobalContext currentGlobalContext = GlobalContext.getRealInstance(request.getSession(), currentContextKey);
				if (currentGlobalContext != null) {
					checkRight(ctx, currentGlobalContext);
					List<String> modules = new LinkedList<String>();
					for (Module mod : moduleContext.getAllModules()) {
						if (requestService.getParameter(mod.getName(), null) != null) {
							modules.add(mod.getName());
						}
					}
					currentGlobalContext.setModules(modules);
					moduleContext.loadModule(request.getSession(), GlobalContext.getInstance(request));

					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.modules-selected"), GenericMessage.INFO));

				} else {
					msg = "context not found : " + currentContextKey;
				}
			} else {
				msg = "bad request structure need 'context' as parameter.";
			}
		}
		return msg;
	}

	public static final String performClearCache(HttpServletRequest request, GlobalContext globalContext, HttpSession session, User user, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "security error !";
		}
		globalContext.clearTransformShortURL();
		globalContext.resetURLFactory();
		globalContext.storeRedirectUrlList();
		String currentContextKey = request.getParameter("context");
		if (currentContextKey == null) { // param context is used only for check
											// the type of call, but you can
											// clear only current context
			ContentService.clearAllContextCache(ctx);
		} else {
			if (!AdminUserSecurity.getInstance().isMaster(user) && !AdminUserSecurity.getInstance().isGod(user)) {
				return "security error !";
			}
			ContentService.clearCache(ctx, globalContext);
		}
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.clear cache"), GenericMessage.INFO));
		Tracker.getTracker(globalContext, session);
		LogService.getInstance(session).clear();

		AdminUserFactory.createUserFactory(globalContext, session).reload(globalContext, session);
		UserFactory.createUserFactory(globalContext, session).reload(globalContext, session);

		TemplateFactory.copyDefaultTemplate(session.getServletContext());
		SharedContentService.getInstance(ctx).clearCache(ctx);
		System.gc();
		return null;
	}

	public static String performClearimagecache(HttpServletRequest request, GlobalContext globalContext, HttpSession session, User user, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, FileCache fileCache) {
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "security error !";
		}
		String currentContextKey = request.getParameter("context");
		if (currentContextKey == null) { // param context is used only for check
											// the type of call, but you can
											// clear only current context
			fileCache.clear();
		} else {
			if (!AdminUserSecurity.getInstance().isMaster(user) && !AdminUserSecurity.getInstance().isGod(user)) {
				return "security error !";
			}
			fileCache.clear(globalContext.getContextKey());
		}
		return null;
	}

	public static final String performSelectTemplate(ContentContext ctx, RequestService requestService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String msg = null;
		String currentContextKey = requestService.getParameter("context", null);
		if (currentContextKey != null) {
			GlobalContext currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest().getSession(), currentContextKey);
			if (currentGlobalContext != null) {
				checkRight(ctx, currentGlobalContext);
				String templateName = requestService.getParameter("template", null);
				if (templateName != null) {
					currentGlobalContext.addTemplate(templateName);
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.linked-template") + ' ' + templateName, GenericMessage.INFO));
				} else {
					return "bad request structure : need 'template' as parameter";
				}
			} else {
				msg = "context not found : " + currentContextKey;
			}
		} else {
			msg = "bad request structure need 'context' as parameter.";
		}
		return msg;
	}

	public static final String performUnlinkTemplate(ContentContext ctx, RequestService requestService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String msg = null;
		String currentContextKey = requestService.getParameter("context", null);
		if (currentContextKey != null) {
			GlobalContext currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest().getSession(), currentContextKey);
			if (currentGlobalContext != null) {
				checkRight(ctx, currentGlobalContext);
				String templateName = requestService.getParameter("template", null);
				if (templateName != null) {
					currentGlobalContext.removeTemplate(templateName);
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.unlinked-template") + ' ' + templateName, GenericMessage.INFO));
				} else {
					return "bad request structure : need 'template' as parameter";
				}
			} else {
				msg = "context not found : " + currentContextKey;
			}
		} else {
			msg = "bad request structure need 'context' as parameter.";
		}
		return msg;
	}

	public static String performCreateSite(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule) throws ConfigurationException, IOException, JavloSecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		String siteName = rs.getParameter("context", null);
		if (siteName == null) {
			return "bad request structure, need 'context' param.";
		}

		if (GlobalContext.isExist(ctx.getRequest(), siteName)) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.site-exist"), GenericMessage.ERROR));
		}

		if (PatternHelper.HOST_PATTERN.matcher(siteName).matches()) {
			if (siteName != null && siteName.length() > 0) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest().getSession(), siteName);
				globalContext.setUsersAccess(Arrays.asList(ctx.getCurrentEditUser().getLogin()));
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.new-site-create"), GenericMessage.INFO));
				editGlobalContext(ctx, currentModule, globalContext);
			}
		} else {
			logger.warning("bad site name : " + siteName);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.bad-site-name"), GenericMessage.ERROR));
		}
		return null;
	}

	public static String performRemoveSite(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws ConfigurationException, IOException {
		String siteName = rs.getParameter("removed-context", null);
		if (siteName == null) {
			return "bad request structure, need 'context' param.";
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest().getSession(), siteName);
		if (globalContext == null) {
			return "site not found : " + siteName;
		}
		globalContext.delete(ctx.getRequest().getSession().getServletContext());
		return null;
	}

	public static String performBlockView(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws ConfigurationException, IOException {
		String siteName = rs.getParameter("context", null);
		if (siteName == null) {
			return "bad request structure, need 'context' param.";
		}
		GlobalContext currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest().getSession(), siteName);
		if (currentGlobalContext != null) {
			currentGlobalContext.setView(!currentGlobalContext.isView());
		} else {
			return "context not found : " + siteName;
		}
		return null;
	}

	public static String performBlockEdit(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws ConfigurationException, IOException {
		String siteName = rs.getParameter("context", null);
		if (siteName == null) {
			return "bad request structure, need 'context' param.";
		}
		GlobalContext currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest().getSession(), siteName);
		if (currentGlobalContext != null) {
			currentGlobalContext.setEditable(!currentGlobalContext.isEditable());
		} else {
			return "context not found : " + siteName;
		}
		return null;
	}

	public static final String performEditStaticConfig(HttpServletRequest request, RequestService requestService, ContentContext ctx, Module currentModule, StaticConfig staticConfig) throws FileNotFoundException, IOException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		request.setAttribute("config_content", staticConfig.getAllProperties());
		String uri = request.getRequestURI();
		currentModule.pushBreadcrumb(new Module.HtmlLink(uri, I18nAccess.getInstance(request).getText("admin.edit-static-config"), ""));
		return null;
	}

	public static String performUpdateStaticConfig(RequestService requestService, HttpServletRequest request, HttpSession session, ContentContext ctx, AdminUserFactory adminUserFactory, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule, StaticConfig staticConfig) throws Exception {
		if (!AdminUserSecurity.getInstance().isGod((adminUserFactory.getCurrentUser(session)))) {
			return "Security error.";
		}

		String msg = null;
		if (requestService.getParameter("back", null) != null) {
			currentModule.restoreRenderer();
			currentModule.restoreToolsRenderer();
		} else {
			String newContent = requestService.getParameter("config_content", null);
			if (newContent != null) {
				request.setAttribute("config_content", newContent);
				staticConfig.storeAllProperties(newContent);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.static-config-update"), GenericMessage.INFO));

				DebugHelper.updateLoggerLevel(request.getSession().getServletContext());
			}
		}
		return msg;
	}

	public static String performComponentsDefault(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws ConfigurationException, IOException {
		GlobalContext defaultSite = GlobalContext.getDefaultContext(session);
		if (defaultSite != null) {
			GlobalContext currentContext = GlobalContext.getInstance(session, rs.getParameter("context", null));
			if (currentContext == null) {
				return "context not found.";
			} else {
				currentContext.setComponents(new LinkedList<String>(defaultSite.getComponents()));
			}
		} else {
			return "default web site not found.";
		}
		return null;
	}

	public static String performComponentsForAll(RequestService rs, HttpSession session, AdminUserSecurity adminUserSecurity, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws ConfigurationException, IOException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		GlobalContext currentContext = GlobalContext.getInstance(session, rs.getParameter("context", null));
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(session.getServletContext());
		for (GlobalContext context : allContext) {
			if (ctx.getCurrentEditUser() != null) {
				if (adminUserSecurity.isAdmin(ctx.getCurrentEditUser()) || context.getUsersAccess().contains(ctx.getCurrentEditUser().getLogin())) {
					if (!context.getContextKey().equals(currentContext.getContextKey())) {
						context.setComponents(new LinkedList(currentContext.getComponents()));
					}
				}
			}
		}
		return null;
	}

	public static String performUpload(RequestService rs, HttpSession session, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, ConfigurationException {
		GlobalContext currentContext = GlobalContext.getInstance(session, rs.getParameter("context", null));
		InputStream in = null;
		try {
			for (FileItem file : rs.getAllFileItem()) {
				in = file.getInputStream();
			}
			String urlStr = rs.getParameter("url", "");
			if (urlStr.trim().length() > 0) {
				URL url = new URL(urlStr);
				in = url.openConnection().getInputStream();
			}
			if (in != null) {
				Properties prop = new Properties();
				prop.load(in);
				currentContext.setConfig(prop);
			}
		} finally {
			ResourceHelper.closeResource(in);
		}
		return null;
	}
}
