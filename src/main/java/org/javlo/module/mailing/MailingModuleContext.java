package org.javlo.module.mailing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.bean.LinkToRenderer;
import org.javlo.context.GlobalContext;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;

public class MailingModuleContext extends AbstractModuleContext {

	private static final String MODULE_NAME = "mailing";

	public static MailingModuleContext getInstance(HttpServletRequest request) throws ModuleException, FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		HttpSession session = request.getSession();
		Module module = ModulesContext.getInstance(session, globalContext).searchModule(MODULE_NAME);
		return (MailingModuleContext) AbstractModuleContext.getInstance(session, globalContext, module, MailingModuleContext.class);
	}

	private String currentTemplate;
	private String sender;
	private String subject;
	private String reportTo;
	private List<String> groups;
	private String recipients;
	private boolean isTestMailing;
	static final String SEND_WIZARD_BOX = "sendwizard";

	@Override
	public List<LinkToRenderer> getNavigation() {
		return null;
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

	public void reset() {
		sender = null;
		subject = null;
		reportTo = null;
		groups = null;
		recipients = null;
		isTestMailing = false;
		setWizardStep(SEND_WIZARD_BOX, null);
	}

}
