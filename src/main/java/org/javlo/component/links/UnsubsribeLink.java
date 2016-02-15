package org.javlo.component.links;

import org.javlo.module.mailing.MailingAction;

public class UnsubsribeLink extends InternalLink {
	
	@Override
	public String getType() {
		return "unsubscribe-link";
	}
	
	@Override
	protected String getParam() throws Exception {
		return "?webaction=mailing.unsubscribe&roles="+MailingAction.DATA_MAIL_PREFIX+"roles"+MailingAction.DATA_MAIL_SUFFIX;
	}

}
