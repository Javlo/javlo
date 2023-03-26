package org.javlo.module.admin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.javlo.css.CssColor;
import org.javlo.data.InfoBean;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IMacro;
import org.javlo.macro.core.MacroFactory;
import org.javlo.mailing.DKIMFactory;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.ContentService;
import org.javlo.service.ListService;
import org.javlo.service.NotificationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.log.LogService;
import org.javlo.service.shared.SharedContentService;
import org.javlo.service.visitors.CookiesService;
import org.javlo.template.Template;
import org.javlo.template.TemplateData;
import org.javlo.template.TemplateFactory;
import org.javlo.template.TemplatePlugin;
import org.javlo.template.TemplatePluginFactory;
import org.javlo.tracking.Tracker;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.exception.JavloSecurityException;
import org.javlo.utils.StructuredProperties;
import org.javlo.utils.TimeTracker;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.ResourceFactory;
import org.javlo.ztatic.StaticInfo;

public class AdminAction extends AbstractModuleAction {

	public static final String LOGO_PATH = "logo";
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminAction.class.getName());

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
		
		request.setAttribute("componentErrorMessage", ComponentFactory.loadErrorMessage);

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
			Map<String, GlobalContextBean> masterCtx = new HashMap<String, GlobalContextBean>();
			for (GlobalContext context : allContext) {
				logger.fine("load context : " + context.getContextKey());
				if (ctx.getCurrentEditUser() != null) {
					if (adminUserSecurity.isAdmin(ctx.getCurrentEditUser()) || context.getUsersAccess().contains(ctx.getCurrentEditUser().getLogin())) {
						GlobalContextBean contextBean = new GlobalContextBean(ctx, context, ctx.getRequest().getSession());
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

			List<GlobalContextBean> sortedContext = new LinkedList<GlobalContextBean>(masterCtx.values());
			Collections.sort(sortedContext, new GlobalContextBean.SortOnKey());
			request.setAttribute("contextList", sortedContext);
		}

		request.setAttribute("dkimpublickey", DKIMFactory.getDKIMPublicKey(globalContext));

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
			request.setAttribute("currentContext", new GlobalContextBean(ctx, currentGlobalContext, request.getSession()));
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
				IContentVisualComponent[] componentsType = ComponentFactory.getComponents(ctx, currentGlobalContext);
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

				if (ctx.getCurrentTemplate() != null) {
					List<String> fonts = ctx.getCurrentTemplate().getWebFonts(currentGlobalContext);
					Collections.sort(fonts);
					request.setAttribute("fonts", fonts);
					Map<String,String> mapFontTranslated = new HashMap<String,String>();
					Properties mapFont = ctx.getCurrentTemplate().getFontReference(currentGlobalContext);
					String baseUrlTpl = InfoBean.getCurrentInfoBean(ctx).getRootTemplateURL();
					for (Object key : mapFont.keySet()) {
						String value = mapFont.getProperty(""+key);
						value = StringHelper.removeCR(value.replace("##BASE_URI##", baseUrlTpl));
						mapFontTranslated.put(""+key, value);
					}
					request.setAttribute("fontsMap", mapFontTranslated);
				}

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
						// currentGlobalContext.removeTemplate(name);
						selectedTemplate.add(new Template.TemplateBean(ctx, Template.getFakeTemplate(name)));
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
				MacroFactory macroFactory = MacroFactory.getInstance(ctx);
				Collection<MacroBean> macrosName = new LinkedList<MacroBean>();
				for (IMacro macro : macroFactory.getMacros()) {
					macrosName.add(new MacroBean(macro.getName(), macro.getInfo(ctx), null, macro.getPriority()));
				}
				request.setAttribute("macros", macrosName);
				Map<String, String> selectedMacros = new HashMap<String, String>();
				for (String selected : currentGlobalContext.getMacros()) {
					selectedMacros.put(selected, StringHelper.SOMETHING);
				}
				request.setAttribute("selectedMacros", selectedMacros);

				Map<String, String> screenshortParam = new HashMap<String, String>();
				screenshortParam.put(ContentContext.TAKE_SCREENSHOT, "true");
				ctx.getRequest().setAttribute("takeSreenshotUrl", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), screenshortParam));

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
						newLogoURL = URLHelper.createTransformURL(absoluteURLCtx, null, URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), currentGlobalContext.getTemplateData().getLogo()), LOGO_PATH, null);
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
		 * AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance(); if
		 * (adminUserSecurity.isAdmin(user)) { return true; }
		 */

		if (user == null) {
			return false;
		}

		try {
			/*
			 * Collection<GlobalContext> allContext =
			 * GlobalContextFactory.getAllGlobalContext (session.getServletContext()); for
			 * (GlobalContext globalContext : allContext) { if
			 * (globalContext.getUsersAccess().contains(user.getLogin())) { return true; } }
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

	public static String performUpdateGlobalContextLight(RequestService requestService, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule, User user) throws Exception {
		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
		if (adminUserSecurity.canRole(user, AdminUserSecurity.CONTENT_ROLE)) {
			ctx.getGlobalContext().setGlobalTitle(requestService.getParameter("global-title", null));
			ctx.setClosePopup(true);
			ctx.getGlobalContext().setEditTemplateMode(requestService.getParameter("template-mode", null));
			ctx.getGlobalContext().setQuitArea(requestService.getParameterListValues("quietAreas"));
			if (StringHelper.isTrue(requestService.getParameter("graphic-charter", null))) {
				boolean updateCharte = updateGraphicCharter(ctx, ctx.getGlobalContext());
				if (updateCharte) {
					ctx.getCurrentTemplate().clearRenderer(ctx);
					ctx.setNeedRefresh(true);
				}
			}
			/** owner **/
			ctx.getGlobalContext().setOwnerName(requestService.getParameter("owner.name", ""));
			ctx.getGlobalContext().setOwnerAddress(requestService.getParameter("owner.address", ""));
			ctx.getGlobalContext().setOwnerNumber(requestService.getParameter("owner.number", ""));
			ctx.getGlobalContext().setOwnerPhone(requestService.getParameter("owner.phone", ""));
			ctx.getGlobalContext().setOwnerEmail(requestService.getParameter("owner.email", ""));
			/** macro **/
			MacroFactory.getInstance(ctx).clear(ctx);
			MacroFactory macroFactory = MacroFactory.getInstance(ctx);
			List<String> macros = new LinkedList<String>();
			for (IMacro macro : macroFactory.getMacros()) {
				if (requestService.getParameter(macro.getName(), null) != null) {
					macros.add(macro.getName());
				}
			}
			ctx.getGlobalContext().setMacros(macros);
		}
		return null;
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
					currentGlobalContext.setAliasActive(StringHelper.isTrue(requestService.getParameter("alias-active", null)));
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
					currentGlobalContext.setComponentsFiltered(StringHelper.isTrue(requestService.getParameter("components-filtered", null)));
					
					/** owner **/
					currentGlobalContext.setOwnerName(requestService.getParameter("owner.name", ""));
					currentGlobalContext.setOwnerContact(requestService.getParameter("owner.contact", ""));
					currentGlobalContext.setOwnerAddress(requestService.getParameter("owner.address", ""));
					currentGlobalContext.setOwnerPostcode(requestService.getParameter("owner.postcode", ""));
					currentGlobalContext.setOwnerCity(requestService.getParameter("owner.city", ""));
					currentGlobalContext.setOwnerNumber(requestService.getParameter("owner.number", ""));
					currentGlobalContext.setOwnerPhone(requestService.getParameter("owner.phone", ""));
					currentGlobalContext.setOwnerEmail(requestService.getParameter("owner.email", ""));
					currentGlobalContext.setOwnerTwitter(requestService.getParameter("owner.twitter", ""));
					currentGlobalContext.setOwnerFacebook(requestService.getParameter("owner.facebook", ""));
					currentGlobalContext.setOwnerInstagram(requestService.getParameter("owner.instagram", ""));
					currentGlobalContext.setOwnerLinkedin(requestService.getParameter("owner.linkedin", ""));
					
					try {
						currentGlobalContext.setURLFactory(requestService.getParameter("urlfactory", ""));
					} catch (Exception e1) {
						messageRepository.setGlobalMessage(new GenericMessage(e1.getMessage(), GenericMessage.ERROR));
						e1.printStackTrace();
					}

					String uriAlias = requestService.getParameter("uri-alias", null);
					if (uriAlias != null) {
						StructuredProperties prop = new StructuredProperties();
						Properties properties = new Properties();
						Reader reader = new InputStreamReader(new ByteArrayInputStream(uriAlias.getBytes()));
						properties.load(reader);
						reader.close();
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
					currentGlobalContext.setMainHelpURL(requestService.getParameter("main-help-url", ""));
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
					currentGlobalContext.setUnsubscribeLink(requestService.getParameter("mailing-unsubscribe", ""));

					currentGlobalContext.setDKIMDomain(requestService.getParameter("mailing-dkimdomain", ""));
					currentGlobalContext.setDKIMSelector(requestService.getParameter("mailing-dkimselector", ""));

					currentGlobalContext.setForcedHttps(StringHelper.isTrue(requestService.getParameter("security-forced-https", null), false));
					currentGlobalContext.setBackupThread(StringHelper.isTrue(requestService.getParameter("security-backup-thread", null), false));
					
					currentGlobalContext.setCookies(StringHelper.isTrue(requestService.getParameter("cookies", null), false));
					currentGlobalContext.setCookiesPolicyUrl(requestService.getParameter("cookies-url", null));
					
					/** cookies types **/
					List<String> cookiesList = new LinkedList<>();
					for (String type : CookiesService.COOKIES_TYPES) {
						if (StringHelper.isTrue(requestService.getParameter("cookies_"+type))) {
							cookiesList.add(type);
						}
					}
					currentGlobalContext.setCookiesTypes(cookiesList);

					currentGlobalContext.setPortail(StringHelper.isTrue(requestService.getParameter("security-portail", "")));

					if (requestService.getParameter("resetdkim", null) != null) {
						DKIMFactory.resetKeys(currentGlobalContext);
					}

					/** special config **/
					String specialConfig = requestService.getParameter("specialconfig", "");
					if (!StringHelper.isEmpty(specialConfig)) {
						ResourceHelper.writeStringToFile(currentGlobalContext.getSpecialConfigFile(), specialConfig);
					}

					/** POP **/
					String popHost = requestService.getParameter("mailing-pophost", null);
					String popPort = requestService.getParameter("mailing-popport", null);
					String popUser = requestService.getParameter("mailing-popuser", null);
					boolean popSSL = StringHelper.isTrue(requestService.getParameter("mailing-popssl", null));
					boolean resetPOPThread = false;
					if (!("" + currentGlobalContext.getPOPHost()).equals(popHost) || !("" + currentGlobalContext.getPOPPort()).equals(popPort) || !("" + currentGlobalContext.getPOPUser()).equals(popUser) || currentGlobalContext.isPOPSsl() != popSSL) {
						currentGlobalContext.setPOPHost(popHost);
						currentGlobalContext.setPOPPort(popPort);
						currentGlobalContext.setPOPUser(popUser);
						currentGlobalContext.setPOPSsl(popSSL);
						resetPOPThread = true;
					}

					String popPwd = requestService.getParameter("mailing-poppassword", "");
					if (popPwd.length() > 0) {
						currentGlobalContext.setPOPPassword(popPwd);
						resetPOPThread = true;
					}
					if (requestService.getParameter("mailing-resetpoppassword", null) != null) {
						currentGlobalContext.setPOPPassword("");
					}
					if (resetPOPThread) {
						currentGlobalContext.activePopThread();
					}

					/** SMTP **/
					currentGlobalContext.setSMTPHost(requestService.getParameter("mailing-smtphost", null));
					currentGlobalContext.setSMTPPort(requestService.getParameter("mailing-smtpport", null));
					currentGlobalContext.setSMTPUser(requestService.getParameter("mailing-smtpuser", null));

					String pwd = requestService.getParameter("mailing-smtppassword", "");
					if (pwd.length() > 0) {
						currentGlobalContext.setSMTPPassword(pwd);
					}
					if (StringHelper.isTrue(requestService.getParameter("mailing-resetpassword", null))) {
						currentGlobalContext.setSMTPPassword("");
					} else if (!StringHelper.isEmpty(currentGlobalContext.getSMTPHost())) {
						Transport t = null;
						try {
							t = MailService.getMailTransport(new MailConfig(currentGlobalContext, null, null));
							logger.info("smtp:" + currentGlobalContext.getSMTPHost() + " connected ? " + t.isConnected());
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

					currentGlobalContext.setMetaBloc(requestService.getParameter("meta-bloc", null));
					currentGlobalContext.setHeaderBloc(requestService.getParameter("header-bloc", null));
					currentGlobalContext.setFooterBloc(requestService.getParameter("footer-bloc", null));

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
					currentGlobalContext.setGoogleApiKey(requestService.getParameter("google-key", ""));

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
					
					/** TODO: Naceur : récupérer la valeur CSP **/
					currentGlobalContext.setSecurityCsp(requestService.getParameter("content-security-policy", ""));

					/** macro **/
					MacroFactory.getInstance(ctx).clear(ctx);
					MacroFactory macroFactory = MacroFactory.getInstance(ctx);
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

					if (StringHelper.isTrue(requestService.getParameter("graphic-charter", null))) {
						updateGraphicCharter(ctx, currentGlobalContext);
					}

					if (importTemplate) {
						TemplateFactory.cleanRenderer(ctx, currentGlobalContext.getTemplatesNames(), true);
					}

					currentGlobalContext.setQuitArea(requestService.getParameterListValues("quietAreas"));

					currentGlobalContext.reload();

					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.context-updated"), GenericMessage.INFO));
				} else {
					msg = "context not found : " + currentContextKey;
				}
			}
		}
		if (!messageRepository.haveImportantMessage()) {
			ctx.setClosePopup(true);
		}
		return msg;
	}

	public static boolean updateGraphicCharter(ContentContext ctx, GlobalContext currentGlobalContext) throws IOException {
		/** template data **/
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		TemplateData td = currentGlobalContext.getTemplateData();
		int tdHash = td.hashCodeForDeployTemplate();
		td.setBackground(StringHelper.parseColor(requestService.getParameter("background", "" + td.getBackground())));
		td.setBackgroundActive(StringHelper.parseColor(requestService.getParameter("backgroundActive", "" + td.getBackgroundActive())));
		td.setForeground(StringHelper.parseColor(requestService.getParameter("foreground", "" + td.getForeground())));
		td.setBorder(StringHelper.parseColor(requestService.getParameter("border", "" + td.getBorder())));
		td.setBackgroundMenu(StringHelper.parseColor(requestService.getParameter("backgroundMenu", "" + td.getBackgroundMenu())));
		td.setText(StringHelper.parseColor(requestService.getParameter("text", "" + td.getText())));
		td.setTextMenu(StringHelper.parseColor(requestService.getParameter("textMenu", "" + td.getTextMenu())));
		td.setComponentBackground(StringHelper.parseColor(requestService.getParameter("componentBackground", "" + td.getComponentBackground())));
		td.setLink(StringHelper.parseColor(requestService.getParameter("link", "" + td.getLink())));
		td.setTitle(StringHelper.parseColor(requestService.getParameter("title", "" + td.getTitle())));
		td.setSpecial(StringHelper.parseColor(requestService.getParameter("special", "" + td.getSpecial())));
		td.setFontText(requestService.getParameter("fontText", "" + td.getFontText()));
		td.setFontHeading(requestService.getParameter("fontHeading", "" + td.getFontHeading()));
		td.setFixMenu(StringHelper.isTrue(requestService.getParameter("fixMenu", null)));		
		td.setLargeMenu(StringHelper.isTrue(requestService.getParameter("largeMenu", null)));
		td.setLoginMenu(StringHelper.isTrue(requestService.getParameter("loginMenu", null)));
		td.setSearchMenu(StringHelper.isTrue(requestService.getParameter("searchMenu", null)));
		td.setJssearchMenu(StringHelper.isTrue(requestService.getParameter("jssearchMenu", null)));
		td.setDropdownMenu(StringHelper.isTrue(requestService.getParameter("dropdownMenu", null)));
		td.setLarge(StringHelper.isTrue(requestService.getParameter("large", null)));
		td.setSmall(StringHelper.isTrue(requestService.getParameter("small", null)));
		td.setFixSidebar(StringHelper.isTrue(requestService.getParameter("fixSidebar", null)));
		td.setMenuLeft(StringHelper.isTrue(requestService.getParameter("menuLeft", null)));		
		td.setMenuRight(StringHelper.isTrue(requestService.getParameter("menuRight", null)));
		td.setMenuCenter(StringHelper.isTrue(requestService.getParameter("menuCenter", null)));
		td.setMenuAround(StringHelper.isTrue(requestService.getParameter("menuAround", null)));
		td.setExtendSub(StringHelper.isTrue(requestService.getParameter("extendSub", null)));
		
		td.setPrimaryColor(CssColor.getInstance(StringHelper.parseColor(requestService.getParameter("primaryColor", "" + td.getPrimaryColor()))));
		td.setSecondaryColor(CssColor.getInstance(StringHelper.parseColor(requestService.getParameter("secondaryColor", "" + td.getSecondaryColor()))));
		td.setThirdColor(CssColor.getInstance(StringHelper.parseColor(requestService.getParameter("thirdColor", "" + td.getThirdColor()))));

		/** message **/
		td.setMessagePrimary(StringHelper.parseColor(requestService.getParameter("messagePrimary", "" + td.getMessagePrimary())));
		td.setMessageSecondary(StringHelper.parseColor(requestService.getParameter("messageSecondary", "" + td.getMessageSecondary())));
		td.setMessageSuccess(StringHelper.parseColor(requestService.getParameter("messageSuccess", "" + td.getMessageSuccess())));
		td.setMessageDanger(StringHelper.parseColor(requestService.getParameter("messageDanger", "" + td.getMessageDanger())));
		td.setMessageWarning(StringHelper.parseColor(requestService.getParameter("messageWarning", "" + td.getMessageWarning())));
		td.setMessageInfo(StringHelper.parseColor(requestService.getParameter("messageInfo", "" + td.getMessageInfo())));
		for (int i = 0; i < TemplateData.COLOR_LIST_SIZE; i++) {
			td.setColorList(StringHelper.parseColor(requestService.getParameter("colorList" + i, null)), i);
		}
		MailService.resetInstance();

		for (FileItem file : requestService.getAllFileItem()) {
			if (file.getFieldName().equals(LOGO_PATH)) {
				File oldLogo = null;
				if (td.getLink() != null) {
					oldLogo = new File(URLHelper.mergePath(currentGlobalContext.getStaticFolder(), td.getLogo()));
				}
				if (file.getName().trim().length() > 0) {
					String logoPath = URLHelper.mergePath(LOGO_PATH, file.getName());
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
		return tdHash != td.hashCodeForDeployTemplate();
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
						content.releaseViewNav(currentGlobalContext);
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
					IContentVisualComponent[] componentsType = ComponentFactory.getComponents(ctx, currentGlobalContext);
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
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.clear cache"), GenericMessage.INFO));
		String msg = clearCache(ctx);
		TemplateFactory.copyDefaultTemplate(session.getServletContext());
		NotificationService.getInstance(globalContext).clearList();
		PersistenceService.getInstance(globalContext).flush();
		PersistenceService.getInstance(globalContext).clearTrackCache();
		AdminUserFactory.createUserFactory(globalContext, session).reload(globalContext, session);
		UserFactory.createUserFactory(globalContext, session).reload(globalContext, session);
		StaticConfig staticConfig = globalContext.getStaticConfig();
		TimeTracker.reset(staticConfig);
		ResourceHelper.deleteFolder(globalContext.getStaticConfig().getWebTempDir());
		ListService.getInstance(ctx).clear();
		MacroFactory.getInstance(ctx).clear(ctx);
		System.gc();
		return msg;
	}
	
	public static final String clearCache(ContentContext ctx) throws Exception {
		User user = ctx.getCurrentEditUser();
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "security error !";
		}
		GlobalContext globalContext = ctx.getGlobalContext();
		HttpServletRequest request = ctx.getRequest();
		HttpSession session = request.getSession();
		globalContext.clearTransformShortURL();
		globalContext.resetURLFactory();
		globalContext.storeRedirectUrlList();
		globalContext.resetRedirectUrlMap();
		globalContext.reset404UrlMap();
		String currentContextKey = request.getParameter("context");
		if (currentContextKey == null && globalContext.isMaster()) {
			ContentService.clearAllContextCache(ctx);
		} else {
			if (!AdminUserSecurity.getInstance().isMaster(user) && !AdminUserSecurity.getInstance().isGod(user)) {
				return "security error !";
			}
			ContentService.clearCache(ctx, globalContext);
		}
		Tracker.getTracker(globalContext, session);
		LogService.getInstance(session).clear();
		SharedContentService.getInstance(ctx).clearCache(ctx);
		StaticConfig staticConfig = globalContext.getStaticConfig();
		staticConfig.clearCache();

		I18nAccess.getInstance(ctx).resetViewLanguage(ctx);

		ResourceFactory.getInstance(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE)).clearCache();
		ResourceFactory.getInstance(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE)).clearCache();
		ResourceFactory.getInstance(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE)).clearCache();
		
		globalContext.GLOBAL_ERROR=null;

		return null;
	}

	/**
	 * @param request
	 * @param globalContext
	 * @param session
	 * @param user
	 * @param ctx
	 * @param messageRepository
	 * @param i18nAccess
	 * @param fileCache
	 * @return
	 */
	public static String performClearimagecache(HttpServletRequest request, GlobalContext globalContext, HttpSession session, User user, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, FileCache fileCache) {
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "security error !";
		}
		
		int cleanAtribute = ContentService.getInstance(globalContext).cleanAttribute(ctx, StaticInfo.IMAGE_SIZE_PREFIX);
		logger.info("clear attribute in preview end with "+StaticInfo.IMAGE_SIZE_PREFIX+" -> "+cleanAtribute);
		cleanAtribute = ContentService.getInstance(globalContext).cleanAttribute(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), StaticInfo.IMAGE_SIZE_PREFIX);
		logger.info("clear attribute in view end with "+StaticInfo.IMAGE_SIZE_PREFIX+" -> "+cleanAtribute);
		
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
		
		/** clear CDN **/
		for (String cdnUrlStr : ctx.getGlobalContext().getSpecialConfig().getCdnRefreshUrl()) {
			try {
				URL cdnUrl = new URL(cdnUrlStr);
				logger.info("call refresh cdn : "+cdnUrl);
				try (InputStream in = cdnUrl.openConnection().getInputStream()) {
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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

	public static String performCreateSite(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule, User user) throws IOException, JavloSecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "security error !";
		}
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

	public static String performRemoveSite(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
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

	public static String performBlockView(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
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

	public static String performBlockEdit(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
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

	public static String performComponentsDefault(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
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

	public static String performComponentsForAll(RequestService rs, HttpSession session, AdminUserSecurity adminUserSecurity, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
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

	public static String performRemovelogo(RequestService rs, HttpSession session, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateData td = ctx.getGlobalContext().getTemplateData();
		td.setLogo(null);
		ctx.getGlobalContext().setTemplateData(td);
		return null;
	}

	public static String performUpload(RequestService rs, HttpSession session, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
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

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return ctx.getCurrentEditUser() != null;
	}

}
