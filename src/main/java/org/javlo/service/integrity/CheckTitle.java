package org.javlo.service.integrity;

import org.javlo.component.core.ContentElementList;
import org.javlo.context.ContentContext;
import org.javlo.context.IntegrityBean;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;

public class CheckTitle extends AbstractIntegrityChecker {
	
	@Override
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception {
		ctx = ctx.getContextWithArea(null);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		String title = StringHelper.neverNull(page.getPageTitle(ctx));
		boolean isTitle = false;
		ContentElementList content = page.getContent(ctx);
		while (!isTitle && content.hasNext(ctx)) {
			if (content.next(ctx).getLabelLevel(ctx)>0) {
				isTitle=true;
			}
		}
		if (!isTitle) {
			setErrorMessage(i18nAccess.getText("integrity.error.no_title", "no title found on the page."));
			setErrorCount(1);
			setLevel(DANGER_LEVEL);
			return false;
		} else {
			IntegrityBean integrity = ctx.getGlobalContext().getIntegrityDefinition();
			if (title.length() > integrity.getTitleMaxSize()) {
				setErrorMessage(i18nAccess.getText("integrity.error.description_too_long", "You title is too long ("+title.length()+" max: "+integrity.getTitleMaxSize()+')'));
				setErrorCount(1);
				setLevel(WARNING_LEVEL);
				return false;
			} else {
				setLevel(SUCCESS_LEVEL);
			}
		}
		return true;
	}
	
}
