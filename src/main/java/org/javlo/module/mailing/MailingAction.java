package org.javlo.module.mailing;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.FeedBackMailingBean;
import org.javlo.mailing.Mailing;
import org.javlo.mailing.MailingFactory;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.Module.Box;
import org.javlo.module.core.Module.BoxStep;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.DataToIDService;
import org.javlo.service.RequestService;
import org.javlo.service.UnsubscribeService;
import org.javlo.service.UnsubscribeTokenService;
import org.javlo.service.syncro.SynchroHelper;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.*;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class MailingAction extends AbstractModuleAction {

	public static final String MAILING_FEEDBACK_PARAM_NAME = "_mfb";

	public static final String DATA_MAIL_PREFIX = "_ml_";

	public static final String DATA_MAIL_SUFFIX = "__";

	public static final String MAILING_FEEDBACK_VALUE_NAME = DATA_MAIL_PREFIX + "data" + DATA_MAIL_SUFFIX;

	private static Logger logger = Logger.getLogger(MailingAction.class.getName());

	public static final String SEND_WIZARD_BOX = "sendwizard";

	public static final String SEND_WIZARD_BOX_PREVIEW = "main-renderer";

	@Override
	public String getActionGroupName() {
		return "mailing";
	}

	@Override
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return AbstractModuleContext.getInstance(session, GlobalContext.getSessionInstance(session), module, MailingModuleContext.class);
	}

	/***************/
	/** WEBACTION **/
	/***************/

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {

		String msg = super.prepare(ctx, modulesContext);

		HttpServletRequest request = ctx.getRequest();
		HttpSession session = request.getSession();
		GlobalContext globalContext = GlobalContext.getSessionInstance(session);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);

		HashMap<String, String> params = new HashMap<String, String>();
		params.put(ContentContext.NO_DMZ_PARAM_NAME, "true");
		request.setAttribute("previewURL", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE), params));

		MailingModuleContext mailingContext = MailingModuleContext.getInstance(request);
		request.setAttribute("mailing", mailingContext);

		Module currentModule = modulesContext.getCurrentModule();
		if (mailingContext.getWizardStep(SEND_WIZARD_BOX) == 4) {
			String content;
			StaticConfig sc = globalContext.getStaticConfig();
			URL url = new URL(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE).getContextForAbsoluteURL()));
			if (sc.getApplicationLogin() != null) {
				content = NetHelper.readPageForMailing(url, sc.getApplicationLogin(), sc.getApplicationPassword());
			} else {
				User user = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession()).getUser(ctx.getCurrentEditUser().getLogin());
				String token = null;
				if (user != null) {
					if (user.getUserInfo().getToken() == null || user.getUserInfo().getToken().trim().length() == 0) {
						user.getUserInfo().setToken(StringHelper.getRandomIdBase64());
					}
					token = user.getUserInfo().getToken();
				}
				content = NetHelper.readPageForMailing(url, token);
			}
			if (content == null) {
				logger.severe("error on read : " + url);
				content = "error on read : " + url;
			}
			request.setAttribute("content", content);			
			request.setAttribute("outlookContent", XHTMLHelper.escapeXHTML(StringHelper.removeCR(content)));
			url = new URL(URLHelper.addParam(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE).getContextForAbsoluteURL()), "mailing", "true"));			
			request.setAttribute("exportURL", url);
			ContentContext emlCtx = new ContentContext(ctx);
			emlCtx.setFormat("eml");
			url = new URL(URLHelper.addParam(URLHelper.createURL(emlCtx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getContextForAbsoluteURL()), "mailing", "true"));
			request.setAttribute("exportURLEML", url);
			if (request.getParameter("wizardStep") != null && request.getParameter("wizardStep").equals("4")) {
				String threadId = SynchroHelper.performSynchro(ctx);
				request.setAttribute("threadId", threadId);
				if (threadId != null) {
					request.setAttribute("checkThreadURL", URLHelper.createStaticURL(ctx, "/rest/thread/" + threadId));
				}
			}
		}
		if (ctx.isEditPreview()) {
			if (mailingContext.getWizardStep(SEND_WIZARD_BOX) == 1) {
				mailingContext.setWizardStep(SEND_WIZARD_BOX, 2);
				currentModule.setRenderer("/jsp/step2.jsp");
				request.setAttribute("currentTemplate", mailingContext.getCurrentTemplate());
			} else {
				Box b = currentModule.getBox(SEND_WIZARD_BOX);
				if (b==null) {
					logger.severe("box '"+SEND_WIZARD_BOX+"' not found, please active mailing module.");
				} else {
					BoxStep s = b.getSteps().get(mailingContext.getWizardStep(SEND_WIZARD_BOX) - 1);
					currentModule.setRenderer(s.getRenderer());
				}
			}
		} else {
			if (mailingContext.getCurrentLink().equals("send")) {
				currentModule.setSidebar(true);
				currentModule.setBreadcrumb(true);
				currentModule.restoreRenderer();
			} else {
				currentModule.setSidebar(false);
				currentModule.setBreadcrumb(false);
				MailingFactory mailingFactory = MailingFactory.getInstance(session.getServletContext());
				if (currentModule.getRenderer().contains("unsubscribe")) {
					request.setAttribute("unsubscribeList", UnsubscribeService.getInstance(globalContext).getAll());
				} else if (currentModule.getRenderer().contains("history")) {
				if (!globalContext.isMaster()) {
					request.setAttribute("allMailing", mailingFactory.getOldMailingListByContext(globalContext.getContextKey()));
				} else {
					request.setAttribute("allMailing", mailingFactory.getOldMailingList());
				}
				} else {
					if (!globalContext.isMaster()) {
						request.setAttribute("allMailing", mailingFactory.getMailingListByContext(globalContext.getContextKey()));
					} else {
						request.setAttribute("allMailing", mailingFactory.getMailingList());
					}	
				}
			}
		}

		switch (mailingContext.getWizardStep(SEND_WIZARD_BOX)) {
		case 1:
			Collection<Template> allTemplate = TemplateFactory.getAllDiskTemplates(ctx.getRequest().getSession().getServletContext());
			Collection<String> contextTemplates = globalContext.getTemplatesNames();

			List<Template.TemplateBean> templates = new LinkedList<Template.TemplateBean>();
			for (Template template : allTemplate) {
				if (template.isMailing() && contextTemplates.contains(template.getName())) {
					if (!template.isTemplateInWebapp(ctx)) {
						template.importTemplateInWebapp(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()), ctx);
					}
					templates.add(new Template.TemplateBean(ctx, template));
				}
			}
			if (mailingContext.getCurrentTemplate() == null && templates.size() > 0) {
				mailingContext.setCurrentTemplate(templates.get(0).getName());
			}
			request.setAttribute("currentTemplate", mailingContext.getCurrentTemplate());
			request.setAttribute("templates", templates);
			break;
		case 2:
			if (mailingContext.getReportTo() == null) {
				mailingContext.setReportTo(globalContext.getAdministratorEmail());
			}
			IUserFactory userFactory = UserFactory.createUserFactory(request);
			AdminUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, session);
			List<String> groups = new LinkedList(userFactory.getAllRoles(globalContext, session));
			Collections.sort(groups);
			request.setAttribute("groups", groups);
			List<String> adminGroups = new LinkedList(globalContext.getAdminUserRoles());
			Collections.sort(adminGroups);
			request.setAttribute("adminGroups", adminGroups);

			String senders = adminUserFactory.getRoleWrapper(ctx, adminUserFactory.getCurrentUser(session)).getMailingSenders();
			if (senders == null || senders.trim().length() == 0) {
				senders = globalContext.getMailingSenders().trim();
			} else {
				senders = (senders + ',' + globalContext.getMailingSenders()).trim();
			}
			if (senders.trim().length() > 0) {
				/* hash for remove same entry */
				request.setAttribute("senders", new HashSet(StringHelper.stringToCollection(senders, ",")));
			} else {
				if (ctx.getCurrentTemplate().getSenders() != null) {
					request.setAttribute("senders", ctx.getCurrentTemplate().getSenders());
				}
			}
			break;
		case 3:
			String confirmMessage = i18nAccess.getText("mailing.message.confirm", new String[][] { { "count", "" + mailingContext.getAllRecipients().size() } });
			request.setAttribute("confirmMessage", confirmMessage);
			break;
		}

		return msg;
	}

	private static boolean checkRight(ContentContext ctx) {
		AdminUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		User user = userFactory.getCurrentUser(ctx.getRequest().getSession());
		if (user == null) {
			return false;
		} else {
			if (user.validForRoles(AdminUserSecurity.MAILING_ROLE)) {
				return true;
			} else {
				return false;
			}
		}
	}

	public String performWizard(ContentContext ctx, GlobalContext globalContext, ServletContext application, StaticConfig staticConfig, HttpServletRequest request, RequestService rs, Module currentModule, MessageRepository messageRepository, MailingModuleContext mailingContext, I18nAccess i18nAccess) throws Exception {

		if (!checkRight(ctx)) {
			return "Security error.";
		}

		if (ctx.getRequest().getParameter("wizardStep") == null) {
			switch (mailingContext.getWizardStep(SEND_WIZARD_BOX)) {
			case 1:
				if (mailingContext.getCurrentTemplate() == null) {
					String msg = i18nAccess.getText("mailing.message.no-template-selected");
					MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.ALERT));
					return null;
				}
				break;
			case 2:
				String sender = rs.getParameter("sender", null);
				AdminUserFactory adminUserFactory = AdminUserFactory.createAdminUserFactory(globalContext, ctx.getRequest().getSession());
				String senders = adminUserFactory.getRoleWrapper(ctx, adminUserFactory.getCurrentUser(ctx.getRequest().getSession())).getMailingSenders();				
				if (senders == null || senders.trim().length() == 0) {
					senders = globalContext.getMailingSenders().trim()+','+StringHelper.neverNull(ctx.getCurrentTemplate().getSenders());
				} else {
					senders = (senders + ',' + globalContext.getMailingSenders()).trim()+','+StringHelper.neverNull(ctx.getCurrentTemplate().getSenders());
				}
				if (StringHelper.neverNull(senders).trim().length() <= 1 || senders.contains(sender)) {
					mailingContext.setSender(sender);
					mailingContext.setSubject(rs.getParameter("subject", null));
					mailingContext.setReportTo(rs.getParameter("report-to", null));
					mailingContext.setGroups(rs.getParameterListValues("groups", new LinkedList<String>()));
					mailingContext.setAdminGroups(rs.getParameterListValues("admin-groups", new LinkedList<String>()));
					mailingContext.setRecipients(rs.getParameter("recipients", null));
					mailingContext.setStructuredRecipients(rs.getParameter("structuredRecipients", null));
					mailingContext.setTestMailing(rs.getParameter("test-mailing", null) != null);
					boolean isValid = mailingContext.validate(ctx);
					if (ctx.isAjax()) {
						currentModule.getBox(SEND_WIZARD_BOX).update(ctx);
					}
					if (!isValid) {
						return null;
					}
				} else {
					logger.warning("Security error : bad mail sender. (" + globalContext.getContextKey() + " - " + ctx.getCurrentUserId() + ") (valid : "+senders+")");
					return "Security error : bad mail sender.";
				}
				break;
			case 3:
				if (rs.getParameter("send", null) != null) {
					mailingContext.sendMailing(ctx);
					String msg = i18nAccess.getText("mailing.message.sent");
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.SUCCESS));
					mailingContext.reset();
					mailingContext.setWizardStep(SEND_WIZARD_BOX, null);
					if (ctx.isAjax()) {
						currentModule.getBox(SEND_WIZARD_BOX).update(ctx);
					}
					if (ctx.isEditPreview()) {
						ctx.setClosePopup(true);
						if (ctx.getParentURL() != null) {
							ctx.setParentURL(messageRepository.forwardMessage(ctx.getParentURL()));
						}
					}
					SynchroHelper.performSynchro(ctx);
				}
				break;
			}
		}
		return super.performWizard(ctx, rs, currentModule, mailingContext);
	}

	public String performSelectMailingTemplate(ContentContext ctx, RequestService rs, Module currentModule, MailingModuleContext mailingContext) throws Exception {
		mailingContext.setCurrentTemplate(rs.getParameter("name", null));
		if (ctx.isAjax()) {
			if (ctx.isEditPreview()) {
				currentModule.getBox(SEND_WIZARD_BOX_PREVIEW).update(ctx);
			} else {
				currentModule.getBox(SEND_WIZARD_BOX).update(ctx);
			}
			currentModule.updateMainRenderer(ctx);
		}
		return null;
	}

	public static final String UNSUBSCRIBE_TOKEN_PARAM_NAME = "lut";

	/**
	 * Désabonne un destinataire. Deux jetons possibles : 'lut', signé, porté par
	 * l'en-tête List-Unsubscribe ; ou '_mfb', porté par le lien présent dans le
	 * corps du mail.
	 *
	 * Renvoie toujours null : un jeton invalide ne doit pas être distingué d'un
	 * jeton valide, sous peine de renseigner un attaquant.
	 */
	public static String performUnsubscribe(ServletContext application, HttpServletRequest request, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		String email = null;
		String mailingId = null;
		Collection<String> roles = new LinkedList<String>();
		/**
		 * Rôles écrits sur la liste de suppression. Ils ne suivent 'roles' que
		 * sur le chemin signé : voir plus bas pour le chemin '_mfb'.
		 */
		Collection<String> suppressionRoles = roles;

		String signedToken = rs.getParameter(UNSUBSCRIBE_TOKEN_PARAM_NAME, null);
		if (signedToken != null) {
			UnsubscribeTokenService tokenService = new UnsubscribeTokenService(globalContext.getUnsubscribeSecret());
			UnsubscribeTokenService.TokenData data = tokenService.read(signedToken, globalContext.getContextKey());
			if (data == null) {
				logger.warning("invalid unsubscribe token on site : " + globalContext.getContextKey());
				return null;
			}
			email = data.getEmail();
			mailingId = data.getMailingId();
			roles.addAll(data.getRoles());
		} else {
			String mfb = rs.getParameter(MailingAction.MAILING_FEEDBACK_PARAM_NAME, null);
			if (mfb == null) {
				return null;
			}
			DataToIDService serv = DataToIDService.getInstance(application);
			String rawData = serv.getData(mfb);
			if (rawData == null) {
				return null;
			}
			Map<String, String> params = StringHelper.uriParamToMap(rawData);
			if (params == null) {
				return null;
			}
			email = params.get("to");
			mailingId = params.get("mailing");
			roles.addAll(StringHelper.stringToCollection(rs.getParameter("roles", ""), ";"));
			/**
			 * Sur ce chemin les rôles viennent d'un paramètre de requête non
			 * signé, et l'identifiant '_mfb' est un StringHelper.getRandomId()
			 * prédictible. Un appelant pourrait donc choisir les rôles écrits
			 * sur la liste de suppression d'une adresse qui n'est pas la
			 * sienne. On ignore ce paramètre pour l'écriture et on enregistre
			 * ALL_ROLES : se désabonner de tout est la direction prudente, et
			 * c'est déjà ce que fait le service quand la collection est vide.
			 * La granularité par rôle reste sur le jeton signé 'lut'.
			 */
			suppressionRoles = Collections.singletonList(UnsubscribeService.ALL_ROLES);
		}

		if (StringHelper.isEmpty(email)) {
			return null;
		}

		logger.info("mailing unsubscribe : " + email + " site:" + globalContext.getContextKey() + " roles:" + roles + " suppression:" + suppressionRoles);

		/** liste de suppression : couvre toutes les origines de destinataires **/
		UnsubscribeService.getInstance(globalContext).unsubscribe(email, suppressionRoles);

		/** retrait des rôles du compte utilisateur, s'il existe **/
		try {
			InternetAddress add = new InternetAddress(email);
			IUserFactory userFactory = UserFactory.createUserFactory(request);
			User user = userFactory.getUser(add.getAddress());
			if (user != null) {
				user.getUserInfo().removeRoles(new HashSet<String>(roles));
				userFactory.store();
			} else if (signedToken != null) {
				/**
				 * Chemin one-click RFC 8058 : pas de mail à l'administrateur.
				 * Une poignée de main SMTP sur le thread de la requête ferait
				 * expirer le POST que le client mail attend court, et une vague
				 * de désabonnements inonderait la boîte de l'administrateur. La
				 * notification était un palliatif à l'absence d'écran ; la liste
				 * de suppression enregistre l'adresse et l'écran du module
				 * mailing l'affiche.
				 */
				logger.info("unsubscribe without account, no admin mail on one-click path : " + email);
			} else {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				out.println("Site title : " + globalContext.getGlobalTitle());
				out.println("E-Mail     : " + email);
				out.println("");
				out.println("--");
				out.println("Direct Link : " + URLHelper.createAbsoluteViewURL(ctx, "/"));
				out.close();
				NetHelper.sendMailToAdministrator(globalContext, new InternetAddress(email), "Mailing unsubscribe : " + globalContext.getContextKey(), new String(outStream.toByteArray()));
			}
		} catch (AddressException e) {
			logger.warning("bad email on unsubscribe : " + email);
		}

		/** trace pour la statistique du module mailing **/
		if (mailingId != null) {
			try {
				Mailing mailing = new Mailing();
				if (mailing.isExist(application, mailingId)) {
					mailing.setId(StaticConfig.getInstance(application).getMailingStaticConfig(), mailingId);
					FeedBackMailingBean bean = new FeedBackMailingBean();
					bean.setEmail(email);
					bean.setDate(new Date());
					bean.setUrl(ctx.getRequest().getPathInfo());
					bean.setWebaction("unsecure.unsubscribe");
					bean.setIp(ctx.getRequest().getRemoteHost());
					mailing.addFeedBack(bean);
				}
			} catch (Exception e) {
				logger.warning("can not store unsubscribe feedback : " + e.getMessage());
			}
		}

		/**
		 * Accusé de réception, uniquement ici. Aucun chemin d'échec ne parle :
		 * un jeton invalide doit rester indiscernable d'un jeton valide. Le
		 * client one-click ignore le corps de la réponse ; c'est le
		 * destinataire arrivant en GET depuis le lien du mail qui le lit.
		 */
		if (messageRepository != null && i18nAccess != null) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("mailing.unsubscribe.confirmation"), GenericMessage.INFO));
		}

		return null;
	}

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		return Boolean.TRUE;
	}

	/**
	 * Réinscription d'une adresse : écriture sur la liste de suppression,
	 * réservée aux administrateurs du site.
	 *
	 * Le contrôle est explicite et ne peut pas s'appuyer sur le
	 * 'security.roles=mailing' du module : ActionManager:220-232 délègue à
	 * Module.haveRight, qui court-circuite sur MailingAction.haveRight(session,
	 * user) — lequel renvoie TRUE sans condition. Sans cette garde, un GET
	 * anonyme sur mailing.resubscribe suffirait à désuspendre n'importe quelle
	 * adresse.
	 */
	public static String performResubscribe(ContentContext ctx, RequestService rs) throws Exception {
		if (!AdminUserSecurity.getInstance().canRole(ctx.getCurrentEditUser(), AdminUserSecurity.MAILING_ROLE)) {
			logger.warning("access refused to mailing.resubscribe : " + ctx.getRequest().getRequestURI() + " user:" + ctx.getCurrentUserId());
			return "security error.";
		}
		String email = rs.getParameter("email", null);
		if (StringHelper.isEmpty(email)) {
			return "need 'email' as parameter.";
		}
		UnsubscribeService.getInstance(ctx.getGlobalContext()).resubscribe(email);
		return null;
	}

		public static String performDeletemailing(RequestService rs, ServletContext application, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String id = rs.getParameter("id", null);
		if (id == null) {
			return "need 'id' as parameter.";
		}
		MailingFactory mailingFactory = MailingFactory.getInstance(session.getServletContext());
		Mailing mailing = mailingFactory.getLiveMailing(id);
		if (mailing == null) {
			return "mailing "+id+" not found.";
		} else {
			mailing.delete(application);
			return null;
		}
	}

}
