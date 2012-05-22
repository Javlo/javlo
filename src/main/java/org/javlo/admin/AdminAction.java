package org.javlo.admin;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.javlo.actions.EditActions;
import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.ConfigHelper;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.PageConfiguration;
import org.javlo.service.CompatibiltyService;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.servlet.SynchronisationServlet;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.thread.ThreadManager;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.FileCache;

public class AdminAction implements IAction {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(AdminAction.class.getName());

	public static final String performChangelicence(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		RequestService requestService = RequestService.getInstance(request);

		String licence = requestService.getParameter("licence", null);
		if (licence.equals(GlobalContext.LICENCE_FREE) || (licence.equals(GlobalContext.LICENCE_FREE_PLUS))) {
			globalContext.setLicence(licence);
		} else { // NEED BILLING
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			editCtx.setMainRenderer("/jsp/edit/data/xtendblog_billing.jsp");
		}

		MessageRepository msgRepo = MessageRepository.getInstance(ContentContext.getContentContext(request, response));
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.licence-changed", new String[][] { { "licence", licence } }), GenericMessage.INFO));

		return null;
	}

	public static String performChangepassword(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		MessageRepository msgRepo = MessageRepository.getInstance(ContentContext.getContentContext(request, response));
		String password = requestService.getParameter("new-password", "");
		if (password.trim().length() > 0) {
			StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
			GlobalContext globalContext = GlobalContext.getInstance(request);
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			String oldPassword = requestService.getParameter("old-password", null);
			IUserFactory fact = AdminUserFactory.createUserFactory(globalContext, request.getSession());
			if (StringHelper.isPasswordMath(fact.getCurrentUser(request.getSession()).getPassword(), oldPassword, staticConfig.isPasswordEncryt())) {
				String password2 = requestService.getParameter("new-password2", ".");
				if (password2.equals(password)) {
					if (password.length() <= 3) {
						msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.password-too-short"), GenericMessage.ERROR));
					} else {
						String newPassword = password;
						if (staticConfig.isPasswordEncryt()) {
							newPassword = StringHelper.encryptPassword(password);
						}
						User currentUser = fact.getCurrentUser(request.getSession());
						currentUser.getUserInfo().setPassword(newPassword);
						fact.store();
						logger.info("user : " + currentUser.getLogin() + " has change his password.");
						msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.password-changed"), GenericMessage.INFO));
					}
				} else {
					msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.bad-password"), GenericMessage.ERROR));
				}
			} else {
				msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.bad-old-password"), GenericMessage.ERROR));
			}
		}
		return null;
	}

	public static final String performChangeview(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException, IOException {

		RequestService requestService = RequestService.getInstance(request);

		try {
			int view = Integer.parseInt(requestService.getParameter("view", "0"));

			AdminContext adminContext = AdminContext.getInstance(request.getSession());

			adminContext.setCurrentView(view);

			switch (view) {
			case 0:
				adminContext.setRenderer("global.jsp");
				adminContext.setCommandRenderer("global_command.jsp");
				break;

			case 1:
				adminContext.setRenderer("template/template.jsp");
				adminContext.setCommandRenderer("template/template_command.jsp");
				break;

			case 2:
				adminContext.setRenderer("component/component_list.jsp");
				adminContext.setCommandRenderer(null);
				break;

			case 3:
				adminContext.setRenderer("/jsp/edit/static/dir_view.jsp");
				adminContext.setCommandRenderer("/jsp/edit/static/command.jsp");
				break;
			case 4:
				adminContext.setRenderer("config.jsp");
				adminContext.setCommandRenderer(null);
				break;

			case 10:
				adminContext.setRenderer("metadata/metadata.jsp");
				adminContext.setCommandRenderer("global_command.jsp");
				break;

			default:
				break;
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final String performCleanallrenderer(HttpServletRequest request, HttpServletResponse response) throws Exception {
		AdminContext.getInstance(request.getSession());
		ContentContext ctx = ContentContext.getContentContext(request, response);
		TemplateFactory.cleanAllRenderer(ctx, true, true); // web template
		TemplateFactory.cleanAllRenderer(ctx, false, true); // mailing template
		return null;
	}

	public static final String performClearcache(HttpServletRequest request, HttpServletResponse response) throws Exception {
		FileCache.getInstance(request.getSession().getServletContext()).clear();
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService.clearAllCache(ctx, GlobalContext.getInstance(request));
		System.gc();
		return null;
	}

	public static final String performCleartemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService requestService = RequestService.getInstance(request);

		String templateId = requestService.getParameter("template", null);

		if (templateId != null) {
			AdminContext adminContext = AdminContext.getInstance(request.getSession());
			Template template = Template.getApplicationInstance(request.getSession().getServletContext(), null, templateId, (adminContext.getTemplateType() == AdminContext.MAILING_TEMPLATE));
			template.clearRenderer(null);
			ContentContext ctx = ContentContext.getContentContext(request, response);
			MessageRepository msgRepo = MessageRepository.getInstance(ctx);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("command.admin.template.commited"), GenericMessage.INFO));
		}

		return null;
	}

	public static String performComponent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String msg = null;

		RequestService requestService = RequestService.getInstance(request);
		String contextID = requestService.getParameter("context", null);

		if (contextID != null) {

			GlobalContext globalContext = GlobalContext.getInstance(request, contextID);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
			if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
				return null;
			}

			// List<String> currentSelection = globalContext.getComponents();
			List<String> currentSelection = new ArrayList<String>();
			if (requestService.getParameter("default", null) != null) {
				String[] comps = ConfigHelper.getDefaultComponentsClasses(request.getSession().getServletContext());
				for (String comp : comps) {
					currentSelection.add(comp);
				}
			} else {
				globalContext = GlobalContext.getInstance(request, contextID);
				String[] componentsListed = requestService.getParameterValues("component-listed", new String[0]);
				for (String element : componentsListed) {
					if (requestService.getParameter(element, null) != null) {
						// if (!currentSelection.contains(componentsListed[i])) {
						currentSelection.add(element);
						// }
						// } else {
						// currentSelection.remove(componentsListed[i]);
					}
				}

			}
			globalContext.setComponents(currentSelection);
			ComponentFactory.cleanComponentList(request.getSession().getServletContext(), globalContext);
		}

		return msg;
	}

	public static final String performComponentcommand(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);

		String compType = requestService.getParameter("type", null);
		if (compType != null) {
			if (requestService.getParameter("inall", null) != null) {
				Collection<GlobalContext> allGlobalContext = GlobalContextFactory.getAllGlobalContext(request.getSession());
				for (GlobalContext globalContext : allGlobalContext) {
					List<String> componentList = globalContext.getComponents();
					componentList.add(compType);
					globalContext.setComponents(componentList);
				}
			} else if (requestService.getParameter("outall", null) != null) {
				Collection<GlobalContext> allGlobalContext = GlobalContextFactory.getAllGlobalContext(request.getSession());
				for (GlobalContext globalContext : allGlobalContext) {
					List<String> componentList = globalContext.getComponents();
					componentList.remove(compType);
					globalContext.setComponents(componentList);
				}
			}
		}
		return null;
	}

	public static final String performConvert(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		CompatibiltyService.convertCMS(System.out, ctx);
		return null;
	}

	public static final String performCreatesite(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String siteName = requestService.getParameter("site-name", null);
		if (siteName != null && siteName.length() > 0 && PatternHelper.HOST_PATTERN.matcher(siteName).matches()) {
			GlobalContext globalContext = GlobalContext.getInstance(request, siteName);
			globalContext.setDefaultTemplate(requestService.getParameter("default-template", globalContext.getDefaultTemplate()));
			globalContext.setAdministrator(requestService.getParameter("admin", globalContext.getAdministrator()));
			globalContext.setPassword(requestService.getParameter("password", null));
			globalContext.setRAWLanguages(requestService.getParameter("languages", globalContext.getRAWLanguages()));
			if (requestService.getParameter("template-data", null) != null) {
				Template.TemplateData templateData = new Template.TemplateData(requestService.getParameter("template-data", null));
				globalContext.setTemplateData(templateData);
				if (requestService.getAllFileItem().size() > 0) {
					ContentContext ctx = ContentContext.getContentContext(request, response);
					InputStream in = requestService.getAllFileItem().iterator().next().getInputStream();
					try {
						globalContext.storeLogo(ctx, in);
					} finally {
						ResourceHelper.closeResource(in);
					}
				}
				globalContext.setDynamic(true);
			}
		} else {
			logger.warning("bad site name : " + siteName);
		}
		return null;
	}

	public static String performDefault(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;

		RequestService requestService = RequestService.getInstance(request);
		String contextID = requestService.getParameter("context", null);
		String templateId = requestService.getParameter("template-id", null);
		if ((contextID != null) && (templateId != null)) {
			GlobalContext globalContext = GlobalContext.getRealInstance(request, contextID);
			globalContext.setDefaultTemplate(templateId);
			PageConfiguration pageCfg = PageConfiguration.getInstance(globalContext);
			pageCfg.loadTemplate(globalContext);
		}
		return msg;
	}

	public static String performDeletesite(HttpServletRequest request, HttpServletResponse response) {

		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			return null;
		}

		String msg = null;

		RequestService requestService = RequestService.getInstance(request);
		String contextId = requestService.getParameter("context", null);
		if (contextId != null) {
			try {
				GlobalContext ctx = GlobalContext.getRealInstance(request, contextId);
				ctx.delete(request.getSession().getServletContext());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msg;
	}

	public static final String performDeletetemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			return null;
		}
		RequestService requestService = RequestService.getInstance(request);
		String templateId = requestService.getParameter("template", null);

		if (templateId != null) {
			AdminContext adminContext = AdminContext.getInstance(request.getSession());
			Template template = Template.getApplicationInstance(request.getSession().getServletContext(), null, templateId, (adminContext.getTemplateType() == AdminContext.MAILING_TEMPLATE));
			template.delete();

			//TemplateFactory.clearTemplate(request.getSession().getServletContext());
		}
		return null;
	}

	public static final String performEditable(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException, IOException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			return null;
		}

		RequestService requestService = RequestService.getInstance(request);

		String contextName = requestService.getParameter("context", null);
		if (contextName != null) {
			GlobalContext ctx = GlobalContext.getRealInstance(request, contextName);
			if (ctx != null) {
				ctx.setEditable(!ctx.isEditable());
			}
		}

		return null;
	}

	public static String performEditfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		AdminContext adminCtx = AdminContext.getInstance(request.getSession());
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		String localFile = requestService.getParameter("file", null);
		if (localFile != null) {
			if (adminCtx.getTemplateType() == AdminContext.MAILING_TEMPLATE) {
				String fileName = URLHelper.mergePath(staticConfig.getMailingTemplateFolder(), localFile);
				adminCtx.setURIFileToEdit(localFile);
				adminCtx.setFileToEdit(fileName);
			} else {
				String fileName = URLHelper.mergePath(staticConfig.getTemplateFolder(), localFile);
				adminCtx.setURIFileToEdit(localFile);
				adminCtx.setFileToEdit(fileName);
			}
			adminCtx.setActiveRessource(requestService.getParameter("ressource", null));
		}
		return null;
	}

	public static final String performEmptytrash(HttpServletRequest request, HttpServletResponse response) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		File trash = new File(staticConfig.getTrashFolder());
		if (trash.exists()) {
			FileUtils.deleteDirectory(trash);
		}
		return null;
	}

	public static String performFiltertemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;

		RequestService requestService = RequestService.getInstance(request);

		AdminContext adminContext = AdminContext.getInstance(request.getSession());
		String templateType = requestService.getParameter("template_type", "");
		if (templateType.equals("web")) {
			adminContext.setTemplateType(AdminContext.WEB_TEMPLATE);
		} else if (templateType.equals("mailing")) {
			adminContext.setTemplateType(AdminContext.MAILING_TEMPLATE);
		}

		return msg;
	}

	public static String performMetadata(HttpServletRequest request, HttpServletResponse response) throws Exception {

		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		MessageRepository msgRepo = MessageRepository.getInstance(ContentContext.getContentContext(request, response));
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.security.noright", new String[][] { { "action", "admin-metadata" } }), GenericMessage.ERROR));
			return null;
		}

		String msg = null;

		RequestService requestService = RequestService.getInstance(request);
		String contextID = requestService.getParameter("context", null);

		if (requestService.getParameter("synchro", null) != null) {
			return EditActions.performSynchro(request, response);
		}

		boolean adminMode = false;
		if (contextID != null) { // admin mode
			globalContext = GlobalContext.getRealInstance(request, contextID);
			adminMode = true;
		} else { // edit mode
			globalContext = GlobalContext.getInstance(request);
		}

		// change licence request
		if ((!adminMode) && (requestService.getParameter("licence", null) != null)) {
			String lang = globalContext.getEditLanguage();
			editCtx.setMainRenderer("/jsp/edit/data/xtendblog_price_list_" + lang + ".jsp");
			return "";
		}

		globalContext.reload();
		if (globalContext != null) {
			String admin = requestService.getParameter("admin", "");
			if (admin.trim().length() > 0) {
				if (PatternHelper.MAIL_PATTERN.matcher(admin).matches()) {
					globalContext.setAdministrator(admin);
				} else {
					msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.bad-email"), GenericMessage.ERROR));
				}
			}
			String globalTitle = requestService.getParameter("global-title", null);
			if (globalTitle != null) {
				globalContext.setGlobalTitle(globalTitle);
			}
			String homepage = requestService.getParameter("homepage", null);
			if (homepage != null) {
				globalContext.setHomePage(homepage);
			}

			String alias = requestService.getParameter("alias", null);
			if (alias != null) {
				globalContext.setAliasOf(alias);
			}
			String country = requestService.getParameter("country", null);
			if (country != null) {
				globalContext.setCountry(country);
			}
			String language = requestService.getParameter("language", null);
			if (language != null) {
				globalContext.setDefaultLanguages(language);
			}
			String languages = requestService.getParameter("languages", null);
			if (languages != null) {
				globalContext.setRAWLanguages(languages);
			}

			/*** date format ***/

			String fullDate = requestService.getParameter("full-date", null);
			if (fullDate != null) {
				try {
					new SimpleDateFormat(fullDate);
					globalContext.setFullDateFormat(fullDate);
				} catch (Exception e) {
					msgRepo.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
				}
			}
			String mediumDate = requestService.getParameter("medium-date", null);
			if (mediumDate != null) {
				try {
					new SimpleDateFormat(mediumDate);
					globalContext.setMediumDateFormat(mediumDate);
				} catch (Exception e) {
					msgRepo.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
				}
			}
			String shortDate = requestService.getParameter("short-date", null);
			if (shortDate != null) {
				try {
					new SimpleDateFormat(shortDate);
					globalContext.setShortDateFormat(shortDate);
				} catch (Exception e) {
					msgRepo.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
				}
			}

			String Contentlanguages = requestService.getParameter("content-languages", null);
			if (Contentlanguages != null) {
				globalContext.setRAWContentLanguages(Contentlanguages);
			}

			String adminUserFactory = requestService.getParameter("admin-user-factory", "");
			if (adminUserFactory.trim().length() > 0) {

				try {
					Class.forName(adminUserFactory);
					globalContext.setAdminUserFactoryClassName(adminUserFactory);
					globalContext.resetAdmimUserFactory();
				} catch (ClassNotFoundException e) {
					msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.bad-user-factory"), GenericMessage.ERROR));
				}

			}

			String userFactory = requestService.getParameter("user-factory", "");
			if (userFactory.trim().length() > 0) {

				try {
					Class.forName(userFactory);
					globalContext.setUserFactoryClassName(userFactory);
					globalContext.resetUserFactory();
				} catch (ClassNotFoundException e) {
					msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.bad-user-factory"), GenericMessage.ERROR));
				}

			}

			String urlFactory = requestService.getParameter("url-factory", null);
			if (urlFactory != null) {
				if (urlFactory.trim().length() > 0) {
					try {
						if (((Class<IURLFactory>) Class.forName(urlFactory)).newInstance() instanceof IURLFactory) {
							globalContext.setURLFactory(urlFactory);
						} else {
							throw new ClassNotFoundException("bad class : " + urlFactory);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.bad-url-factory"), GenericMessage.ERROR));
					}

				} else {
					globalContext.setURLFactory(null);
				}
			}

			/** edit languages **/
			String EditLanguages = requestService.getParameter("edit-language", null);
			if (EditLanguages != null) {
				globalContext.setEditLanguage(EditLanguages);
			}

			if (globalContext.isMailing()) {
				/** mail of mailing sender **/
				String mailingSender = requestService.getParameter("mailing-sender", "");
				globalContext.setMailingSender(mailingSender);

				/** mail for mailing report **/
				String mailingReport = requestService.getParameter("mailing-report", "");
				globalContext.setMailingReport(mailingReport);

				/** mailing subject **/
				String mailingSubject = requestService.getParameter("mailing-subject", "");
				globalContext.setMailingSubject(mailingSubject);
			}

			/** google stat **/
			String googleUACTT = requestService.getParameter("google-uactt", null);
			if (googleUACTT != null) {
				globalContext.setGoogleAnalyticsUACCT(googleUACTT);
			}

			/** help URL **/
			String helpURL = requestService.getParameter("help-url", "");
			if (helpURL.trim().length() > 0) {
				globalContext.setHelpURL(helpURL);
			}

			/** DMZ URL **/
			if (adminMode) {
				String dmzURL = requestService.getParameter("dmz-url", "");
				if (dmzURL.trim().length() > 0) {
					try {

						String urlStr = URLHelper.mergePath(dmzURL, SynchronisationServlet.PUSH_RESSOURCES_DESCRIPTION_URI);

						new URL(URLHelper.addParam(urlStr, SynchronisationServlet.SHYNCRO_CODE_PARAM_NAME, StaticConfig.getInstance(request.getSession()).getSynchroCode()));
						globalContext.setDMZServerInter(dmzURL);
						/*
						 * logger.info("try to connect to DMZ server : " + url); HttpURLConnection conn = (HttpURLConnection) url.openConnection(); int responseCode = conn.getResponseCode(); if (responseCode == HttpURLConnection.HTTP_OK) { globalContext.setDMZServerInter(dmzURL); } else { msgRepo.setGlobalMessage(new GenericMessage(dmzURL + " response : " + conn.getResponseMessage() + " [" + conn.getResponseCode() + "]", GenericMessage.ERROR)); }
						 */
					} catch (Exception e) {
						e.printStackTrace();
						msgRepo.setGlobalMessage(new GenericMessage("error DMZ : " + dmzURL + " -> " + e.getMessage(), GenericMessage.ERROR));
					}
				} else {
					globalContext.setDMZServerInter(null);
				}
				/** intra **/
				String dmzURLIntra = requestService.getParameter("dmz-intra-url", "");
				if (dmzURLIntra.trim().length() == 0) {
					dmzURLIntra = dmzURL;
				}
				if (dmzURLIntra.trim().length() > 0) {
					try {
						String urlStr = URLHelper.mergePath(dmzURLIntra, SynchronisationServlet.PUSH_RESSOURCES_DESCRIPTION_URI);

						URL url = new URL(URLHelper.addParam(urlStr, SynchronisationServlet.SHYNCRO_CODE_PARAM_NAME, StaticConfig.getInstance(request.getSession()).getSynchroCode()));
						logger.info("try to connect to DMZ server intra : " + url);
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						int responseCode = conn.getResponseCode();
						if (responseCode == HttpURLConnection.HTTP_OK) {
							globalContext.setDMZServerIntra(dmzURLIntra);
						} else {
							msgRepo.setGlobalMessage(new GenericMessage(dmzURLIntra + " response : " + conn.getResponseMessage() + " [" + conn.getResponseCode() + "]", GenericMessage.ERROR));
						}
					} catch (Exception e) {
						e.printStackTrace();
						msgRepo.setGlobalMessage(new GenericMessage("error DMZ intra : " + dmzURLIntra + " -> " + e.getMessage(), GenericMessage.ERROR));
					}
				} else {
					globalContext.setDMZServerIntra(null);
				}
			}

			/** look **/
			String look = requestService.getParameter("look", "");
			if (look.trim().length() > 0) {
				globalContext.setLook(look);
			}

			/** default encoding **/
			String encoding = requestService.getParameter("default-encoding", "");
			if (encoding.trim().length() > 0) {
				try {
					Charset.forName(encoding);
					globalContext.setDefaultEncoding(encoding);
				} catch (RuntimeException e) {
					msgRepo.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
					e.printStackTrace();
				}
			}

			String[] macros = requestService.getParameterValues("macro", new String[0]);
			if (macros.length > 0) {
				globalContext.setMacros(Arrays.asList(macros));
			}

			/** alias uri */
			String rawAliasURI = requestService.getParameter("uri-alias", null);
			if (rawAliasURI != null) {
				Properties aliasProp = new Properties();
				InputStream in = new ByteArrayInputStream(rawAliasURI.getBytes());
				aliasProp.load(in);
				in.close();
				globalContext.setAliasURI(aliasProp);
			}

			/** image view filter */
			String rawImageViewFilter = requestService.getParameter("view-filter", null);
			if (rawImageViewFilter != null) {
				globalContext.setImageViewFilter(rawImageViewFilter);
			}

			if (adminMode) {
				/** boolean values **/
				globalContext.setNewPageVisible(requestService.getParameter("default-visibility", null) != null);
				globalContext.setHelpLink(requestService.getParameter("help-link", null) != null);
				globalContext.setEasy(requestService.getParameter("easy", null) != null);
				globalContext.setPrivatePage(requestService.getParameter("private-page", null) != null);
				globalContext.setRightOnPage(requestService.getParameter("right-on-page", null) != null);
				globalContext.setDownloadContent(requestService.getParameter("download-content", null) != null);
				globalContext.setImportable(requestService.getParameter("importable-content", null) != null);
				globalContext.setChangeMenu(requestService.getParameter("change-menu", null) != null);
				globalContext.setLightMenu(requestService.getParameter("light-menu", null) != null);
				globalContext.setTags(requestService.getParameter("tags", null) != null);
				globalContext.setMailing(requestService.getParameter("mailing", null) != null);
				globalContext.setPortail(requestService.getParameter("portail", null) != null);
				globalContext.setUserManagement(requestService.getParameter("user-management", null) != null);
				globalContext.setAdminManagement(requestService.getParameter("admin-management", null) != null);
				globalContext.setViewBar(requestService.getParameter("view-bar", null) != null);
				globalContext.setExtendMenu(requestService.getParameter("extend-menu", null) != null);
				globalContext.setTemplateFilter(requestService.getParameter("template-filter", null) != null);
				globalContext.setChangeLicence(requestService.getParameter("change-licence", null) != null);
				globalContext.setReversedLink(requestService.getParameter("reversed-link", null) != null);
				globalContext.setVirtualPaternity(requestService.getParameter("virtual-paternity", null) != null);
				globalContext.setPageStructure(requestService.getParameter("page-structure", null) != null);
				globalContext.setCSSInline(requestService.getParameter("css-inline", null) != null);
				globalContext.setHelpLink(requestService.getParameter("help-link", null) != null);
				globalContext.setView(requestService.getParameter("view", null) != null);
				globalContext.setDynamic(requestService.getParameter("dynamic", null) != null);
				globalContext.setInstantMessaging(requestService.getParameter("im", null) != null);

				/* check coherence */
				if (globalContext.isMailing() || globalContext.isPrivatePage()) {
					globalContext.setUserManagement(true);
				}
			} else {
				/** boolean values for edit mode **/
				globalContext.setAutoSwitchToDefaultLanguage(requestService.getParameter("autoswitchlg", null) != null);
				globalContext.setAutoSwitchToFirstLanguage(requestService.getParameter("autoswitchfirstlg", null) != null);
				globalContext.setOpenExernalLinkAsPopup(requestService.getParameter("popup", null) != null);
				globalContext.setNoPopupDomainRAW(requestService.getParameter("no-popup-domain", ""));

				globalContext.setOpenFileAsPopup(requestService.getParameter("popup-file", null) != null);
				globalContext.setImagePreview(requestService.getParameter("image-preview", null) != null);

				/* dynamic template */
				Template.TemplateData templateData = new Template.TemplateData();
				try {
					String background = requestService.getParameter("background", "").replace("#", "");
					ContentContext ctx = ContentContext.getContentContext(request, response);
					if (background.trim().length() > 0) {
						Color color = Color.decode('#' + background);
						if (templateData.getBackground() == null || !templateData.getBackground().equals(color)) {
							templateData.setBackground(color);
							if (ctx.getCurrentTemplate() != null) {
								ctx.getCurrentTemplate().clearRenderer(ctx);
							}
						}
					}
					String foreground = requestService.getParameter("foreground", "").replace("#", "");
					if (foreground.trim().length() > 0) {
						Color color = Color.decode('#' + foreground);
						if (templateData.getForeground() == null || !templateData.getForeground().equals(color)) {
							templateData.setForeground(color);
							if (ctx.getCurrentTemplate() != null) {
								ctx.getCurrentTemplate().clearRenderer(ctx);
							}
						}
					}
					String textColor = requestService.getParameter("text-color", "").replace("#", "");
					if (textColor.trim().length() > 0) {
						Color color = Color.decode('#' + textColor);
						if (templateData.getText() == null || !templateData.getText().equals(color)) {
							templateData.setText(color);
							if (ctx.getCurrentTemplate() != null) {
								ctx.getCurrentTemplate().clearRenderer(ctx);
							}
						}
					}
					String bgMenu = requestService.getParameter("menu-background", "").replace("#", "");
					if (bgMenu.trim().length() > 0) {
						Color color = Color.decode('#' + bgMenu);
						if (templateData.getBackgroundMenu() == null || !templateData.getBackgroundMenu().equals(color)) {
							templateData.setBackgroundMenu(color);
							if (ctx.getCurrentTemplate() != null) {
								ctx.getCurrentTemplate().clearRenderer(ctx);
							}
						}
					}
					String menuColor = requestService.getParameter("text-menu-color", "").replace("#", "");
					;
					if (menuColor.trim().length() > 0) {
						Color color = Color.decode('#' + menuColor);
						if (templateData.getTextMenu() == null || !templateData.getTextMenu().equals(color)) {
							templateData.setTextMenu(color);
							if (ctx.getCurrentTemplate() != null) {
								ctx.getCurrentTemplate().clearRenderer(ctx);
							}
						}
					}
					String border = requestService.getParameter("border-color", "").replace("#", "");
					if (border.trim().length() > 0) {
						Color color = Color.decode('#' + border);
						if (templateData.getBorder() == null || !templateData.getBorder().equals(color)) {
							templateData.setBorder(color);
							if (ctx.getCurrentTemplate() != null) {
								ctx.getCurrentTemplate().clearRenderer(ctx);
							}
						}
					}
					String link = requestService.getParameter("link", "").replace("#", "");
					if (link.trim().length() > 0) {
						Color color = Color.decode('#' + link);
						if (templateData.getLink() == null || !templateData.getLink().equals(color)) {
							templateData.setLink(color);
							if (ctx.getCurrentTemplate() != null) {
								ctx.getCurrentTemplate().clearRenderer(ctx);
							}
						}
					}
					String serverTools = requestService.getParameter("server-tools", "").replace("#", "");
					if (serverTools.trim().length() > 0) {
						if (templateData.getToolsServer() == null || !templateData.getToolsServer().equals(serverTools)) {
							templateData.setToolsServer(serverTools);
							if (ctx.getCurrentTemplate() != null) {
								ctx.getCurrentTemplate().clearRenderer(ctx);
							}
						}
					}

					if (requestService.getParameter("delete-logo", null) != null) {
						globalContext.storeLogo(ctx, null);
					}

					/** logo upload **/
					Collection<FileItem> files = requestService.getAllFileItem();
					if (files.size() > 0) {
						FileItem file = files.iterator().next();
						if (file.getSize() > 0) {
							InputStream in = file.getInputStream();
							try {
								globalContext.storeLogo(ctx, in);
							} finally {
								ResourceHelper.closeResource(in);
							}
						}
					}

					if (globalContext.getLogo() != null) {
						templateData.setLogo(GlobalContext.LOGO_FILE_NAME);
					}

					globalContext.setTemplateData(templateData);
				} catch (Exception e) {
					e.printStackTrace();
					msgRepo.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
				}

			}
			msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.updated"), GenericMessage.INFO));
		}
		return msg;
	}

	public static final String performModifytemplate(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException, IOException {
		RequestService requestService = RequestService.getInstance(request);

		String templateId = requestService.getParameter("template", null);

		if (templateId != null) {
			AdminContext adminContext = AdminContext.getInstance(request.getSession());
			adminContext.setRenderer("/jsp/admin/template/modify_template.jsp");
			adminContext.setCommandRenderer("/jsp/admin/template/template_upload.jsp");
			adminContext.setFileToEdit(null);
		}

		return null;

	}

	public static final String performPurgethread(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ThreadManager threadManager = ThreadManager.getInstance(request.getSession().getServletContext());
		int threadPurged = threadManager.purgeAllThread();
		MessageRepository msgRepo = MessageRepository.getInstance(ContentContext.getContentContext(request, response));
		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.thread.message.purged", new String[][] { { "#", "" + threadPurged } }), GenericMessage.INFO));

		return null;
	}

	public static String performRank(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;
		if (request.getSession().getAttribute("__VOTED__") != null) {
			return msg;
		}

		RequestService requestService = RequestService.getInstance(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		if (globalContext != null) {
			try {
				globalContext.setOneVote(Integer.parseInt(requestService.getParameter("rank", "0")));
				request.getSession().setAttribute("__VOTED__", "__VOTED__");
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}

	public static final String performReadytemplate(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException, IOException {

		RequestService requestService = RequestService.getInstance(request);

		String templateId = requestService.getParameter("template", null);

		if (templateId != null) {
			AdminContext adminContext = AdminContext.getInstance(request.getSession());
			Template template = Template.getApplicationInstance(request.getSession().getServletContext(), null, templateId, (adminContext.getTemplateType() == AdminContext.MAILING_TEMPLATE));

			GlobalContext globalContext = GlobalContext.getInstance(request);
			IUserFactory userFact = UserFactory.createUserFactory(globalContext, request.getSession());
			if (userFact.getCurrentUser(request.getSession()) == null) {
				return null;
			} else {
				if (userFact.getCurrentUser(request.getSession()).getLogin().equals(template.getOwner())) {
					template.setReady(!template.isReady());
				}
			}
		}

		return null;
	}

	public static final String performReleaseContent(HttpServletRequest request, HttpServletResponse response, boolean view) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			return null;
		}
		RequestService requestService = RequestService.getInstance(request);
		String contextName = requestService.getParameter("context", null);
		if (contextName != null) {
			if (globalContext != null) {
				ContentService content = ContentService.getInstance(globalContext);
				if (view) {
					content.releaseViewNav(ContentContext.getContentContext(request, response),globalContext);
				} else {
					content.setPreviewNav(null);
				}
			}
		}
		return null;
	}

	public static final String performReleasepreview(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return performReleaseContent(request, response, false);
	}

	public static final String performReleaseview(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return performReleaseContent(request, response, true);
	}

	public static final String performReload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService content = ContentService.createContent(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		content.releaseAll(ContentContext.getContentContext(request, response), globalContext);		
		UserFactory.createUserFactory(globalContext, request.getSession()).reload(globalContext, request.getSession());
		AdminUserFactory.createUserFactory(globalContext, request.getSession()).reload(globalContext, request.getSession());
		globalContext.reload();
		return null;
	}

	public static final String performTag(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		String tag = requestService.getParameter("tag", null);
		if (tag != null) {
			if (requestService.getParameter("remove", null) != null) {
				globalContext.removeTag(tag);
			} else if (requestService.getParameter("add", null) != null) {
				globalContext.addTag(tag);
			}
		}
		return null;
	}

	public static String performTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;

		RequestService requestService = RequestService.getInstance(request);
		String contextID = requestService.getParameter("context", null);
		String templateId = requestService.getParameter("template-id", null);
		if ((contextID != null) && (templateId != null)) {
			GlobalContext globalContext = GlobalContext.getRealInstance(request, contextID);
			AdminContext adminCtx = AdminContext.getInstance(request.getSession());
			List<String> templatesSelected;
			if (adminCtx.getTemplateType() == AdminContext.MAILING_TEMPLATE) {
				templatesSelected = globalContext.getMailingTemplates();
			} else {
				templatesSelected = globalContext.getTemplates();
			}

			if (templatesSelected.contains(templateId)) {
				templatesSelected.remove(templateId);
			} else {
				templatesSelected.add(templateId);
			}

			if (adminCtx.getTemplateType() == AdminContext.MAILING_TEMPLATE) {
				globalContext.setMailingTemplates(templatesSelected);
			} else {
				globalContext.setTemplates(templatesSelected);
			}

			PageConfiguration pageCfg = PageConfiguration.getInstance(globalContext);
			pageCfg.loadTemplate(globalContext);
		}
		return msg;
	}

	public static final String performUpdateconfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String newContent = request.getParameter("config_content");
		if (newContent != null) {
			StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
			staticConfig.storeAllProperties(newContent);
			MessageRepository msgRepo = MessageRepository.getInstance(ContentContext.getContentContext(request, response));
			GlobalContext globalContext = GlobalContext.getInstance(request);
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.config-update"), GenericMessage.INFO));

			DebugHelper.updateLoggerLevel(request.getSession().getServletContext());
		}
		return null;
	}

	public static String performUpdatefile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String content = requestService.getParameter("content", null);
		AdminContext adminCtx = AdminContext.getInstance(request.getSession());

		boolean fullAccess = false;
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			fullAccess = true;
		}

		String templateId = requestService.getParameter("template", null);
		if (templateId != null) {
			Template template;
			if (adminCtx.getTemplateType() == AdminContext.MAILING_TEMPLATE) {
				template = TemplateFactory.getMailingTemplates(request.getSession().getServletContext()).get(templateId);
			} else {
				template = TemplateFactory.getTemplates(request.getSession().getServletContext()).get(templateId);
			}
			IUserFactory userFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());

			if (template.getOwner().equals(userFactory.getCurrentUser(request.getSession()).getLogin()) || fullAccess) {
				if (requestService.getParameter("cancel", null) != null) {
					adminCtx.setFileToEdit(null); // return to view index
					// file
				} else {
					if (content != null) {
						adminCtx.saveFileToEdit(request.getSession(), content);
						// adminCtx.setFileToEdit(null); // return to view
						// index file
					} else {
						Collection<FileItem> files = requestService.getAllFileItem();
						if (files.size() > 0) {
							FileItem file = files.iterator().next();
							InputStream in = file.getInputStream();
							try {
								adminCtx.writeStreamToEdit(request.getSession().getServletContext(), in);
							} finally {
								ResourceHelper.closeResource(in);
							}
						}
					}
					if (template != null) {
						template.reload();
					}
				}
			}

			List<GenericMessage> messages = template.checkRenderer(globalContext, I18nAccess.getInstance(globalContext, request.getSession()));
			if (messages.size() > 0) {
				ContentContext ctx = ContentContext.getContentContext(request, response);
				MessageRepository msgRepo = MessageRepository.getInstance(ctx);
				msgRepo.setGlobalMessage(messages.iterator().next());
			} else {
				ContentContext ctx = ContentContext.getContentContext(request, response);
				MessageRepository msgRepo = MessageRepository.getInstance(ctx);
				GenericMessage msg = new GenericMessage(I18nAccess.getInstance(globalContext, request.getSession()).getText("template.noerror"), GenericMessage.INFO);
				msgRepo.setGlobalMessage(msg);
			}

		}
		TemplateFactory.clearTemplate(request.getSession().getServletContext());
		return null;
	}

	public static final String performUpdatetemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			return null;
		}

		RequestService requestService = RequestService.getInstance(request);

		String templateId = requestService.getParameter("template", null);

		if (templateId != null) {
			AdminContext adminContext = AdminContext.getInstance(request.getSession());
			Template template = Template.getApplicationInstance(request.getSession().getServletContext(), null, templateId, (adminContext.getTemplateType() == AdminContext.MAILING_TEMPLATE));
			template.setAuthors(requestService.getParameter("authors", template.getAuthors()));
			template.setSource(requestService.getParameter("source", template.getSource()));
			template.setDominantColor(requestService.getParameter("dominant_color", template.getDominantColor()));
			Date creationDate;
			ContentContext ctx = ContentContext.getContentContext(request, response);
			MessageRepository msgRepo = MessageRepository.getInstance(ctx);
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			try {
				creationDate = StringHelper.parseDate(requestService.getParameter("date", StringHelper.renderDate(template.getCreationDate())));
				template.setCreationDate(creationDate);
				msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("admin.form.message.updated"), GenericMessage.INFO));
			} catch (ParseException e) {
				msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("component.message.warning.date-format"), GenericMessage.ERROR));
			}

		}
		TemplateFactory.clearTemplate(request.getSession().getServletContext());

		return null;
	}

	public static String performUploadtemplate(HttpServletRequest request, HttpServletResponse response) {
		String msg = null;

		ContentContext ctx = null;
		try {
			ctx = ContentContext.getContentContext(request, response);
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		MessageRepository msgRepo = MessageRepository.getInstance(ctx);

		RequestService requestService = RequestService.getInstance(request);
		Collection<FileItem> fileItems = requestService.getAllFileItem();

		for (FileItem item : fileItems) {
			try {
				if (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("zip")) {

					String templateId = FilenameUtils.getBaseName(item.getName());
					AdminContext adminContext = AdminContext.getInstance(request.getSession());
					GlobalContext globalContext = GlobalContext.getInstance(request);
					IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
					I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

					/* check right */
					if (TemplateFactory.isTemplateExistOnDisk(request.getSession().getServletContext(), templateId, adminContext.getTemplateType() == AdminContext.MAILING_TEMPLATE)) {
						Template template = Template.getApplicationInstance(request.getSession().getServletContext(), null, templateId, (adminContext.getTemplateType() == AdminContext.MAILING_TEMPLATE));
						if (!template.getOwner().equals(userFactory.getCurrentUser(request.getSession()).getLogin())) {
							msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("command.admin.template.name-exist"), GenericMessage.ERROR));
							return null;
						} else if (template.isValid()) {
							msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("command.admin.template.lock"), GenericMessage.ERROR));
							return null;
						}
					}

					AdminContext adminCtx = AdminContext.getInstance(request.getSession());
					InputStream in = item.getInputStream();
					try {
						ZipManagement.uploadZipTemplate(ContentContext.getContentContext(request, response), in, templateId, adminCtx.getTemplateType() == AdminContext.MAILING_TEMPLATE);
					} finally {
						ResourceHelper.closeResource(in);
					}

					Template template = Template.getApplicationInstance(request.getSession().getServletContext(), null, templateId, (adminContext.getTemplateType() == AdminContext.MAILING_TEMPLATE));

					template.setCreationDate(new Date());
					template.setValid(false);
					template.clearRenderer(null);
					template.importTemplateInWebapp(null);
					List<GenericMessage> messages = template.checkRenderer(globalContext, I18nAccess.getInstance(globalContext, request.getSession()));
					if (messages.size() > 0) {
						msgRepo.setGlobalMessage(messages.iterator().next());
					} else {
						GenericMessage errorMsg = new GenericMessage(I18nAccess.getInstance(globalContext, request.getSession()).getText("template.noerror"), GenericMessage.INFO);
						msgRepo.setGlobalMessage(errorMsg);
					}
					if (userFactory.getCurrentUser(request.getSession()) != null) {
						template.setOwner(userFactory.getCurrentUser(request.getSession()).getLogin());
					}
					TemplateFactory.clearTemplate(request.getSession().getServletContext());
					if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
						GlobalContext globalCtx = GlobalContext.getInstance(request);
						globalCtx.addTemplate(templateId, false);
						PageConfiguration pageConfig = PageConfiguration.getInstance(globalContext);
						pageConfig.loadTemplate(globalContext);
					}
				}
			} catch (Throwable e) {
				msgRepo.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
				e.printStackTrace();
			}
		}
		return msg;
	}

	public static final String performValidalltemplate(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException, IOException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			return null;
		}

		AdminContext adminCtx = AdminContext.getInstance(request.getSession());
		Collection<Template> templates;
		if (adminCtx.getTemplateType() == AdminContext.MAILING_TEMPLATE) {
			templates = TemplateFactory.getAllMaillingTemplates(request.getSession().getServletContext());
		} else {
			templates = TemplateFactory.getAllTemplates(request.getSession().getServletContext());
		}

		for (Template template : templates) {
			template.setValid(true);
		}

		return null;
	}

	public static final String performValidtemplate(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException, IOException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			return null;
		}

		RequestService requestService = RequestService.getInstance(request);

		String templateId = requestService.getParameter("template", null);

		if (templateId != null) {
			AdminContext adminContext = AdminContext.getInstance(request.getSession());
			Template template = Template.getApplicationInstance(request.getSession().getServletContext(), null, templateId, (adminContext.getTemplateType() == AdminContext.MAILING_TEMPLATE));
			template.setValid(!template.isValid());
		}

		return null;
	}

	public static final String performVisibility(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException, IOException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			return null;
		}

		RequestService requestService = RequestService.getInstance(request);

		String contextName = requestService.getParameter("context", null);
		if (contextName != null) {
			GlobalContext ctx = GlobalContext.getRealInstance(request, contextName);
			if (ctx != null) {
				ctx.setVisible(!ctx.isVisible());
			}
		}

		return null;
	}

	@Override
	public String getActionGroupName() {
		return "admin";
	}

}
