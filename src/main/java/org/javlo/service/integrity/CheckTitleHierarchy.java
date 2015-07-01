package org.javlo.service.integrity;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.ISubTitle;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class CheckTitleHierarchy extends AbstractIntegrityChecker {

	@Override
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception {
		Template template = TemplateFactory.getTemplate(ctx, page);
		int error = 0;
		int lastLevel = 0;
		if (template != null) {
			ContentContext areaCtx = new ContentContext(ctx);
			for (String area : template.getAreas()) {
				areaCtx.setArea(area);
				ContentElementList content = page.getContent(areaCtx);
				IContentVisualComponent comp = content.next(areaCtx);
				while (comp != null) {
					if (comp instanceof ISubTitle) {
						if (Math.abs(((ISubTitle) comp).getSubTitleLevel(areaCtx) - lastLevel) > 1) {
							error++;							
						}
						lastLevel = ((ISubTitle) comp).getSubTitleLevel(areaCtx);
					} else if (comp instanceof Title) {
						if (lastLevel > 2) {
							error++;							
						}
						lastLevel = 1;
					}
					comp = content.next(areaCtx);
				}
			}
		}

		if (error > 0) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			setMessage(i18nAccess.getText("integrity.error.title_hierarchy", "Bad title hierachy, max one level between two title."));
			setErrorCount(error);
			return false;
		} else {
			return true;
		}
	}

}
