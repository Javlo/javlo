package org.javlo.module.admin;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.MetaTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.LangHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.PageConfiguration;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.template.TemplatePlugin;
import org.javlo.template.TemplatePluginFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.user.exception.JavloSecurityException;
import org.javlo.ztatic.FileCache;

public class AdminAction extends AbstractModuleAction {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminAction.class.getName());

	public static class GlobalContextBean {
		private String key;
		private String administrator;
		private String aliasOf;
		private String creationDate;
		private String latestLoginDate;
		private String defaultTemplate;
		private String globalTitle;
		private String defaultLanguage;
		private String languages;
		private String contentLanguages;
		private String size;
		private String folder;
		private String usersAccess;
		private String googleAnalyticsUACCT;
		private String tags;
		private String blockPassword;

		private int countUser;
		private boolean view;
		private boolean edit;
		private boolean visibility;
		private boolean editability;
		private String userFactoryClassName = "";
		private String adminUserFactoryClassName = "";

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
			setEditability(globalContext.isEditable());
			setDefaultTemplate(globalContext.getDefaultTemplate());

			setSize(StringHelper.renderSize(globalContext.getAccountSize()));
			setGlobalTitle(globalContext.getGlobalTitle());
			setDefaultLanguage(globalContext.getDefaultLanguage());
			setLanguages(StringHelper.collectionToString(globalContext.getLanguages(), ";"));
			setContentLanguages(StringHelper.collectionToString(globalContext.getContentLanguages(), ";"));

			setGoogleAnalyticsUACCT(globalContext.getGoogleAnalyticsUACCT());
			setTags(globalContext.getRAWTags());
			setBlockPassword(globalContext.getBlockPassword());

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
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

	}

	@Override
	public String getActionGroupName() {
		return "admin";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		HttpServletRequest request = ctx.getRequest();
		ServletContext application = request.getSession().getServletContext();

		ContentContext viewCtx = new ContentContext(ctx);
		Module currentModule = moduleContext.getCurrentModule();
		String msg = "";
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);

		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();

		Collection<GlobalContextBean> ctxAllBean = new LinkedList<GlobalContextBean>();
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(request.getSession());
		for (GlobalContext context : allContext) {
			if (adminUserSecurity.isAdmin(ctx.getCurrentEditUser()) || context.getUsersAccess().contains(ctx.getCurrentEditUser().getLogin())) {
				ctxAllBean.add(new GlobalContextBean(context, ctx.getRequest().getSession()));
			}
		}

		request.setAttribute("contextList", ctxAllBean);

		/* breadcrumb */
		if (currentModule.getBreadcrumbList() == null || currentModule.getBreadcrumbList().size() == 0) {
			currentModule.pushBreadcrumb(new Module.HtmlLink(URLHelper.createURL(ctx), I18nAccess.getInstance(request).getText("global.home"), ""));
		}

		/*** current context ***/

		String currentContextKey = request.getParameter("context");
		if (currentContextKey != null || request.getAttribute("prepareContext") != null) {
			GlobalContext currentGlobalContext;
			if (currentContextKey != null) {
				request.setAttribute("context", currentContextKey);
				currentGlobalContext = GlobalContext.getRealInstance(request, currentContextKey);
			} else {
				currentGlobalContext = (GlobalContext) request.getAttribute("prepareContext");
				request.setAttribute("context", currentGlobalContext.getContextKey());
			}
			request.setAttribute("currentContext", new GlobalContextBean(currentGlobalContext, request.getSession()));
			if (currentGlobalContext != null) {
				List<Template> templates = TemplateFactory.getAllDiskTemplates(request.getSession().getServletContext());
				Collections.sort(templates);

				Template defaultTemplate = TemplateFactory.getDiskTemplate(request.getSession().getServletContext(), currentGlobalContext.getDefaultTemplate());

				if (defaultTemplate != null) {
					defaultTemplate.importTemplateInWebapp(ctx);
					String templateImageURL = URLHelper.createTransformStaticTemplateURL(ctx, defaultTemplate, "template", defaultTemplate.getVisualFile());
					request.setAttribute("templateImageUrl", templateImageURL);
				}
				/*** component list ***/
				List<String> currentComponents = null;
				currentComponents = currentGlobalContext.getComponents();
				IContentVisualComponent[] componentsType = ComponentFactory.getComponents(currentGlobalContext);
				Collection<IContentVisualComponent> components = new LinkedList<IContentVisualComponent>();
				for (int i = 0; i < componentsType.length; i++) {
					if (!componentsType[i].isHidden(ctx) && !(componentsType[i] instanceof MetaTitle)) {
						components.add(componentsType[i]);
					}
				}
				request.setAttribute("components", components);
				request.setAttribute("currentComponents", currentComponents);

				request.setAttribute("allModules", moduleContext.getAllModules());
				request.setAttribute("currentModules", currentGlobalContext.getModules());

				List<String> templatesName = currentGlobalContext.getTemplates();
				List<Template.TemplateBean> selectedTemplate = new LinkedList<Template.TemplateBean>();
				for (String name : templatesName) {
					Template template = TemplateFactory.getDiskTemplate(request.getSession().getServletContext(), name, false);
					if (template != null) {
						if (!template.isTemplateInWebapp(ctx)) {
							template.importTemplateInWebapp(ctx);
						}
						selectedTemplate.add(new Template.TemplateBean(ctx, template));
					} else {
						currentGlobalContext.removeTemplate(name, false);
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

				/** template plugin **/
				ctx.getRequest().setAttribute("templatePlugins", TemplatePluginFactory.getInstance(application).getAllTemplatePlugin());

				Map<String, String> selectedPlugin = new HashMap<String, String>();
				for (String selected : currentGlobalContext.getTemplatePlugin()) {
					selectedPlugin.put(selected, StringHelper.SOMETHING);
				}

				ctx.getRequest().setAttribute("selectedTemplatePlugins", selectedPlugin);
				ctx.getRequest().setAttribute("templatePluginConfig", currentGlobalContext.getTemplatePluginConfig());

			} else {
				msg = "bad context : " + currentContextKey;
				currentModule.restoreRenderer();
				currentModule.restoreToolsRenderer();
			}
		} else {
			currentModule.restoreRenderer();
			currentModule.restoreToolsRenderer();
			currentModule.clearBreadcrump();
			currentModule.pushBreadcrumb(new Module.HtmlLink(URLHelper.createURL(ctx), I18nAccess.getInstance(request).getText("global.home"), ""));
		}

		return msg;
	}

	public static final void editGlobalContext(HttpServletRequest request, Module currentModule, GlobalContext globalContext) throws FileNotFoundException, IOException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (globalContext != null) {
			request.setAttribute("prepareContext", globalContext);
		}
		currentModule.setRenderer("/jsp/site_properties.jsp");
		currentModule.setToolsRenderer(null);
		String uri = request.getRequestURI();
		currentModule.pushBreadcrumb(new Module.HtmlLink(uri, I18nAccess.getInstance(request).getText("global.change") + " : " + request.getParameter("context"), ""));
	}

	public static final String performChangeSite(HttpServletRequest request, RequestService requestService, ContentContext ctx, Module currentModule) throws FileNotFoundException, IOException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (requestService.getParameter("change", null) != null) {
			editGlobalContext(request, currentModule, null);
		} else if (requestService.getParameter("components", null) != null) {
			currentModule.setRenderer("/jsp/components.jsp");
			currentModule.setToolsRenderer(null);
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

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		/*
		 * AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance(); if (adminUserSecurity.isAdmin(user)) { return true; }
		 */

		if (user == null) {
			return false;
		}

		try {
			Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(session);
			for (GlobalContext globalContext : allContext) {
				if (globalContext.getUsersAccess().contains(user.getLogin())) {
					return true;
				}
			}
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

	public static String performUpdateGlobalContext(RequestService requestService, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule) throws ConfigurationException, IOException, org.javlo.user.exception.JavloSecurityException {
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
				currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest(), currentContextKey);

				if (currentGlobalContext != null) {
					checkRight(ctx, currentGlobalContext);
					currentGlobalContext.setGlobalTitle(requestService.getParameter("global-title", null));
					currentGlobalContext.setAliasOf(requestService.getParameter("alias", "").trim());
					currentGlobalContext.setDefaultTemplate(requestService.getParameter("default-template", null));
					currentGlobalContext.setRAWLanguages(requestService.getParameter("languages", null));
					currentGlobalContext.setRAWContentLanguages(requestService.getParameter("content-languages", null));
					currentGlobalContext.setRAWTags(requestService.getParameter("tags", null));
					currentGlobalContext.setAdministrator(requestService.getParameter("administrator", ""));

					String usersAccess = requestService.getParameter("users-access", "");
					if (usersAccess.trim().length() > 0) {
						currentGlobalContext.setUsersAccess(StringHelper.textToList(usersAccess));
					}

					String defaultLanguage = requestService.getParameter("default-languages", null);
					currentGlobalContext.setDefaultLanguages(defaultLanguage);

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

					if (importTemplate) {
						TemplateFactory.cleanRenderer(ctx, currentGlobalContext.getTemplates(), false, true);
						// TemplateFactory.cleanRenderer(ctx, currentGlobalContext.getMailingTemplates(), true, true);
					}

					PageConfiguration.getInstance(currentGlobalContext).loadTemplate(currentGlobalContext); // reload templates

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
				GlobalContext currentGlobalContext = GlobalContext.getRealInstance(request, contextName);
				checkRight(ctx, currentGlobalContext);
				if (globalContext != null) {
					ContentService content = ContentService.getInstance(currentGlobalContext);
					if (view) {
						content.releaseViewNav(ctx, currentGlobalContext);
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

	public static final String performComponentsSelect(HttpServletRequest request, ContentContext ctx, RequestService requestService, MessageRepository messageRepository, I18nAccess i18nAccess, Module currentModule) throws Exception {
		String msg = null;
		if (requestService.getParameter("back", null) != null) {
			currentModule.restoreAll();
		} else {
			String currentContextKey = requestService.getParameter("context", null);
			if (currentContextKey != null) {
				GlobalContext currentGlobalContext = GlobalContext.getRealInstance(request, currentContextKey);
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
				GlobalContext currentGlobalContext = GlobalContext.getRealInstance(request, currentContextKey);
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

	public static final String performClearCache(HttpServletRequest request, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, FileCache fileCache) throws Exception {
		ContentService.clearAllContextCache(ctx);
		fileCache.clear();
		System.gc();
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.clear cache"), GenericMessage.INFO));
		return null;
	}

	public static final String performSelectTemplate(ContentContext ctx, RequestService requestService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String msg = null;
		String currentContextKey = requestService.getParameter("context", null);
		if (currentContextKey != null) {
			GlobalContext currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest(), currentContextKey);
			if (currentGlobalContext != null) {
				checkRight(ctx, currentGlobalContext);
				String templateName = requestService.getParameter("template", null);
				if (templateName != null) {
					currentGlobalContext.addTemplate(templateName, StringHelper.isTrue(requestService.getParameter("mailing", null)));
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.linked-template") + ' ' + templateName, GenericMessage.INFO));
					PageConfiguration.getInstance(currentGlobalContext).loadTemplate(currentGlobalContext); // reload templates
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
			GlobalContext currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest(), currentContextKey);
			if (currentGlobalContext != null) {
				checkRight(ctx, currentGlobalContext);
				String templateName = requestService.getParameter("template", null);
				if (templateName != null) {
					currentGlobalContext.removeTemplate(templateName, StringHelper.isTrue(requestService.getParameter("mailing", null)));
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("admin.message.unlinked-template") + ' ' + templateName, GenericMessage.INFO));
					PageConfiguration.getInstance(currentGlobalContext).loadTemplate(currentGlobalContext); // reload templates
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

		if (PatternHelper.ALPHANNUM_NOSPACE_PATTERN.matcher(siteName).matches()) {
			if (siteName != null && siteName.length() > 0) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest(), siteName);
				globalContext.setUsersAccess(Arrays.asList(ctx.getCurrentEditUser().getLogin()));
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.message.new-site-create"), GenericMessage.INFO));
				editGlobalContext(ctx.getRequest(), currentModule, globalContext);
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest(), siteName);
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
		GlobalContext currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest(), siteName);
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
		GlobalContext currentGlobalContext = GlobalContext.getRealInstance(ctx.getRequest(), siteName);
		if (currentGlobalContext != null) {
			currentGlobalContext.setEditable(!currentGlobalContext.isEditable());
		} else {
			return "context not found : " + siteName;
		}
		return null;
	}
};