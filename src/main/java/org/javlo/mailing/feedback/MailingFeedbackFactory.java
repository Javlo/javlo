package org.javlo.mailing.feedback;

import java.util.LinkedList;
import java.util.List;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;

public class MailingFeedbackFactory {

	private List<IMailingFeedback> mailingFeedbackList = null;
	private StaticConfig staticConfig = null;

	private static final String KEY = MailingFeedbackFactory.class.getName();

	public static MailingFeedbackFactory getInstance(ContentContext ctx) {
		MailingFeedbackFactory outFact = (MailingFeedbackFactory) ctx.getRequest().getSession().getServletContext()
				.getAttribute(KEY);
		if (outFact == null) {
			outFact = new MailingFeedbackFactory();
			outFact.staticConfig = ctx.getGlobalContext().getStaticConfig();
			ctx.getRequest().getSession().getServletContext().setAttribute(KEY, outFact);
		}
		return outFact;
	}

	public void reset() {
		mailingFeedbackList = null;
	}

	public List<IMailingFeedback> getAllMailingFeedback() {
		if (mailingFeedbackList == null) {
			synchronized (KEY) {
				if (mailingFeedbackList == null) {
					mailingFeedbackList = new LinkedList<IMailingFeedback>();
					for (String mailingFeedbackClass : staticConfig.getMailingFeedbackClass()) {
						try {
							IMailingFeedback mailingFeedback = (IMailingFeedback) Class.forName(mailingFeedbackClass).newInstance();
							mailingFeedbackList.add(mailingFeedback);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return mailingFeedbackList;
	}

}