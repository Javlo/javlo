package org.javlo.service.integrity;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;

public class CheckDescription extends AbstractIntegrityChecker {
	
	@Override
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		String description = StringHelper.neverNull(page.getDescription(ctx));
		if (description.length()==0) {
			setMessage(i18nAccess.getText("integrity.error.no_description", "no description found on the page."));
			setErrorCount(1);
			return false;
		}
		return true;
	}
	
}
