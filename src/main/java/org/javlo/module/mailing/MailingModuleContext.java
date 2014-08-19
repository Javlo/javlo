package org.javlo.module.mailing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.bean.LinkToRenderer;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.Mailing;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;

//TODO Use MailingBuilder
public class MailingModuleContext extends AbstractModuleContext {

	private static Logger logger = Logger.getLogger(MailingModuleContext.class.getName());

	private static final String MODULE_NAME = "mailing";

	public static MailingModuleContext getInstance(HttpServletRequest request) throws ModuleException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		HttpSession session = request.getSession();
		Module module = ModulesContext.getInstance(session, globalContext).searchModule(MODULE_NAME);

		MailingModuleContext outContext = (MailingModuleContext) AbstractModuleContext.getInstance(session, globalContext, module, MailingModuleContext.class);

		if (outContext.navigation.size() == 0) {
			LinkToRenderer defaultNav = new LinkToRenderer(I18nAccess.getInstance(request).getText("mailing.title.send"), "send", "../content/jsp/preview.jsp");			
			outContext.navigation.add(defaultNav);
			outContext.navigation.add(new LinkToRenderer(I18nAccess.getInstance(request).getText("mailing.title.history"), "history", "jsp/history.jsp"));
			outContext.setCurrentLink(defaultNav.getName());
		}

		return outContext;
	}

	private String currentTemplate;
	private String sender;
	private String subject;
	private String reportTo;
	private List<String> groups;
	private String recipients;
	private boolean isTestMailing;
	private Set<InternetAddress> allRecipients = new LinkedHashSet<InternetAddress>();
	private final List<LinkToRenderer> navigation = new LinkedList<LinkToRenderer>();

	@Override
	public List<LinkToRenderer> getNavigation() {
		return navigation;
	}

	@Override
	public void init() {
	}

	@Override
	public LinkToRenderer getHomeLink() {
		return null;
	}

	public void setCurrentTemplate(String currentTemplate) {
		this.currentTemplate = currentTemplate;
	}

	public String getCurrentTemplate() {
		return currentTemplate;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getReportTo() {
		return reportTo;
	}

	public void setReportTo(String reportTo) {
		this.reportTo = reportTo;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public boolean isTestMailing() {
		return isTestMailing;
	}

	public void setTestMailing(boolean isTest) {
		this.isTestMailing = isTest;
	}

	public Set<InternetAddress> getAllRecipients() {
		return allRecipients;
	}

	public void setAllRecipients(Set<InternetAddress> allRecipients) {
		this.allRecipients = allRecipients;
	}

	public boolean validate(ContentContext ctx) {
		try {
			HttpServletRequest request = ctx.getRequest();
			GlobalContext globalContext = GlobalContext.getInstance(request);
			IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());

			if (sender == null || (sender = sender.trim()).isEmpty()) {
				String msg = i18nAccess.getText("mailing.message.sender.mandatory");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.ALERT));
				return false;
			}
			if (!StringHelper.isMail(sender)) {
				String msg = i18nAccess.getText("mailing.message.sender.not-valid");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.ALERT));
				return false;
			}
			if (subject == null || (subject = subject.trim()).isEmpty()) {
				String msg = i18nAccess.getText("mailing.message.subject.mandatory");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.ALERT));
				return false;
			}
			if (reportTo == null || (reportTo = reportTo.trim()).isEmpty()) {
				String msg = i18nAccess.getText("mailing.message.report-to.mandatory");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.ALERT));
				return false;
			}
			if (!StringHelper.isMail(reportTo)) {
				String msg = i18nAccess.getText("mailing.message.report-to.not-valid");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.ALERT));
				return false;
			}
			allRecipients.clear();
			if (groups != null) {
				for (String group : groups) {
					List<IUserInfo> users = userFactory.getUserInfoForRoles(new String[] { group });
					for (IUserInfo user : users) {
						if (!StringHelper.isEmpty(user.getEmail())) {
							InternetAddress email = new InternetAddress(user.getEmail(), StringHelper.neverNull(user.getFirstName()) + " " + StringHelper.neverNull(user.getLastName()));
							if (!allRecipients.contains(email)) {
								allRecipients.add(email);
							}
						}
					}
				}
			}
			if (recipients != null) {
				for (String fullEmail : StringHelper.searchEmail(recipients)) {
					InternetAddress email = new InternetAddress(fullEmail);
					if (!allRecipients.contains(email)) {
						allRecipients.add(email);
					}
				}
			}
			return true;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		} catch (AddressException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void reset() {
		sender = null;
		subject = null;
		reportTo = null;
		groups = null;
		recipients = null;
		isTestMailing = false;
		allRecipients.clear();
	}

	public void sendMailing(ContentContext ctx) throws Exception {
		ContentContext pageCtx = ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE);
		pageCtx.setAbsoluteURL(true);
		pageCtx.resetDMZServerInter();
		URL url = new URL(URLHelper.createURL(pageCtx) + ";jsessionid=" + ctx.getRequest().getRequestedSessionId());

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Mailing m = new Mailing();
		m.setFrom(new InternetAddress(sender));
		m.setReceivers(allRecipients);
		m.setSubject(subject);
		m.setAdminEmail(globalContext.getAdministratorEmail());
		m.setNotif(new InternetAddress(reportTo));
		m.setContextKey(ctx.getGlobalContext().getContextKey());
		StaticConfig sc = ctx.getGlobalContext().getStaticConfig();
		String content = NetHelper.readPageForMailing(url, sc.getApplicationLogin(), sc.getApplicationPassword());
		if (content == null) {
			logger.severe("error on read : " + url);
		}
		m.setContent(content);
		m.setHtml(true);
		m.setRoles(groups);
		m.setSendDate(new Date());
		m.store(ctx.getRequest().getSession().getServletContext());
	}

}
