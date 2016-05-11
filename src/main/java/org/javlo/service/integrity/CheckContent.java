package org.javlo.service.integrity;

import org.javlo.context.ContentContext;
import org.javlo.context.IntegrityBean;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;

public class CheckContent extends AbstractIntegrityChecker {

	@Override
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception {
		ctx = ctx.getContextWithArea(null);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		String content = StringHelper.neverNull(page.getContentAsText(ctx));

		IntegrityBean integrity = ctx.getGlobalContext().getIntegrityDefinition();
		if (content.length() < integrity.getContentMinSize()) {
			setErrorMessage(i18nAccess.getText("integrity.error.content_too_short", "You content is too short (" + content.length() + " min: " + integrity.getContentMinSize() + ')'));
			setErrorCount(1);
			setLevel(WARNING_LEVEL);
			return false;
		}
		setLevel(SUCCESS_LEVEL);
		return true;
	}

}
