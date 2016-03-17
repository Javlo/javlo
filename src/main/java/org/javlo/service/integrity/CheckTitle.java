package org.javlo.service.integrity;

import org.javlo.context.ContentContext;
import org.javlo.context.IntegrityBean;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;

public class CheckTitle extends AbstractIntegrityChecker {
	
	@Override
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		String title = StringHelper.neverNull(page.getContentTitle(ctx));
		if (title.length()==0) {
			setErrorMessage(i18nAccess.getText("integrity.error.no_title", "no title found on the page."));
			setErrorCount(1);
			setLevel(DANGER_LEVEL);
			return false;
		} else {
			IntegrityBean integrity = ctx.getGlobalContext().getIntegrityDefinition();
			if (title.length() < integrity.getTitleMaxSize()) {
				setErrorMessage(i18nAccess.getText("integrity.error.description_too_short", "You title is too short ("+title.length()+" min: "+integrity.getTitleMaxSize()+')'));
				setErrorCount(1);
				return false;
			}
		}
		return true;
	}
	
}
