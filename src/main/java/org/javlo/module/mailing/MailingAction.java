package org.javlo.module.mailing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
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
import org.javlo.service.syncro.SynchroHelper;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

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
			url = new URL(URLHelper.addParam(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE).getContextForAbsoluteURL()), "mailing", "true"));
			request.setAttribute("exportURL", url);
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
				BoxStep s = b.getSteps().get(mailingContext.getWizardStep(SEND_WIZARD_BOX) - 1);
				currentModule.setRenderer(s.getRenderer());
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
				if (currentModule.getRenderer().contains("history")) {
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
					senders = globalContext.getMailingSenders().trim();
				} else {
					senders = (senders + ',' + globalContext.getMailingSenders()).trim();
				}
				if (senders.contains(sender)) {
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
					logger.warning("Security error : bad mail sender. (" + globalContext.getContextKey() + " - " + ctx.getCurrentUserId() + ")");
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

	public static String performUnsubscribe(ServletContext application, HttpServletRequest request, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String mfb = rs.getParameter(MailingAction.MAILING_FEEDBACK_PARAM_NAME, null);
		if (mfb != null) {
			DataToIDService serv = DataToIDService.getInstance(application);
			Map<String, String> params = StringHelper.uriParamToMap(serv.getData(mfb));
			String to = params.get("to");
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			logger.info("mailing unsubscribe : " + to + " site:" + globalContext.getContextKey());
			InternetAddress add;
			try {
				add = new InternetAddress(to);
				IUserFactory userFactory = UserFactory.createUserFactory(request);
				User user = userFactory.getUser(add.getAddress());
				if (user != null) {
					Set<String> roles = new HashSet<String>(StringHelper.stringToCollection(rs.getParameter("roles", ""), ";"));
					user.getUserInfo().removeRoles(roles);
					userFactory.store();
				} else {
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(outStream);

					out.println("Site title : " + globalContext.getGlobalTitle());
					out.println("E-Mail     : " + to);
					out.println("");
					out.println("--");
					out.println("Direct Link : " + URLHelper.createAbsoluteViewURL(ctx, "/"));
					out.close();
					String mailContent = new String(outStream.toByteArray());

					NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), new InternetAddress(to), "Mailing unsubscribe : " + globalContext.getContextKey(), mailContent);
				}
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		return Boolean.TRUE;
	}
	
	public static String performDeletemailing(RequestService rs, ServletContext application, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws ConfigurationException, IOException {
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
