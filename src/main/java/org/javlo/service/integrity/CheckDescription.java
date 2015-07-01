package org.javlo.service.integrity;

import org.javlo.context.ContentContext;
import org.javlo.context.IntegrityBean;
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
			setLevel(DANGER_LEVEL);
			return false;
		} else {
			IntegrityBean integrity = ctx.getGlobalContext().getIntegrityDefinition();
			if (description.length() < integrity.getDescriptionMinSize()) {
				setMessage(i18nAccess.getText("integrity.error.description_too_short", "You description is too short (min: "+integrity.getDescriptionMinSize()+')'));
				setErrorCount(1);
				return false;
			} else if (description.length() > integrity.getDescriptionMaxSize()) {				
				setMessage(i18nAccess.getText("integrity.error.description_too_large", "You description is too large (max: "+integrity.getDescriptionMaxSize()+')'));
				setErrorCount(1);
				return false;
			}
		}
		return true;
	}
	
}
