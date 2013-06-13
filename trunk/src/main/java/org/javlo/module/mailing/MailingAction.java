package org.javlo.module.mailing;

import java.io.IOException;
import java.util.Collection;
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

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.DataToIDService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class MailingAction extends AbstractModuleAction {

	public static final String MAILING_FEEDBACK_PARAM_NAME = "_mfb";

	private static Logger logger = Logger.getLogger(MailingAction.class.getName());

	public static final String SEND_WIZARD_BOX = "sendwizard";

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

		request.setAttribute("previewURL", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE)));

		MailingModuleContext mailingContext = MailingModuleContext.getInstance(request);
		request.setAttribute("mailing", mailingContext);
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
			Set<String> roles = userFactory.getAllRoles(globalContext, session);
			request.setAttribute("groups", roles);
			break;
		case 3:
			String confirmMessage = i18nAccess.getText("mailing.message.confirm", new String[][] { { "count", "" + mailingContext.getAllRecipients().size() } });
			request.setAttribute("confirmMessage", confirmMessage);
			break;
		}
		return msg;
	}

	public String performWizard(ContentContext ctx, GlobalContext globalContext, HttpServletRequest request, RequestService rs, Module currentModule, MailingModuleContext mailingContext, I18nAccess i18nAccess) throws Exception {
		switch (mailingContext.getWizardStep(SEND_WIZARD_BOX)) {
		case 1:
			if (mailingContext.getCurrentTemplate() == null) {
				String msg = i18nAccess.getText("mailing.message.no-template-selected");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.ALERT));
				return null;
			}
			break;
		case 2:
			mailingContext.setSender(rs.getParameter("sender", null));
			mailingContext.setSubject(rs.getParameter("subject", null));
			mailingContext.setReportTo(rs.getParameter("report-to", null));
			mailingContext.setGroups(rs.getParameterListValues("groups", new LinkedList<String>()));
			mailingContext.setRecipients(rs.getParameter("recipients", null));
			mailingContext.setTestMailing(rs.getParameter("test-mailing", null) != null);
			boolean isValid = mailingContext.validate(ctx);
			if (ctx.isAjax()) {
				currentModule.getBox(SEND_WIZARD_BOX).update(ctx);
			}
			if (!isValid) {
				return null;
			}
			break;
		case 3:
			if (rs.getParameter("send", null) != null) {
				mailingContext.sendMailing(ctx);
				String msg = i18nAccess.getText("mailing.message.sent");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.SUCCESS));
				mailingContext.reset();
				mailingContext.setWizardStep(SEND_WIZARD_BOX, null);
				if (ctx.isAjax()) {
					currentModule.getBox(SEND_WIZARD_BOX).update(ctx);
				}
			}
			break;
		}
		return super.performWizard(ctx, rs, currentModule, mailingContext);
	}

	public String performSelectMailingTemplate(ContentContext ctx, RequestService rs, Module currentModule, MailingModuleContext mailingContext) throws Exception {
		mailingContext.setCurrentTemplate(rs.getParameter("name", null));
		if (ctx.isAjax()) {
			currentModule.getBox(SEND_WIZARD_BOX).update(ctx);
			currentModule.updateMainRenderer(ctx);
		}
		return null;
	}
	
	public static String performUnsubscribe(ServletContext application, HttpServletRequest request, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String mfb = rs.getParameter(MailingAction.MAILING_FEEDBACK_PARAM_NAME, null);
		if (mfb != null) {
			DataToIDService serv = DataToIDService.getInstance(application);			
			Map<String, String> params = StringHelper.uriParamToMap(serv.getData(mfb));
			String to = params.get("to");
			InternetAddress add;
			try {
				add = new InternetAddress(to);	
				IUserFactory userFactory = UserFactory.createUserFactory(request);
				User user = userFactory.getUser(add.getAddress());
				if (user != null) {
					System.out.println("***** MailingAction.performUnsubscribe : roles  : "+StringHelper.stringToCollection(rs.getParameter("roles", ""))); //TODO: remove debug trace
					Set<String> roles = new HashSet<String>(StringHelper.stringToCollection(rs.getParameter("roles", ""),";"));
					user.getUserInfo().removeRoles(roles);
					userFactory.store();
				}
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}

}
