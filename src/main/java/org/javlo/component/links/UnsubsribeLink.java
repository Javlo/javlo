package org.javlo.component.links;

public class UnsubsribeLink extends InternalLink {
	
	@Override
	public String getType() {
		return "unsubscribe-link";
	}
	
	@Override
	protected String getParam() throws Exception {
		return "?webaction=mailing.unsubscribe&roles=##roles##";
	}

}
