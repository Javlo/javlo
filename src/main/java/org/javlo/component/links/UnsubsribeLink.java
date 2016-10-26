package org.javlo.component.links;

import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.mailing.MailingAction;

public class UnsubsribeLink extends InternalLink {
	
	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		I18nAccess i18nAccess;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String[] styleLabel = getConfig(ctx).getStyleLabelList();
			if (styleLabel.length != getStyleList(ctx).length) {
				return getStyleList(ctx);
			}
			for (int i = 0; i < styleLabel.length; i++) {
				styleLabel[i] = i18nAccess.getText(styleLabel[i], styleLabel[i]);
			}
			return styleLabel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String getType() {
		return "unsubscribe-link";
	}
	
	@Override
	protected String getParam() throws Exception {
		return "?webaction=mailing.unsubscribe&roles="+MailingAction.DATA_MAIL_PREFIX+"roles"+MailingAction.DATA_MAIL_SUFFIX;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

}
